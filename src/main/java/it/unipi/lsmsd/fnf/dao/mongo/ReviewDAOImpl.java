package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
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
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;

public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    private static final String COLLECTION_NAME = "reviews";

    @Override
    public void createReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            MongoCollection<Document> mediaCollection;
            Bson filter;
            if (reviewDTO.getMediaContent() instanceof AnimeDTO) {
                mediaCollection = getCollection("anime");
                // Check if the media content exists
                if (mediaCollection.find(eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()))).first() == null) {
                    throw new MongoException("Anime with id " + reviewDTO.getMediaContent().getId() + " does not exist");
                }
                // Create a filter based on anime.id/manga.id and user.id
                filter = and(
                        eq("anime.id", reviewDTO.getMediaContent().getId()),
                        eq("user.id", reviewDTO.getUser().getId())
                );
            } else if (reviewDTO.getMediaContent() instanceof MangaDTO){
                mediaCollection = getCollection("manga");
                // Check if the media content exists
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
                throw new MongoException("The user have already reviewed this media content.");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public void updateReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(reviewDTO.getId()));
            Bson updatedKeys = combine(set("date", ConverterUtils.localDateToDate(LocalDate.now())));
            if (reviewDTO.getComment() != null) {
                updatedKeys = combine(updatedKeys, set("comment", reviewDTO.getComment()));
            } else {
                updatedKeys = combine(updatedKeys, unset("comment"));
            }
            if (reviewDTO.getRating() != null) {
                updatedKeys = combine(updatedKeys, set("rating", reviewDTO.getRating()));
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

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public void deleteReviewsWithNoAuthor() throws DAOException {
        try {

            MongoCollection<Document> userCollection = getCollection("user");

            List<ObjectId> userIds = userCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();

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

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public PageDTO<ReviewDTO> getReviewByUser(String userId, int page) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userId));
            Bson projection = exclude("user");

            int offset = (page - 1) * Constants.PAGE_SIZE;
            List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                    .sort(descending("date")).skip(offset).limit(Constants.PAGE_SIZE)
                    .map(this::documentToReviewDTO).into(new ArrayList<>());
            int totalCount = (int) reviewCollection.countDocuments(filter);
            return new PageDTO<>(result, totalCount);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public PageDTO<ReviewDTO> getReviewByMedia(String mediaId, MediaContentType type, int page) throws DAOException {
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

            int offset = (page - 1) * Constants.PAGE_SIZE;
            List<ReviewDTO> result = reviewCollection.find(filter).projection(projection)
                    .sort(descending("date")).skip(offset).limit(Constants.PAGE_SIZE)
                    .map(this::documentToReviewDTO).into(new ArrayList<>());
            int totalCount = (int) reviewCollection.countDocuments(filter);
            return new PageDTO<>(result, totalCount);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    //MongoDB queries
    //Find the average rating a user has given to media contents given the userId
    @Override
    public int averageRatingUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection("reviewDTO");

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

    private Document reviewDTOToDocument(ReviewDTO reviewDTO) {
        Document reviewDocument = new Document()
                .append("user", new Document()
                        .append("id", new ObjectId(reviewDTO.getUser().getId()))
                        .append("username", reviewDTO.getUser().getUsername())
                        .append("picture", reviewDTO.getUser().getProfilePicUrl()))
                .append("date", ConverterUtils.localDateToDate(LocalDate.now()));
        if (reviewDTO.getComment() != null) {
            reviewDocument.append("comment", reviewDTO.getComment());
        }
        if (reviewDTO.getRating() != null) {
            reviewDocument.append("rating", reviewDTO.getRating());
        }
        if (reviewDTO.getMediaContent() instanceof AnimeDTO) {
            reviewDocument.append("anime", new Document()
                    .append("id", new ObjectId(reviewDTO.getMediaContent().getId()))
                    .append("title", reviewDTO.getMediaContent().getTitle())
                    .append("image", reviewDTO.getMediaContent().getImageUrl()));
        } else if (reviewDTO.getMediaContent() instanceof MangaDTO) {
            reviewDocument.append("manga", new Document()
                    .append("id", new ObjectId(reviewDTO.getMediaContent().getId()))
                    .append("title", reviewDTO.getMediaContent().getTitle())
                    .append("image", reviewDTO.getMediaContent().getImageUrl()));
        }

        return reviewDocument;
    }

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
        UserSummaryDTO userDTO = (userDoc != null) ? new UserSummaryDTO(userDoc.getObjectId("id").toString(), userDoc.getString("username"), userDoc.getString("picture")) : null;

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }
}
