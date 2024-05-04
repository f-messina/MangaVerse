package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.utils.Constants;

import com.mongodb.client.model.*;
import com.mongodb.client.*;
import it.unipi.lsmsd.fnf.utils.DocumentUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.*;

/**
 * Implementation of the MediaContentDAO interface for Anime objects, providing CRUD operations for Anime data in MongoDB.
 */
public class AnimeDAOMongoImpl extends BaseMongoDBDAO implements MediaContentDAO<Anime> {
    private static final String COLLECTION_NAME = "anime";



    /**
     * Inserts an Anime object into the MongoDB database.
     *
     * @param anime The Anime object to insert.
     * @throws DAOException If an error occurs during the insertion process.
     */
    @Override
    public void saveMediaContent(Anime anime) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            // save the anime in the database only if it doesn't already exist
            Bson filter = eq("title", anime.getTitle());
            Document animeDocument = animeToDocument(anime);
            Bson doc = setOnInsert(animeDocument);
            UpdateOptions options = new UpdateOptions().upsert(true);

            Optional.ofNullable(animeCollection.updateOne(filter,doc, options).getUpsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { anime.setId(id); return id; })
                    .orElseThrow(() -> new DuplicatedException(DuplicatedExceptionType.GENERIC, "AnimeDAOMongoDBImpl : saveMediaContent: An anime with the same title already exists"));

        } catch (DuplicateKeyException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

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

            // Check if another anime with the same title exists in the database
            Bson filterTitle = and(eq("title", anime.getTitle()), ne("_id", new ObjectId(anime.getId())));
            if (animeCollection.countDocuments(filterTitle) > 0) {
                throw new DuplicatedException(DuplicatedExceptionType.GENERIC, "AnimeDAOMongoDBImpl : updateMediaContent: An anime with the same name already exists");
            }

            // Update the anime in the database
            Bson filter = Filters.eq("_id", new ObjectId(anime.getId()));
            Bson update = new Document("$set", animeToDocument(anime));

            if (animeCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : updateMediaContent: No anime was updated");
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
                throw new MongoException("AnimeDAOMongoDBImpl : deleteMediaContent: No anime was deleted");
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

            // Convert the Document to an Anime object and return it if found
            if (result == null) {
                throw new MongoException("AnimeDAOMongoDBImpl : readMediaContent: No anime found");
            }
            return documentToAnime(result);

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

            // Build the MongoDB query pipeline
            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);
            Bson projection = include("title", "picture", "average_rating", "anime_season");

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

            // Extract the list of AnimeDTO objects and the total count of results from the query result
            List<AnimeDTO> animeList = Optional.ofNullable(result)
                    .map(doc -> doc.getList(Constants.PAGINATION_FACET, Document.class))
                    .orElseThrow(() -> new MongoException("AnimeDAOMongoDBImpl : search: No results found"))
                    .stream()
                    .map(DocumentUtils::documentToAnimeDTO)
                    .toList();

            int totalCount = Optional.of(result)
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            // Return the list of AnimeDTO objects and the total count of results
            return new PageDTO<>(animeList, totalCount);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates the latest review for an Anime object.
     *
     * @param reviewDTO The ReviewDTO object containing the review information.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void upsertReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            // Convert ReviewDTO to Document
            Document reviewDocument = reviewDTOToNestedDocument(reviewDTO);

            ObjectId reviewId = new ObjectId(reviewDTO.getId());
            ObjectId animeId = new ObjectId(reviewDTO.getMediaContent().getId());

            Bson filter = eq("_id", animeId);

            // Fetch the current document from the collection
            Document animeDocument = animeCollection.find(filter).first();

            if (animeDocument == null) {
                throw new MongoException("AnimeDAOMongoDBImpl : upsertReview: Anime not found");
            }

            // Get the latestReviews array from the fetched document
            List<Document> latestReviews = animeDocument.getList("latest_reviews", Document.class);

            // Update the latestReviews array in memory
            if (latestReviews == null) {
                latestReviews = new ArrayList<>();
                latestReviews.addFirst(reviewDocument);
            } else {
                latestReviews.removeIf(review -> review.getObjectId("id").equals(reviewId));
                latestReviews.addFirst(reviewDocument);
                latestReviews = latestReviews.subList(0, Math.min(latestReviews.size(), Constants.LATEST_REVIEWS_SIZE));
            }

            Bson update = set("latest_reviews", latestReviews);
            UpdateResult result = animeCollection.updateOne(filter, update);
            // Apply the combined update operations
            if (result.getMatchedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : upsertReview: Anime not found");
            }
            if (result.getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoImpl: upsertReview: No review redundancy was updated or inserted");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Refresh the latest review for a Manga object.
     *
     * @param latestReviews The ReviewDTO list with the latest reviews.
     * @param animeId       The ObjectId of the Anime object to update.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void refreshLatestReviews(List<ReviewDTO> latestReviews, String animeId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            // Convert ReviewDTOs to Documents
            List<Document> reviewDocuments = Optional.ofNullable(latestReviews)
                    .map(reviews -> reviews.stream()
                            .map(DocumentUtils::reviewDTOToNestedDocument)
                            .limit(Constants.LATEST_REVIEWS_SIZE)
                            .toList())
                    .orElse(null);


            // Update the latest reviews in the database
            Bson filter = eq("_id", new ObjectId(animeId));
            Bson update;
            if (reviewDocuments == null) {
                update = unset("latest_reviews");
            } else {
                update = set("latest_reviews", reviewDocuments);
            }

            UpdateResult result = mangaCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : refreshLatestReviews: Anime not found");
            }
            if (result.getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : upsertReview: the reviewArray was not updated");
            }
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public boolean isInLatestReviews(String animeId, String reviewId) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("_id", new ObjectId(animeId)), eq("latest_reviews.id", new ObjectId(reviewId)));

            return animeCollection.countDocuments(filter) > 0;

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the best criteria based on the average rating of the Anime objects in the MongoDB database.
     *
     * @param criteria The criteria to search for.
     * @param isArray  A boolean indicating whether the criteria are an array.
     * @param page     The page number for pagination.
     * @return A map containing the best criteria and their average rating.
     * @throws DAOException If an error occurs during the search process.
     */
    @Override
    public Map<String, Double> getBestCriteria (String criteria, boolean isArray, int page) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);
            int pageOffset = (page - 1) * Constants.PAGE_SIZE;

            List<Bson> pipeline;
            if (isArray) {
                pipeline = List.of(
                        match(and(exists(criteria), ne("average_rating", null))),
                        unwind("$" + criteria),
                        group("$" + criteria, avg("criteria_average_rating", "$average_rating")),
                        sort(descending("criteria_average_rating")),
                        skip(pageOffset),
                        limit(25)
                );
            } else {
                pipeline = List.of(
                        match(Filters.exists(criteria)),
                        group("$" + criteria, avg("criteria_average_rating", "$average_rating")),
                        sort(new Document("criteria_average_rating", -1)),
                        skip(pageOffset),
                        limit(25)
                );
            }

            List <Document> document = animeCollection.aggregate(pipeline).into(new ArrayList<>());
            Map<String, Double> bestCriteria = new LinkedHashMap<>();
            for (Document doc : document) {
                Double avgRating = doc.get("criteria_average_rating") instanceof Integer?
                        doc.getInteger("criteria_average_rating").doubleValue() :
                        doc.getDouble("criteria_average_rating");
                bestCriteria.put(doc.get("_id").toString(), avgRating);
            }

            return bestCriteria;

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }

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
    public List<String> getMediaContentGenresTrend() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<? extends MediaContentDTO> getMediaContentTrendByLikes() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
}

