package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.AnimeDAO;

import com.mongodb.client.*;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Status;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//Watch anime, add anime, update anime info, search anime by name, tags, year, remove anime
public class AnimeDAOImpl implements AnimeDAO {
    private static final String ANIME = "anime";

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> animeCollection;



    public AnimeDAOImpl() {
        this.mongoClient = BaseMongoDBDAO.getConnection();
        this.database = mongoClient.getDatabase(BaseMongoDBDAO.getMongoDBName());
        this.animeCollection = database.getCollection(ANIME);
    }

    @Override
    public void addAnime(Anime anime) {
        Document animeDocument = new Document("_id", anime.getId())
                .append("title", anime.getTitle())
                .append("type", anime.getType())
                .append("episodeCount", anime.getEpisodeCount())
                .append("status", anime.getStatus())
                .append("season", anime.getSeason())
                .append("image", anime.getImageUrl())
                .append("relations", anime.getRelatedAnime())
                .append("tags", anime.getTags())
                .append("year", anime.getYear())
                .append("producers", anime.getProducers())
                .append("studios", anime.getStudios())
                .append("synopsis", anime.getSynopsis());
                // append recent reviews and average Rating?


        animeCollection.insertOne(animeDocument);
    }

    @Override
    public void updateAnime(Anime anime) {
        Document filter = new Document("_id", anime.getId());
        Document update = new Document("$set", new Document("title",anime.getTitle()))
                .append("type", anime.getType())
                .append("episodeCount", anime.getEpisodeCount())
                .append("status", anime.getStatus())
                .append("season", anime.getSeason())
                .append("image", anime.getImageUrl())
                .append("relations", anime.getRelatedAnime())
                .append("tags", anime.getTags())
                .append("reviews", anime.getReviews())
                .append("averageRating", anime.getAverageRating())
                .append("year", anime.getYear())
                .append("producers", anime.getProducers())
                .append("studios", anime.getStudios())
                .append("synopsis", anime.getSynopsis());

        animeCollection.updateOne(filter, update);
    }

    @Override
    public List<Anime> searchAnimeByTitle(String title) {
        Document query = new Document("title", title);
        List<Anime> result = new ArrayList<>();

        animeCollection.find(query).forEach(document -> {
            Anime anime = documentToAnime(document);
            result.add(anime);
        });

        return result;
    }

    @Override
    public List<Anime>  searchAnimeByYear(int year) {
        Document query = new Document("year", year);
        List<Anime> result = new ArrayList<>();

        animeCollection.find(query).forEach(document -> {
            Anime anime = documentToAnime(document);
            result.add(anime);
        });

        return result;
    }

    @Override
    public List<Anime> searchAnimeByTags(List<String> tags) {
        Document query = new Document("tags", new Document("$in", tags));
        List<Anime> result = new ArrayList<>();

        animeCollection.find(query).forEach(document -> {
            Anime anime = documentToAnime(document);
            result.add(anime);
        });

        return result;
    }

    @Override
    public void removeAnime(String animeId) {
        Document query = new Document("_id", animeId);
        animeCollection.deleteOne(query);
    }

    @Override
    public void closeConnection() {
        BaseMongoDBDAO.closeConnection(mongoClient);
    }

    private Anime documentToAnime(Document document) {
        Anime anime = new Anime();
        anime.setId(document.getObjectId("_id").toString());
        anime.setTitle(document.getString("title"));
        anime.setType(document.getString("type"));
        anime.setEpisodeCount(document.getInteger("episodeCount"));
        anime.setStatus(Status.valueOf(document.getString("status")));
        anime.setSeason(document.getString("season"));
        anime.setImageUrl(document.getString("image"));
        anime.setRelatedAnime(Collections.singletonList(document.getString("relations")));
        anime.setTags(Collections.singletonList(document.getString("tags")));
        anime.setYear(document.getInteger("year"));
        anime.setProducers(document.getString("producers"));
        anime.setStudios(document.getString("studios"));
        anime.setSynopsis(document.getString("synopisis"));


        List<Document> reviewsDocuments = document.getList("reviews",Document.class);
        List<Review<Anime>> reviewList = new ArrayList<>();

        if(reviewsDocuments != null) {
            for(Document reviewDocument : reviewsDocuments) {

            }
        }

        anime.setReviews(reviewList);

        anime.setAverageRating(document.getDouble("averageRating"));

        return anime;

    }


}
