package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    @Override
    public void insert(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            reviewCollection.insertOne(reviewDTOToDocument(review));
        } catch (Exception e) {
            throw new DAOException("Error while inserting review", e);
        }
    }

    @Override
    public void update(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = eq("_id", review.getId());
            Bson updatedKeys = combine(set("date", ConverterUtils.convertLocalDateToDate(LocalDate.now())));
            if (review.getComment() != null) {
                updatedKeys = combine(updatedKeys, set("comment", review.getComment()));
            }
            if (review.getRating() != null) {
                updatedKeys = combine(updatedKeys, set("rating", review.getRating()));
            }

            reviewCollection.updateOne(filter, updatedKeys);
        } catch (Exception e) {
            throw new DAOException("Error while updating review", e);
        }
    }

    @Override
    public void delete(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = eq("_id", id);

            reviewCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    public void deleteByMedia(ObjectId mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = or(
                    eq("anime.id", mediaId),
                    eq("manga.id", mediaId)
            );

            reviewCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = eq("user.id", userId);
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

    @Override
    public List<ReviewDTO> findByMedia(ObjectId mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = or(
                    eq("anime.id", mediaId),
                    eq("manga.id", mediaId)
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

    @Override
    public List<ReviewDTO> findByUserAndMedia(ObjectId userId, ObjectId mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = and(
                    eq("user.id", userId),
                    or(
                            eq("anime.id", mediaId),
                            eq("manga.id", mediaId)
                    )
            );
            Bson projection = exclude("user", "anime", "manga");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(filter).projection(projection).forEach(document -> {
                ReviewDTO review = documentToReviewDTO(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user and media", e);
        }
    }

    private Document reviewDTOToDocument(ReviewDTO review) {
        Document reviewDocument = new Document()
                .append("user", new Document()
                        .append("id", review.getUser().getId())
                        .append("username", review.getUser().getUsername())
                        .append("picture", review.getUser().getProfilePicUrl()))
                .append("date", ConverterUtils.convertLocalDateToDate(LocalDate.now()));
        if (review.getComment() != null) {
            reviewDocument.append("comment", review.getComment());
        }
        if (review.getRating() != null) {
            reviewDocument.append("rating", review.getRating());
        }
        if (review.getMediaContent() instanceof AnimeDTO) {
            reviewDocument.append("anime", new Document()
                    .append("id", review.getMediaContent().getId())
                    .append("title", review.getMediaContent().getTitle())
                    .append("image", review.getMediaContent().getImageUrl()));
        } else if (review.getMediaContent() instanceof MangaDTO) {
            reviewDocument.append("manga", new Document()
                    .append("id", review.getMediaContent().getId())
                    .append("title", review.getMediaContent().getTitle())
                    .append("image", review.getMediaContent().getImageUrl()));
        }

        return reviewDocument;
    }

    private ReviewDTO documentToReviewDTO(Document reviewDoc) {
        ObjectId reviewId = reviewDoc.getObjectId("_id");
        LocalDate date = ConverterUtils.convertDateToLocalDate(reviewDoc.getDate("date"));
        String comment = reviewDoc.getString("comment");
        Integer rating = reviewDoc.getInteger("rating");

        MediaContentDTO mediaDTO = null;
        Document mediaDoc;
        if ((mediaDoc = reviewDoc.get("anime", Document.class)) != null) {
            mediaDTO = new AnimeDTO(mediaDoc.getObjectId("id"), mediaDoc.getString("title"), mediaDoc.getString("image"));
        } else if ((mediaDoc = reviewDoc.get("manga", Document.class)) != null) {
            mediaDTO = new MangaDTO(mediaDoc.getObjectId("id"), mediaDoc.getString("title"), mediaDoc.getString("image"));
        }

        Document userDoc = reviewDoc.get("user", Document.class);
        RegisteredUserDTO userDTO = (userDoc != null) ? new RegisteredUserDTO(userDoc.getObjectId("id"), userDoc.getString("username"), userDoc.getString("picture")) : null;

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }

    //MongoDB queries
    //Find the average rating a user has given to media contents given the userId
    public int averageRatingUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { 'user.id': '" + userId + "' }}")); // Match reviews by user ID
            pipeline.add(Document.parse("{$group: { _id: '$user.id', averageRating: { $avg: '$rating' }}}")); // Group by user ID and calculate average rating
            // Execute the aggregation
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            // Retrieve the average rating from the aggregation result
            if (!result.isEmpty()) {
                Document aggregationResult = result.get(0);
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
    public int ratingAnimeYear(int year, ObjectId animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"anime.id\": ObjectId(\"" + animeId + "\"), \"date\": { $gte: ISODate(\"" + year + "-01-01\"), $lt: ISODate(\"" + (year + 1) + "-01-01\")}}}}"));
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

    //Trend of the rating of the score of a specific anime grouped by month
    public int ratingAnimeMonth(int month, int year, ObjectId animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"anime.id\": ObjectId(\"" + animeId + "\"), \" +\n" +
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

    //Trend of the rating of the score of a specific manga grouped by year

    public int ratingMangaYear(int year, ObjectId mangaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"anime.id\": ObjectId(\"" + mangaId + "\"), \"date\": { $gte: ISODate(\"" + year + "-01-01\"), $lt: ISODate(\"" + (year + 1) + "-01-01\")}}}}"));
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



    //Trend of the rating of the score of a specific manga grouped by month
    public int ratingMangaMonth(int month, int year, ObjectId mangaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"manga.id\": ObjectId(" + mangaId + "), " +
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

}
