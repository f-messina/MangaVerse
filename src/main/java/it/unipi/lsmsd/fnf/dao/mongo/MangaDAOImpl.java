package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.MangaDemographics;
import it.unipi.lsmsd.fnf.model.enums.MangaType;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Facet;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.setOnInsert;

/**
 * Implementation of the MediaContentDAO interface for handling Manga objects in MongoDB.
 */
public class MangaDAOImpl extends BaseMongoDBDAO implements MediaContentDAO<Manga> {
    private static final String COLLECTION_NAME = "manga";


    /**
     * Inserts a Manga object into the MongoDB collection.
     *
     * @param manga The Manga object to insert.
     * @return The ID of the inserted Manga.
     * @throws DAOException If an error occurs during insertion.
     */
    @Override
    public String insert(Manga manga) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("title", manga.getTitle());
            Bson update = setOnInsert(mangaToDocument(manga));

            UpdateResult result = mangaCollection.updateOne(filter, update, new UpdateOptions().upsert(true));
            if (result.getUpsertedId() == null) {
                throw new DAOException("Manga already exists");
            } else {
                return result.getUpsertedId().asObjectId().getValue().toString();
            }
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.EXIST_MANGA,"Error while inserting manga");
        }
    }

    /**
     * Updates a Manga object in the MongoDB collection.
     *
     * @param manga The Manga object to update.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void update(Manga manga) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(manga.getId()));
            Bson update = new Document("$set", mangaToDocument(manga));

            mangaCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error while updating manga", e);
        }
    }

    /**
     * Finds a Manga object in the MongoDB collection by its ID.
     *
     * @param mangaId The ID of the Manga to find.
     * @return The Manga object if found, otherwise null.
     * @throws DAOException If an error occurs during search.
     */
    @Override
    public Manga find(String mangaId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(mangaId));

            Document result = mangaCollection.find(filter).first();

            return (result != null) ? documentToManga(result) : null;
        } catch (Exception e) {
            throw new DAOException("Error while searching manga", e);
        }
    }

    /**
     * Searches for Manga objects in the MongoDB collection based on provided filters, order, and pagination.
     *
     * @param filters  The list of filters to apply.
     * @param orderBy  The map containing sorting criteria.
     * @param page     The page number for pagination.
     * @return A PageDTO containing the results and total count.
     * @throws DAOException If an error occurs during search.
     */
    public PageDTO<MangaDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);
            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);
            Bson projection = include("title", "picture", "average_rating", "start_date", "end_date");
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
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            return new PageDTO<>(mangaList, totalCount);
        } catch (Exception e) {
            throw new DAOException("Error while searching manga", e);
        }
    }


    /**
     * Updates the latest review for a Manga object.
     *
     * @param reviewDTO The ReviewDTO object containing the review information.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void updateLatestReview(ReviewDTO reviewDTO) throws DAOException {
    }

    /**
     * Deletes a Manga object from the MongoDB collection by its ID.
     *
     * @param mangaId The ID of the Manga to delete.
     * @throws DAOException If an error occurs during deletion.
     */
    @Override
    public void delete(String mangaId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(mangaId));

            mangaCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while removing manga", e);
        }
    }

    /**
     * Converts a Manga object to a MongoDB Document.
     *
     * @param manga The Manga object to be converted.
     * @return The MongoDB Document representation of the Manga object.
     */
    private Document mangaToDocument(Manga manga) {
        Document doc = new Document();

        appendIfNotNull(doc, "title", manga.getTitle());
        appendIfNotNull(doc, "status", manga.getStatus());
        appendIfNotNull(doc, "type", manga.getType().name());
        appendIfNotNull(doc, "picture", manga.getImageUrl());
        appendIfNotNull(doc, "genres", manga.getGenres());
        appendIfNotNull(doc, "start_date", ConverterUtils.localDateToDate(manga.getStartDate()));
        appendIfNotNull(doc, "end_date", ConverterUtils.localDateToDate(manga.getEndDate()));
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
                                    .append("date", ConverterUtils.localDateToDate(review.getDate())))
                            .toList();
                    appendIfNotNull(doc, "recent_reviews", reviewsDocuments);
                });

        return doc;
    }

    /**
     * Converts a MongoDB Document to a Manga object.
     *
     * @param document The MongoDB Document to be converted.
     * @return The Manga object representation of the MongoDB Document.
     */
    public static Manga documentToManga(Document document) {
        Manga manga = new Manga();
        manga.setId(document.getObjectId("_id").toString());
        manga.setTitle(document.getString("title"));
        manga.setType(MangaType.fromString(document.getString("type")));
        manga.setStatus(Status.valueOf(document.getString("status")));
        manga.setThemes(document.getList("themes", String.class));
        manga.setGenres(document.getList("genres", String.class));
        manga.setImageUrl(document.getString("picture"));
        manga.setDemographics(Optional.ofNullable(document.getList("demographics", String.class)).stream()
                .flatMap(List::stream)
                .map(MangaDemographics::fromString)
                .collect(Collectors.toList()));
        manga.setSerializations(document.getString("serializations"));
        manga.setBackground(document.getString("background"));
        manga.setTitleEnglish(document.getString("title_english"));
        manga.setTitleJapanese(document.getString("title_japanese"));
        manga.setStartDate(ConverterUtils.dateToLocalDate(document.getDate("start_date")));
        manga.setEndDate(ConverterUtils.dateToLocalDate(document.getDate("end_date")));
        manga.setVolumes(document.getInteger("volumes"));
        manga.setChapters(document.getInteger("chapters"));
        Object averageRatingObj = document.get("average_rating");
        manga.setAverageRating(
                (averageRatingObj instanceof Integer) ? ((Integer) averageRatingObj).doubleValue() :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : 0.0
        );

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
                                reviewer.setId(userDocument.getObjectId("id").toString());
                                reviewer.setUsername(userDocument.getString("username"));
                                reviewer.setProfilePicUrl(userDocument.getString("picture"));
                                review.setUser(reviewer);
                                review.setId(reviewDocument.getObjectId("id").toString());
                                review.setComment(reviewDocument.getString("comment"));
                                review.setDate(ConverterUtils.dateToLocalDate(reviewDocument.getDate("date")));
                                return review;
                            })
                            .toList();
                    manga.setReviews(reviewList);
                });

        return manga;
    }

    /**
     * Converts a MongoDB Document to a MangaDTO object.
     *
     * @param doc The MongoDB Document to be converted.
     * @return The MangaDTO object representation of the MongoDB Document.
     */
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
    public void createNode(MediaContentDTO mangaDTO) throws DAOException {
    }
    @Override
    public void like(String userId, String mediaContentId) throws DAOException {
    }
    @Override
    public void unlike(String userId, String mediaContentId) throws DAOException {
    }
    @Override
    public boolean isLiked(String userId, String mediaId) throws DAOException {
        return false;
    }
    @Override
    public List<? extends MediaContentDTO> getLiked(String userId) throws DAOException {
        return null;
    }
    @Override
    public List<? extends MediaContentDTO> getSuggested(String userId) throws DAOException {
        return null;
    }
    @Override
    public List<? extends MediaContentDTO> getTrendMediaContentByYear(int year) throws DAOException {
        return null;
    }
    @Override
    public List<String> getMediaContentGenresTrendByYear(int year) throws DAOException {
        return null;
    }
    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByGenre() throws DAOException {
        return null;
    }
    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByLikes() throws DAOException {
        return null;
    }
    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        return null;
    }
}

