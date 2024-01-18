package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.AnimeDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.model.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class AnimeDAOImpl extends BaseMongoDBDAO implements AnimeDAO {

    @Override
    public void insert(Anime anime) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            animeCollection.insertOne(animeToDocument(anime));
        } catch (Exception e) {
            throw new DAOException("Error while inserting anime", e);
        }
    }

    @Override
    public void update(Anime anime) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Document filter = new Document("_id", anime.getId());
            animeCollection.updateOne(filter, new Document("$set", animeToDocument(anime)));
        } catch (Exception e) {
            throw new DAOException("Error while updating anime", e);
        }
    }

    @Override
    public List<Anime> search(String title) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            // Filtering
            Bson filter = Filters.text(title);

            // Sorting
            Bson sort = Sorts.metaTextScore("score");
            FindIterable<Document> documents = animeCollection.find(filter).sort(sort);

            List<Anime> result = new ArrayList<>();
            documents.forEach(document -> {
                Anime anime = documentToAnime(document);
                result.add(anime);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while searching anime", e);
        }
    }

    public List<Anime> search(Map<String, Object> filters, Map<String, Integer> orderBy) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            // Filtering
            Bson filter = buildFilter(filters);
            // Sorting
            List<Bson> sortList = buildSort(orderBy);
            Bson sort = null;
            if (!sortList.isEmpty()) {
                sort = Sorts.orderBy(sortList);
            }
            FindIterable<Document> documents = animeCollection.find(filter).sort(sort);

            List<Anime> result = new ArrayList<>();
            documents.forEach(document -> {
                Anime anime = documentToAnime(document);
                result.add(anime);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while searching anime", e);
        }
    }


    @Override
    public void remove(String animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Document query = new Document("_id", new ObjectId(animeId));
            animeCollection.deleteOne(query);
        } catch (Exception e) {
            throw new DAOException("Error while removing anime", e);
        }
    }

    private Document animeToDocument(Anime anime) {
        Document doc = new Document("title", anime.getTitle())
                .append("episodes", anime.getEpisodeCount())
                .append("status", anime.getStatus())
                .append("picture", anime.getImageUrl())
                .append("average_score", anime.getAverageRating());
        if (anime.getType() != null) {
            doc.append("type", anime.getType());
        }
        if (anime.getSeason() != null) {
            doc.append("anime_season", new Document("season", anime.getSeason()));
        }
        if (anime.getYear() != 0) {
            doc.append("anime_season", new Document("year", anime.getYear()));
        }
        if (anime.getProducers() != null) {
            doc.append("producers", anime.getProducers());
        }
        if (anime.getStudios() != null) {
            doc.append("studios", anime.getStudios());
        }
        if (anime.getSynopsis() != null) {
            doc.append("synopsis", anime.getSynopsis());
        }
        if (anime.getTags() != null) {
            doc.append("tags", anime.getTags());
        }
        if (anime.getRelatedAnime() != null) {
            doc.append("relations", anime.getRelatedAnime());
        }
        if (anime.getReviews() != null) {
            List<Document> reviewsDocuments = new ArrayList<>();
            for (Review<Anime> review : anime.getReviews()) {
                Document reviewDocument = new Document()
                        .append("id", review.getId())
                        .append("user", new Document()
                                .append("id", review.getUser().getId())
                                .append("username", review.getUser().getUsername())
                                .append("picture", review.getUser().getprofilePicUrl()))
                        .append("comment", review.getComment())
                        .append("date", ConverterUtils.convertLocalDateToDate(review.getDate()));
                reviewsDocuments.add(reviewDocument);
            }
            doc.append("latest_reviews", reviewsDocuments);
        }
        return doc;
    }

    private Anime documentToAnime(Document document) {
        Anime anime = new Anime();
        anime.setId(document.getObjectId("_id"));
        anime.setTitle(document.getString("title"));
        anime.setEpisodeCount(document.getInteger("episodes"));
        anime.setStatus(Status.valueOf(document.getString("status")));
        anime.setImageUrl(document.getString("picture"));
        anime.setAverageRating(document.getDouble("average_score"));

        List<Document> reviewsDocuments = document.getList("latest_reviews",Document.class);
        List<Review<Anime>> reviewList = new ArrayList<>();
        if(reviewsDocuments != null) {
            for(Document reviewDocument : reviewsDocuments) {
                Review<Anime> review = new Review<>();
                User reviewer = new User();
                Document userDocument = reviewDocument.get("user", Document.class);
                reviewer.setId(userDocument.getObjectId("id"));
                reviewer.setUsername(userDocument.getString("username"));
                reviewer.setprofilePicUrl(userDocument.getString("picture"));
                review.setUser(reviewer);
                review.setId(reviewDocument.getObjectId("id"));
                review.setComment(reviewDocument.getString("comment"));
                review.setDate(ConverterUtils.convertDateToLocalDate(reviewDocument.getDate("date")));
                reviewList.add(review);
            }
        }
        anime.setReviews(reviewList);

        if (document.getString("type") != null) {
            anime.setType(document.getString("type"));
        }
        if (document.get("anime_season", Document.class) != null) {
            Document seasonDocument = document.get("anime_season", Document.class);
            if (seasonDocument.getString("season") != null) {
                anime.setSeason(seasonDocument.getString("season"));
            }
            if (seasonDocument.getInteger("year") != null) {
                anime.setYear(seasonDocument.getInteger("year"));
            }
        }
        if (document.getList("relations",String.class) != null) {
            anime.setRelatedAnime(document.getList("relations", String.class));
        }
        if (document.getList("tags",String.class) != null) {
            anime.setTags(document.getList("tags", String.class));
        }
        if (document.getString("anime_season.year") != null) {
            anime.setYear(document.getInteger("year"));
        }
        if (document.getString("producers") != null) {
            anime.setProducers(document.getString("producers"));
        }
        if (document.getString("studios") != null) {
            anime.setStudios(document.getString("studios"));
        }
        if (document.getString("synopsis") != null) {
            anime.setSynopsis(document.getString("synopsis"));
        }
        return anime;
    }
}
