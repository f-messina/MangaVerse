package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
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

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.setOnInsert;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.animeToDocument;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.documentToAnime;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.reviewDTOToNestedDocument;

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

            // Check if another anime with the same title exists in the database
            Bson filterTitle = and(eq("title", anime.getTitle()), ne("_id", new ObjectId(anime.getId())));
            if (animeCollection.countDocuments(filterTitle) > 0) {
                throw new DAOException(DAOExceptionType.DUPLICATED_KEY, "An anime with the same name already exists");
            }

            // Update the anime in the database
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

            // Convert the Document to an Anime object and return it if found
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

            // Build the MongoDB query pipeline
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

            // Extract the list of AnimeDTO objects and the total count of results from the query result
            List<AnimeDTO> animeList = Optional.ofNullable(result)
                    .map(doc -> doc.getList(Constants.PAGINATION_FACET, Document.class))
                    .orElseThrow(() -> new DAOException("Error while searching anime"))
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
            Document reviewDocument = reviewDTOToNestedDocument(reviewDTO);

            // Check if the review already exists in the latestReviews array inside the Anime document
            Document filter = new Document("latestReviews._id", reviewDTO.getId());
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

    //MongoDB queries
    //Best tags based on the average rating
    @Override
    public Map<String, Double> getBestCriteria (String criteria, boolean isArray, int page) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);
            int pageOffset = (page-1)*Constants.PAGE_SIZE;
            //criteria can be tags
            //I have to use unwind, I don't have another way to do the query
            List<Document> pipeline = new ArrayList<>();

            pipeline.add(Document.parse("{$match:{" + criteria + ": { $exists: true } } }"));
            if (isArray) {
                pipeline.add(Document.parse("{$unwind: \"$" + criteria + "\"}"));
            }

            pipeline.add(Document.parse("{$group: {_id: \"$" + criteria + "\", max_average_rating: {$max: \"$average_rating\"} } }"));
            pipeline.add(Document.parse("{$sort: {max_average_rating: -1}}"));
            pipeline.add(Document.parse("{$skip: " + pageOffset + "}"));
            //Limit to 25 results
            pipeline.add(Document.parse("{$limit: 25}"));

            List <Document> document = animeCollection.aggregate(pipeline).into(new ArrayList<>());
            Map<String, Double> bestCriteria = new LinkedHashMap<>();
            for (Document doc : document) {
                if (doc.get("max_average_rating") instanceof Integer) {
                    bestCriteria.put(doc.get("_id").toString(), ((Integer) doc.get("max_average_rating")).doubleValue());
                } else
                    bestCriteria.put(doc.get("_id").toString(), doc.getDouble("max_average_rating"));
            }

            return bestCriteria;

        } catch (Exception e) {
            throw new DAOException("Error while searching anime", e);
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

