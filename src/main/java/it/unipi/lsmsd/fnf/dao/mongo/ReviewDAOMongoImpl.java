package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
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
import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.appendIfNotNull;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.reviewDTOToDocument;

/**
 * Implementation of ReviewDAO interface for MongoDB data access operations related to reviews.
 * This class provides methods to insert, update, delete, and retrieve reviews from the database,
 * as well as methods to perform various analytical queries on review data.
 */
public class ReviewDAOMongoImpl extends BaseMongoDBDAO implements ReviewDAO {
    private static final String COLLECTION_NAME = "reviews";

    /**
     * Inserts a new review into the database or updates an existing one if the user has already reviewed the media content.
     *
     * @param reviewDTO The ReviewDTO object representing the review to be inserted or updated.
     * @throws DAOException If an error occurs during the insertion or update process.
     */
    @Override
    public void saveReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            MongoCollection<Document> mediaCollection;
            Bson filter;

            if (reviewDTO.getMediaContent() instanceof AnimeDTO) {
                mediaCollection = getCollection("anime");
                // Check if the anime exists
                if (mediaCollection.countDocuments(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))) == 0) {
                    throw new MongoException("ReviewDAOMongoImpl: saveReview: Anime not found");
                }
                // Create a filter based on anime.id/manga.id and user.id
                filter = and(
                        eq("anime.id", new ObjectId(reviewDTO.getMediaContent().getId())),
                        eq("user.id", new ObjectId(reviewDTO.getUser().getId()))
                );
            } else if (reviewDTO.getMediaContent() instanceof MangaDTO) {
                mediaCollection = getCollection("manga");
                // Check if the manga exists
                if (mediaCollection.countDocuments(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))) == 0) {
                    throw new MongoException("ReviewDAOMongoImpl: saveReview: Manga not found");
                }
                // Create a filter based on anime.id/manga.id and user.id
                filter = and(
                        eq("manga.id", new ObjectId(reviewDTO.getMediaContent().getId())),
                        eq("user.id", new ObjectId(reviewDTO.getUser().getId()))
                );
            } else {
                throw new DAOException("Invalid media content type");
            }

            reviewDTO.setDate(LocalDate.now());
            Bson update = setOnInsert(reviewDTOToDocument(reviewDTO));

            // Insert the reviewDTO if it does not exist
            UpdateResult result = reviewCollection.updateOne(filter, update, new UpdateOptions().upsert(true));

            // Check if the document was inserted
            if (result.getUpsertedId() != null) {
                reviewDTO.setId(result.getUpsertedId().asObjectId().getValue().toHexString());
            } else {
                // Document was not inserted or updated, indicating that a similar review already exists
                throw new DuplicatedException(DuplicatedExceptionType.GENERIC, "ReviewDAOMongoImpl: saveReview: The user have already reviewed this media content.");
            }

        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

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
            Bson updatedKeys = combine(set("date", ConverterUtils.localDateToDate(LocalDate.now())));
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

    @Override
    public void updateMediaRedundancy(MediaContentDTO mediaContentDTO) throws DAOException {
        //create media embedded Document
        boolean isAnime = mediaContentDTO instanceof AnimeDTO;
        Document mediaDoc = new Document(isAnime ? "anime" : "manga", new Document()
                .append("id", new ObjectId(mediaContentDTO.getId()))
                .append("title", mediaContentDTO.getTitle()));

        //update the recipe data in all the reviews
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            Bson filter = eq(isAnime ? "anime.id" : "manga.id", new ObjectId(mediaContentDTO.getId()));

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

    @Override
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException {

        //create user embedded Document
        Document userDoc = new Document();
        appendIfNotNull(userDoc, "user.id", new ObjectId(userSummaryDTO.getId()));
        appendIfNotNull(userDoc, "user.username", userSummaryDTO.getUsername());
        appendIfNotNull(userDoc, "user.picture", userSummaryDTO.getProfilePicUrl());
        appendIfNotNull(userDoc, "user.location", userSummaryDTO.getLocation());
        appendIfNotNull(userDoc, "user.birthday", ConverterUtils.localDateToDate(userSummaryDTO.getBirthDate()));

        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userSummaryDTO.getId()));

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

    @Override
    public void updateAverageRatingMedia() throws DAOException {
        try {
            // get the list of anime and manga IDs whose average rating has not been updated
            Bson filter = or(eq("avg_rating_last_update", false), exists("avg_rating_last_update", false));
            List<ObjectId> animeIds = getCollection("anime").find(filter)
                    .projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();
            List<ObjectId> mangaIds = getCollection("manga").find(filter)
                    .projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();

            // get the average rating for each media content
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = List.of(
                    facet(
                            // anime facet
                            new Facet("anime", List.of(
                                    match(and(
                                            in("anime.id", animeIds),
                                            exists("rating", true)
                                    )),
                                    group("$anime.id", avg("average_rating", "$rating")),
                                    project(computed("average_rating", new Document("$round", List.of("$average_rating", 2))))
                            )),
                            // manga facet
                            new Facet("manga", List.of(
                                    match(and(
                                            in("manga.id", mangaIds),
                                            exists("rating", true)
                                    )),
                                    group("$manga.id", avg("average_rating", "$rating")),
                                    project(computed("average_rating", new Document("$round", List.of("$average_rating", 2))))
                            ))
                    )
            );
            Document result = reviewCollection.aggregate(pipeline).first();

            // update the average rating for each media content and set the last update flag to true
            Optional.ofNullable(result).orElse(new Document()).getList("anime", Document.class).forEach(document -> {
                Bson update = combine(set("average_rating", document.get("average_rating")), set("avg_rating_last_update", true));
                getCollection("anime").updateOne(eq("_id", document.getObjectId("_id")), update);
            });
            Optional.ofNullable(result).orElse(new Document()).getList("manga", Document.class).forEach(document -> {
                Bson update = combine(set("average_rating", document.get("average_rating")), set("avg_rating_last_update", true));
                getCollection("manga").updateOne(eq("_id", document.getObjectId("_id")), update);
            });

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Deletes a review from the database based on its ID.
     *
     * @param reviewId The ID of the review to be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteReview(String reviewId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(reviewId));

            if (reviewCollection.deleteOne(filter).getDeletedCount() == 0) {
                throw new MongoException("ReviewDAOMongoImpl: deleteReview: Review not found");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR,"The review is not found.");

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public void refreshLatestReviewsOnUserDeletion(String userId) throws DAOException {
        try {

            // Remove the latest reviews array from the anime and manga that have only the user's reviews
            Bson filterRemoveLatestReviewsArray = and(
                    eq("latest_reviews.user.id", new ObjectId(userId)),
                    eq("latest_reviews", new Document("$size", 1))
            );
            Bson removeLatestReviewsArray = unset("latest_reviews");
            getCollection("anime").updateMany(filterRemoveLatestReviewsArray, removeLatestReviewsArray);
            getCollection("manga").updateMany(filterRemoveLatestReviewsArray, removeLatestReviewsArray);

            // Remove the user's reviews from the latest reviews array of the anime and manga that have less than 5 reviews
            Bson filterRemoveUserReviews = and(
                    eq("latest_reviews.user.id", new ObjectId(userId)),
                    not(size("latest_reviews", 5))
            );
            Bson removeUserReview = pull("latest_reviews", eq("user.id", new ObjectId(userId)));
            getCollection("anime").updateMany(filterRemoveUserReviews, removeUserReview);
            getCollection("manga").updateMany(filterRemoveUserReviews, removeUserReview);

            // Get the IDs of the remaining anime and manga that the user has reviewed recently
            List<ObjectId> animeIds = getCollection("anime").find(Filters.elemMatch("latest_reviews", eq("user.id", new ObjectId(userId))))
                    .map(doc -> doc.getObjectId("_id")).into(new ArrayList<>());
            List<ObjectId> mangaIds = getCollection("manga").find(Filters.elemMatch("latest_reviews", eq("user.id", new ObjectId(userId))))
                    .map(doc -> doc.getObjectId("_id")).into(new ArrayList<>());

            // Get the latest reviews for the anime and manga that the user has reviewed recently
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Bson> pipeline = List.of(
                    facet(
                            // anime facet
                            new Facet("anime", List.of(
                                    match(in("anime.id", animeIds)),
                                    sort(descending("date")),
                                    group("$anime.id", Accumulators.push("latest_reviews", "$$ROOT")),
                                    project(computed("latest_reviews", new Document("$map",
                                            new Document("input", new Document("$slice", Arrays.asList("$latest_reviews", 5)))
                                                    .append("in", new Document()
                                                                    .append("user", "$$this.user")
                                                                    .append("comment", "$$this.comment")
                                                                    .append("date", "$$this.date")
                                                                    .append("rating", "$$this.rating")
                                                    )
                                    )))
                            )),
                            new Facet("manga", List.of(
                                    match(in("manga.id", mangaIds)),
                                    sort(descending("date")),
                                    group("$manga.id", Accumulators.push("latest_reviews", "$$ROOT")),
                                    project(computed("latest_reviews", new Document("$map",
                                            new Document("input", new Document("$slice", Arrays.asList("$latest_reviews", 5)))
                                                    .append("in", new Document()
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
                Bson filter = eq("_id", document.getObjectId("_id"));
                Bson update = set("latest_reviews", document.getList("latest_reviews", Document.class));
                getCollection("anime").updateOne(filter, update);
            });
            latestReviews.getList("manga", Document.class).forEach(document -> {
                Bson filter = eq("_id", document.getObjectId("_id"));
                Bson update = set("latest_reviews", document.getList("latest_reviews", Document.class));
                getCollection("manga").updateOne(filter, update);
            });

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Deletes all reviews associated with media content not present in the database.
     * This method is used to clean up the database when media content is deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    public void deleteReviewsWithNoMedia() throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection("anime");
            MongoCollection<Document> mangaCollection = getCollection("manga");
            List<ObjectId> animeIds = animeCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();
            List<ObjectId> mangaIds = mangaCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();

            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            Bson filter = and(
                    nin("anime.id", animeIds),
                    nin("manga.id", mangaIds)
            );

            // Delete reviews with anime IDs or manga IDs not present in the anime or manga collection
            System.out.println(reviewCollection.deleteMany(filter).getDeletedCount());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public void deleteReviewsByMedia(String mediaId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = or(eq("anime.id", new ObjectId(mediaId)), eq("manga.id", new ObjectId(mediaId)));

            System.out.println("Deleted reviews:" + reviewCollection.deleteMany(filter).getDeletedCount());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Deletes all reviews associated with users not present in the database.
     * This method is used to clean up the database when users are deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteReviewsWithNoAuthor() throws DAOException {
        try {

            MongoCollection<Document> userCollection = getCollection("users");
            List<ObjectId> userIds = userCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();

            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            // Delete reviews with user IDs not present in the users collection
            System.out.println(reviewCollection.deleteMany(nin("user.id", userIds)).getDeletedCount());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public void deleteReviewsByAuthor(String userId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userId));

            System.out.println("Deleted reviews:" + reviewCollection.deleteMany(filter).getDeletedCount());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }


    @Override
    public PageDTO<ReviewDTO> getReviewByUser(String userId, Integer page) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userId));
            Bson projection = exclude("user");

            if (page == null) {
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date"))
                        .map(DocumentUtils::documentToReviewDTO).into(new ArrayList<>());

                if (result.isEmpty()) {
                    throw new MongoException("ReviewDAOMongoImpl: getReviewByUser: No reviews found");
                } else {
                    return new PageDTO<>(result, result.size());
                }
            } else {
                int offset = (page - 1) * Constants.PAGE_SIZE;
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date")).skip(offset).limit(Constants.PAGE_SIZE)
                        .map(DocumentUtils::documentToReviewDTO).into(new ArrayList<>());
                int totalCount = (int) reviewCollection.countDocuments(filter);

                if (result.isEmpty()) {
                    throw new MongoException("ReviewDAOMongoImpl: getReviewByUser: No reviews found");
                } else {
                    return new PageDTO<>(result, totalCount);
                }
            }
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Retrieves a page of reviews associated with a specific media content from the database.
     *
     * @param mediaId The ID of the media content for which reviews should be retrieved.
     * @param type    The type of media content (anime or manga).
     * @param page    The page number of the reviews to be retrieved.
     * @return A PageDTO object containing the reviews associated with the specified media content.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public PageDTO<ReviewDTO> getReviewByMedia(String mediaId, MediaContentType type, Integer page) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter;
            Bson projection;
            if (type == MediaContentType.ANIME) {
                filter = eq("anime.id", new ObjectId(mediaId));
                projection = exclude("anime");
            } else if (type == MediaContentType.MANGA) {
                filter = eq("manga.id", new ObjectId(mediaId));
                projection = exclude("manga");
            } else {
                throw new Exception("ReviewDAOMongoImpl: getReviewByMedia: Invalid media content type");
            }

            if (page == null) {
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date"))
                        .map(DocumentUtils::documentToReviewDTO).into(new ArrayList<>());


                if (result.isEmpty()) {
                    throw new MongoException("ReviewDAOMongoImpl: getReviewByMedia: No reviews found");
                }

                return new PageDTO<>(result, result.size());
            } else {
                int offset = (page - 1) * Constants.PAGE_SIZE;
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date")).skip(offset).limit(Constants.PAGE_SIZE)
                        .map(DocumentUtils::documentToReviewDTO).into(new ArrayList<>());
                int totalCount = (int) reviewCollection.countDocuments(filter);
                if (result.isEmpty()) {
                    throw new MongoException("ReviewDAOMongoImpl: getReviewByMedia: No reviews found");
                }

                return new PageDTO<>(result, totalCount);
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    //Find the trend of an anime or manga by year, giving in input the media content id
    //It returns the average rating of the anime or manga for each year
    @Override
    public Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws  DAOException {
        try  {
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
            if (result.isEmpty()) {
                throw new MongoException("ReviewDAOMongoImpl: getMediaContentRatingByYear: No reviews found");
            }
            Map<String, Double> resultMap = new LinkedHashMap<>();
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

    //This function returns the average rating of media content by month when giving in input a certain year and the media content id
    @Override
    public Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws DAOException {
        try  {
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
            if (result.isEmpty()) {
                throw new MongoException("ReviewDAOMongoImpl: getMediaContentRatingByMonth: No reviews found");
            }

            Map<String, Double> resultMap = new LinkedHashMap<>();
            for (Document document : result) {

                Double averageRating;
                Object ratingObj = document.get("average_rating");
                if (ratingObj instanceof Integer) {
                    averageRating = ((Integer) ratingObj).doubleValue();
                } else {
                    averageRating = (Double) ratingObj;
                }
                Integer month = document.getInteger("month");
                resultMap.put(String.format("%02d", month), averageRating); // Format month as two digits
            }

            return resultMap;

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    //For users: suggestions based on birthday year and location. For example: show the 25 anime or manga with the highest average ratings in Italy.
    //criteriaType is either birthday (more specifically it's the birthday year) or location
    //criteriaValue is the value of the criteriaType
    @Override
    public PageDTO<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteriaType, String criteriaValue) throws DAOException {
        try  {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            String nodeType = mediaContentType.equals(MediaContentType.ANIME) ? "anime" : "manga";

            List<Bson> pipeline = new ArrayList<>();

            if (criteriaType.equals("location")) {
                pipeline.add(match(eq("user." + criteriaType, criteriaValue)));

            } else if (criteriaType.equals("birthday")) {
                // Transform the criteriaValue into an integer
                Date startDate = ConverterUtils.localDateToDate(LocalDate.of(Integer.parseInt(criteriaValue), 1, 1));
                Date endDate = ConverterUtils.localDateToDate(LocalDate.of(Integer.parseInt(criteriaValue) + 1, 1, 1));
                pipeline.add(match(and(
                        gte("user." + criteriaType, startDate),
                        lt("user." + criteriaType, endDate)
                )));

            } else {
                throw new Exception("ReviewDAOMongoImpl: suggestMediaContent: Invalid criteria type");
            }

            pipeline.addAll(List.of(
                    group("$" + nodeType + ".id",
                    first("title", "$" + nodeType + ".title"),
                    avg("average_rating", "$rating")),
                    sort(descending("average_rating")),
                    limit(Constants.PAGE_SIZE)));

            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());
            if (result.isEmpty()) {
                throw new MongoException("ReviewDAOMongoImpl: suggestMediaContent: No reviews found");
            }

            List<MediaContentDTO> entries = new ArrayList<>();
            for (Document document : result) {
                String contentId = String.valueOf(document.getObjectId("_id"));
                String title = document.getString("title");

                Object ratingObj = document.get("average_rating");
                Double averageRating = ratingObj instanceof Integer ratingInt? ratingInt.doubleValue() :
                        (Double) ratingObj;

                MediaContentDTO mediaContentDTO;// imageUrl is null because not included in the query results
                if (nodeType.equals("anime")) {
                    mediaContentDTO = new AnimeDTO(contentId, title, null, averageRating);
                } else {
                    mediaContentDTO = new MangaDTO(contentId, title, null, averageRating);
                }
                entries.add(mediaContentDTO);
            }
            int totalCount = entries.size();

            return new PageDTO<>(entries, totalCount);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
