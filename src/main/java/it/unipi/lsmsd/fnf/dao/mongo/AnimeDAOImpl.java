package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.mapper.ModelToDtoMapper;
import it.unipi.lsmsd.fnf.utils.Constants;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.model.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.setOnInsert;

/**
 * Implementation of the MediaContentDAO interface for Anime objects, providing CRUD operations for Anime data in MongoDB.
 */
public class AnimeDAOImpl extends BaseMongoDBDAO implements MediaContentDAO<Anime> {
    private static final String COLLECTION_NAME = "anime";

    /**
     * Inserts an Anime object into the MongoDB database.
     *
     * @param anime The Anime object to insert.
     * @throws DAOException If an error occurs during the insertion process.
     */
    @Override
    public void createMediaContent(Anime anime) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("title", anime.getTitle());
            Document animeDocument = animeToDocument(anime);
            Bson doc = setOnInsert(animeDocument);
            UpdateOptions options = new UpdateOptions().upsert(true);

            Optional.ofNullable(animeCollection.updateOne(filter,doc, options).getUpsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { anime.setId(id); return id; })
                    .orElseThrow(() -> new DuplicatedException(DuplicatedExceptionType.DUPLICATED_KEY, "An anime with the same title already exists"));

        } catch (DuplicateKeyException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates an existing Anime object in the MongoDB database.
     *
     * @param anime The Anime object to update.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateMediaContent(Anime anime) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filterTitle = and(eq("title", anime.getTitle()), ne("_id", new ObjectId(anime.getId())));
            if (animeCollection.countDocuments(filterTitle) > 0) {
                throw new DAOException(DAOExceptionType.DUPLICATED_KEY, "An anime with the same name already exists");
            }

            Bson filter = Filters.eq("_id", new ObjectId(anime.getId()));
            Bson update = new Document("$set", animeToDocument(anime));

            if (animeCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("No anime was updated");
            }

        } catch (DuplicateKeyException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Deletes an Anime object from the MongoDB database based on its ObjectId.
     *
     * @param animeId The ObjectId of the Anime to delete.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteMediaContent(String animeId) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = Filters.eq("_id", new ObjectId(animeId));

            if (animeCollection.deleteOne(filter).getDeletedCount() == 0) {
                throw new MongoException("No anime was deleted");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Finds an Anime object in the MongoDB database based on its ObjectId.
     *
     * @param animeId The ObjectId of the Anime to find.
     * @return The found Anime object, or null if not found.
     * @throws DAOException If an error occurs during the search process.
     */
    @Override
    public Anime readMediaContent(String animeId) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = Filters.eq("_id", new ObjectId(animeId));

            Document result = animeCollection.find(filter).first();

            return (result != null)? documentToAnime(result) : null;

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Searches for Anime objects in the MongoDB database based on provided filters, ordering, and pagination.
     *
     * @param filters  The list of filters to apply to the search.
     * @param orderBy  The map defining the ordering criteria for the search.
     * @param page     The page number for pagination.
     * @return A PageDTO object containing a list of AnimeDTO objects matching the search criteria and the total count of results.
     * @throws DAOException If an error occurs during the search process.
     */
    @Override
    public PageDTO<AnimeDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);
            Bson projection = Projections.include("title", "picture", "average_rating", "anime_season");

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
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            if (totalCount > 0) {
                return new PageDTO<>(animeList, totalCount);
            } else {
                throw new MongoException("No anime found matching the search criteria");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates the latest review for an Anime in the MongoDB database.
     *
     * @param reviewDTO The ReviewDTO object representing the latest review to update.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateLatestReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            // Convert ReviewDTO to Document
            Document reviewDocument = reviewDTOtoDocument(reviewDTO);

            // Create a filter to check if the review with the given ID already exists in the array
            Document filter = new Document("latestReviews._id", reviewDTO.getId());

            // Create a projection to include only the necessary fields
            Document projection = new Document("latestReviews.$", 1);

            // Execute the query to find the matching review
            Document existingReview = animeCollection.find(filter)
                    .projection(projection)
                    .first();

            // Combine all update operations into a single update statement
            Document updateOperations = new Document();

            if (existingReview != null) {
                // Review already exists, move it to the first position
                updateOperations.append("$pull", new Document("latestReviews", new Document("_id", reviewDTO.getId())));
                updateOperations.append("$push", new Document("latestReviews", new Document("$each", List.of(reviewDocument)).append("$position", 0)));
            } else {
                // Review doesn't exist, add it to the first position
                updateOperations.append("$push", new Document("latestReviews", new Document("$each", List.of(reviewDocument)).append("$position", 0)));
            }

            // Ensure the array has at most 5 reviews
            updateOperations.append("$push", new Document("latestReviews", new Document("$each", List.of()).append("$slice", -5)));

            // Apply the combined update operations
            animeCollection.updateOne(filter, updateOperations);

        } catch (Exception e) {
            throw new DAOException("Error updating latest review", e);
        }
    }

    /**
     * Converts a ReviewDTO object into a Document for MongoDB storage.
     *
     * @param reviewDTO The ReviewDTO object to convert.
     * @return A Document representing the ReviewDTO.
     */
    private Document reviewToDocument(ReviewDTO reviewDTO) {
        Document doc = new Document();
        doc.append("id", reviewDTO.getId());
        doc.append("comment", reviewDTO.getComment());
        doc.append("date", reviewDTO.getDate());
        // Add other fields as necessary
        return doc;
    }


    /**
     * Converts an Anime object into a Document for MongoDB storage.
     *
     * @param anime The Anime object to convert.
     * @return A Document representing the Anime.
     */
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
                    ReviewDTO reviewDTO = ModelToDtoMapper.convertToDTO(review);
                    return reviewDTOtoDocument(reviewDTO);
                })
                .toList();

        appendIfNotNull(doc, "latest_reviews", reviewsDocuments);

        return doc;
    }

    /**
     * Converts a Document from MongoDB storage into an Anime object.
     *
     * @param doc The Document to convert.
     * @return An Anime object representing the Document.
     */
    private Anime documentToAnime(Document doc) {
        Anime anime = new Anime();
        anime.setId(doc.getObjectId("_id").toString());
        anime.setTitle(doc.getString("title"));
        anime.setEpisodeCount(doc.getInteger("episodes"));
        anime.setStatus(Status.valueOf(doc.getString("status")));
        anime.setImageUrl(doc.getString("picture"));
        Object averageRatingObj = doc.get("average_rating");
        anime.setAverageRating(
                (averageRatingObj instanceof Integer) ? ((Integer) averageRatingObj).doubleValue() :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : 0.0
        );
        anime.setType(AnimeType.fromString(doc.getString("type")));
        anime.setRelatedAnime(doc.getList("relations", String.class));
        anime.setTags(doc.getList("tags", String.class));
        anime.setProducers(doc.getString("producers"));
        anime.setStudios(doc.getString("studios"));
        anime.setSynopsis(doc.getString("synopsis"));

        Optional.ofNullable(doc.get("anime_season", Document.class))
                .ifPresent(seasonDocument -> {
                    anime.setSeason(seasonDocument.getString("season"));
                    anime.setYear(seasonDocument.getInteger("year"));
                });

        List<Review> reviewList = Optional.ofNullable(doc.getList("latest_reviews", Document.class))
                .orElse(Collections.emptyList())
                .stream()
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
        anime.setReviews(reviewList);

        return anime;
    }

    /**
     * Converts a Document from MongoDB storage into an AnimeDTO object.
     *
     * @param doc The Document to convert.
     * @return An AnimeDTO object representing the Document.
     */
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

    private Document reviewDTOtoDocument(ReviewDTO reviewDTO) {
        Document reviewDocument = new Document();
        appendIfNotNull(reviewDocument, "id", reviewDTO.getId());
        appendIfNotNull(reviewDocument, "comment", reviewDTO.getComment());
        appendIfNotNull(reviewDocument, "date", ConverterUtils.localDateToDate(reviewDTO.getDate()));
        Document userDocument = new Document();
        appendIfNotNull(userDocument, "id", reviewDTO.getUser().getId());
        appendIfNotNull(userDocument, "username", reviewDTO.getUser().getUsername());
        appendIfNotNull(userDocument, "picture", reviewDTO.getUser().getProfilePicUrl());
        appendIfNotNull(reviewDocument, "user", userDocument);
        return reviewDocument;
    }

    // Neo4J specific methods
    @Override
    public void createNode(MediaContentDTO animeDTO) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public void like(String userId, String mediaContentId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public void unlike(String userId, String mediaContentId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public boolean isLiked(String userId, String mediaId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<? extends MediaContentDTO> getLiked(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<? extends MediaContentDTO> getSuggested(String userId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<? extends MediaContentDTO> getTrendMediaContentByYear(int year) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<String> getMediaContentGenresTrendByYear(int year) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByGenre() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByLikes() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
}

