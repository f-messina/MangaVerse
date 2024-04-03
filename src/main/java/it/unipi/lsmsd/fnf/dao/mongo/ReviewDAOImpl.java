package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

import it.unipi.lsmsd.fnf.dao.interfaces.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;

/**
 * Implementation of ReviewDAO interface for MongoDB data access operations related to reviews.
 * This class provides methods to insert, update, delete, and retrieve reviews from the database,
 * as well as methods to perform various analytical queries on review data.
 */
public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    private static final String COLLECTION_NAME = "reviews";

    /**
     * Inserts a new review into the database or updates an existing one if the user has already reviewed the media content.
     *
     * @param reviewDTO The ReviewDTO object representing the review to be inserted or updated.
     * @throws DAOException If an error occurs during the insertion or update process.
     */
    @Override
    public void createReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            MongoCollection<Document> mediaCollection;
            Bson filter;
            if (reviewDTO.getMediaContent() instanceof AnimeDTO) {
                mediaCollection = getCollection("anime");
                // Check if the anime exists
                if (mediaCollection.find(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))).first() == null) {
                    throw new MongoException("Anime with id " + reviewDTO.getMediaContent().getId() + " does not exist");
                }
                // Create a filter based on anime.id/manga.id and user.id
                filter = and(
                        eq("anime.id", reviewDTO.getMediaContent().getId()),
                        eq("user.id", reviewDTO.getUser().getId())
                );
            } else if (reviewDTO.getMediaContent() instanceof MangaDTO) {
                mediaCollection = getCollection("manga");
                // Check if the manga exists
                if (mediaCollection.find(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))).first() == null) {
                    throw new MongoException("Manga with id " + reviewDTO.getMediaContent().getId() + " does not exist");
                }
                // Create a filter based on anime.id/manga.id and user.id
                filter = and(
                        eq("manga.id", reviewDTO.getMediaContent().getId()),
                        eq("user.id", reviewDTO.getUser().getId())
                );
            } else {
                throw new DAOException("Invalid media content type");
            }

            Bson update = setOnInsert(reviewDTOToDocument(reviewDTO));

            // Insert the reviewDTO if it does not exist
            UpdateResult result = reviewCollection.updateOne(filter, update, new UpdateOptions().upsert(true));

            // Check if the document was inserted
            if (result.getUpsertedId() != null) {
                reviewDTO.setId(result.getUpsertedId().asObjectId().getValue().toHexString());
            } else {
                // Document was not inserted or updated, indicating that a similar review already exists
                throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_KEY, "The user have already reviewed this media content.");
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

            reviewCollection.updateOne(filter, updatedKeys);

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
            if (reviewCollection.updateMany(eq(isAnime ? "anime.id" : "manga.id",
                    new ObjectId(mediaContentDTO.getId())), new Document("$set", mediaDoc)).getModifiedCount() == 0) {
                throw new MongoException("No reviews found for the media content");
            }


        /*
        try (ClientSession session = getMongoClient().startSession()) {

            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary()) //reading from primary data should be the most up to date
                    .readConcern(ReadConcern.LOCAL) //read from local data
                    .writeConcern(WriteConcern.MAJORITY) //write to the majority of replicas
                    .build();

            //create media embedded Document
            Document mediaDoc = new Document(mediaType == MediaContentType.ANIME ? "anime" : "manga", new Document()
                    .append("id", new ObjectId(mediaContentDTO.getId()))
                    .append("title", mediaContentDTO.getTitle()));
            TransactionBody<String> txnBody = () -> {

                    MongoCollection<Document> reviewCollection = getCollection("review");

                    //update the recipe data in all the reviews
                    reviewCollection.updateMany(session, eq(mediaType == MediaContentType.ANIME ? "anime" : "manga", new ObjectId(mediaContentDTO.getId())), new Document("$set", mediaDoc));

                    return "Done";
            };

            //start the transaction
            session.withTransaction(txnBody, txnOptions);

         */

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException {

        //create user embedded Document
        Document userInfo = new Document();
        appendIfNotNull(userInfo, "id", new ObjectId(userSummaryDTO.getId()));
        appendIfNotNull(userInfo, "username", userSummaryDTO.getUsername());
        appendIfNotNull(userInfo, "picture", userSummaryDTO.getProfilePicUrl());
        appendIfNotNull(userInfo, "location", userSummaryDTO.getLocation());
        appendIfNotNull(userInfo, "birthday", ConverterUtils.localDateToDate(userSummaryDTO.getBirthDate()));
        Document userDoc = new Document("user", userInfo);

        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            if (reviewCollection.updateMany(eq("user.id",
                    new ObjectId(userSummaryDTO.getId())), new Document("$set", userDoc)).getModifiedCount() == 0) {
                throw new MongoException("No reviews found for the user");
            }

        /*try (ClientSession session = getMongoClient().startSession()) {

            TransactionOptions txnOptions = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary()) //reading from primary data should be the most up to date
                    .readConcern(ReadConcern.LOCAL) //read from local data
                    .writeConcern(WriteConcern.MAJORITY) //write to the majority of replicas
                    .build();

            //create user emebedded embedded Document
            Document userDoc = new Document("user", new Document()
                    .append("username", userSummaryDTO.getUsername())
                    .append("picture", userSummaryDTO.getProfilePicUrl()))
                    .append("location", userSummaryDTO.getLocation())
                    .append("birthday", ConverterUtils.localDateToDate(userSummaryDTO.getBirthDate()));

            TransactionBody<String> txnBody = () -> {

                MongoCollection<Document> reviewCollection = getCollection("review");

                //update the recipe data in all the reviews
                reviewCollection.updateMany(session, eq("user", new ObjectId(userSummaryDTO.getId())), new Document("$set", userDoc));

                return "Done";
            };

            //start the transaction
            session.withTransaction(txnBody, txnOptions);

         */

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

            reviewCollection.deleteOne(filter);

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
            //update the recipe data in all the reviews
            if (reviewCollection.deleteMany(filter).getDeletedCount() == 0) {
                throw new MongoException("No reviews found without media content");
            }

            /*
            try (ClientSession session = getMongoClient().startSession()) {

                TransactionOptions txnOptions = TransactionOptions.builder()
                        .readPreference(ReadPreference.primary()) //reading from primary data should be the most up to date
                        .readConcern(ReadConcern.LOCAL) //read from local data
                        .writeConcern(WriteConcern.MAJORITY) //write to the majority of replicas
                        .build();

                TransactionBody<String> txnBody = () -> {

                    MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
                    Bson filter = and(
                            nin("anime.id", animeIds),
                            nin("manga.id", mangaIds)
                    );
                    //update the recipe data in all the reviews
                    reviewCollection.deleteMany(session, filter);
                    return "Done";
                };

                session.withTransaction(txnBody, txnOptions);
            }
             */
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
            //update the recipe data in all the reviews
            if (reviewCollection.deleteMany(nin("user.id", userIds)).getDeletedCount() == 0) {
                throw new MongoException("No reviews without author found");
            }

            /*
            try (ClientSession session = getMongoClient().startSession()) {

                TransactionOptions txnOptions = TransactionOptions.builder()
                        .readPreference(ReadPreference.primary()) //reading from primary data should be the most up to date
                        .readConcern(ReadConcern.LOCAL) //read from local data
                        .writeConcern(WriteConcern.MAJORITY) //write to the majority of replicas
                        .build();

                TransactionBody<String> txnBody = () -> {

                    MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
                    //update the recipe data in all the reviews
                    reviewCollection.deleteMany(session, nin("user.id", userIds));

                    return "Done";
                };

                session.withTransaction(txnBody, txnOptions);
            }

             */

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
                        .map(this::documentToReviewDTO).into(new ArrayList<>());
                return new PageDTO<>(result, result.size());
            } else {
                int offset = (page - 1) * Constants.PAGE_SIZE;
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date")).skip(offset).limit(Constants.PAGE_SIZE)
                        .map(this::documentToReviewDTO).into(new ArrayList<>());
                int totalCount = (int) reviewCollection.countDocuments(filter);
                return new PageDTO<>(result, totalCount);
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
                throw new DAOException("Invalid media content type");
            }

            if (page == null) {
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date"))
                        .map(this::documentToReviewDTO).into(new ArrayList<>());
                return new PageDTO<>(result, result.size());
            } else {
                int offset = (page - 1) * Constants.PAGE_SIZE;
                List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                        .sort(descending("date")).skip(offset).limit(Constants.PAGE_SIZE)
                        .map(this::documentToReviewDTO).into(new ArrayList<>());
                int totalCount = (int) reviewCollection.countDocuments(filter);
                return new PageDTO<>(result, totalCount);
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Converts a ReviewDTO object to a MongoDB document for storage in the database.
     *
     * @param reviewDTO The ReviewDTO object to be converted.
     * @return A MongoDB Document representing the ReviewDTO object.
     */
    private Document reviewDTOToDocument(ReviewDTO reviewDTO) {
        Document reviewDocument = new Document()
                .append("user", new Document()
                        .append("id", new ObjectId(reviewDTO.getUser().getId()))
                        .append("username", reviewDTO.getUser().getUsername())
                        .append("picture", reviewDTO.getUser().getProfilePicUrl())
                        .append("location", reviewDTO.getUser().getLocation())
                        .append("birthday", ConverterUtils.localDateToDate(reviewDTO.getUser().getBirthDate())))
                .append("date", ConverterUtils.localDateToDate(LocalDate.now()));
        if (reviewDTO.getComment() != null) {
            reviewDocument.append("comment", reviewDTO.getComment());
        }
        if (reviewDTO.getRating() != null) {
            reviewDocument.append("rating", reviewDTO.getRating());
        }
        boolean isAnime = reviewDTO.getMediaContent() instanceof AnimeDTO;
        reviewDocument.append(isAnime? "anime" : "manga", new Document()
                .append("id", new ObjectId(reviewDTO.getMediaContent().getId()))
                .append("title", reviewDTO.getMediaContent().getTitle()));

        return reviewDocument;
    }

    /**
     * Converts a MongoDB document representing a review to a ReviewDTO object.
     *
     * @param reviewDoc The MongoDB document representing the review.
     * @return A ReviewDTO object representing the MongoDB document.
     */
    private ReviewDTO documentToReviewDTO(Document reviewDoc) {
        String reviewId = reviewDoc.getObjectId("_id").toString();
        LocalDate date = ConverterUtils.dateToLocalDate(reviewDoc.getDate("date"));
        String comment = reviewDoc.getString("comment");
        Integer rating = reviewDoc.getInteger("rating");

        MediaContentDTO mediaDTO = null;
        Document mediaDoc;
        if ((mediaDoc = reviewDoc.get("anime", Document.class)) != null) {
            mediaDTO = new AnimeDTO(mediaDoc.getObjectId("id").toString(), mediaDoc.getString("title"));
        } else if ((mediaDoc = reviewDoc.get("manga", Document.class)) != null) {
            mediaDTO = new MangaDTO(mediaDoc.getObjectId("id").toString(), mediaDoc.getString("title"));
        }

        Document userDoc = reviewDoc.get("user", Document.class);
        UserSummaryDTO userDTO = (userDoc != null) ? new UserSummaryDTO(userDoc.getObjectId("id").toString(), userDoc.getString("username"), userDoc.getString("picture")) : null;

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }

    //MongoDB queries
    //Find the average rating a user has given to media contents given the userId
    @Override
    public Double averageRatingUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { 'user.id': ObjectId('" + new ObjectId(userId) + "') }}")); // Match reviews by user ID
            pipeline.add(Document.parse("{$group: { _id: '$user.id', averageRating: { $avg: '$rating' }}}")); // Group by user ID and calculate average rating
            // Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Retrieve the average rating from the aggregation result
            if (!result.isEmpty()) {
                Document aggregationResult = result.getFirst();
                Double averageRating = aggregationResult.getDouble("averageRating");
                return averageRating;
            }
            return null; // Return -1 if no reviews are found

        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user", e);
        }
    }


    //Find the trend of an anime or manga by year, giving in input the media content id
    //It returns the average rating of the anime or manga for each year
    @Override
    public Map<String, Double> getMediaContentRatingByYear(MediaContentType type, String mediaContentId, int startYear, int endYear) throws  DAOException {
        try  {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            String nodeType = type.equals(MediaContentType.ANIME) ? "anime" : "manga";

            List<Document> pipeline = new ArrayList<>();


            pipeline.add(Document.parse("{$match: { \"" + nodeType + ".id\": ObjectId(\"" + mediaContentId + "\"), rating: {$exists: true}, date: {$gte: ISODate(\"" + startYear + "-01-01T00:00:00.000Z\"), $lte: ISODate(\"" + endYear + "-12-31T23:59:59.999Z\")}}}"));
            pipeline.add(Document.parse("{$group: {_id: {$year: \"$date\",}, average_rating: {$avg: \"$rating\"}}}"));
            pipeline.add(Document.parse("{$project: {_id: 0, year: \"$_id\", average_rating: 1}}"));
            pipeline.add(Document.parse("{$sort: {year: 1}}"));

            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());


            Map<String, Double> resultMap = new LinkedHashMap<>();
            for (Document document : result) {
                Double averageRating = document.getDouble("average_rating");
                Integer year = document.getInteger("year");
                resultMap.put(String.valueOf(year), averageRating);
            }


            return resultMap;

        } catch (MongoException e) {
            throw new DAOException("Error while finding ratings by user " + e.getMessage(), e);
        }


    }

    //This function returns the average rating of media content by month when giving in input a certain year and the media content id
    // 01 = 6.7, 02 = 7.5, 03 = 8.0, 04 = 4.5 (2020)
    @Override
    public Map<String, Double> getMediaContentRatingByMonth(MediaContentType type, String mediaContentId, int year) throws DAOException {
        try  {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            String nodeType = type.equals(MediaContentType.ANIME) ? "anime" : "manga";

            List<Document> pipeline = new ArrayList<>();

            pipeline.add(Document.parse("{$match: { \"" + nodeType + ".id\": ObjectId(\"" + mediaContentId + "\"), rating: {$exists: true}, date: { $gte: ISODate(\"" + year + "-01-01T00:00:00.000Z\"), $lt: ISODate(\"" + (year + 1) + "-01-01T00:00:00.000Z\")}}}"));
            pipeline.add(Document.parse("{$group: {_id: { $month: \"$date\" }, average_rating: {$avg: \"$rating\"}}}"));
            pipeline.add(Document.parse("{$project: {_id: 0, month: \"$_id\", average_rating: 1}}"));
            pipeline.add(Document.parse("{$sort: {month: 1}}"));

            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

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
                resultMap.put(String.format("%02d", month), averageRating); // formatta il mese come "01", "02", ecc.
            }

            return resultMap;

        } catch (MongoException e) {
            throw new DAOException("Error while finding ratings by user " + e.getMessage(), e);
        }
    }


    //For users: suggestions based on birthday year and location. For example: show the 25 anime or manga with the highest average ratings in Italy.
    @Override
    public PageDTO<MediaContentDTO> suggestMediaContent(MediaContentType mediaContentType, String criteria, String type) throws DAOException {
        try  {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            String nodeType = mediaContentType.equals(MediaContentType.ANIME) ? "anime" : "manga";

            //nodeType is either anime or manga
            //type is either birthday (more specifically it's the birthday year) or location
            //criteria is the value of the type

            List<Document> pipeline = new ArrayList<>();
            if (criteria.equals("location")) {
                pipeline.add(Document.parse("{$match: {\"user." + type + "\": \"" + criteria + "\"}}"));


            } else if(criteria.equals("birthday")) {
                //Transform the criteria into an integer
                Integer.parseInt(criteria);
                pipeline.add(Document.parse("{$match: { \"user." + type + "\": { $gte: ISODate(\"" + criteria + "-01-01\"), $lt: ISODate(\"" + criteria + "-12-31\") } }}"));
            }
            pipeline.add(Document.parse("{$group: {_id: \"$" + nodeType + ".id\",  title: { $first: \"$"+ nodeType + ".title\" }, average_rating: { $avg: \"$rating\" }}}"));
            pipeline.add(Document.parse("{$sort: {average_rating: -1 }}"));
            pipeline.add(Document.parse("{$limit: 25}"));


            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            List<MediaContentDTO> entries = new ArrayList<>();

            for (Document document : result) {
                String contentId = String.valueOf(document.getObjectId("_id"));
                String title = document.getString("title");

                Object ratingObj = document.get("average_rating");
                Double averageRating = ratingObj instanceof Integer ratingInt? ratingInt.doubleValue() :
                        (Double) ratingObj;

                if (nodeType.equals("anime")) {
                    MediaContentDTO mediaContentDTO = new AnimeDTO(contentId, title, null, averageRating); // imageUrl is null because not included in the query results
                    entries.add(mediaContentDTO);


                } else if (nodeType.equals("manga")) {
                    MediaContentDTO mediaContentDTO = new MangaDTO(contentId, title, null, averageRating); // imageUrl is null because not included in the query results
                    entries.add(mediaContentDTO);

                }


            }

            int totalCount = entries.size();
            return new PageDTO<>(entries, totalCount);

        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by users", e);

        }
    }

    private AnimeDTO documentToAnimeDTO(Document doc) {
        AnimeDTO anime = new AnimeDTO();
        anime.setId(doc.getObjectId("_id").toString());
        anime.setTitle(doc.getString("title"));
        anime.setImageUrl(doc.getString("picture"));
        Object averageRatingObj = doc.get("average_rating");
        anime.setAverageRating(
                (averageRatingObj instanceof Integer) ? ((Integer) averageRatingObj).doubleValue() :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : 0.0
        );
        if ((doc.get("anime_season", Document.class) != null)) {
            anime.setYear(doc.get("anime_season", Document.class).getInteger("year"));
            anime.setSeason(doc.get("anime_season", Document.class).getString("season"));
        }

        return anime;
    }

    private MangaDTO documentToMangaDTO(Document doc) {
        MangaDTO manga = new MangaDTO();
        manga.setId(doc.getObjectId("_id").toString());
        manga.setTitle(doc.getString("title"));
        manga.setImageUrl(doc.getString("picture"));
        Object averageRatingObj = doc.get("average_rating");
        manga.setAverageRating(
                (averageRatingObj instanceof Integer) ? Double.valueOf(((Integer) averageRatingObj)) :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : null
        );
        manga.setStartDate(ConverterUtils.dateToLocalDate(doc.getDate("start_date")));
        manga.setEndDate(ConverterUtils.dateToLocalDate(doc.getDate("end_date")));

        return manga;
    }
}
