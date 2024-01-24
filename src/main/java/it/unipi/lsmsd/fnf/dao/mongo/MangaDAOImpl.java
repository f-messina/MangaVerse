package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Facet;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.setOnInsert;

public class MangaDAOImpl extends BaseMongoDBDAO implements MediaContentDAO<Manga> {

    @Override
    public ObjectId insert(Manga manga) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Bson filter = eq("title", manga.getTitle());
            Bson update = setOnInsert(mangaToDocument(manga));

            UpdateResult result = mangaCollection.updateOne(filter, update, new UpdateOptions().upsert(true));
            if (result.getUpsertedId() == null) {
                throw new DAOException("Manga already exists");
            } else {
                return result.getUpsertedId().asObjectId().getValue();
            }
        } catch (Exception e) {
            throw new DAOException("Error while inserting manga", e);
        }
    }

    @Override
    public void update(Manga manga) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Bson filter = eq("_id", manga.getId());
            Bson update = new Document("$set", mangaToDocument(manga));

            mangaCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error while updating manga", e);
        }
    }

    @Override
    public Manga find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Bson filter = eq("_id", id);

            Document result = mangaCollection.find(filter).first();

            return (result != null) ? documentToManga(result) : null;
        } catch (Exception e) {
            throw new DAOException("Error while searching manga", e);
        }
    }

    public PageDTO<MangaDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);
            Bson projection = include("title", "picture", "average_score", "start_date", "end_date");

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

            Document result = mangaCollection.aggregate(pipeline).first();

            List<MangaDTO> mangaList = Optional.ofNullable(result)
                    .map(doc -> doc.getList(Constants.PAGINATION_FACET, Document.class))
                    .orElseThrow(() -> new DAOException("Error while searching manga"))
                    .stream()
                    .map(this::documentToMangaDTO)
                    .toList();

            int totalCount = Optional.of(result)
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class).getFirst())
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            return new PageDTO<>(mangaList, totalCount);
        } catch (Exception e) {
            throw new DAOException("Error while searching manga", e);
        }
    }

    @Override
    public void updateLatestReview(ReviewDTO reviewDTO) throws DAOException {
        // do it later
    }


    @Override
    public void delete(ObjectId mangaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Bson filter = eq("_id", mangaId);

            mangaCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while removing manga", e);
        }
    }

    private Document mangaToDocument(Manga manga) {
        Document doc = new Document();

        appendIfNotNull(doc, "title", manga.getTitle());
        appendIfNotNull(doc, "status", manga.getStatus());
        appendIfNotNull(doc, "type", manga.getType());
        appendIfNotNull(doc, "picture", manga.getImageUrl());
        appendIfNotNull(doc, "genres", manga.getGenres());
        appendIfNotNull(doc, "start_date", manga.getStartDate());
        appendIfNotNull(doc, "end_date", manga.getEndDate());
        appendIfNotNull(doc, "demographics", manga.getDemographics());
        appendIfNotNull(doc, "serializations", manga.getSerializations());
        appendIfNotNull(doc, "synopsis", manga.getSynopsis());
        appendIfNotNull(doc, "themes", manga.getThemes());
        appendIfNotNull(doc, "background", manga.getBackground());
        appendIfNotNull(doc, "title_english", manga.getTitleEnglish());
        appendIfNotNull(doc, "title_japanese", manga.getTitleJapanese());
        appendIfNotNull(doc, "average_rating", manga.getAverageRating());
        appendIfNotNull(doc, "volumes", manga.getVolumes());
        appendIfNotNull(doc, "chapters", manga.getChapters());

        Optional.ofNullable(manga.getAuthors())
                .ifPresent(authors -> {
                    List<Document> authorsDocument = authors.stream()
                            .map(author -> new Document()
                                    .append("id", author.getId())
                                    .append("name", author.getName())
                                    .append("role", author.getRole()))
                            .toList();
                    appendIfNotNull(doc, "authors", authorsDocument);
                });

        Optional.ofNullable(manga.getReviews())
                .ifPresent(reviews -> {
                    List<Document> reviewsDocuments = reviews.stream()
                            .map(review -> new Document()
                                    .append("id", review.getId())
                                    .append("user", new Document()
                                            .append("id", review.getUser().getId())
                                            .append("username", review.getUser().getUsername())
                                            .append("picture", review.getUser().getProfilePicUrl()))
                                    .append("comment", review.getComment())
                                    .append("date", ConverterUtils.convertLocalDateToDate(review.getDate())))
                            .toList();
                    appendIfNotNull(doc, "recent_reviews", reviewsDocuments);
                });

        return doc;
    }

    public static Manga documentToManga(Document document) {
        Manga manga = new Manga();
        manga.setId(document.getObjectId("_id"));
        manga.setTitle(document.getString("title"));
        manga.setType(document.getString("type"));
        manga.setStatus(Status.valueOf(document.getString("status")));
        manga.setThemes(document.getList("themes", String.class));
        manga.setGenres(document.getList("genres", String.class));
        manga.setImageUrl(document.getString("picture"));
        manga.setDemographics(document.getList("demographics", String.class));
        manga.setSerializations(document.getList("serializations", String.class));
        manga.setBackground(document.getString("background"));
        manga.setTitleEnglish(document.getString("title_english"));
        manga.setTitleJapanese(document.getString("title_japanese"));
        manga.setStartDate(document.getString("start_date"));
        manga.setEndDate(document.getString("end_date"));
        manga.setVolumes(document.getInteger("volumes"));
        manga.setChapters(document.getInteger("chapters"));
        manga.setAverageRating(document.getDouble("average_rating"));

        Optional.ofNullable(document.getList("authors", Document.class))
                .ifPresent(authors -> {
                    List<MangaAuthor> authorsList = authors.stream()
                            .map(authorDocument -> {
                                MangaAuthor author = new MangaAuthor();
                                author.setId(authorDocument.getInteger("id"));
                                author.setName(authorDocument.getString("name"));
                                author.setRole(authorDocument.getString("role"));
                                return author;
                            })
                            .toList();
                    manga.setAuthors(authorsList);
                });

        Optional.ofNullable(document.getList("recent_reviews", Document.class))
                .ifPresent(reviews -> {
                    List<Review> reviewList = reviews.stream()
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
                    manga.setReviews(reviewList);
                });

        return manga;
    }

    private MangaDTO documentToMangaDTO(Document doc) {
        MangaDTO manga = new MangaDTO();
        manga.setId(doc.getObjectId("_id"));
        manga.setTitle(doc.getString("title"));
        manga.setImageUrl(doc.getString("picture"));
        manga.setAverageRating(doc.getDouble("average_score"));
        manga.setStartDate(doc.getString("start_date"));
        manga.setEndDate(doc.getString("end_date"));

        return manga;
    }
}

