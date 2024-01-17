package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.MangaDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MangaDAOImpl implements MangaDAO {
    @Override
    public void addManga(Manga manga) {

    }

    @Override
    public void updateManga(Manga manga) {

    }

    @Override
    public List<Manga> searchMangaByTitle(String title) {
        return null;
    }

    @Override
    public List<Manga> searchMangaByStartDate(int startDate) {
        return null;
    }

    @Override
    public List<Manga> searchMangaByGenres(List<String> genres) {
        return null;
    }

    @Override
    public void removeAnime(String mangaId) {

    }

    @Override
    public void removeManga(String mangaId) {

    }

    @Override
    public void closeConnection() {

    }

    /*
    private static final String MANGA = "manga";

    private final MongoClient mongoClient;
    private final MongoCollection<Document> mangaCollection;



    public MangaDAOImpl() {
        this.mongoClient = BaseMongoDBDAO.getConnection();
        MongoDatabase database = mongoClient.getDatabase(BaseMongoDBDAO.getMongoDBName());
        this.mangaCollection = database.getCollection(MANGA);
    }

    @Override
    public void addManga(Manga manga) {
        Document animeDocument = new Document("_id", manga.getId())
                .append("title", manga.getTitle())
                .append("type", manga.getType())
                .append("themes", manga.getThemes())
                .append("status", manga.getStatus())
                .append("demographic", manga.getDemographics())
                .append("image", manga.getImageUrl())
                .append("authors", manga.getAuthors())
                .append("genres", manga.getGenres())
                .append("startDate", manga.getStartDate())
                .append("endDate", manga.getEndDate())
                .append("background", manga.getBackground())
                .append("titleEnglish", manga.getTitleEnglish())
                .append("titleJapanese", manga.getTitleJapanese())
                .append("serializations", manga.getSerializations())
                .append("synopsis", manga.getSynopsis());
        // append recent reviews and average Rating?


        mangaCollection.insertOne(animeDocument);
    }

    @Override
    public void updateManga(Manga manga) {
        Document filter = new Document("_id", manga.getId());
        Document update = new Document("$set", new Document("title",manga.getTitle()))
                .append("title", manga.getTitle())
                .append("type", manga.getType())
                .append("themes", manga.getThemes())
                .append("status", manga.getStatus())
                .append("demographic", manga.getDemographics())
                .append("image", manga.getImageUrl())
                .append("authors", manga.getAuthors())
                .append("genres", manga.getGenres())
                .append("startDate", manga.getStartDate())
                .append("endDate", manga.getEndDate())
                .append("background", manga.getBackground())
                .append("reviews", manga.getReviews())
                .append("averageRating", manga.getAverageRating())
                .append("titleEnglish", manga.getTitleEnglish())
                .append("titleJapanese", manga.getTitleJapanese())
                .append("serializations", manga.getSerializations())
                .append("synopsis", manga.getSynopsis());

        mangaCollection.updateOne(filter, update);
    }

    @Override
    public List<Manga> searchMangaByTitle(String title) {
        Document query = new Document("title", title);
        List<Manga> result = new ArrayList<>();

        mangaCollection.find(query).forEach(document -> {
            Manga manga = documentToManga(document);
            result.add(manga);
        });

        return result;
    }

    @Override
    public List<Manga>  searchMangaByStartDate(int startDate) {
        Document query = new Document("startDate", startDate);
        List<Manga> result = new ArrayList<>();

        mangaCollection.find(query).forEach(document -> {
            Manga manga = documentToManga(document);
            result.add(manga);
        });

        return result;
    }

    @Override
    public List<Manga> searchMangaByGenres(List<String> genres) {
        Document query = new Document("genres", new Document("$in", genres));
        List<Manga> result = new ArrayList<>();

        mangaCollection.find(query).forEach(document -> {
            Manga manga = documentToManga(document);
            result.add(manga);
        });

        return result;
    }

    @Override
    public void removeAnime(String mangaId) {

    }

    @Override
    public void removeManga(String mangaId) {
        Document query = new Document("_id", mangaId);
        mangaCollection.deleteOne(query);
    }

    @Override
    public void closeConnection() {
        BaseMongoDBDAO.closeConnection(mongoClient);
    }

    private Manga documentToManga(Document document) {
        Manga manga = new Manga();
        manga.setId(document.getObjectId("_id").toString());
        manga.setTitle(document.getString("title"));
        manga.setType(document.getString("type"));
        manga.setThemes(Collections.singletonList(document.getString("themes")));
        manga.setStatus(Status.valueOf(document.getString("status")));
        manga.setDemographics(Collections.singletonList(document.getString("demographics")));
        manga.setImageUrl(document.getString("image"));
        manga.setGenres(Collections.singletonList(document.getString("genres")));
        manga.setSerializations(Collections.singletonList(document.getString("serializations")));
        manga.setBackground(document.getString("background"));
        manga.setTitleEnglish(document.getString("titleEnglish"));
        manga.setTitleJapanese(document.getString("titleJapanese"));
        manga.setStartDate(document.getDate("startDate"));
        manga.setEndDate(document.getDate("endDate"));


        List<Document> reviewsDocuments = document.getList("reviews",Document.class);
        List<Review<Anime>> reviewList = new ArrayList<>();
        manga.setReviews(reviewList);

        List<Document> authorsDocuments = document.getList("authors",Document.class);
        List<Review<Anime>> authorList = new ArrayList<>();

        if(authorsDocuments != null) {
            for(Document authorDocument : authorsDocuments) {

            }
        }
        manga.setAuthors(authorList);


        manga.setAverageRating(document.getDouble("averageRating"));

        return manga;
    }
     */
}
