package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Collectors;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.*;

public class PersonalListDAOImpl extends BaseMongoDBDAO implements PersonalListDAO {
    private static final String COLLECTION_NAME = "lists";

    @Override
    public String insert(PersonalListDTO list) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Document listDoc = PersonalListDTOTODocument(list);

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

    @Override
    public void update(PersonalListDTO list) throws DAOException {
        try {
            MongoCollection<Document> listsCollection = getCollection(COLLECTION_NAME);

            Bson filter = eq("_id", new ObjectId(list.getId()));
            Bson update = combine();
            if (list.getName() != null) {
                Bson nameUpdate = set("name", list.getName());
                update = combine(update, nameUpdate);
            }
            if (list.getUser() != null) {
                Bson userUpdate = set("user", userDTOToDocument(list.getUser()));
                update = combine(update, userUpdate);
            }

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error updating list", e);
        }
    }

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

    @Override
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) throws DAOException {
        return null;
    }

    @Override
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) throws DAOException {
        return null;
    }

    @Override
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) throws DAOException {
        return null;
    }

    @Override
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) throws DAOException {
        return null;
    }

    private PersonalListDTO documentToPersonalListDTO(Document document) {
        Document userDoc = document.get("user", Document.class);
        RegisteredUserDTO user = null;
        if (userDoc != null) {
            user = new RegisteredUserDTO(
                    userDoc.getObjectId("id").toString(),
                    userDoc.getString("location"),
                    ConverterUtils.dateToLocalDate(userDoc.getDate("birthday"))
            );
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
    public PageDTO<? extends MediaContentDTO> popularMediaContentList(MediaContentType mediaContentType) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse" ).getCollection("lists" );

            String nodeType = mediaContentType.equals(MediaContentType.ANIME) ? "anime" : "manga";

            //N.B.: use of unwind (I have arrays). Is it worth it?
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(Document.parse("{$project: { items: { $concatArrays: ['" + nodeType + "_list'] } }}" ));
            pipeline.add(Document.parse("{$unwind: '$items'}" ));
            pipeline.add(Document.parse("{$group: { _id: '$items.id', title: { $first: '$items.title' }, totalLists: { $sum: 1 } }}" ));
            pipeline.add(Document.parse("{$sort: { totalLists: -1 }}" ));
            pipeline.add(Document.parse("{$limit: 25 }" ));


            List<Document> result = listsCollection.aggregate(pipeline).into(new ArrayList<>());

            List<MediaContentDTO> mediaContentDTOs = new ArrayList<>();

            for (Document document : result) {
                String contentId = document.getString("_id");
                MediaContentDTO mediaContentDTO;
                if (nodeType.equals("anime")) {
                    mediaContentDTO = documentToAnimeDTO(listsCollection.find(Filters.eq("_id", new ObjectId(contentId))).first());
                } else { // Assume manga for other cases
                    mediaContentDTO = documentToMangaDTO(listsCollection.find(Filters.eq("_id", new ObjectId(contentId))).first());
                }
                mediaContentDTOs.add(mediaContentDTO);
            }

            return new PageDTO<>(mediaContentDTOs, mediaContentDTOs.size());
        } catch (Exception e) {
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
        return anime;
    }

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
}
