package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.model.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.setOnInsert;


public class AnimeDAOImpl extends BaseMongoDBDAO implements MediaContentDAO<Anime> {

    @Override
    public ObjectId insert(Anime anime) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Bson filter = eq("title", anime.getTitle());
            Bson update = setOnInsert(animeToDocument(anime));

            UpdateResult result = animeCollection.updateOne(filter, update, new UpdateOptions().upsert(true));
            if (result.getUpsertedId() == null) {
                throw new DAOException("Anime already exists");
            } else {
                return result.getUpsertedId().asObjectId().getValue();
            }
        } catch (Exception e) {
            throw new DAOException("Error while inserting anime", e);
        }
    }

    @Override
    public void update(Anime anime) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Bson filter = Filters.eq("_id", anime.getId());
            Bson update = new Document("$set", animeToDocument(anime));

            animeCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error while updating anime", e);
        }
    }

    @Override
    public void delete(ObjectId animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Bson filter = Filters.eq("_id", animeId);

            animeCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while removing anime", e);
        }
    }

    @Override
    public Anime find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Bson filter = Filters.eq("_id", id);

            Document result = animeCollection.find(filter).first();

            return (result != null)? documentToAnime(result) : null;
        } catch (Exception e) {
            throw new DAOException("Error while searching anime", e);
        }
    }

    @Override
    public PageDTO<AnimeDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> animeCollection = mongoClient.getDatabase("mangaVerse").getCollection("anime");

            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);
            Bson projection = Projections.include("title", "picture", "average_score", "anime_season.year");

            int pageOffset = (page - 1) * Constants.PAGE_SIZE;

            List<Bson> pipeline = Arrays.asList(
                    match(filter),
                    facet(
                            List.of(
                                    new Facet(Constants.PAGINATION_FACET,
                                            List.of(
                                                    sort(sort),
                                                    skip(pageOffset),
                                                    limit(Constants.PAGE_SIZE),
                                                    project(projection)
                                            )
                                    ),
                                    new Facet(Constants.COUNT_FACET,
                                            List.of(
                                                    count("total")
                                            )
                                    )
                            )
                    )
            );

            Document result = animeCollection.aggregate(pipeline).first();

            List<AnimeDTO> animeList = Optional.ofNullable(result)
                    .map(doc -> doc.getList(Constants.PAGINATION_FACET, Document.class))
                    .orElseThrow(() -> new DAOException("Error while searching anime"))
                    .stream()
                    .map(this::documentToAnimeDTO)
                    .toList();

            int totalCount = Optional.of(result)
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class).getFirst())
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            return new PageDTO<>(animeList, totalCount);
        } catch (Exception e) {
            throw new DAOException("Error while searching anime", e);
        }
    }

    @Override
    public void updateLatestReview(ReviewDTO reviewDTO) throws DAOException {
        //do it later
        // if the review id is already inside just change the review, if it doesnt exist it measn it is new so oush the new one and pull the oldest one
        //check if checking the date or the id is better according to the performance
        // sonra  bu methodu reviewservice de insert kısmında kullan ki yeni review eklendiğinde latest review da değişmiş olsun
        //mangaya da koy aynısından
    }


    private Document animeToDocument(Anime anime) {
        Document doc = new Document();
        appendIfNotNull(doc, "title", anime.getTitle());
        appendIfNotNull(doc, "episodes", anime.getEpisodeCount());
        appendIfNotNull(doc, "status", anime.getStatus());
        appendIfNotNull(doc, "picture", anime.getImageUrl());
        appendIfNotNull(doc, "average_score", anime.getAverageRating());
        appendIfNotNull(doc, "type", anime.getType());
        appendIfNotNull(doc, "producers", anime.getProducers());
        appendIfNotNull(doc, "studios", anime.getStudios());
        appendIfNotNull(doc, "synopsis", anime.getSynopsis());
        appendIfNotNull(doc, "tags", anime.getTags());
        appendIfNotNull(doc, "relations", anime.getRelatedAnime());

        if (anime.getSeason() != null || anime.getYear() != null) {
            Document seasonDocument = new Document();
            appendIfNotNull(seasonDocument, "season", anime.getSeason());
            appendIfNotNull(seasonDocument, "year", anime.getYear());
            doc.append("anime_season", seasonDocument);
        }

        List<Document> reviewsDocuments = Optional.ofNullable(anime.getReviews())
                .orElse(Collections.emptyList())
                .stream()
                .map(review -> {
                    Document reviewDocument = new Document();
                    appendIfNotNull(reviewDocument, "id", review.getId());
                    appendIfNotNull(reviewDocument, "comment", review.getComment());
                    appendIfNotNull(reviewDocument, "date", ConverterUtils.convertLocalDateToDate(review.getDate()));
                    Document userDocument = new Document();
                    appendIfNotNull(userDocument, "id", review.getUser().getId());
                    appendIfNotNull(userDocument, "username", review.getUser().getUsername());
                    appendIfNotNull(userDocument, "picture", review.getUser().getProfilePicUrl());
                    appendIfNotNull(reviewDocument, "user", userDocument);
                    return reviewDocument;
                })
                .toList();

        appendIfNotNull(doc, "latest_reviews", reviewsDocuments);

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
        anime.setType(document.getString("type"));
        anime.setRelatedAnime(document.getList("relations", String.class));
        anime.setTags(document.getList("tags", String.class));
        anime.setProducers(document.getString("producers"));
        anime.setStudios(document.getString("studios"));
        anime.setSynopsis(document.getString("synopsis"));

        Optional.ofNullable(document.get("anime_season", Document.class))
                .ifPresent(seasonDocument -> {
                    anime.setSeason(seasonDocument.getString("season"));
                    anime.setYear(seasonDocument.getInteger("year"));
                });

        List<Review> reviewList = Optional.ofNullable(document.getList("latest_reviews", Document.class))
                .orElse(Collections.emptyList())
                .stream()
                .map(reviewDocument -> {
                    Review review = new Review();
                    User reviewer = new User();
                    Document userDocument = reviewDocument.get("user", Document.class);
                    reviewer.setId(userDocument.getObjectId("id"));
                    reviewer.setUsername(userDocument.getString("username"));
                    reviewer.setProfilePicUrl(userDocument.getString("picture"));
                    review.setUser(reviewer);
                    review.setId(reviewDocument.getObjectId("id"));
                    review.setComment(reviewDocument.getString("comment"));
                    review.setDate(ConverterUtils.convertDateToLocalDate(reviewDocument.getDate("date")));
                    return review;
                })
                .toList();
        anime.setReviews(reviewList);

        return anime;
    }

    private AnimeDTO documentToAnimeDTO(Document doc) {
        AnimeDTO anime = new AnimeDTO();
        anime.setId(doc.getObjectId("_id"));
        anime.setTitle(doc.getString("title"));
        anime.setImageUrl(doc.getString("picture"));
        anime.setAverageRating(doc.getDouble("average_score"));
        if ((doc.get("anime_season", Document.class) != null)) {
            anime.setYear(doc.get("anime_season", Document.class).getInteger("year"));
        }

        return anime;
    }
}
