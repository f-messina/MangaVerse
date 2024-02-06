package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import it.unipi.lsmsd.fnf.dao.ListDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListDAOImpl extends BaseMongoDBDAO implements ListDAO {

    @Override
    public void insert(PersonalListDTO list) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Document listDoc = PersonalListDTOTODocument(list);

            listsCollection.insertOne(listDoc);
        } catch (Exception e) {
            throw new DAOException("Error inserting list", e);
        }
    }

    @Override
    public void changeName(ObjectId id, String name) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("id", id);
            Bson update = Updates.set("name", name);

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error updating list", e);
        }
    }

    @Override
    public void addAnime(ObjectId listId, AnimeDTO anime) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("_id", listId);
            Bson update = Updates.addToSet("anime_list", animeDTOToDocument(anime));

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error inserting anime", e);
        }
    }

    @Override
    public void addManga(ObjectId listId, MangaDTO manga) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("_id", listId);
            Bson update = Updates.addToSet("manga_list", mangaDTOToDocument(manga));

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error inserting manga", e);
        }
    }

    @Override
    public void removeAnime(ObjectId listId, ObjectId animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("_id", listId);
            Bson update = Updates.pull("anime_list", new Document("id", animeId));

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error removing anime", e);
        }
    }

    @Override
    public void removeManga(ObjectId listId, ObjectId mangaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("_id", listId);
            Bson update = Updates.pull("manga_list", new Document("id", mangaId));

            listsCollection.updateOne(filter, update);
        } catch (Exception e) {
            throw new DAOException("Error removing manga", e);
        }
    }

    @Override
    public void delete(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("_id", id);

            listsCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error deleting list", e);
        }
    }

    @Override
    public void deleteByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("user.id", userId);

            listsCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error deleting lists by user", e);
        }
    }

    @Override
    public List<PersonalListDTO> findByUserId(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");

            Bson filter = Filters.eq("user.id", userId);
            Bson projection = Projections.exclude("user");

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

            Bson filter = Filters.eq("_id", id);
            Bson projection = Projections.exclude("user");

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
        RegisteredUserDTO user = new RegisteredUserDTO(
                userDoc.getObjectId("id"),
                userDoc.getString("location"),
                ConverterUtils.dateToLocalDate(userDoc.getDate("birthday"))
        );

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
