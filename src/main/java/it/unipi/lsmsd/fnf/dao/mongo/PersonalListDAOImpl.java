package it.unipi.lsmsd.fnf.dao.mongo;

<<<<<<< HEAD
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
=======
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
>>>>>>> noemi
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
<<<<<<< HEAD
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedException;
import it.unipi.lsmsd.fnf.dao.exception.DuplicatedExceptionType;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
=======
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
>>>>>>> noemi
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
=======
import java.util.*;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
>>>>>>> noemi

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

<<<<<<< HEAD
=======
        List<MangaDTO> mangaList = Optional.ofNullable(document.getList("manga_list", Document.class))
                .orElse(new ArrayList<>())
                .stream()
                .map(doc -> new MangaDTO(doc.getObjectId("id").toString(), doc.getString("title"), doc.getString("picture")))
                .toList();


        return new PersonalListDTO(
                document.getObjectId("_id").toString(),
                document.getString("name"),
                user,
                mangaList,
                animeList
        );
    }

    private Document animeDTOToDocument(AnimeDTO anime) {
        return new Document("id", new ObjectId(anime.getId()))
                .append("title", anime.getTitle())
                .append("picture", anime.getImageUrl());
    }

    private Document mangaDTOToDocument(MangaDTO manga) {
        return new Document("id", new ObjectId(manga.getId()))
                .append("title", manga.getTitle())
                .append("picture", manga.getImageUrl());
    }

    private Document userDTOToDocument(RegisteredUserDTO user) {
        Document doc = new Document("id", new ObjectId(user.getId()));
        appendIfNotNull(doc, "location", user.getLocation());
        appendIfNotNull(doc, "birthday", ConverterUtils.localDateToDate(user.getBirthday()));
        return doc;
    }

    private Document PersonalListDTOTODocument(PersonalListDTO list) {
        List<Document> animeDoc = list.getAnime().stream().map(this::animeDTOToDocument).collect(Collectors.toList());
        List<Document> mangaDoc = list.getManga().stream().map(this::mangaDTOToDocument).collect(Collectors.toList());

        return new Document("name", list.getName())
                .append("user", userDTOToDocument(list.getUser()))
                .append("anime_list", animeDoc)
                .append("manga_list", mangaDoc);
    }


    //MongoDB queries
    //Find the manga or anime which is more present in the lists
    @Override
    public Map<PageDTO<? extends MediaContentDTO>, Integer> popularMediaContentList(MediaContentType mediaContentType) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            String nodeType = mediaContentType.equals(MediaContentType.ANIME) ? "anime" : "manga";

            //N.B.: use of unwind (I have arrays). Is it worth it?
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$project: { items: { $concatArrays: ['$" + nodeType + "_list'] } }}" ));
            pipeline.add(Document.parse("{$unwind: '$items'}" ));
            pipeline.add(Document.parse("{$group: { _id: '$items.id', title: { $first: '$items.title' }, totalLists: { $sum: 1 } }}" ));
            pipeline.add(Document.parse("{$sort: { totalLists: -1 }}" ));
            pipeline.add(Document.parse("{$limit: 25 }" ));



            List<Document> result = listsCollection.aggregate(pipeline).into(new ArrayList<>());

            Map<PageDTO<? extends MediaContentDTO>, Integer> popularMediaContentMap = new HashMap<>();

            System.out.println("map ok");
            for (Document document : result) {
                String contentId = document.getString("_id");
                MediaContentDTO mediaContentDTO;
                if (nodeType.equals("anime")) {
                    mediaContentDTO = documentToAnimeDTO(Objects.requireNonNull(listsCollection.find(eq("_id", new ObjectId(contentId))).first()));
                } else { // Assume manga for other cases
                    mediaContentDTO = documentToMangaDTO(Objects.requireNonNull(listsCollection.find(eq("_id", new ObjectId(contentId))).first()));
                }
                PageDTO<MediaContentDTO> pageDTO = new PageDTO<>(Collections.singletonList(mediaContentDTO), 1);
                popularMediaContentMap.put(pageDTO, document.getInteger("totalLists"));
>>>>>>> noemi
            }
            System.out.println(popularMediaContentMap.size());

<<<<<<< HEAD
        } catch (DuplicatedException e) {
            throw new DAOException(DAOExceptionType.DUPLICATED_KEY, e.getMessage());

        } catch (MongoException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());
=======
            return popularMediaContentMap;
>>>>>>> noemi

            /*List<MediaContentDTO> mediaContentDTOs = new ArrayList<>();


            for (Document document : result) {
                String contentId = document.getString("_id");
                MediaContentDTO mediaContentDTO;
                if (nodeType.equals("anime")) {
                    mediaContentDTO = documentToAnimeDTO(Objects.requireNonNull(listsCollection.find(eq("_id", new ObjectId(contentId))).first()));
                } else { // Assume manga for other cases
                    mediaContentDTO = documentToMangaDTO(Objects.requireNonNull(listsCollection.find(eq("_id", new ObjectId(contentId))).first()));
                }
                mediaContentDTOs.add(mediaContentDTO);
            }
            System.out.println(mediaContentDTOs.size());

            return new PageDTO<>(mediaContentDTOs, mediaContentDTOs.size());*/
        } catch (Exception e) {
<<<<<<< HEAD
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

=======
            throw new DAOException("Error finding popular media content", e);
        }
    }

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
>>>>>>> noemi
        }
        return anime;
    }

<<<<<<< HEAD
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
=======
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
>>>>>>> noemi
    }
}
