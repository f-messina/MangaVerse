package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
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
     * @param review The ReviewDTO object representing the review to be inserted or updated.
     * @return The ID of the inserted or updated review, or a message indicating that the user has already reviewed this media content.
     * @throws DAOException If an error occurs during the insertion or update process.
     */
    @Override
    public String insert(ReviewDTO review) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            // Create a filter based on anime.id/manga.id and user.id
            Bson filter = and(
                    or(
                            eq("anime.id", review.getMediaContent().getId()),
                            eq("manga.id", review.getMediaContent().getId())
                    ),
                    eq("user.id", review.getUser().getId())
            );
            Bson update = setOnInsert(reviewDTOToDocument(review));

            UpdateResult result = reviewCollection.updateOne(filter, update, new UpdateOptions().upsert(true));

            // Check if the document was inserted or updated
            if (result.getUpsertedId() != null) {
                return result.getUpsertedId().asObjectId().getValue().toString();
            } else {
                // Document was not inserted or updated, indicating that a similar review already exists
                return "The user have already reviewed this media content.";
            }
        } catch (Exception e) {
            throw new DAOException("Error while inserting/updating review", e);
        }
    }

    /**
     * Updates an existing review in the database.
     *
     * @param review The ReviewDTO object representing the review to be updated.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void update(ReviewDTO review) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(review.getId()));
            Bson updatedKeys = combine(set("date", ConverterUtils.localDateToDate(LocalDate.now())));
            if (review.getComment() != null) {
                updatedKeys = combine(updatedKeys, set("comment", review.getComment()));
            } else {
                updatedKeys = combine(updatedKeys, unset("comment"));
            }
            if (review.getRating() != null) {
                updatedKeys = combine(updatedKeys, set("rating", review.getRating()));
            } else {
                updatedKeys = combine(updatedKeys, unset("rating"));
            }

            reviewCollection.updateOne(filter, updatedKeys);
        } catch (Exception e) {
            throw new DAOException("Error while updating review", e);
        }
    }

    /**
     * Deletes a review from the database based on its ID.
     *
     * @param reviewId The ID of the review to be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void delete(String reviewId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(reviewId));

            reviewCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    /**
     * Deletes all reviews associated with a specific media content from the database.
     *
     * @param mediaId The ID of the media content for which reviews should be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    public void deleteByMedia(String mediaId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = or(
                    eq("anime.id", new ObjectId(mediaId)),
                    eq("manga.id", new ObjectId(mediaId))
            );

            reviewCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    /**
     * Retrieves all reviews submitted by a specific user from the database.
     *
     * @param userId The ID of the user whose reviews should be retrieved.
     * @return A list of ReviewDTO objects representing the reviews submitted by the user.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public List<ReviewDTO> findByUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userId));
            Bson projection = exclude("user");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(filter).projection(projection).forEach(document -> {
                ReviewDTO review = documentToReviewDTO(document);
                result.add(review);
            });
            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user", e);
        }
    }

    /**
     * Retrieves all reviews associated with a specific media content from the database.
     *
     * @param mediaId The ID of the media content for which reviews should be retrieved.
     * @return A list of ReviewDTO objects representing the reviews associated with the media content.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public List<ReviewDTO> findByMedia(String mediaId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = or(
                    eq("anime.id", new ObjectId(mediaId)),
                    eq("manga.id", new ObjectId(mediaId))
            );
            Bson projection = exclude("anime", "manga");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(filter).projection(projection).forEach(document -> {
                ReviewDTO review = documentToReviewDTO(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by media", e);
        }
    }

    //MongoDB queries
    //Find the average rating a user has given to media contents given the userId
    /**
     * Calculates the average rating given by a specific user to all media contents.
     *
     * @param userId The ID of the user whose average rating is to be calculated.
     * @return The average rating given by the user.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int averageRatingUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection("review");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { 'user.id': '" + new ObjectId(userId) + "' }}")); // Match reviews by user ID
            pipeline.add(Document.parse("{$group: { _id: '$user.id', averageRating: { $avg: '$rating' }}}")); // Group by user ID and calculate average rating
            // Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Retrieve the average rating from the aggregation result
            if (!result.isEmpty()) {
                Document aggregationResult = result.getFirst();
                Double averageRating = aggregationResult.getDouble("averageRating");
                if (averageRating != null) {
                    return averageRating.intValue(); // Convert average rating to int
                }
            }
            return -1; // Return -1 if no reviews are found

        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user", e);
        }
    }

    //Trend of the rating of a specific anime grouped by year
    /**
     * Calculates the average rating of a specific anime for a given year.
     *
     * @param year    The year for which the average rating is to be calculated.
     * @param animeId The ID of the anime for which the average rating is to be calculated.
     * @return The average rating of the anime for the specified year.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int ratingAnimeYear(int year, String animeId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"anime.id\": ObjectId(\"" +  new ObjectId(animeId) + "\"), \"date\": { $gte: ISODate(\"" + year + "-01-01\"), $lt: ISODate(\"" + (year + 1) + "-01-01\")}}}}"));
            pipeline.add(Document.parse("{$project: { year: { $year: \"$date\" }, rating: 1 }}"));
            pipeline.add(Document.parse("{$group: { _id: \"$year\", averageRating: { $avg: \"$rating\" }}}"));// Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Convert the aggregation result to a list of AnimeDTO
            for (Document document : result) {
                double averageRating = document.getDouble("averageRating");
                // Convert average rating to integer (rounded)
                return (int) Math.round(averageRating);
            }

            // Return -1 if no aggregation result is found
            return -1;


        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }
    }

    //Trend of the rating of a specific anime grouped by month
    /**
     * Calculates the average rating of a specific anime for a given month and year.
     *
     * @param month   The month for which the average rating is to be calculated.
     * @param year    The year for which the average rating is to be calculated.
     * @param animeId The ID of the anime for which the average rating is to be calculated.
     * @return The average rating of the anime for the specified month and year.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int ratingAnimeMonth(int month, int year, String animeId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"anime.id\": ObjectId(\"" +  new ObjectId(animeId) + "\"), \" +\n" +
                    "        \"date\": { \"$gte: ISODate(\"\"" + year + "\"-\"" + month + "\"-01\"),  \"$lt: ISODate(\"" + year + "\"-\"" + (month + 1) + "\"-01\")\"} }"));
            pipeline.add(Document.parse("{$project: { month: { $month: \"$date\" }, rating: 1 }}"));
            pipeline.add(Document.parse("{$group: { _id: \"$month\", averageRating: { $avg: \"$rating\" }}}"));// Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Convert the aggregation result to a list of AnimeDTO
            for (Document document : result) {
                double averageRating = document.getDouble("averageRating");
                // Convert average rating to integer (rounded)
                return (int) Math.round(averageRating);
            }

            // Return -1 if no aggregation result is found
            return -1;


        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }
    }

    //Trend of the rating of a specific manga grouped by year

    /**
     * Calculates the average rating of a specific manga for a given year.
     *
     * @param year    The year for which the average rating is to be calculated.
     * @param mangaId The ID of the manga for which the average rating is to be calculated.
     * @return The average rating of the manga for the specified year.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int ratingMangaYear(int year, String mangaId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"anime.id\": ObjectId(\"" + new ObjectId(mangaId) + "\"), \"date\": { $gte: ISODate(\"" + year + "-01-01\"), $lt: ISODate(\"" + (year + 1) + "-01-01\")}}}}"));
            pipeline.add(Document.parse("{$project: { year: { $year: \"$date\" }, rating: 1 }}"));
            pipeline.add(Document.parse("{$group: { _id: \"$year\", averageRating: { $avg: \"$rating\" }}}"));// Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Convert the aggregation result to a list of AnimeDTO
            for (Document document : result) {
                double averageRating = document.getDouble("averageRating");
                // Convert average rating to integer (rounded)
                return (int) Math.round(averageRating);
            }

            // Return -1 if no aggregation result is found
            return -1;


        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }
    }



    //Trend of the rating of a specific manga grouped by month
    /**
     * Calculates the average rating of a specific manga for a given month and year.
     *
     * @param month   The month for which the average rating is to be calculated.
     * @param year    The year for which the average rating is to be calculated.
     * @param mangaId The ID of the manga for which the average rating is to be calculated.
     * @return The average rating of the manga for the specified month and year.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int ratingMangaMonth(int month, int year, String mangaId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"manga.id\": ObjectId(" + new ObjectId(mangaId) + "), " +
                    "        \"date\": { \"$gte: ISODate(\"" + month + "\"" + month + "\"-01\"),  \"$lt: ISODate(\"" + year + "\"-\"" + (month + 1) + "\"-01\")\"} }"));
            pipeline.add(Document.parse("{$project: { month: { $month: \"$date\" }, rating: 1 }}"));
            pipeline.add(Document.parse("{$group: { _id: \"$month\", averageRating: { $avg: \"$rating\" }}}"));
            // Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Convert the aggregation result to a list of AnimeDTO
            for (Document document : result) {
                double averageRating = document.getDouble("averageRating");
                // Convert average rating to integer (rounded)
                return (int) Math.round(averageRating);
            }

            // Return -1 if no aggregation result is found
            return -1;


        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }
    }

    //Average rating given by users of a certain age: select a year of birth and see what is the average rating in general
    /**
     * Calculates the average rating given by users born in a specific year.
     *
     * @param yearOfBirth The year of birth for which the average rating is to be calculated.
     * @return The average rating given by users born in the specified year.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int averageRatingByAge(int yearOfBirth) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: {\"user.birthday\": {$gte: ISODate(\"" + yearOfBirth + "-01-01T00:00:00.000Z\"),\n" +
                    "        $lt: ISODate(\"" + (yearOfBirth + 1) + "-01-01T00:00:00.000Z\")      }    }  }\n"));
            pipeline.add(Document.parse("  {    $group: {      _id: null,      totalRating: { $sum: \"$rating\" },      count: { $sum: 1 }    }  }"));
            pipeline.add(Document.parse("  {    $project: {      _id: 0,      averageRating: { $divide: [\"$totalRating\", \"$count\"] }    }  }"));
            // Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Convert the aggregation result to a list of AnimeDTO
            for (Document document : result) {
                double averageRating = document.getDouble("averageRating");
                // Convert average rating to integer (rounded)
                return (int) Math.round(averageRating);
            }

            // Return -1 if no aggregation result is found
            return -1;

        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }

    }

    //Average rating given by users of a certain location: select a location and see what is the average rating in general
    /**
     * Calculates the average rating given by users from a specific location.
     *
     * @param location The location for which the average rating is to be calculated.
     * @return The average rating given by users from the specified location.
     * @throws DAOException If an error occurs during the calculation process.
     */
    @Override
    public int averageRatingByLocation(String location) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: {\"user.location\": " + location + "    }  }"));
            pipeline.add(Document.parse("  {$group: {_id: null, totalRating: { $sum: \"$rating\" }, count: { $sum: 1 }}}"));
            pipeline.add(Document.parse("{$project: {_id: 0, averageRating: { $divide: [\"$totalRating\", \"$count\"] }}}"));
            // Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Convert the aggregation result to a list of AnimeDTO
            for (Document document : result) {
                double averageRating = document.getDouble("averageRating");
                // Convert average rating to integer (rounded)
                return (int) Math.round(averageRating);
            }

            // Return -1 if no aggregation result is found
            return -1;

        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }
    }

    /**
     * Converts a ReviewDTO object to a MongoDB document for storage in the database.
     *
     * @param review The ReviewDTO object to be converted.
     * @return A MongoDB Document representing the ReviewDTO object.
     */
    private Document reviewDTOToDocument(ReviewDTO review) {
        Document reviewDocument = new Document()
                .append("user", new Document()
                        .append("id", new ObjectId(review.getUser().getId()))
                        .append("username", review.getUser().getUsername())
                        .append("picture", review.getUser().getProfilePicUrl()))
                .append("date", ConverterUtils.localDateToDate(LocalDate.now()));
        if (review.getComment() != null) {
            reviewDocument.append("comment", review.getComment());
        }
        if (review.getRating() != null) {
            reviewDocument.append("rating", review.getRating());
        }
        if (review.getMediaContent() instanceof AnimeDTO) {
            reviewDocument.append("anime", new Document()
                    .append("id", new ObjectId(review.getMediaContent().getId()))
                    .append("title", review.getMediaContent().getTitle())
                    .append("image", review.getMediaContent().getImageUrl()));
        } else if (review.getMediaContent() instanceof MangaDTO) {
            reviewDocument.append("manga", new Document()
                    .append("id", new ObjectId(review.getMediaContent().getId()))
                    .append("title", review.getMediaContent().getTitle())
                    .append("image", review.getMediaContent().getImageUrl()));
        }

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
            mediaDTO = new AnimeDTO(mediaDoc.getObjectId("id").toString(), mediaDoc.getString("title"), mediaDoc.getString("image"));
        } else if ((mediaDoc = reviewDoc.get("manga", Document.class)) != null) {
            mediaDTO = new MangaDTO(mediaDoc.getObjectId("id").toString(), mediaDoc.getString("title"), mediaDoc.getString("image"));
        }

        Document userDoc = reviewDoc.get("user", Document.class);
        RegisteredUserDTO userDTO = (userDoc != null) ? new RegisteredUserDTO(userDoc.getObjectId("id").toString(), userDoc.getString("username"), userDoc.getString("picture")) : null;

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }
}
