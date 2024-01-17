package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.dao.ListDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ListDAOImpl extends BaseMongoDBDAO implements ListDAO {

    private PersonalList documentToPersonalList(Document document) {
        User user = new User();
        Document userDoc = (Document) document.get("user");
        user.setId(userDoc.getObjectId("id"));
        user.setLocation(userDoc.getString("location"));
        user.setBirthday(ConverterUtils.convertDateToLocalDate(userDoc.getDate("birthday")));

        List<Anime> anime_list = new ArrayList<>();
        List<Document> animeDoc = document.getList("anime_list", Document.class);
        if (animeDoc != null) {
            for (Document doc : animeDoc) {
                Anime anime = new Anime();
                anime.setId(doc.getObjectId("id"));
                anime.setTitle(doc.getString("title"));
                anime.setImageUrl(doc.getString("picture"));
                anime_list.add(anime);
            }
        }

        List<Manga> manga_list = new ArrayList<>();
        List<Document> mangaDoc = document.getList("manga_list", Document.class);
        if (mangaDoc != null) {
            for (Document doc : mangaDoc) {
                Manga manga = new Manga();
                manga.setId(doc.getObjectId("id"));
                manga.setTitle(doc.getString("title"));
                manga.setImageUrl(doc.getString("picture"));
                manga_list.add(manga);
            }
        }

        PersonalList list = new PersonalList();
        list.setId(document.getObjectId("_id"));
        list.setName(document.getString("name"));
        list.setUser(user);
        list.setAnime(anime_list);
        list.setManga(manga_list);

        return list;
    }

    private Document animeToDocument(AnimeDTO anime) {
        return new Document("id", anime.getId())
                .append("title", anime.getTitle())
                .append("picture", anime.getImageUrl());
    }

    private Document mangaToDocument(MangaDTO manga) {
        return new Document("id", manga.getId())
                .append("title", manga.getTitle())
                .append("picture", manga.getImageUrl());
    }

    private Document userToDocument(RegisteredUserDTO user) {
        Document userDoc = new Document("id", user.getId());
        if (user.getLocation() != null) {
            userDoc.append("location", user.getLocation());
        }
        if (user.getBirthday() != null) {
            userDoc.append("birthday", ConverterUtils.convertLocalDateToDate(user.getBirthday()));
        }
        return userDoc;
    }

    @Override
    public List<PersonalList> findByUserId(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            List<PersonalList> personalLists = new ArrayList<>();
            listsCollection.find(new Document("user.id", userId)).forEach(document -> {
                personalLists.add(documentToPersonalList(document));
            });
            return personalLists;
        } catch (Exception e) {
            throw new DAOException("Error finding lists by user id");
        }
    }

    @Override
    public List<PersonalList> findAll() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            List<PersonalList> personalLists = new ArrayList<>();
            listsCollection.find().forEach(document -> {
                personalLists.add(documentToPersonalList(document));
            });
            return personalLists;
        } catch (Exception e) {
            throw new DAOException("Error finding all lists");
        }
    }

    @Override
    public PersonalList find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            PersonalList personalList = new PersonalList();
            Document listDoc = listsCollection.find(new Document("_id", id)).first();
            if (listDoc != null) {
                personalList = documentToPersonalList(listDoc);
            }
            return personalList;
        } catch (Exception e) {
            throw new DAOException("Error finding list by id");
        }
    }

    @Override
    public void insert(PersonalListDTO list) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            List<Document> animeDoc = new ArrayList<>();
            for (AnimeDTO anime : list.getAnime()) {
                animeDoc.add(animeToDocument(anime));
            }
            List<Document> mangaDoc = new ArrayList<>();
            for (MangaDTO manga : list.getManga()) {
                mangaDoc.add(mangaToDocument(manga));
            }

            Document listDoc = new Document("name", list.getName())
                    .append("user", userToDocument(list.getUser()))
                    .append("anime_list", animeDoc)
                    .append("manga_list", mangaDoc);
            listsCollection.insertOne(listDoc);
        } catch (Exception e) {
            throw new DAOException("Error inserting list");
        }
    }

    @Override
    public void insert(List<PersonalListDTO> lists) throws DAOException {
        for (PersonalListDTO list : lists) {
            insert(list);
        }
    }

    @Override
    public void insertAnime(ObjectId listId, AnimeDTO anime) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            Document animeDoc = animeToDocument(anime);
            listsCollection.updateOne(new Document("_id", listId), new Document("$push", new Document("anime_list", animeDoc)));
        } catch (Exception e) {
            throw new DAOException("Error inserting anime");
        }
    }

    @Override
    public void insertManga(ObjectId listId, MangaDTO manga) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            Document mangaDoc = mangaToDocument(manga);
            listsCollection.updateOne(new Document("_id", listId), new Document("$push", new Document("manga_list", mangaDoc)));
        } catch (Exception e) {
            throw new DAOException("Error inserting manga");
        }
    }

    @Override
    public void removeAnime(ObjectId listId, ObjectId animeId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            listsCollection.updateOne(new Document("_id", listId), new Document("$pull", new Document("anime_list", new Document("id", animeId))));
        } catch (Exception e) {
            throw new DAOException("Error removing anime");
        }
    }

    @Override
    public void removeManga(ObjectId listId, ObjectId mangaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            listsCollection.updateOne(new Document("_id", listId), new Document("$pull", new Document("manga_list", new Document("id", mangaId))));
        } catch (Exception e) {
            throw new DAOException("Error removing manga");
        }
    }

    @Override
    public void update(PersonalListDTO list) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            List<Document> animeDoc = new ArrayList<>();
            for (AnimeDTO anime : list.getAnime()) {
                animeDoc.add(animeToDocument(anime));
            }
            List<Document> mangaDoc = new ArrayList<>();
            for (MangaDTO manga : list.getManga()) {
                mangaDoc.add(mangaToDocument(manga));
            }

            Document listDoc = new Document("name", list.getName())
                    .append("user", userToDocument(list.getUser()))
                    .append("anime_list", animeDoc)
                    .append("manga_list", mangaDoc);
            listsCollection.updateOne(new Document("_id", list.getId()), new Document("$set", listDoc));
        } catch (Exception e) {
            throw new DAOException("Error updating list");
        }
    }

    @Override
    public void delete(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            listsCollection.deleteOne(new Document("_id", id));
        } catch (Exception e) {
            throw new DAOException("Error deleting list");
        }
    }

    @Override
    public void deleteByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
            listsCollection.deleteMany(new Document("user.id", userId));
        } catch (Exception e) {
            throw new DAOException("Error deleting lists by user");
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
  
}
