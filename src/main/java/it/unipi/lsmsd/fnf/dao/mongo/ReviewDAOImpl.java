package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    @Override
    public String insert(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            InsertOneResult result = reviewCollection.insertOne(reviewDTOToDocument(review));
            return result.getInsertedId().asObjectId().getValue().toString();
        } catch (Exception e) {
            throw new DAOException("Error while inserting review", e);
        }
    }

    @Override
    public void update(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            Bson filter = eq("_id", new ObjectId(review.getId()));
            Bson updatedKeys = combine(set("date", ConverterUtils.localDateToDate(LocalDate.now())));
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
    public void delete(String reviewId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            Bson filter = eq("_id", new ObjectId(reviewId));

            reviewCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    public void deleteByMedia(String mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            Bson filter = or(
                    eq("anime.id", new ObjectId(mediaId)),
                    eq("manga.id", new ObjectId(mediaId))
            );

            reviewCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(String userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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

    @Override
    public List<ReviewDTO> findByMedia(String mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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

    //MongoDB queries
    //Find the average rating a user has given to media contents given the userId
    @Override
    public int averageRatingUser(String userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

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
    /*@Override
    public int ratingAnimeYear(int year, String animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: { \"manga.id\": ObjectId(" + new ObjectId(mangaId) + "), " +
                    "        \"date\": { \"$gte: ISODate(\"" + year + "\"" + month + "\"-01\"),  \"$lt: ISODate(\"" + year + "\"-\"" + (month + 1) + "\"-01\")\"} }"));
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
    }*/

    @Override
    public Map<String, Double> ratingMediaContentByPeriod(MediaContentType type, String mediaContentId, String period) throws  DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

            String nodeType = type.equals(MediaContentType.ANIME) ? "anime" : "manga";


            List<Document> pipeline = new ArrayList<>();

            if (period.equals("year")) {
                pipeline.add(Document.parse("{$match: { \"" + nodeType + ".id\": ObjectId(\"" + mediaContentId + "),},}"));
                pipeline.add(Document.parse("{$group: {_id: {$year: \"$date\",}, average_rating: {$avg: \"$rating\",},},},\n"));
                pipeline.add(Document.parse("{$project: {_id: 0, year: \"$_id\", average_rating: 1,},},"));
                pipeline.add(Document.parse("{$sort: {year: 1}}"));

            } else if(period.equals("month")) {
                pipeline.add(Document.parse("{$match: {\"" + nodeType + ".id\": ObjectId( \"" + mediaContentId + "),\n" +
                                                "rating: {$exists: true,},},},\n"));
                pipeline.add(Document.parse("$group:{_id: {year: {$year: \"$date\",}, month: {$month: \"$date\" } },\n" +
                                                "average_rating: {$avg: \"$rating\",},},\n"));
                pipeline.add(Document.parse("{$project: {_id: 0, period: \"$_id\", average_rating: 1,} }"));
                pipeline.add(Document.parse("{$sort: {\"period.year\": 1, \"period.month\": 1 } }"));

            } else {
                throw new DAOException("Invalid period");
            }
            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<String, Double> resultMap = new HashMap<>();

            // Convert the aggregation result to a map of String and Double
            for (Document document : result) {
                String periodKey = document.getString("period");
                double averageRating = document.getDouble("average_rating");
                resultMap.put(periodKey, averageRating);
            }

            return resultMap;

        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by user", e);
        }



    }

    //Average rating given by users of a certain age: select a year of birth and see what is the average rating in general
    /*@Override
    public int averageRatingByAge(int yearOfBirth) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");

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

    }*/

    //For users: suggestions based on age and location. For example: show the 25 anime or manga with highest average rating in Italy.
    @Override
    public Map<PageDTO<? extends MediaContentDTO>, Double> suggestTopMediaContentByAge(MediaContentType mediaContentType, String criteria, String type) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");
            String nodeType = mediaContentType.equals(MediaContentType.ANIME) ? "anime" : "manga";

            //nodeType is either anime or manga
            //type is either birthday or location
            //criteria is the value of the type


            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match: {\"user." + type + ": \"" + criteria + "}}"));
            pipeline.add(Document.parse("{$group: {_id: \"$" + nodeType + ".id\", average_rating: { $avg: \"$rating\" }}}"));
            pipeline.add(Document.parse("{$sort: {average_rating: -1 }}"));
            pipeline.add(Document.parse("{$limit: 25}"));

            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<PageDTO<? extends MediaContentDTO>, Double> resultMap = new HashMap<>();

            for (Document document : result) {
                String contentId = document.getString("_id");
                double averageRating = document.getDouble("average_rating");
                MediaContentDTO mediaContentDTO;
                if (nodeType.equals("anime")) {
                    AnimeDTO animeDTO = documentToAnimeDTO(reviewCollection.find(Filters.eq("_id", new ObjectId(contentId))).first());
                    mediaContentDTO = animeDTO;
                } else { // Assume manga for other cases
                    MangaDTO mangaDTO = documentToMangaDTO(reviewCollection.find(Filters.eq("_id", new ObjectId(contentId))).first());
                    mediaContentDTO = mangaDTO;
                }
                List<MediaContentDTO> entries = new ArrayList<>();
                entries.add(mediaContentDTO);
                PageDTO<MediaContentDTO> pageDTO = new PageDTO<>(entries, 1); // Assuming total count as 1
                resultMap.put(pageDTO, averageRating);
            }

            return resultMap;

        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by users", e);
        }

    }//Use documentToAnimeDTO from AnimeDAOImpl

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


    @Override
    public Map<String, Double> averageRatingByCriteria(String type) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("reviews");
            //Type can be birthday or location

            List<Document> pipeline = new ArrayList<>();

            pipeline.add(Document.parse("{$match: {\"user." + type + "\": {$exists: true} } }"));
            pipeline.add(Document.parse("{$group: {_id: \"$user.\"" + type + "\", average_rating: {$avg: \"$rating\"}}}"));
            pipeline.add(Document.parse("{$project: {_id: 0, \"user\"" + type + "\"\": \"$_id\", average_rating: 1}}"));
            pipeline.add(Document.parse("{$sort: { \"user." + type + "\": -1}}"));


            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<String, Double> resultMap = new HashMap<>();

            // Convert the aggregation result to a map of String and Double
            for (Document document : result) {
                String periodKey = document.getString("period");
                double averageRating = document.getDouble("average_rating");
                resultMap.put(periodKey, averageRating);
            }

            return resultMap;

        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by users", e);
        }

    }

}
