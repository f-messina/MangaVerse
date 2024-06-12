package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import it.unipi.lsmsd.fnf.utils.DocumentUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.in;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ReviewDAO interface for MongoDB data access operations related to reviews.
 * This class provides methods to insert, update, delete, and retrieve reviews from the database,
 * as well as methods to perform various analytical queries on review data.
 */
public class ReviewDAOMongoImpl extends BaseMongoDBDAO implements ReviewDAO {
    private static final String COLLECTION_NAME = "reviews";

    /**
     * Inserts a new review into the database.
     *
     * @param reviewDTO The ReviewDTO object representing the review to be inserted or updated.
     * @throws DAOException If an error occurs during the insertion or update process.
     */
    @Override
    public void saveReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            MongoCollection<Document> mediaCollection;

            if (reviewDTO.getMediaContent() instanceof AnimeDTO) {
                mediaCollection = getCollection("anime");
                // Check if the anime exists
                if (mediaCollection.countDocuments(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))) == 0) {
                    throw new MongoException("ReviewDAOMongoImpl: saveReview: Anime not found");
                }

            } else if (reviewDTO.getMediaContent() instanceof MangaDTO) {
                mediaCollection = getCollection("manga");
                // Check if the manga exists
                if (mediaCollection.countDocuments(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))) == 0) {
                    throw new MongoException("ReviewDAOMongoImpl: saveReview: Manga not found");
                }
            } else {
                throw new DAOException("Invalid media content type");
            }

            Document reviewDocument = reviewDTOToDocument(reviewDTO);
            reviewDTO.setDate(LocalDateTime.now());

            //Insert the review
            Optional.ofNullable(reviewCollection.insertOne(reviewDocument).getInsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { reviewDTO.setId(id); return id; })
                    .orElseThrow(() -> new MongoException("UserDAOMongoImpl: saveUser: Error saving review"));

            //Append the new review_id to the review_ids fields of the corresponding anime or manga
            mediaCollection.updateOne(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId())), push("review_ids", reviewDTO.getId()));

            //Append the new review_id to the review_ids field of the corresponding user
            getCollection("users").updateOne(eq("_id", new ObjectId(reviewDTO.getUser().getId())), push("review_ids", reviewDTO.getId()));

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates an existing review in the database.
     *
     * @param reviewId     The ID of the review to be updated.
     * @param reviewComment The new comment for the review.
     * @param reviewRating  The new rating for the review.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateReview(String reviewId, String reviewComment, Integer reviewRating) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(reviewId));
            Bson updatedKeys = combine(set("date", ConverterUtils.localDateTimeToDate(LocalDateTime.now())));
            if (reviewComment != null) {
                updatedKeys = combine(updatedKeys, set("comment", reviewComment));
            } else {
                updatedKeys = combine(updatedKeys, unset("comment"));
            }
            if (reviewRating != null) {
                updatedKeys = combine(updatedKeys, set("rating", reviewRating));
            } else {
                updatedKeys = combine(updatedKeys, unset("rating"));
            }

            UpdateResult result = reviewCollection.updateOne(filter, updatedKeys);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("ReviewDAOMongoImpl: updateReview: Review not found");
            }
            if (result.getModifiedCount() == 0) {
                throw new MongoException("ReviewDAOMongoImpl: updateReview: Review not modified");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates the media content information in the specified reviews.
     *
     * @param mediaContentDTO The MediaContentDTO object containing the updated media information.
     * @param review_ids The list of review IDs to be updated.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateMediaRedundancy(MediaContentDTO mediaContentDTO, List<String> review_ids) throws DAOException {
        boolean isAnime = mediaContentDTO instanceof AnimeDTO;
        Document mediaDoc = new Document(isAnime ? "anime" : "manga", new Document()
                .append("id", new ObjectId(mediaContentDTO.getId()))
                .append("title", mediaContentDTO.getTitle()));

        List<ObjectId> reviewObjectIds = new ArrayList<>();
        for (String id : review_ids) {
            reviewObjectIds.add(new ObjectId(id));
        }

        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            Bson filter = Filters.in("_id", reviewObjectIds);

            UpdateResult result = reviewCollection.updateMany(filter, new Document("$set", mediaDoc));
            if (result.getMatchedCount() != 0 && result.getModifiedCount() == 0) {
                throw new MongoException("ReviewDAOMongoImpl: updateMediaRedundancy: No reviews modified");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Updates the user information in the specified reviews.
     *
     * @param userSummaryDTO The UserSummaryDTO object containing the updated user information.
     * @param reviewIds The list of review IDs to be updated.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO, List<String> reviewIds) throws DAOException {
        Document userDoc = new Document();
        appendIfNotNull(userDoc, "user.id", new ObjectId(userSummaryDTO.getId()));
        appendIfNotNull(userDoc, "user.username", userSummaryDTO.getUsername());
        appendIfNotNull(userDoc, "user.picture", userSummaryDTO.getProfilePicUrl());
        appendIfNotNull(userDoc, "user.location", userSummaryDTO.getLocation());
        appendIfNotNull(userDoc, "user.birthday", ConverterUtils.localDateToDate(userSummaryDTO.getBirthDate()));

        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<ObjectId> reviewObjectIds = new ArrayList<>();
            for (String id : reviewIds) {
                reviewObjectIds.add(new ObjectId(id));
            }

            Bson filter = Filters.in("_id", reviewObjectIds);
            Bson update = new Document("$set", userDoc);
            if (Objects.equals(userSummaryDTO.getProfilePicUrl(),Constants.NULL_STRING)) {
                update = combine(update, unset("user.picture"));
            }
            if (Objects.equals(userSummaryDTO.getLocation(), Constants.NULL_STRING)) {
                update = combine(update, unset("user.location"));
            }
            if (Objects.equals(userSummaryDTO.getBirthDate(), Constants.NULL_DATE)) {
                update = combine(update, unset("user.birthday"));
            }

            UpdateResult result = reviewCollection.updateMany(filter, update);
            if (result.getMatchedCount() != 0 && result.getModifiedCount() == 0) {
                throw new MongoException("ReviewDAOMongoImpl: updateUserRedundancy: No reviews modified");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    /**
     * Updates the average rating of the media content based on the reviews.
     * This method performs the following steps:
     * 1. Retrieves the list of reviews IDs of manga/anime whose average rating has not been updated.
     * 2. Aggregates the average rating of the reviews by media content ID.
     * 3. Updates the average rating field of the media content with the new average rating.
     *
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateAverageRatingMedia() throws DAOException {
        try {
            // get the list of anime and manga IDs whose average rating has not been updated
            Bson filter = or(eq("avg_rating_last_update", false), exists("avg_rating_last_update", false));
            Bson projection = fields(include("review_ids"));

            List<Document> animeList = getCollection("anime").find(filter).projection(projection).into(new ArrayList<>());
            List<Document> mangaList = getCollection("manga").find(filter).projection(projection).into(new ArrayList<>());

            // get the average rating for each media content and update the average rating field
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Map<String, List<String>> animeReviewIds = new HashMap<>();
            Map<String, List<String>> mangaReviewIds = new HashMap<>();
            for (Document anime : animeList) {
                animeReviewIds.put(String.valueOf(anime.getObjectId("_id")), anime.getList("review_ids", String.class));
            }
            for (Document manga : mangaList) {
                mangaReviewIds.put(String.valueOf(manga.getObjectId("_id")), manga.getList("review_ids", String.class));
            }

            // anime review aggregation
            for (Map.Entry<String, List<String>> entry : animeReviewIds.entrySet()) {
                String animeId = entry.getKey();
                List<String> reviewIds = entry.getValue();
                if (reviewIds.isEmpty()) {
                    continue;
                }
                Bson filterReviews = in("_id", reviewIds);
                Bson group = group("$anime.id", avg("avg_rating", "$rating"));
                Document avgRating = reviewCollection.aggregate(List.of(match(filterReviews), group)).first();

                // update the anime document with the new average rating
                if (avgRating != null) {
                    getCollection("anime").updateOne(eq("_id", animeId), combine(set("avg_rating", avgRating.getDouble("avg_rating")), set("avg_rating_last_update", true)));
                }
            }

            // manga review aggregation
            for (Map.Entry<String, List<String>> entry : mangaReviewIds.entrySet()) {
                String mangaId = entry.getKey();
                List<String> reviewIds = entry.getValue();
                if(reviewIds.isEmpty()){
                    continue;
                }
                Bson filterReviews = in("_id", reviewIds);
                Bson group = group("$manga.id", avg("avg_rating", "$rating"));
                Document avgRating = reviewCollection.aggregate(List.of(match(filterReviews), group)).first();

                // update the manga document with the new average rating
                if (avgRating != null) {
                    getCollection("manga").updateOne(eq("_id", mangaId), combine(set("avg_rating", avgRating.getDouble("avg_rating")), set("avg_rating_last_update", true)));
                }
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Deletes a review from the database based on its ID.
     * This method performs the following steps:
     * 1. Retrieves the review document to get the associated anime/manga ID.
     * 2. Deletes the review document from the reviews' collection.
     * 3. Removes the review ID redundancy in reviews_ids from the anime/manga collection.
     *
     * @param reviewId The ID of the review to be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteReview(String reviewId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(reviewId));
            Bson projection = fields(include("anime.id", "manga.id"), excludeId());

            //Retrieve the review document to get associated media and user IDs
            Document reviewDocument = reviewCollection.find(filter).projection(projection).first();
            if (reviewDocument == null) {
                throw new MongoException("ReviewDAOMongoImpl: deleteReview: Review not found");
            }

            //Delete the review
            reviewCollection.deleteOne(filter).getDeletedCount();

            //Remove the review ID from the anime and manga collections
            String mediaContentType = reviewDocument.containsKey("anime") ? "anime" : "manga";
            filter = eq("_id", reviewDocument.getObjectId(mediaContentType + ".id" ));
            Bson pullReviewId = pull("review_ids", reviewId);
            getCollection(mediaContentType).updateMany(filter, pullReviewId);

            //Remove the review ID from the user collection
            getCollection("users").updateMany(filter, pullReviewId);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR,"The review is not found.");

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Refreshes the latest reviews for anime and manga collections when a user is deleted.
     * This method performs the following steps:
     * 1. Retrieves the review IDs of the reviews authored by the deleted user.
     * 2. Retrieves the latest reviews for the anime and manga that the user has reviewed recently.
     * 3. Updates the latest reviews for the anime and manga collections.
     *
     * @param reviewsIds The IDs of the reviews being deleted.
     * @throws DAOException if a database error or any other error occurs during the operation.
     */
    @Override
    public void refreshLatestReviewsOnUserDeletion(List<String> reviewsIds) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection("anime");
            MongoCollection<Document> mangaCollection = getCollection("manga");

            // Get the reviewsIds for each manga and anime
            Bson filter = in("latest_reviews.id", reviewsIds.stream().map(ObjectId::new).toList());
            Bson projection = fields(include("review_ids"), excludeId());
            List<String> animeReviewIds = Optional.of(animeCollection.find(filter).projection(projection).into(new ArrayList<>()))
                    .map(list -> list.stream()
                            .flatMap(document -> document.getList("review_ids", String.class).stream())
                            .toList())
                    .orElse(new ArrayList<>());
            List<String> mangaReviewIds = Optional.of(mangaCollection.find(filter).projection(projection).into(new ArrayList<>()))
                    .map(list -> list.stream()
                            .flatMap(document -> document.getList("review_ids", String.class).stream())
                            .toList())
                    .orElse(new ArrayList<>());

            // Get the latest reviews for the anime and manga that the user has reviewed recently
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = List.of(
                    facet(
                            new Facet("anime", List.of(
                                    match(in("_id", animeReviewIds.stream().map(ObjectId::new).toList())),
                                    sort(descending("date")),
                                    group("$anime.id", Accumulators.push("latest_reviews", "$$ROOT")),
                                    project(computed("latest_reviews", new Document("$map",
                                            new Document("input", new Document("$slice", Arrays.asList("$latest_reviews", 5)))
                                                    .append("in", new Document()
                                                                    .append("id", "$$this._id")
                                                                    .append("user", "$$this.user")
                                                                    .append("comment", "$$this.comment")
                                                                    .append("date", "$$this.date")
                                                                    .append("rating", "$$this.rating")
                                                    )
                                    )))
                            )),
                            new Facet("manga", List.of(
                                    match(in("_id", mangaReviewIds.stream().map(ObjectId::new).toList())),
                                    sort(descending("date")),
                                    group("$manga.id", Accumulators.push("latest_reviews", "$$ROOT")),
                                    project(computed("latest_reviews", new Document("$map",
                                            new Document("input", new Document("$slice", Arrays.asList("$latest_reviews", 5)))
                                                    .append("in", new Document()
                                                            .append("id", "$$this._id")
                                                            .append("user", "$$this.user")
                                                            .append("comment", "$$this.comment")
                                                            .append("date", "$$this.date")
                                                            .append("rating", "$$this.rating")
                                                    )
                                    )))
                            ))
                    )
            );
            Document latestReviews = reviewCollection.aggregate(pipeline).first();

            // Update the latest reviews for the anime and manga
            if (latestReviews == null) {
                return;
            }
            latestReviews.getList("anime", Document.class).forEach(document -> {
                Bson filter2 = eq("_id", document.getObjectId("_id"));
                Bson update2 = set("latest_reviews", document.getList("latest_reviews", Document.class));
                getCollection("anime").updateOne(filter2, update2);
            });
            latestReviews.getList("manga", Document.class).forEach(document -> {
                Bson filter2 = eq("_id", document.getObjectId("_id"));
                Bson update2 = set("latest_reviews", document.getList("latest_reviews", Document.class));
                getCollection("manga").updateOne(filter2, update2);
            });

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Deletes all reviews associated with media content not present in the database.
     * This method performs the following steps:
     * 1. Retrieves all the reviews IDs from the anime and manga collections.
     * 2. Deletes the reviews with ID equal to the review IDs not present in the anime or manga collection.
     *
     * @throws DAOException If an error occurs during the deletion process.
     */
    public void deleteReviewsWithNoMedia() throws DAOException {
        try {
            // Get the anime and manga IDs
            MongoCollection<Document> animeCollection = getCollection("anime");
            MongoCollection<Document> mangaCollection = getCollection("manga");
            List<String> reviewIds = Optional.of(animeCollection.find().projection(fields(include("review_ids"), excludeId()))
                    .into(new ArrayList<>()))
                    .map(list -> list.stream()
                            .flatMap(document -> document.getList("review_ids", String.class).stream())
                            .collect(toList()))
                    .orElse(new ArrayList<>());
            reviewIds.addAll(Optional.of(mangaCollection.find().projection(fields(include("review_ids"), excludeId()))
                    .into(new ArrayList<>()))
                    .map(list -> list.stream()
                            .flatMap(document -> document.getList("review_ids", String.class).stream())
                            .toList())
                    .orElse(new ArrayList<>()));


            // Delete reviews with anime IDs or manga IDs not present in the anime or manga collection
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            Bson filter = nin("_id", reviewIds.stream().map(ObjectId::new).toList());

            reviewCollection.deleteMany(filter);
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Deletes all reviews associated with users not present in the database.
     * This method performs the following steps:
     * 1. Retrieves all the reviews IDs from the users' collections.
     * 2. Deletes the reviews with ID equal to the review IDs not present in the users' collection.
     *
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteReviewsWithNoAuthor() throws DAOException {
        try {
            // Get the anime and manga IDs
            MongoCollection<Document> userCollection = getCollection("users");
            List<String> reviewIds = Optional.of(userCollection.find().projection(fields(include("review_ids"), excludeId()))
                            .into(new ArrayList<>()))
                    .map(list -> list.stream()
                            .flatMap(document -> document.getList("review_ids", String.class).stream())
                            .collect(toList()))
                    .orElse(new ArrayList<>());

            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            // Delete reviews with user IDs not present in the users collection
            reviewCollection.deleteMany(nin("_id", reviewIds.stream().map(ObjectId::new).toList()));

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Deletes all reviews from a list of review IDs.
     * This method performs the following steps:
     * 1. Remove all the reviews with the given review IDs from the reviews collection.
     * 2. Remove all the ID redundancy in review_ids in anime/manga/user collection.
     *
     * @param reviewsIds The ID of the user whose reviews are to be deleted.
     * @param elementDeleted The type of element that was deleted: "user", "media", or "all".
     * @throws DAOException if a database error or any other error occurs during the operation.
     */
    @Override
    public void deleteReviews(List<String> reviewsIds, String elementDeleted) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = in("_id", reviewsIds.stream().map(ObjectId::new).toList());

            reviewCollection.deleteMany(filter);

            filter = in("review_ids", reviewsIds);
            Bson pullReviewId = pullAll("review_ids", reviewsIds);
            if (elementDeleted.equals("user")) {
                // Remove the review IDs from the anime and manga collections
                getCollection("manga").updateMany(filter, pullReviewId);
                getCollection("anime").updateMany(filter, pullReviewId);
            } else if (elementDeleted.equals("media")) {
                // Remove the review IDs from the user collection
                getCollection("users").updateMany(filter, pullReviewId);
            } else {
                // Remove the review IDs from all collections
                getCollection("manga").updateMany(filter, pullReviewId);
                getCollection("anime").updateMany(filter, pullReviewId);
                getCollection("users").updateMany(filter, pullReviewId);
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Retrieves reviews based on their IDs and paginates the results.
     * This method performs the following steps:
     * 1. Get total count of reviews based on the review IDs.
     * 2. Retrieve the reviews based on the review IDs and paginates the results.
     *
     * @param reviewIds The list of review IDs to retrieve.
     * @param page      The page number for pagination (optional).
     * @param docExcluded The document to exclude from the retrieved reviews.
     * @return A PageDTO containing the retrieved ReviewDTOs and total count.
     * @throws DAOException if a database error or any other error occurs during the operation.
     */
    @Override
    public PageDTO<ReviewDTO> getReviewByIdsList(List<String> reviewIds, Integer page, String docExcluded) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection("reviews");

            Bson filter = Filters.in("_id", reviewIds.stream().map(ObjectId::new).toList());
            Bson projection;
            if (docExcluded != null && docExcluded.equals("user")) {
                projection = Projections.exclude("user");
            } else {
                projection = Projections.exclude("anime", "manga");
            }

            // Get the total count of reviews
            int totalCount = (int) reviewCollection.countDocuments(filter);
            Integer totalPages = (int) Math.ceil((double) totalCount / 15);
            if (totalCount == 0) {
                throw new MongoException("ReviewDAOMongoImpl: getReviewByIds: No reviews found");
            }

            // Get the reviews page
            List<ReviewDTO> result;
            if (page == null) {
                result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date"))
                        .map(DocumentUtils::documentToReviewDTO)
                        .into(new ArrayList<>());
            } else {
                int offset = (page - 1) * 15;
                result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date"))
                        .skip(offset)
                        .limit(15)
                        .map(DocumentUtils::documentToReviewDTO)
                        .into(new ArrayList<>());
            }
            return new PageDTO<>(result, totalCount, totalPages);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    /**
     * Checks if a user has reviewed a specific media content (anime or manga).
     *
     * @param userId The ID of the user to check.
     * @param reviewIds The list of review IDs of the media content.
     * @return A ReviewDTO object if the user has reviewed the media content, null otherwise.
     * @throws DAOException if a database error or any other error occurs during the operation.
     */
    @Override
    public ReviewDTO isReviewedByUser(String userId, List<String> reviewIds) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(
                    in("_id", reviewIds.stream().map(ObjectId::new).toList()),
                    eq("user.id", new ObjectId(userId))
            );

            return Optional.of(reviewCollection.find(filter))
                    .map(FindIterable::first)
                    .map(DocumentUtils::documentToReviewDTO)
                    .orElse(null);

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }


    /**
     * Retrieves the average ratings for a specific media content (anime or manga) by year within a specified range.
     * The aggregation pipeline performs the following steps:
     * 1. Matches the reviews for the specified media content ID and date range, ensuring the reviews have a rating.
     * 2. Groups the reviews by year and calculates the average rating for each year.
     * 3. Projects the results to include the year and the calculated average rating.
     * 4. Sorts the results by year in ascending order.
     *
     * @param type The type of media content, either `ANIME` or `MANGA`.
     * @param mediaContentId The ID of the specific media content.
     * @param startYear The starting year of the range.
     * @param endYear The ending year of the range.
     * @return A map with years as keys and the corresponding average ratings as values.
     * @throws DAOException if a database error or any other error occurs during the operation.
     */

    @Override
    public Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws  DAOException {
        try  {
            // Get media content rating by year
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            String nodeType = type.equals(MediaContentType.ANIME) ? "anime" : "manga";

            Date startDate = ConverterUtils.localDateToDate(LocalDate.of(startYear, 1, 1));
            Date endDate = ConverterUtils.localDateToDate(LocalDate.of(endYear + 1, 1, 1));
            List<Bson> pipeline = List.of(
                    match(and(
                            eq(nodeType + ".id", new ObjectId(mediaContentId)),
                            exists("rating", true),
                            gte("date", startDate),
                            lt("date", endDate)
                    )),
                    group(new Document("$year", "$date"), avg("average_rating", "$rating")),
                    project(fields(
                            excludeId(),
                            computed("year", "$_id"),
                            include("average_rating"))
                    ),
                    sort(ascending("year"))
            );
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Initialize the result map with years and default values
            Map<String, Double> resultMap = new LinkedHashMap<>();
            for (int year = startYear; year <= endYear; year++) {
                resultMap.put(String.valueOf(year), null);
            }

            // Populate the result map with the average ratings
            for (Document document : result) {
                Double averageRating = document.getDouble("average_rating");
                Integer year = document.getInteger("year");
                resultMap.put(String.valueOf(year), averageRating);
            }
            return resultMap;

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    /**
     * Retrieves the average ratings for a specific media content (anime or manga) by month for a specified year.
     * The aggregation pipeline performs the following steps:
     * 1. Matches the reviews for the specified media content ID and year, ensuring the reviews have a rating.
     * 2. Groups the reviews by month and calculates the average rating for each month.
     * 3. Projects the results to include the month and the calculated average rating.
     * 4. Sorts the results by month in ascending order.
     *
     * @param type The type of media content, either `ANIME` or `MANGA`.
     * @param mediaContentId The ID of the specific media content.
     * @param year The year for which the ratings are to be retrieved.
     * @return A map with month names as keys and the corresponding average ratings as values.
     * @throws DAOException if a database error or any other error occurs during the operation.
     */
    @Override
    public Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws DAOException {
        try  {
            // Get media content rating by month
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            String nodeType = type.equals(MediaContentType.ANIME) ? "anime" : "manga";
            Date startDate = ConverterUtils.localDateToDate(LocalDate.of(year, 1, 1));
            Date endDate = ConverterUtils.localDateToDate(LocalDate.of(year + 1, 1, 1));
            List<Bson> pipeline = List.of(
                    match(and(
                            eq(nodeType + ".id", new ObjectId(mediaContentId)),
                            exists("rating", true),
                            gte("date", startDate),
                            lt("date", endDate)
                    )),
                    group(new Document("$month", "$date"),
                            avg("average_rating", "$rating")
                    ),
                    project(fields(
                            excludeId(),
                            computed("month", "$_id"),
                            include("average_rating")
                    )),
                    sort(ascending("month"))
            );
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Initialize the result map with months and default values
            Map<String, Double> resultMap = new LinkedHashMap<>();
            for (Month month : Month.values()) {
                resultMap.put(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), null);
            }

            // Populate the result map with the average ratings
            for (Document document : result) {
                Object ratingObj = document.get("average_rating");
                Double averageRating = ratingObj instanceof Integer ratingInt ? ratingInt.doubleValue() : (Double) ratingObj;
                Integer month = document.getInteger("month");
                resultMap.put(Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), averageRating);
            }
            return resultMap;

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    /**
     * Suggests media content (anime or manga) based on user criteria (location or birthday year).
     * The aggregation pipeline performs the following steps:
     * 1. Matches the reviews with a rating, the specified media content type and the user criteria.
     * 2. Groups the reviews by media content ID and calculates the average rating for each media content.
     * 3. Projects the results to include the media content title and the calculated average rating.
     * 4. Sorts the results by average rating in descending order.
     * 5. Limits the results to 20 entries.
     *
     * @param mediaContentType The type of media content (ANIME or MANGA).
     * @param criteriaType The type of criteria to filter by ("location" or "birthday").
     * @param criteriaValue The value of the criteria (location as a string or birth year as a string).
     * @return A PageDTO containing a list of suggested media content and the total count.
     * @throws DAOException If there is an error accessing the database or if the criteria type is invalid.
     */
    @Override
    public List<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteriaType, String criteriaValue) throws DAOException {
        try  {
            // Suggest media content based on user criteria
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            String nodeType = mediaContentType.equals(MediaContentType.ANIME) ? "anime" : "manga";

            Bson filter = and(
                    exists("rating", true),
                    exists(nodeType, true)
            );

            if (criteriaType.equals("location")) {
                filter = and(filter, eq("user.location", criteriaValue));
            } else if (criteriaType.equals("birthday")) {
                Date startDate = ConverterUtils.localDateToDate(LocalDate.of(Integer.parseInt(criteriaValue), 1, 1));
                Date endDate = ConverterUtils.localDateToDate(LocalDate.of(Integer.parseInt(criteriaValue) + 1, 1, 1));
                filter = and(filter, gte("user.birthday", startDate), lt("user.birthday", endDate));
            } else {
                throw new Exception("ReviewDAOMongoImpl: suggestMediaContent: Invalid criteria type");
            }

            List<Bson> pipeline = new ArrayList<>(List.of(
                    match(filter),
                    group("$" + nodeType + ".id",
                            first("title", "$" + nodeType + ".title"),
                            avg("average_rating", "$rating")),
                    project(include("title")),
                    sort(descending("average_rating")),
                    limit(20)));

            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());
            if (result.isEmpty()) {
                throw new MongoException("ReviewDAOMongoImpl: suggestMediaContent: No reviews found");
            }

            List<MediaContentDTO> entries = new ArrayList<>();
            for (Document document : result) {
                String contentId = String.valueOf(document.getObjectId("_id"));
                String title = document.getString("title");

                Object ratingObj = document.get("average_rating");
                Double averageRating = ratingObj instanceof Integer ratingInt? ratingInt.doubleValue() : (Double) ratingObj;

                MediaContentDTO mediaContentDTO;
                if (nodeType.equals("anime")) {
                    mediaContentDTO = new AnimeDTO(contentId, title, null, averageRating);
                } else {
                    mediaContentDTO = new MangaDTO(contentId, title, null, averageRating);
                }
                entries.add(mediaContentDTO);
            }
            return entries;

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
