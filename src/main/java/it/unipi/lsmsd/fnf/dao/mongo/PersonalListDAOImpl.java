package it.unipi.lsmsd.fnf.dao.mongo;


import com.mongodb.client.result.InsertOneResult;
import it.unipi.lsmsd.fnf.dao.PersonalListDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Updates.*;

public class PersonalListDAOImpl extends BaseMongoDBDAO implements PersonalListDAO {

    @Override
    public ObjectId insert(PersonalListDTO list) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Document listDoc = PersonalListDTOTODocument(list);

            InsertOneResult result = listsCollection.insertOne(listDoc);
            if (result.getInsertedId() == null) {
                throw new DAOException("Error inserting list");
            } else {
                return result.getInsertedId().asObjectId().getValue();
            }
        } catch (Exception e) {
            throw new DAOException("Error inserting list", e);
        }
    }

    @Override
    public void update(PersonalListDTO list) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = eq("_id", list.getId());
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
    public void addToList(ObjectId listId, MediaContentDTO item) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = eq("_id", listId);
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
    public void removeFromList(ObjectId listId, ObjectId itemId, MediaContentType type) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            String listType = type.toString().toLowerCase() + "_list";
            Bson filter = eq("_id", listId);
            Bson update = pull(listType, new Document("id", itemId));

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error removing item", e);
        }
    }

    @Override
    public void updateItem(MediaContentDTO item) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            String listType = (item instanceof AnimeDTO)? "anime_list" : "manga_list";
            Bson filter = elemMatch(listType, eq("id", item.getId()));
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
    public void removeItem(ObjectId itemId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = or(elemMatch("anime_list" , eq("id", itemId)),elemMatch("manga_list", eq("id", itemId)));
            Bson update = combine(pull("anime_list", new Document("id", itemId)), pull("manga_list", new Document("id", itemId)));

            listsCollection.updateMany(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error removing item", e);
        }
    }

    @Override
    public void delete(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = eq("_id", id);

            listsCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error deleting list", e);
        }
    }

    @Override
    public void deleteByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = eq("user.id", userId);

            listsCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error deleting lists by user", e);
        }
    }

    @Override
    public List<PersonalListDTO> findByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = eq("user.id", userId);
            Bson projection = exclude("user");

            List<PersonalListDTO> personalLists = new ArrayList<>();
            listsCollection.find(filter).projection(projection).forEach(document -> personalLists.add(documentToPersonalListDTO(document)));

            return personalLists;
        } catch (Exception e) {
            throw new DAOException("Error finding lists by user id", e);
        }
    }

    @Override
    public List<PersonalListDTO> findAll() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            List<PersonalListDTO> personalLists = new ArrayList<>();
            listsCollection.find().forEach(document -> personalLists.add(documentToPersonalListDTO(document)));

            return personalLists;
        } catch (Exception e) {
            throw new DAOException("Error finding all lists", e);
        }
    }

    @Override
    public PersonalListDTO find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = eq("_id", id);
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
                    userDoc.getObjectId("id"),
                    userDoc.getString("location"),
                    ConverterUtils.dateToLocalDate(userDoc.getDate("birthday"))
            );
        }

        List<AnimeDTO> animeList = Optional.ofNullable(document.getList("anime_list", Document.class))
                .orElse(new ArrayList<>())
                .stream()
                .map(doc -> new AnimeDTO(doc.getObjectId("id"), doc.getString("title"), doc.getString("picture")))
                .toList();


        List<MangaDTO> mangaList = Optional.ofNullable(document.getList("manga_list", Document.class))
                .orElse(new ArrayList<>())
                .stream()
                .map(doc -> new MangaDTO(doc.getObjectId("id"), doc.getString("title"), doc.getString("picture")))
                .toList();


        return new PersonalListDTO(
                document.getObjectId("_id"),
                document.getString("name"),
                user,
                mangaList,
                animeList
        );
    }

    private Document animeDTOToDocument(AnimeDTO anime) {
        return new Document("id", anime.getId())
                .append("title", anime.getTitle())
                .append("picture", anime.getImageUrl());
    }

    private Document mangaDTOToDocument(MangaDTO manga) {
        return new Document("id", manga.getId())
                .append("title", manga.getTitle())
                .append("picture", manga.getImageUrl());
    }

    private Document userDTOToDocument(RegisteredUserDTO user) {
        Document doc = new Document("id", user.getId());
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
}
