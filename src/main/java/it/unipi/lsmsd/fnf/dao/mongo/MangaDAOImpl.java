package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.MangaDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Status;

import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MangaDAOImpl extends BaseMongoDBDAO implements MangaDAO {

    @Override
    public void insertManga(Manga manga) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            mangaCollection.insertOne(mangaToDocument(manga));
        } catch (Exception e) {
            throw new DAOException("Error while inserting manga", e);
        }
    }


    @Override
    public void updateManga(Manga manga) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Document filter = new Document("_id", manga.getId());
            mangaCollection.updateOne(filter, new Document("$set", mangaToDocument(manga)));
        } catch (Exception e) {
            throw new DAOException("Error while updating manga", e);
        }
    }


    @Override
    public Manga searchMangaByTitle(String title) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Document query = new Document("title", title);
            Manga result = new Manga();

            Document mangaDoc = mangaCollection.find(query).first();
            if (mangaDoc != null) {
                result = documentToManga(mangaDoc);
            }
            return result;
        } catch (Exception e) {
            throw new DAOException("Error while searching manga by title", e);
        }
    }

    @Override
    public List<Manga> searchMangaByStartDate(int startDate) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Document query = new Document("start_date", startDate);
            List<Manga> result = new ArrayList<>();
            mangaCollection.find(query).forEach(document -> {
                Manga manga = documentToManga(document);
                result.add(manga);
            });
            return result;
        } catch (Exception e) {
            throw new DAOException("Error while searching manga by start date", e);
        }
    }

    @Override
    public List<Manga> searchMangaByGenres(List<String> genres) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Document query = new Document("genres", new Document("$in", genres));
            List<Manga> result = new ArrayList<>();
            mangaCollection.find(query).forEach(document -> {
                Manga manga = documentToManga(document);
                result.add(manga);
            });
            return result;
        } catch (Exception e) {
            throw new DAOException("Error while searching manga by genres", e);
        }
    }


    @Override
    public void removeManga(String mangaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> mangaCollection = mongoClient.getDatabase("mangaVerse").getCollection("manga");

            Document query = new Document("_id", new ObjectId(mangaId));
            mangaCollection.deleteOne(query);
        } catch (Exception e) {
            throw new DAOException("Error while removing manga", e);
        }
    }



    private Document mangaToDocument(Manga manga) {
        Document doc = new Document("title", manga.getTitle())
                .append("status", manga.getStatus())
                .append("type", manga.getType());
        if (manga.getImageUrl() != null) {
            doc.append("picture", manga.getImageUrl());
        }
        if (manga.getGenres() != null) {
            doc.append("genres", manga.getGenres());
        }
        if (manga.getStartDate() != null) {
            doc.append("start_date", manga.getStartDate());
        }

        if (manga.getEndDate() != null) {
            doc.append("end_date", manga.getEndDate());
        }

        if (manga.getDemographics() != null) {
            doc.append("demographics", manga.getDemographics());
        }
        if (manga.getSerializations() != null) {
            doc.append("serializations", manga.getSerializations());
        }
        if (manga.getSynopsis() != null) {
            doc.append("synopsis", manga.getSynopsis());
        }
        if (manga.getThemes() != null) {
            doc.append("themes", manga.getThemes());
        }
        if (manga.getBackground() != null) {
            doc.append("background", manga.getBackground());
        }

        if (manga.getTitleEnglish() != null) {
            doc.append("title_english", manga.getTitleEnglish());
        }

        if (manga.getTitleJapanese() != null) {
            doc.append("title_japanese", manga.getTitleJapanese());
        }

        if (manga.getAverageRating() != 0) {
            doc.append("average_rating", manga.getAverageRating());
        }

        if (manga.getVolumes() != 0) {
            doc.append("volumes", manga.getVolumes());
        }

        if (manga.getChapters() != 0) {
            doc.append("chapters", manga.getChapters());
        }

        if(manga.getAuthors() != null) {
            List<Document> authorsDocument = new ArrayList<>();
            for (MangaAuthor author : manga.getAuthors()) {
                Document authorDocument = new Document()
                        .append("id", author.getId())
                        .append("name", author.getName())
                        .append("role", author.getRole());
                authorsDocument.add(authorDocument);
            }
            doc.append("authors", authorsDocument);
        }

        if (manga.getReviews() != null) {
            List<Document> reviewsDocuments = new ArrayList<>();
            for (Review<Manga> review : manga.getReviews()) {
                Document reviewDocument = new Document()
                        .append("id", review.getId())
                        .append("user", new Document()
                                .append("id", review.getUser().getId())
                                .append("username", review.getUser().getUsername())
                                .append("picture", review.getUser().getprofilePicUrl()))
                        .append("comment", review.getComment())
                        .append("date", ConverterUtils.convertLocalDateToDate(review.getDate()));
                reviewsDocuments.add(reviewDocument);
            }
            doc.append("recent_reviews", reviewsDocuments);
        }
        return doc;
    }

    private Manga documentToManga(Document document) {
        Manga manga = new Manga();
        manga.setId(document.getObjectId("_id"));
        manga.setTitle(document.getString("title"));
        manga.setType(document.getString("type"));
        manga.setStatus(Status.valueOf(document.getString("status")));

        List<Document> reviewsDocuments = document.getList("recent_reviews",Document.class);
        List<Review<Manga>> reviewList = new ArrayList<>();
        if(reviewsDocuments != null) {
            for(Document reviewDocument : reviewsDocuments) {
                Review<Manga> review = new Review<>();
                User reviewer = new User();
                Document userDocument = reviewDocument.get("user", Document.class);
                reviewer.setId(userDocument.getObjectId("_id"));
                reviewer.setUsername(userDocument.getString("username"));
                reviewer.setprofilePicUrl(userDocument.getString("picture"));
                review.setUser(reviewer);
                review.setId(reviewDocument.getObjectId("_id"));
                review.setComment(reviewDocument.getString("comment"));
                review.setDate(ConverterUtils.convertDateToLocalDate(reviewDocument.getDate("date")));
                reviewList.add(review);
            }
        }
        manga.setReviews(reviewList);
        manga.setAverageRating(document.getDouble("average_rating"));

        if (document.getString("themes") != null) {
            manga.setThemes(Collections.singletonList(document.getString("themes")));

        }
        if (document.getString("genres") != null) {
            manga.setGenres(Collections.singletonList(document.getString("genres")));
        }
        if (document.getString("picture") != null) {
            manga.setImageUrl(document.getString("picture"));

        }
        if (document.getString("demographics") != null) {
            manga.setDemographics(Collections.singletonList(document.getString("demographics")));
        }
        if (document.getString("Serializations") != null) {
            manga.setSerializations(Collections.singletonList(document.getString("serializations")));
        }
        if (document.getString("background") != null) {
            manga.setBackground(document.getString("background"));
        }
        if (document.getString("title_english") != null) {
            manga.setTitleEnglish(document.getString("title_english"));
        }
        if (document.getString("title_japanese") != null) {
            manga.setTitleEnglish(document.getString("title_japanese"));
        }
        if (document.getDate("start_date") != null) {
            manga.setStartDate(document.getString("start_date"));
        }
        if (document.getDate("end_date") != null) {
            manga.setEndDate(document.getString("end_date"));
        }
        if (document.getInteger("volumes") != null) {
            manga.setVolumes(document.getInteger("volumes"));
        }
        if (document.getInteger("chapters") != null) {
            manga.setChapters(document.getInteger("chapters"));
        }

        List<Document> authorsDocument = document.getList("authors", Document.class);
        List<MangaAuthor> authorsList = new ArrayList<>();
        if (authorsDocument != null) {
            for (Document authorDocument : authorsDocument) {
                MangaAuthor author = new MangaAuthor();
                author.setId(authorDocument.getInteger("id"));
                author.setName(authorDocument.getString("name"));
                author.setRole(authorDocument.getString("role"));
                authorsList.add(author);
            }
        }
        manga.setAuthors(authorsList);


        return manga;
    }

}


