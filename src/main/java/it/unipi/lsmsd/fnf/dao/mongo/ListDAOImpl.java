package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.unipi.lsmsd.fnf.dao.ListDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
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
        List<Document> animeDoc = (List<Document>) document.get("anime_list");
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
        List<Document> mangaDoc = (List<Document>) document.get("manga_list");
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
            userDoc.append("birthday", user.getBirthday());
        }
        return userDoc;
    }

    @Override
    public List<PersonalList> findByUserId(ObjectId userId) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        List<PersonalList> PersonalLists = new ArrayList<>();
        try (MongoCursor<Document> cursor = listsCollection.find(new Document("user.id", userId)).iterator()) {
            while (cursor.hasNext()) {
                PersonalLists.add(documentToPersonalList(cursor.next()));
            }
        }
        closeConnection(mongoClient);
        return PersonalLists;
    }

    @Override
    public List<PersonalList> findAll() {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        List<PersonalList> personalLists = new ArrayList<>();
        try (MongoCursor<Document> cursor = listsCollection.find().iterator()) {
            while (cursor.hasNext()) {
                personalLists.add(documentToPersonalList(cursor.next()));
            }
        }
        closeConnection(mongoClient);
        return personalLists;
    }

    @Override
    public PersonalList find(ObjectId id) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        PersonalList personalList = new PersonalList();
        Document listDoc = listsCollection.find(new Document("_id", id)).first();
        if (listDoc != null) {
            personalList = documentToPersonalList(listDoc);
        }
        closeConnection(mongoClient);
        return personalList;
    }

    @Override
    public void insert(PersonalListDTO list) {
        MongoClient mongoClient = getConnection();
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
        closeConnection(mongoClient);
    }

    @Override
    public void insert(List<PersonalListDTO> lists) {
        for (PersonalListDTO list : lists) {
            insert(list);
        }
    }

    @Override
    public void insertAnime(ObjectId listId, AnimeDTO anime) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        Document animeDoc = animeToDocument(anime);
        listsCollection.updateOne(new Document("_id", listId), new Document("$push", new Document("anime_list", animeDoc)));
        closeConnection(mongoClient);
    }

    @Override
    public void insertManga(ObjectId listId, MangaDTO manga) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        Document mangaDoc = mangaToDocument(manga);
        listsCollection.updateOne(new Document("_id", listId), new Document("$push", new Document("manga_list", mangaDoc)));
        closeConnection(mongoClient);
    }

    @Override
    public void removeAnime(ObjectId listId, ObjectId animeId) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        listsCollection.updateOne(new Document("_id", listId), new Document("$pull", new Document("anime_list", new Document("id", animeId))));
        closeConnection(mongoClient);
    }

    @Override
    public void removeManga(ObjectId listId, ObjectId mangaId) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        listsCollection.updateOne(new Document("_id", listId), new Document("$pull", new Document("manga_list", new Document("id", mangaId))));
        closeConnection(mongoClient);

    }

    @Override
    public void update(PersonalListDTO list) {
        MongoClient mongoClient = getConnection();
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
        closeConnection(mongoClient);
    }

    @Override
    public void delete(ObjectId id) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        listsCollection.deleteOne(new Document("_id", id));
        closeConnection(mongoClient);
    }

    @Override
    public void deleteByUser(ObjectId userId) {
        MongoClient mongoClient = getConnection();
        MongoCollection<Document> listsCollection = mongoClient.getDatabase("mangaVerse").getCollection("lists");
        listsCollection.deleteMany(new Document("user.id", userId));
        closeConnection(mongoClient);
    }

    @Override
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) {
        return null;
    }

    @Override
    public List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) {
        return null;
    }

    @Override
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) {
        return null;
    }

    @Override
    public List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) {
        return null;
    }
}
