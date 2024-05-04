package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.utils.Constants;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Facet;
import it.unipi.lsmsd.fnf.utils.DocumentUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.*;
import static it.unipi.lsmsd.fnf.utils.DocumentUtils.*;

/**
 * Implementation of the MediaContentDAO interface for handling Manga objects in MongoDB.
 */
public class MangaDAOMongoImpl extends BaseMongoDBDAO implements MediaContentDAO<Manga> {
    private static final String COLLECTION_NAME = "manga";


    /**
     * Inserts a Manga object into the MongoDB collection.
     *
     * @param manga The Manga object to insert.
     * @throws DAOException If an error occurs during insertion.
     */
    @Override
    public void saveMediaContent(Manga manga) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("title", manga.getTitle());
            Document mangaDocument = mangaToDocument(manga);
            Bson doc = setOnInsert(mangaDocument);
            UpdateOptions options = new UpdateOptions().upsert(true);

            Optional.ofNullable(mangaCollection.updateOne(filter,doc, options).getUpsertedId())
                    .map(result -> result.asObjectId().getValue().toHexString())
                    .map(id -> { manga.setId(id); return id; })
                    .orElseThrow(() -> new DuplicatedException(DuplicatedExceptionType.GENERIC, "MangaDAOMongoImpl: saveMediaContent: A manga with the same title already exists"));

        } catch (DuplicateKeyException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates a Manga object in the MongoDB collection.
     *
     * @param manga The Manga object to update.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void updateMediaContent(Manga manga) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filterTitle = and(eq("title", manga.getTitle()), ne("_id", new ObjectId(manga.getId())));
            if (mangaCollection.countDocuments(filterTitle) > 0) {
                throw new DAOException(DAOExceptionType.DUPLICATED_KEY, "MangaDAOMongoImpl: updateMediaContent: A manga with the same name already exists");
            }

            Bson filter = eq("_id", new ObjectId(manga.getId()));
            Bson update = new Document("$set", mangaToDocument(manga));

            if (mangaCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("MangaDAOMongoImpl: updateMediaContent: No manga was updated");
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
     * Deletes a Manga object from the MongoDB collection by its ID.
     *
     * @param mangaId The ID of the Manga to delete.
     * @throws DAOException If an error occurs during deletion.
     */
    @Override
    public void deleteMediaContent(String mangaId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(mangaId));

            if (mangaCollection.deleteOne(filter).getDeletedCount() == 0) {
                throw new MongoException("MangaDAOMongoImpl: deleteMediaContent: No manga was deleted");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

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
    public Manga readMediaContent(String mangaId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(mangaId));

            Document result = mangaCollection.find(filter).first();

            if (result == null) {
                throw new MongoException("MangaDAOMongoImpl: readMediaContent: No manga found");
            }
            return documentToManga(result);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

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
                    .orElseThrow(() -> new MongoException("MangaDAOMongoImpl: search: No results found"))
                    .stream()
                    .map(DocumentUtils::documentToMangaDTO)
                    .toList();

            int totalCount = Optional.of(result)
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            return new PageDTO<>(mangaList, totalCount);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }


    /**
     * Updates the latest review for a Manga object.
     *
     * @param reviewDTO The ReviewDTO object containing the review information.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void upsertReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            // Convert ReviewDTO to Document
            Document reviewDocument = reviewDTOToNestedDocument(reviewDTO);

            Bson filter = eq("_id", new ObjectId(reviewDTO.getMediaContent().getId()));

            // Create the combined update operations
            Bson update = combine(
                    pull("latestReviews", new Document("_id", new ObjectId(reviewDTO.getId()))),
                    push("latestReviews", new Document("$each", List.of(reviewDocument)).append("$position", 0)),
                    push("latestReviews", new Document("$each", List.of()).append("$slice", -5))
            );

            // Apply the combined update operations
            if (mangaCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("MangaDAOMongoImpl: upsertReview: No review redundancy was updated or inserted");
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
     * @param mangaId       The ObjectId of the Manga object to update.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void refreshLatestReviews(List<ReviewDTO> latestReviews, String mangaId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            // Convert ReviewDTOs to Documents
            List<Document> reviewDocuments = latestReviews.stream()
                    .map(DocumentUtils::reviewDTOToNestedDocument)
                    .limit(Constants.LATEST_REVIEWS_SIZE)
                    .toList();

            // Update the latest reviews in the database
            Bson filter = eq("_id", new ObjectId(mangaId));
            Bson update = Updates.set("latest_reviews", reviewDocuments);

            UpdateResult result = mangaCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("MangaDAOMongoDBImpl : refreshLatestReviews: Manga not found");
            }
            if (result.getModifiedCount() == 0) {
                throw new MongoException("MangaDAOMongoDBImpl : upsertReview: the reviewArray was not updated");
            }
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public boolean isInLatestReviews(String mangaId, String reviewId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("_id", new ObjectId(mangaId)), eq("latestReviews._id", new ObjectId(reviewId)));

            return mangaCollection.countDocuments(filter) > 0;

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    //MongoDB queries
    //Best genres/themes/demographics/authors based on the average rating
    @Override
    public Map<String, Double> getBestCriteria (String criteria, boolean isArray, int page) throws DAOException {
        try  {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);
            int pageOffset = (page-1)*Constants.PAGE_SIZE;

            //criteria can be genres, themes, demographics, authors
            //I have to use unwind, I don't have another way to do the query

            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$match:{" + criteria + ": { $exists: true } } }"));
            if (isArray) {
                pipeline.add(Document.parse("{$unwind: \"$" + criteria + "\"}"));
            }

            if (criteria.equals("authors")) {
                pipeline.add(Document.parse("{$group: {_id: \"$" + criteria + ".name\", max_average_rating: {$max: \"$average_rating\"} } }"));

            } else {
                pipeline.add(Document.parse("{$group: {_id: \"$" + criteria + "\", max_average_rating: {$max: \"$average_rating\"} } }"));


            }
            pipeline.add(Document.parse("{$sort: {max_average_rating: -1}}"));
            pipeline.add(Document.parse("{$skip: " + pageOffset + "}"));

            //Limit to 25 results
            pipeline.add(Document.parse("{$limit: 25}"));


            List <Document> document = mangaCollection.aggregate(pipeline).into(new ArrayList<>());
            System.out.println("document: " + document);
            Map<String, Double> bestCriteria = new LinkedHashMap<>();
            for (Document doc : document) {
                if (doc.get("max_average_rating") instanceof Integer) {
                    bestCriteria.put(doc.get("_id").toString(), ((Integer) doc.get("max_average_rating")).doubleValue());
                } else
                    bestCriteria.put(doc.get("_id").toString(), doc.getDouble("max_average_rating"));
            }

            return bestCriteria;

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    // Neo4J specific methods
    @Override
    public void createNode(MediaContentDTO mangaDTO) throws DAOException {
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
    public List<? extends MediaContentDTO> getMediaContentTrendByLikes() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }

    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }

}

