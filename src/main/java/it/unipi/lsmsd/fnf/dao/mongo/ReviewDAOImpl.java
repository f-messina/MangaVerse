package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    @Override
    public void insert(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            reviewCollection.insertOne(reviewToDocument(review));
        } catch (Exception e) {
            throw new DAOException("Error while inserting review", e);
        }
    }

    @Override
    public void delete(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Document filter = new Document("_id", id);
            reviewCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    @Override
    public void update(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Document filter = new Document("_id", review.getId());
            reviewCollection.updateOne(filter, new Document("$set", reviewToDocument(review)));
        } catch (Exception e) {
            throw new DAOException("Error while updating review", e);
        }
    }

    @Override
    public List<ReviewDTO> findAll() throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find().forEach(document -> {
                ReviewDTO review = documentToReview(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding all reviews", e);
        }
    }

    @Override
    public ReviewDTO find(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Document query = new Document("_id", id);
            ReviewDTO result = new ReviewDTO();
            Document reviewDoc = reviewCollection.find(query).first();
            if (reviewDoc != null) {
                result = documentToReview(reviewDoc);
            }

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding review", e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Document query = new Document("user.id", userId);
            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(query).forEach(document -> {
                ReviewDTO review = documentToReview(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user", e);
        }
    }

    @Override
    public List<ReviewDTO> findByMedia(ObjectId mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Document query = new Document("$or",
                    List.of(
                            new Document("anime.id", mediaId),
                            new Document("manga.id", mediaId)
                    )
            );

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(query).forEach(document -> {
                ReviewDTO review = documentToReview(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by media", e);
        }
    }

    @Override
    public List<ReviewDTO> findByUserAndMedia(ObjectId userId, ObjectId mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Document query = new Document("$and",
                    List.of(
                            new Document("user.id", userId),
                            new Document("$or",
                                    List.of(
                                            new Document("anime.id", mediaId),
                                            new Document("manga.id", mediaId)
                                    )
                            )
                    )
            );

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(query).forEach(document -> {
                ReviewDTO review = documentToReview(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user and media", e);
        }
    }

    private Document reviewToDocument(ReviewDTO review) {
        Document reviewDocument = new Document()
                .append("id", review.getId())
                .append("user", new Document()
                        .append("id", review.getUser().getId())
                        .append("username", review.getUser().getUsername())
                        .append("picture", review.getUser().getProfilePicUrl()))
                .append("date", ConverterUtils.convertLocalDateToDate(review.getDate()));
        if (review.getComment() != null) {
            reviewDocument.append("comment", review.getComment());
        }
        if (review.getRating() != null) {
            reviewDocument.append("rating", review.getRating());
        }
        if (review.getMediaContent() instanceof AnimeDTO) {
            reviewDocument.append("anime", new Document()
                    .append("id", review.getMediaContent().getId())
                    .append("title", review.getMediaContent().getTitle())
                    .append("image", review.getMediaContent().getImageUrl()));
        } else if (review.getMediaContent() instanceof MangaDTO) {
            reviewDocument.append("manga", new Document()
                    .append("id", review.getMediaContent().getId())
                    .append("title", review.getMediaContent().getTitle())
                    .append("image", review.getMediaContent().getImageUrl()));
        }

        return reviewDocument;
    }

    private ReviewDTO documentToReview(Document reviewDoc) {
        ObjectId reviewId = reviewDoc.getObjectId("_id");
        LocalDate date = ConverterUtils.convertDateToLocalDate(reviewDoc.getDate("date"));
        String comment = null;
        if (reviewDoc.getString("comment") != null) {
            comment = reviewDoc.getString("comment");
        }
        Integer rating = null;
        if (reviewDoc.getInteger("rating") != null) {
            rating = reviewDoc.getInteger("rating");
        }

        MediaContentDTO mediaDTO;
        Document mediaDoc = reviewDoc.get("anime", Document.class);
        if (mediaDoc != null) {
            mediaDTO = new AnimeDTO(
                    mediaDoc.getObjectId("id"),
                    mediaDoc.getString("title"),
                    mediaDoc.getString("image")
            );
        } else {
            mediaDoc = reviewDoc.get("manga", Document.class);
            mediaDTO = new MangaDTO(
                    mediaDoc.getObjectId("id"),
                    mediaDoc.getString("title"),
                    mediaDoc.getString("image")
            );
        }

        RegisteredUserDTO userDTO = new RegisteredUserDTO(
                reviewDoc.get("user", Document.class).getObjectId("id"),
                reviewDoc.get("user", Document.class).getString("username"),
                reviewDoc.get("user", Document.class).getString("picture")
        );

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }
}