package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import it.unipi.lsmsd.fnf.utils.Constants;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Facet;
import it.unipi.lsmsd.fnf.utils.DocumentUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
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

            UpdateResult result = mangaCollection.updateOne(filter, doc, options);
            if (result.getMatchedCount() != 0) {
                throw new DuplicatedException(DuplicatedExceptionType.GENERIC, "MangaDAOMongoDBImpl : saveMediaContent: A manga with the same title already exists");            }

            Optional.ofNullable(result.getUpsertedId())
                    .map(id -> id.asObjectId().getValue().toHexString())
                    .map(StringId -> { manga.setId(StringId); return StringId; })
                    .orElseThrow(() -> new MongoException("MangaDAOMongoDBImpl : saveMediaContent: Error saving manga"));

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
            Bson update = new Document("$set", mangaToDocument(manga))
                    .append("$unset", mangaToUnsetMangaFieldsDocument(manga));

            UpdateResult result = mangaCollection.updateOne(filter, update);

            if (result.getMatchedCount() == 0) {
                throw new MongoException("MangaDAOMongoImpl: updateMediaContent: Manga not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("MangaDAOMongoImpl: updateMediaContent: Error updating manga");
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
                throw new MongoException("MangaDAOMongoImpl: deleteMediaContent: Manga not found");
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
                throw new MongoException("MangaDAOMongoImpl: readMediaContent: Manga not found");
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
    public PageDTO<MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, boolean reducedInfo) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);
            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);

            Bson projection;
            if (reducedInfo) {
                projection = include("title", "picture");
            } else {
                projection = include("title", "picture", "average_rating", "start_date", "end_date", "likes");
            }
            int pageOffset = (page - 1) * Constants.PAGE_SIZE;

            List<Bson> pipeline = List.of(
                    match(filter),
                    facet(
                            // Pagination facet
                            new Facet(Constants.PAGINATION_FACET,
                                    List.of(
                                            sort(sort),
                                            skip(pageOffset),
                                            limit(Constants.PAGE_SIZE),
                                            project(projection)
                                    )
                            ),
                            // Count facet
                            new Facet(Constants.COUNT_FACET,
                                    List.of(
                                            count("total")
                                    )
                            )
                    )
            );

            Document result = mangaCollection.aggregate(pipeline).first();

            List<MediaContentDTO> mangaList = new ArrayList<>();
            Optional.ofNullable(result)
                    .map(doc -> doc.getList(Constants.PAGINATION_FACET, Document.class))
                    .orElseThrow(() -> new MongoException("MangaDAOMongoImpl: search: No results found"))
                    .stream()
                    .map(DocumentUtils::documentToMangaDTO)
                    .forEach(mangaList::add);

            int totalCount = Optional.of(result)
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            return new PageDTO<>(mangaList, totalCount, null);

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

            ObjectId reviewId = new ObjectId(reviewDTO.getId());
            ObjectId mangaId = new ObjectId(reviewDTO.getMediaContent().getId());

            Bson filter = eq("_id", mangaId);

            // Fetch the current document from the collection
            Document animeDocument = mangaCollection.find(filter).first();

            if (animeDocument == null) {
                throw new MongoException("MangaDAOMongoDBImpl : upsertReview: Manga not found");
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
            if (reviewDTO.getRating() != null) {
                update = combine(update, set("avg_rating_last_update", false));
            }

            // Update the latestReviews array in the database and set the flag to recalculate the average rating
            UpdateResult result = mangaCollection.updateOne(filter, update);
            if (result.getMatchedCount() != 0 && result.getModifiedCount() == 0) {
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
     * This method is called when a review is deleted.
     *
     * @param mangaId       The ObjectId of the Manga object to update.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void refreshLatestReviews(String mangaId, List<String> reviewIds) throws DAOException {
        try {
            // Get the latest reviews for the anime
            MongoCollection<Document> reviewCollection = getCollection("reviews");

            Bson reviewFilter = in("_id", reviewIds.stream().map(ObjectId::new).toList());
            Bson reviewProjection = exclude("manga");

            List<Document> latestReviews = reviewCollection.find(reviewFilter).projection(reviewProjection)
                    .sort(descending("date")).limit(Constants.LATEST_REVIEWS_SIZE)
                    .map(DocumentUtils::documentToReviewDTO)
                    .map(DocumentUtils::reviewDTOToNestedDocument).into(new ArrayList<>());

            // Update the latest reviews in the anime document
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            // Update the latest reviews in the database
            Bson filter = eq("_id", new ObjectId(mangaId));
            Bson update;
            if (latestReviews.isEmpty()) {
                update = unset("latest_reviews");
            } else {
                update = set("latest_reviews", latestReviews);
            }

            UpdateResult result = mangaCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("MangaDAOMongoDBImpl : refreshLatestReviews: Manga not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("MangaDAOMongoDBImpl : upsertReview: the reviewArray was not updated");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    void refreshAllLatestReviews() throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Manga manga = new Manga();

            List<ObjectId> mangaIds = mangaCollection.find().map(doc -> doc.getObjectId("_id")).into(new ArrayList<>());
            List<String> reviewIds = manga.getReviewIds();

            for (ObjectId mangaId : mangaIds) {
                refreshLatestReviews(mangaId.toHexString(), reviewIds);
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Checks if a review is in the latest reviews of a Manga object.
     *
     * @param mangaId  The ObjectId of the Manga object.
     * @param reviewId The ObjectId of the Review object.
     * @return True if the review is in the latest reviews, false otherwise.
     * @throws DAOException If an error occurs during the search process.
     */
    @Override
    public boolean isInLatestReviews(String mangaId, String reviewId) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("_id", new ObjectId(mangaId)), eq("latest_reviews.id", new ObjectId(reviewId)));

            return mangaCollection.countDocuments(filter) > 0;

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Updates user-related information (username and profile picture URL) in the "latest_reviews" array
     * of all manga documents where the user has posted reviews. Ensures consistency of user information
     * across multiple reviews.
     *
     * @param userSummaryDTO An object containing the user's ID, username, and profile picture URL.
     * @throws DAOException If a database error or any other generic error occurs during the update process.
     */
    @Override
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException {
        try {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("latest_reviews.user.id", new ObjectId(userSummaryDTO.getId()));

            List<Bson> updateOperations = new ArrayList<>();
            if (userSummaryDTO.getUsername() != null) {
                updateOperations.add(set("latest_reviews.$[elem].user.username", userSummaryDTO.getUsername()));
            }
            if (userSummaryDTO.getProfilePicUrl() != null) {
                if (!userSummaryDTO.getProfilePicUrl().equals(Constants.NULL_STRING))
                    updateOperations.add(set("latest_reviews.$[elem].user.picture", userSummaryDTO.getProfilePicUrl()));
                else
                    updateOperations.add(unset("latest_reviews.$[elem].user.picture"));
            }
            UpdateOptions options = new UpdateOptions().arrayFilters(
                    List.of(Filters.eq("elem.user.id", new ObjectId(userSummaryDTO.getId())))
            );

            // Combine all update operations into a single update operation and update the user redundancy
            if (!updateOperations.isEmpty()) {
                Bson update = combine(updateOperations);
                UpdateResult result = mangaCollection.updateMany(filter, update, options);
                if (result.getMatchedCount() != 0 && result.getModifiedCount() == 0) {
                    throw new MongoException("MangaDAOMongoDBImpl : updateUserRedundancy: No user redundancy was updated");
                }
            } else {
                throw new Exception("MangaDAOMongoDBImpl : updateUserRedundancy: No updated values were provided");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the best criteria based on the average rating of the Manga objects in the MongoDB database.
     *
     * @param criteria The criteria to search for.
     * @param isArray  A boolean indicating whether the criteria are an array.
     * @param page     The page number for pagination.
     * @return A map containing the best criteria and their average rating.
     * @throws DAOException If an error occurs during the search process.
     */
    //MongoDB queries
    //Best genres/themes/demographics/authors based on the average rating
    @Override
    public Map<String, Double> getBestCriteria (String criteria, boolean isArray, int page) throws DAOException {
        try  {
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);
            int pageOffset = (page-1)*Constants.PAGE_SIZE;

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

            List <Document> document = mangaCollection.aggregate(pipeline).into(new ArrayList<>());
            Map<String, Double> bestCriteria = new LinkedHashMap<>();
            for (Document doc : document) {
                Double avgRating = doc.get("criteria_average_rating") instanceof Integer?
                        doc.getInteger("criteria_average_rating").doubleValue() :
                        doc.getDouble("criteria_average_rating");
                if (criteria.equals("authors")) {
                    bestCriteria.put(doc.get("_id", Document.class).getString("name"), avgRating);
                } else {
                    bestCriteria.put(doc.get("_id").toString(), avgRating);
                }
            }

            return bestCriteria;

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Updates the number of likes for a Manga object in the MongoDB database.
     *
     * @param mangaId The ObjectId of the Manga object.
     * @param likes   The new number of likes.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateNumOfLikes(String mangaId, Integer likes) throws DAOException {
        try{
            MongoCollection<Document> mangaCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(mangaId));
            Bson update = set("likes", likes);

            UpdateResult result = mangaCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("MangaDAOMongoDBImpl : updateNumOfLikes: Manga not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("MangaDAOMongoDBImpl : updateNumOfLikes: Error updating number of likes");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    // Neo4J specific methods
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
    public Integer getNumOfLikes(String mediaId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }

    @Override
    public PageDTO<MediaContentDTO> getLiked(String userId, int page) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
    @Override
    public List<MediaContentDTO> getSuggestedByFollowings(String userId, Integer limit) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }

    @Override
    public List<MediaContentDTO> getSuggestedByLikes(String userId, Integer limit) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }

    @Override
    public Map<MediaContentDTO, Integer> getTrendMediaContentByYear(int year, Integer limit) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }

    @Override
    public List<MediaContentDTO> getMediaContentTrendByLikes(Integer limit) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in MongoDB");
    }
}

