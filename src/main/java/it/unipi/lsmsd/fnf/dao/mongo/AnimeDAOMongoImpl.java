package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.client.result.UpdateResult;
import com.sun.tools.jconsole.JConsoleContext;
import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
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
     * If an Anime object with the same title already exists, a DuplicatedException is thrown.
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

            UpdateResult result = animeCollection.updateOne(filter, doc, options);
            if (result.getMatchedCount() != 0) {
                throw new DuplicatedException(DuplicatedExceptionType.GENERIC, "AnimeDAOMongoDBImpl : saveMediaContent: An anime with the same title already exists");            }

            Optional.ofNullable(result.getUpsertedId())
                    .map(id -> id.asObjectId().getValue().toHexString())
                    .map(StringId -> { anime.setId(StringId); return StringId; })
                    .orElseThrow(() -> new MongoException("AnimeDAOMongoDBImpl : saveMediaContent: Error saving anime"));

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
     * The method performs the following steps:
     * 1. Checks if another anime with the same title exists in the database (if the title has been updated).
     * 2. Updates the anime in the database.
     *
     * @param anime The Anime object to update (containing only the fields to update).
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
            Bson update = new Document("$set", animeToDocument(anime))
                    .append("$unset", animeToUnsetAnimeFieldsDocument(anime));

            UpdateResult result = animeCollection.updateOne(filter, update);

            if (result.getMatchedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : updateMediaContent: Anime not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : updateMediaContent: Error updating anime");
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
                throw new MongoException("AnimeDAOMongoDBImpl : deleteMediaContent: Anime not found");
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
            Bson projection = exclude("avg_rating_last_update");

            // Find the anime in the database and return it as an Anime object
            return Optional.ofNullable(animeCollection.find(filter).projection(projection).first())
                    .map(DocumentUtils::documentToAnime)
                    .orElseThrow(() -> new MongoException("AnimeDAOMongoDBImpl : readMediaContent: Anime not found"));

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
     *                 Each filter is a map containing the operator as key and the name of the field and the related for as values.
     *                 For equality, the filter is a map containing the field name as key and the value to match as value.
     *                 Example: Map.of("$in", Map.of("tags", List.of("comedy", "fantasy")))
     *                 Supported operators: $and, $or, $all, $in, $nin, $gte, $lte, $exists, $regex
     * @param orderBy  The map defining the ordering criteria for the search.
     * @param page     The page number for pagination.
     * @param reducedInfo A boolean indicating whether to return reduced information for the Anime objects.
     * @return A PageDTO object containing a list of AnimeDTO objects matching the search criteria and the total count of results.
     * @throws DAOException If an error occurs during the search process.
     */
    @Override
    public PageDTO<MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, boolean reducedInfo) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = buildFilter(filters);
            Bson sort = buildSort(orderBy);
            Bson projection;

            if (reducedInfo)
                projection = include("title", "picture");
            else
                projection = include("title", "picture", "average_rating", "anime_season", "likes");

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
            Document result = animeCollection.aggregate(pipeline).first();

            // Extract the list of AnimeDTO objects and the total count of results from the query result
            List<MediaContentDTO> animeList = new ArrayList<>();
            Optional.ofNullable(result)
                    .map(doc -> doc.getList(Constants.PAGINATION_FACET, Document.class))
                    .orElseThrow(() -> new MongoException("AnimeDAOMongoDBImpl : search: No results found"))
                    .stream()
                    .map(DocumentUtils::documentToAnimeDTO)
                    .forEach(animeList::add);

            int totalCount = Optional.of(result)
                    .map(doc -> doc.getList(Constants.COUNT_FACET, Document.class))
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .filter(doc -> !doc.isEmpty())
                    .map(doc -> doc.getInteger("total"))
                    .orElse(0);

            return new PageDTO<>(animeList, totalCount, null);

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Inserts or updates a review in the latest review for an Anime object.
     * The method performs the following steps:
     * 1. Takes the latest reviews array from the Anime Document.
     * 2  Checks if the review is already in the latest reviews:
     *     - If it is, it is removed and added to the beginning of the list.
     *     - If it is not, it is added to the beginning of the list.
     * 3. Updates the latest reviews array in the Anime Document.
     *
     * @param reviewDTO The ReviewDTO object containing the review information.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    public void upsertReview(ReviewDTO reviewDTO) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            ObjectId reviewId = new ObjectId(reviewDTO.getId());
            ObjectId animeId = new ObjectId(reviewDTO.getMediaContent().getId());

            Bson filter = eq("_id", animeId);
            Bson projection = include("latest_reviews");

            //Get the latest reviews from the anime document
            Document animeDocument = animeCollection.find(filter).projection(projection).first();

            if (animeDocument == null) {
                throw new MongoException("AnimeDAOMongoDBImpl : upsertReview: Anime not found");
            }

            // Update the latestReviews array in memory
            Document reviewDocument = reviewDTOToNestedDocument(reviewDTO);
            List<Document> latestReviews = animeDocument.getList("latest_reviews", Document.class);
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
            UpdateResult result = animeCollection.updateOne(filter, update);
            if (result.getMatchedCount() != 0 && result.getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoImpl: upsertReview: No review redundancy was updated or inserted");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Refresh the latest review for an Anime object.
     * The method performs the following steps:
     * 1. Get the latest reviews for the anime in Review Collection.
     * 2. Update the latest reviews in the Anime Document.
     *
     * @param animeId       The ObjectId of the Anime object to update.
     * @throws DAOException If an error occurs during update.
     */
    @Override
    //Put in input the animeId and refresh the latest reviews
    public void refreshLatestReviews(String animeId, List<String> reviewIds) throws DAOException {
        try {
            // Get the latest reviews for the anime
            MongoCollection<Document> reviewCollection = getCollection("reviews");

            Bson reviewFilter = in("_id", reviewIds.stream().map(ObjectId::new).toList());
            Bson reviewProjection = exclude("anime");

            List<Document> latestReviews = reviewCollection.find(reviewFilter).projection(reviewProjection)
                    .sort(descending("date")).limit(Constants.LATEST_REVIEWS_SIZE)
                    .map(DocumentUtils::documentToReviewDTO)
                    .map(DocumentUtils::reviewDTOToNestedDocument).into(new ArrayList<>());

            // Update the latest reviews in the database
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);
            Bson filter = eq("_id", new ObjectId(animeId));
            Bson update;
            if (latestReviews.isEmpty()) {
                update = unset("latest_reviews");
            } else {
                update = set("latest_reviews", latestReviews);
            }

            UpdateResult result = animeCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : refreshLatestReviews: Anime not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : upsertReview: the reviewArray was not updated");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    // Used by the tests to refresh the latest reviews of all anime objects
    void refreshAllLatestReviews() throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);
            Anime anime = new Anime();

            List<ObjectId> animeIds = animeCollection.find().map(doc -> doc.getObjectId("_id")).into(new ArrayList<>());
            List<String> reviewIds = anime.getReviewIds();
            for (ObjectId animeId : animeIds) {
                refreshLatestReviews(animeId.toHexString(), reviewIds);
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Checks if a review is in the latest reviews of an Anime object.
     *
     * @param animeId  The ObjectId of the Anime object.
     * @param reviewId The ObjectId of the Review object.
     * @return True if the review is in the latest reviews, false otherwise.
     * @throws DAOException If an error occurs during the search process.
     */
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
     * Updates user-related information (username and profile picture URL) in the "latest_reviews" array
     * of all anime documents where the user has posted reviews. Ensures consistency of user information
     * across multiple reviews.
     *
     * @param userSummaryDTO An object containing the user's ID, username, and profile picture URL.
     * @throws DAOException If a database error or any other generic error occurs during the update process.
     */
    @Override
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException {
        try {
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

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

            if (!updateOperations.isEmpty()) {
                Bson update = combine(updateOperations);
                UpdateResult result = animeCollection.updateMany(filter, update, options);
                if (result.getMatchedCount() != 0 && result.getModifiedCount() == 0) {
                    throw new MongoException("AnimeDAOMongoDBImpl : updateUserRedundancy: No user redundancy was updated");
                }
            } else {
                throw new Exception("AnimeDAOMongoDBImpl : updateUserRedundancy: No updated values were provided");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves the best criteria based on the average rating of the Anime objects in the MongoDB database.
     *
     * @param criteria The criteria to search for.
     *                 Criteria values:  "tags", "producers", "studios".
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

    /**
     * Updates the number of likes for an Anime object in the MongoDB database.
     *
     * @param animeId The ObjectId of the Anime object.
     * @param likes   The new number of likes.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateNumOfLikes(String animeId, Integer likes) throws DAOException {
        try{
            MongoCollection<Document> animeCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(animeId));
            Bson update = set("likes", likes);

            UpdateResult result = animeCollection.updateOne(filter, update);
            if (result.getMatchedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : updateNumOfLikes: Anime not found");
            } else if (result.getModifiedCount() == 0) {
                throw new MongoException("AnimeDAOMongoDBImpl : updateNumOfLikes: Error updating number of likes");
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

