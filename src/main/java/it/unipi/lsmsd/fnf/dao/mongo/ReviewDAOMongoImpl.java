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

    //Add and remove review, add the id in the other collections.
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

    @Override
    //Take review ids in input to update the media content
    public void updateMediaRedundancy(MediaContentDTO mediaContentDTO, List<String> review_ids) throws DAOException {
        //create media embedded Document
        boolean isAnime = mediaContentDTO instanceof AnimeDTO;
        Document mediaDoc = new Document(isAnime ? "anime" : "manga", new Document()
                .append("id", new ObjectId(mediaContentDTO.getId()))
                .append("title", mediaContentDTO.getTitle()));


        //Convert review ids to ObjectId list
        List<ObjectId> reviewObjectIds = new ArrayList<>();
        for (String id : review_ids) {
            reviewObjectIds.add(new ObjectId(id));
        }

        //update the recipe data in all the reviews
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

    @Override
    //Take review ids in input to update the user
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO, List<String> reviewIds) throws DAOException {
        //create user embedded Document
        Document userDoc = new Document();
        appendIfNotNull(userDoc, "user.id", new ObjectId(userSummaryDTO.getId()));
        appendIfNotNull(userDoc, "user.username", userSummaryDTO.getUsername());
        appendIfNotNull(userDoc, "user.picture", userSummaryDTO.getProfilePicUrl());
        appendIfNotNull(userDoc, "user.location", userSummaryDTO.getLocation());
        appendIfNotNull(userDoc, "user.birthday", ConverterUtils.localDateToDate(userSummaryDTO.getBirthDate()));
        Logger logger = LoggerFactory.getLogger(ReviewDAOMongoImpl.class);
        logger.info("userDoc: " + userDoc);
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            //Convert review ids to ObjectId list
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
            logger.info("update: " + update);
            logger.info("filter: " + filter);
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
            //The media contents that have the flag false: we need to take their review ids

            //Get the anime and manga ids and review_ids
            List<Document> animeList = getCollection("anime").find(filter).projection(fields(include("_id", "review_ids"))).into(new ArrayList<>());
            List<Document> mangaList = getCollection("manga").find(filter).projection(fields(include("_id", "review_ids"))).into(new ArrayList<>());


            //map of anime id and review ids
            Map<String, List<String>> animeReviewIds = new HashMap<>();
            Map<String, List<String>> mangaReviewIds = new HashMap<>();


            for (Document anime : animeList) {
                animeReviewIds.put(String.valueOf(anime.getObjectId("_id")), anime.getList("review_ids", String.class));
            }
            for (Document manga : mangaList) {
                mangaReviewIds.put(String.valueOf(manga.getObjectId("_id")), manga.getList("review_ids", String.class));
            }


            // get the average rating for each media content
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            for (Map.Entry<String, List<String>> entry : animeReviewIds.entrySet()) {
                String animeId = entry.getKey();
                List<String> reviewIds = entry.getValue();
                if (reviewIds.isEmpty()) {
                    continue;
                }
                Bson filterReviews = in("_id", reviewIds);
                Bson group = group("$anime.id", avg("avg_rating", "$rating"));
                Document avgRating = reviewCollection.aggregate(List.of(match(filterReviews), group)).first();
                if (avgRating != null) {
                    getCollection("anime").updateOne(eq("_id", animeId), combine(set("avg_rating", avgRating.getDouble("avg_rating")), set("avg_rating_last_update", true)));
                }
            }

            for (Map.Entry<String, List<String>> entry : mangaReviewIds.entrySet()) {
                String mangaId = entry.getKey();
                List<String> reviewIds = entry.getValue();
                if(reviewIds.isEmpty()){
                    continue;
                }
                Bson filterReviews = in("_id", reviewIds);
                Bson group = group("$manga.id", avg("avg_rating", "$rating"));
                Document avgRating = reviewCollection.aggregate(List.of(match(filterReviews), group)).first();
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
     *
     * @param reviewId The ID of the review to be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteReview(String reviewId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(reviewId));

            //Retrieve the review document to get associated media and user IDs
            Document reviewDocument = reviewCollection.find(filter).first();
            if (reviewDocument == null) {
                throw new MongoException("ReviewDAOMongoImpl: deleteReview: Review not found");
            }

            //Delete the review
            if (reviewCollection.deleteOne(filter).getDeletedCount()==0) {
                throw new MongoException("ReviewDAOMongoImpl: deleteReview: Review not deleted");
            }

            //Remove the review ID from the anime and manga collections
            String mediaContentType = reviewDocument.containsKey("anime") ? "anime" : "manga";
            filter = eq("review_ids", reviewId);
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
                            // anime facet
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
            reviewCollection.deleteMany(filter);

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
            reviewCollection.deleteMany(nin("user.id", userIds));

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

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

    @Override
    //id reviews in input
    public PageDTO<ReviewDTO> getReviewByIdsList(List<String> reviewIds, Integer page, String docExcluded) throws DAOException {
        try {
            // Convert reviewIds to ObjectId list
            List<ObjectId> reviewObjectIds = new ArrayList<>();

            for (String id : reviewIds) {
                reviewObjectIds.add(new ObjectId(id));
            }

            // Get the Reviews collection
            MongoCollection<Document> reviewCollection = getCollection("reviews");

            // Create a filter using the reviewObjectIds
            Bson filter = Filters.in("_id", reviewObjectIds);
            Bson projection;
            if (docExcluded != null && docExcluded.equals("user")) {
                projection = Projections.exclude("user");
            } else {
                projection = Projections.exclude("anime", "manga");
            }

            List<ReviewDTO> result;
            int totalCount = (int) reviewCollection.countDocuments(filter);
            Integer totalPages = (int) Math.ceil((double) totalCount / 15);
            if (totalCount == 0) {
                throw new MongoException("ReviewDAOMongoImpl: getReviewByIds: No reviews found");
            }

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

    @Override
    public ReviewDTO isReviewedByUser(String userId, List<String> reviewIds) throws DAOException {
        try {
            Logger logger = LoggerFactory.getLogger(ReviewDAOMongoImpl.class);
            logger.info("Checking if the user has reviewed the media content in dao");
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

            Map<String, Double> resultMap = new LinkedHashMap<>();

            for (int year = startYear; year <= endYear; year++) {
                resultMap.put(String.valueOf(year), null);
            }

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

            Map<String, Double> resultMap = new LinkedHashMap<>();

            for (Month month : Month.values()) {
                resultMap.put(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), null);
            }

            for (Document document : result) {

                Double averageRating;
                Object ratingObj = document.get("average_rating");
                if (ratingObj instanceof Integer) {
                    averageRating = ((Integer) ratingObj).doubleValue();
                } else {
                    averageRating = (Double) ratingObj;
                }
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


    //For users: suggestions based on birthday year and location. For example: show the 25 anime or manga with the highest average ratings in Italy.
    //criteriaType is either birthday (more specifically it's the birthday year) or location
    //criteriaValue is the value of the criteriaType
    @Override
    public List<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteriaType, String criteriaValue) throws DAOException {
        try  {
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
                    sort(descending("average_rating")),
                    project(include("title")),
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

            return entries;

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
