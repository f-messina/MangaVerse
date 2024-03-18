package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCollection;

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

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.neo4j.driver.Record;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Updates.*;

public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    private static final String COLLECTION_NAME = "reviews";

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


            pipeline.add(Document.parse("{$match: { \"" + nodeType + ".id\": ObjectId(\"" + mediaContentId + "\"), rating: {$exists: true}, date: {$gte: ISODate(\" " + startYear + " -01-01T00:00:00.000Z\"), $lte: ISODate(\"" + endYear + "-12-31T23:59:59.999Z\")}}}"));
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


    //Show the average rating given by users by location or birthday year
    //Maybe we don't need this

    /*@Override
    public Map<String, Double> averageRatingByCriteria(String type) throws DAOException {
        try {
            MongoCollection<Document> reviewCollection = getCollection(COLLECTION_NAME);
            //Type can be birthday or location

            List<Document> pipeline = new ArrayList<>();

            pipeline.add(Document.parse("{$match: { \"user." + type + "\": {$exists: true, $ne: \"Unknown\"},\n" +
                    "  \"rating\": {$exists: true, $ne: null}}  }"));
            if (type.equals("location")) {
                pipeline.add(Document.parse("{$group: {_id: \"$user." + type + "\", average_rating: {$avg: \"$rating\"}}}"));
            } else if (type.equals("birthday")) {
                pipeline.add(Document.parse("{$group: { _id: { $substr: [\"$user." + type + "\", 0, 4] },  average_rating: { $avg: \"$rating\" } }}"));

            }
            pipeline.add(Document.parse("{$project: {_id: 0, \"criteria\": \"$_id\", average_rating: 1}}"));
            pipeline.add(Document.parse("{$sort: { \"average_rating\": -1}}"));
            pipeline.add(Document.parse("{$limit: 25}"));


            List<Document> result = reviewCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<String, Double> resultMap = new LinkedHashMap<>();

            // Convert the aggregation result to a map of String and Double
            for (Document document : result) {
                String criteria = (document.getString("criteria"));
                Double averageRating;
                Object ratingObj = document.get("average_rating");
                if (ratingObj instanceof Integer) {
                    averageRating = ((Integer) ratingObj).doubleValue();
                } else {
                    averageRating = (Double) ratingObj;
                }
                resultMap.put(criteria, averageRating);
            }


            return resultMap;


        } catch (Exception e) {
            throw new DAOException("Error while finding ratings by users", e);
        }

    } */

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
