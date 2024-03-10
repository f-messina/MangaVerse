package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.*;

/**
 * Implementation of the PersonalListDAO interface for MongoDB.
 * Provides methods to interact with personal lists stored in MongoDB.
 */
public class PersonalListDAOImpl extends BaseMongoDBDAO implements PersonalListDAO {
    private static final String COLLECTION_NAME = "lists";


    /**
     * Inserts a new personal list into the database.
     *
     * @param userId The ID of the user who owns the list.
     * @param name   The name of the list.
     * @return The ID of the inserted list.
     * @throws DAOException If an error occurs during the insertion process.
     */
    @Override
    public String insert(String userId, String name) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Document listDoc = new Document("name", name)
                    .append("user", userInfoToDocument(userId, null, null));

            InsertOneResult result = listsCollection.insertOne(listDoc);
            if (result.getInsertedId() == null) {
                throw new DAOException("Error inserting list");
            } else {
                return result.getInsertedId().asObjectId().getValue().toString();
            }
        } catch (Exception e) {
            throw new DAOException("Error inserting list", e);
        }
    }

    /**
     * Updates a personal list in the database.
     *
     * @param listId The ID of the list to be updated.
     * @param name   The new name of the list.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void update(String listId, String name) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(listId));
            Bson update = set("name", name);

            if (listsCollection.updateOne(filter, update).getMatchedCount() == 0) {
                throw new DAOException("Error updating list: list not found");
            }
        } catch (Exception e) {
            throw new DAOException("Error updating list", e);
        }
    }

    /**
     * Adds a media item to a personal list.
     *
     * @param listId The ID of the list to which the item will be added.
     * @param item   The MediaContentDTO object representing the item to be added.
     * @throws DAOException If an error occurs during the addition process.
     */
    @Override
    public void addToList(String listId, MediaContentDTO item) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(listId));
            Bson update = combine();
            if (item instanceof AnimeDTO) {
                update = addToSet("anime_list", animeDTOToDocument((AnimeDTO) item));
            } else if (item instanceof MangaDTO) {
                update = addToSet("manga_list", mangaDTOToDocument((MangaDTO) item));
            }

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error inserting item", e);
        }
    }

    /**
     * Removes a media item from a personal list.
     *
     * @param listId The ID of the list from which the item will be removed.
     * @param itemId The ID of the item to be removed.
     * @param type   The type of media content (Anime or Manga).
     * @throws DAOException If an error occurs during the removal process.
     */
    @Override
    public void removeFromList(String listId, String itemId, MediaContentType type) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            String listType = type.toString().toLowerCase() + "_list";
            Bson filter = eq("_id", new ObjectId(listId));
            Bson update = pull(listType, new Document("id", new ObjectId(itemId)));

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error removing item", e);
        }
    }

    /**
     * Updates information about a media item in all personal lists.
     *
     * @param item The MediaContentDTO object representing the updated item.
     * @throws DAOException If an error occurs during the update process.
     */
    @Override
    public void updateItem(MediaContentDTO item) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            String listType = (item instanceof AnimeDTO)? "anime_list" : "manga_list";
            Bson filter = elemMatch(listType, eq("id", new ObjectId(item.getId())));
            Bson update = combine();
            if (item.getTitle() != null) {
                Bson titleUpdate = set(listType + ".$[elem].title", item.getTitle());
                update = combine(update, titleUpdate);
            }
            if (item.getImageUrl() != null) {
                Bson pictureUpdate = set(listType + ".$[elem].picture", item.getImageUrl());
                update = combine(update, pictureUpdate);
            }
            UpdateOptions options = new UpdateOptions().arrayFilters(List.of(eq("elem.id", item.getId())));

            listsCollection.updateMany(filter, update, options);
        } catch (Exception e) {
            throw new DAOException("Error updating item", e);
        }
    }


    /**
     * Removes a media item from all personal lists.
     *
     * @param itemId The ID of the item to be removed.
     * @throws DAOException If an error occurs during the removal process.
     */
    @Override
    public void removeItem(String itemId) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = or(elemMatch("anime_list" , eq("id", new ObjectId(itemId))),elemMatch("manga_list", eq("id", itemId)));
            Bson update = combine(pull("anime_list", new Document("id", new ObjectId(itemId))), pull("manga_list", new Document("id", itemId)));

            listsCollection.updateMany(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error removing item", e);
        }
    }

    /**
     * Deletes a personal list from the database.
     *
     * @param listId The ID of the list to be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void delete(String listId) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(listId));

            listsCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error deleting list", e);
        }
    }

    /**
     * Deletes all personal lists associated with a user from the database.
     *
     * @param userId The ID of the user whose lists will be deleted.
     * @throws DAOException If an error occurs during the deletion process.
     */
    @Override
    public void deleteByUser(String userId) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userId));

            listsCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error deleting lists by user", e);
        }
    }

    /**
     * Retrieves all personal lists associated with a user from the database.
     *
     * @param userId      The ID of the user whose lists will be retrieved.
     * @param redusedInfo Flag indicating whether to retrieve reduced information about the lists.
     * @return A list of PersonalListDTO objects representing the user's lists.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public List<PersonalListDTO> findByUser(String userId, boolean redusedInfo) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("user.id", new ObjectId(userId));
            Bson projection = redusedInfo? include("name") : exclude("user");

            List<PersonalListDTO> personalLists = new ArrayList<>();
            listsCollection.find(filter).projection(projection).forEach(document -> personalLists.add(documentToPersonalListDTO(document)));

            return personalLists;
        } catch (Exception e) {
            throw new DAOException("Error finding lists by user id", e);
        }
    }

    /**
     * Retrieves all personal lists from the database.
     *
     * @return A list of all PersonalListDTO objects stored in the database.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public List<PersonalListDTO> findAll() throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            List<PersonalListDTO> personalLists = new ArrayList<>();
            listsCollection.find().forEach(document -> personalLists.add(documentToPersonalListDTO(document)));

            return personalLists;
        } catch (Exception e) {
            throw new DAOException("Error finding all lists", e);
        }
    }

    /**
     * Retrieves a personal list from the database by its ID.
     *
     * @param listId The ID of the list to be retrieved.
     * @return The PersonalListDTO object representing the retrieved list, or null if not found.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public PersonalListDTO find(String listId) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(listId));
            Bson projection = exclude("user");

            Document listDoc = listsCollection.find(filter).projection(projection).first();

            return (listDoc != null)? documentToPersonalListDTO(listDoc) : null;
        } catch (Exception e) {
            throw new DAOException("Error finding list by id", e);
        }
    }

    /**
     * Converts a MongoDB document representing a personal list to a PersonalListDTO object.
     *
     * @param document The MongoDB document representing the personal list.
     * @return The PersonalListDTO object.
     */
    private PersonalListDTO documentToPersonalListDTO(Document document) {
        String userId;
        Document userDoc = document.get("user", Document.class);
        if (userDoc != null && userDoc.getObjectId("id") != null) {
            userId = userDoc.getObjectId("id").toString();
        } else {
            userId = null;
        }

        List<AnimeDTO> animeList = Optional.ofNullable(document.getList("anime_list", Document.class))
                .orElse(new ArrayList<>())
                .stream()
                .map(doc -> new AnimeDTO(doc.getObjectId("id").toString(), doc.getString("title"), doc.getString("picture")))
                .toList();

        List<MangaDTO> mangaList = Optional.ofNullable(document.getList("manga_list", Document.class))
                .orElse(new ArrayList<>())
                .stream()
                .map(doc -> new MangaDTO(doc.getObjectId("id").toString(), doc.getString("title"), doc.getString("picture")))
                .toList();

        return new PersonalListDTO(
                document.getObjectId("_id").toString(),
                document.getString("name"),
                userId,
                mangaList,
                animeList
        );
    }

    /**
     * Converts an AnimeDTO object to a MongoDB document.
     *
     * @param anime The AnimeDTO object to be converted.
     * @return The MongoDB document representing the AnimeDTO.
     */
    private Document animeDTOToDocument(AnimeDTO anime) {
        return new Document("id", new ObjectId(anime.getId()))
                .append("title", anime.getTitle())
                .append("picture", anime.getImageUrl());
    }

    /**
     * Converts a MangaDTO object to a MongoDB document.
     *
     * @param manga The MangaDTO object to be converted.
     * @return The MongoDB document representing the MangaDTO.
     */
    private Document mangaDTOToDocument(MangaDTO manga) {
        return new Document("id", new ObjectId(manga.getId()))
                .append("title", manga.getTitle())
                .append("picture", manga.getImageUrl());
    }

    private Document userInfoToDocument(String userId, String location, LocalDate birthDate) {
        Document doc = new Document("id", new ObjectId(userId));
        appendIfNotNull(doc, "location", location);
        appendIfNotNull(doc, "birthday", ConverterUtils.localDateToDate(birthDate));
        return doc;
    }

    private Document PersonalListDTOTODocument(PersonalListDTO list) {
        List<Document> animeDoc = list.getAnime().stream().map(this::animeDTOToDocument).collect(Collectors.toList());
        List<Document> mangaDoc = list.getManga().stream().map(this::mangaDTOToDocument).collect(Collectors.toList());
        Document userDoc = userInfoToDocument(list.getUserId(), null, null);
        return new Document("name", list.getName())
                .append("user", userDoc)
                .append("anime_list", animeDoc)
                .append("manga_list", mangaDoc);
    }


    //MongoDB queries
    //Find tha anime most present in all of the lists
    /**
     * Retrieves a list of popular anime based on the number of occurrences in all personal lists.
     *
     * @return A list of AnimeDTO objects representing popular anime.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    @Override
    public List<AnimeDTO> popularAnime() throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);


            //N.B.: use of unwind (I have arrays). Is it worth it?
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$project: { items: { $concatArrays: ['$anime_list'] } }}"));
            pipeline.add(Document.parse("{$unwind: '$items'}"));
            pipeline.add(Document.parse("{$group: { _id: '$items.id', title: { $first: '$items.title' }, totalLists: { $sum: 1 } }}"));
            pipeline.add(Document.parse("{$sort: { totalLists: -1 }}"));
            pipeline.add(Document.parse("{$limit: 5 }"));


            // Execute aggregation and store results in a list
            List<Document> result = listsCollection.aggregate(pipeline).into(new ArrayList<>());

            // Map aggregation results to AnimeDTO objects
            List<AnimeDTO> popularAnimeList = new ArrayList<>();

            for (Document doc : result) {
                ObjectId id = doc.getObjectId("_id");
                String title = doc.getString("title");

                AnimeDTO animeDTO = new AnimeDTO(id.toString(), title, null);
                popularAnimeList.add(animeDTO);
            }

            return popularAnimeList;

        } catch (Exception e) {
            throw new DAOException("Error finding popular anime", e);
        }
    }

    /**
     * Retrieves a list of popular manga based on the number of occurrences in all personal lists.
     *
     * @return A list of MangaDTO objects representing popular manga.
     * @throws DAOException If an error occurs during the retrieval process.
     */
    //Find tha anime most present in all of the lists
    @Override
    public List<MangaDTO> popularManga () throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);


            //N.B.: use of unwind (I have arrays). Is it worth it?
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$project: { items: { $concatArrays: ['$manga_list'] } }}"));
            pipeline.add(Document.parse("{$unwind: '$items'}"));
            pipeline.add(Document.parse("{$group: { _id: '$items.id', title: { $first: '$items.title' }, totalLists: { $sum: 1 } }}"));
            pipeline.add(Document.parse("{$sort: { totalLists: -1 }}"));
            pipeline.add(Document.parse("{$limit: 5 }"));


            // Execute aggregation and store results in a list
            List<Document> result = listsCollection.aggregate(pipeline).into(new ArrayList<>());

            // Map aggregation results to AnimeDTO objects
            List<MangaDTO> popularMangaList = new ArrayList<>();

            for (Document doc : result) {
                ObjectId id = doc.getObjectId("_id");
                String title = doc.getString("title");


                MangaDTO mangaDTO = new MangaDTO(id.toString(), title, null);
                popularMangaList.add(mangaDTO);
            }

            return popularMangaList;

        } catch (Exception e) {
            throw new DAOException("Error finding popular anime", e);
        }
    }

}
