package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.nin;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Updates.pullByFilter;

public class PersonalListDAOImpl extends BaseMongoDBDAO implements PersonalListDAO {
    private static final String COLLECTION_NAME = "users";
    /**
     * Inserts a new personal list into the database.
     *
     * @param listSummaryDTO The personal list to be inserted.
     * @throws DAOException If an error occurs during the insertion process.
     */
    @Override
    public void insertList(PersonalListSummaryDTO listSummaryDTO) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = and(eq("_id", new ObjectId(listSummaryDTO.getUserId())), ne("lists.name", listSummaryDTO.getName()));
            ObjectId listId = new ObjectId();
            Bson listDoc = new Document("id", listId).append("name", listSummaryDTO.getName());
            Bson update = push("lists", listDoc);

            if (usersCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("No list was inserted: user not found or list already exists");
            } else {
                listSummaryDTO.setListId(listId.toString());
                System.out.println("List inserted successfully");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Updates a personal list in the database.
     *
     * @param listSummaryDTO The personal list to be updated.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateList(PersonalListSummaryDTO listSummaryDTO) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            String userId = listSummaryDTO.getUserId();
            String listId = listSummaryDTO.getListId();
            String name = listSummaryDTO.getName();

            Bson filter = and(
                    eq("_id", new ObjectId(userId)),
                    nor(
                            and(
                                    eq("lists.name", name),
                                    ne("lists.id", new ObjectId(listId))
                            )
                    )
            );
            Bson update = set("lists.$[elem].name", name);
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", new ObjectId(listId))));

            UpdateResult result = usersCollection.updateOne(filter, update, options);
            if (result.getMatchedCount() == 0) {
                if (usersCollection.countDocuments(and(eq("_id", new ObjectId(userId)), eq("lists.id", new ObjectId(listId)))) == 0)
                    throw new MongoException("No list was updated: list not found");
                else
                    throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_NAME, "List with name " + name + " already exists");

            } else {
                System.out.println("List updated successfully");

            }

        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    @Override
    public void deleteList(String userId, String listId) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = pull("lists", eq("id", new ObjectId(listId)));

            if (usersCollection.updateOne(filter, update).getModifiedCount() == 0) {
                throw new MongoException("No list was deleted: list not found");

            } else {
                System.out.println("List deleted successfully");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Adds an element to a personal list in the database.
     *
     * @param userId The ID of the user who owns the list.
     * @param listId The ID of the list to add the element to.
     * @param mediaId The ID of the media to add to the list.
     * @param mediaType The type of media to add to the list.
     * @throws DAOException If an error occurs during the addition process.
     */
    @Override
    public void addToList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            String mediaTypeString = mediaType.name().toLowerCase();
            if (mediaType == MediaContentType.ANIME) {
                MongoCollection<Document> animeCollection = getCollection("anime");
                if(animeCollection.countDocuments(new Document("_id", new ObjectId(mediaId))) == 0)
                    throw new MongoException("Anime with id " + mediaId + " not found");
            } else if (mediaType == MediaContentType.MANGA) {
                MongoCollection<Document> mangaCollection = getCollection("manga");
                if(mangaCollection.countDocuments(new Document("_id", new ObjectId(mediaId))) == 0)
                    throw new MongoException("Manga with id " + mediaId + " not found");
            }
            Bson filter = and(
                    eq("_id", new ObjectId(userId)),
                    eq("lists.id", new ObjectId(listId))
            );
            Bson update = addToSet("lists.$[elem]." + mediaTypeString + "_list", new ObjectId(mediaId));
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", new ObjectId(listId))));

            UpdateResult result = usersCollection.updateOne(filter, update, options);
            if (result.getModifiedCount() == 0) {
                if (result.getMatchedCount() == 0)
                    throw new MongoException("List not found");
                else
                    throw new DuplicatedException(DuplicatedExceptionType.DUPLICATED_KEY, "Element already exists in the list");

            } else {
                System.out.println("Element added to the list successfully");

            }

        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());
        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Removes an element from a personal list in the database.
     *
     * @param userId The ID of the user who owns the list.
     * @param listId The ID of the list to remove the element from.
     * @param mediaId The ID of the media to remove from the list.
     * @param mediaType The type of media to remove from the list.
     * @throws DAOException If an error occurs during the removal process.
     */
    @Override
    public void removeFromList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException {
        try {
            MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);
            String mediaTypeString = mediaType.name().toLowerCase();
            Bson filter = eq("_id", new ObjectId(userId));
            Bson update = pull("lists.$[elem]." + mediaTypeString + "_list", new ObjectId(mediaId));
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", new ObjectId(listId))));

            if (usersCollection.updateOne(filter, update, options).getModifiedCount() == 0) {
                throw new MongoException("No element was removed from the list: list not found");

            } else {
                System.out.println("Element removed from the list successfully");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }

    /**
     * Removes all the elements in the lists that are not present in the media collections.
     *
     * @throws DAOException If an error occurs during the removal process.
     */
    @Override
    public void removeElementInListWithoutMedia() throws DAOException {
        MongoCollection<Document> animeCollection = getCollection("anime");
        MongoCollection<Document> mangaCollection = getCollection("manga");
        MongoCollection<Document> usersCollection = getCollection(COLLECTION_NAME);

        try {
            List<ObjectId> animeId = animeCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();
            List<ObjectId> mangaId = mangaCollection.find().projection(new Document("_id", 1)).into(new ArrayList<>()).stream()
                    .map(document -> document.getObjectId("_id"))
                    .toList();


            Bson filter = exists("lists", true);
            Bson mangaFilter = nin("lists.$[].manga_list", mangaId);
            Bson animeFilter = nin("lists.$[].anime_list", animeId);
            Bson update = combine(pullByFilter(mangaFilter), pullByFilter(animeFilter));
            if(usersCollection.updateMany(filter, update).getModifiedCount() == 0){
                throw new MongoException("No inconsistency found in the anime and manga lists");
            }

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());

        }
    }
}
