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
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAOImpl extends BaseMongoDBDAO implements ReviewDAO {
    @Override
    public void insert(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            reviewCollection.insertOne(reviewDTOToDocument(review));
        } catch (Exception e) {
            throw new DAOException("Error while inserting review", e);
        }
    }

    @Override
    public void update(ReviewDTO review) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = Filters.eq("_id", review.getId());
            Bson updatedKeys = Updates.combine(Updates.set("date", ConverterUtils.convertLocalDateToDate(LocalDate.now())));
            if (review.getComment() != null) {
                updatedKeys = Updates.combine(updatedKeys, Updates.set("comment", review.getComment()));
            }
            if (review.getRating() != null) {
                updatedKeys = Updates.combine(updatedKeys, Updates.set("rating", review.getRating()));
            }

            reviewCollection.updateOne(filter, updatedKeys);
        } catch (Exception e) {
            throw new DAOException("Error while updating review", e);
        }
    }

    @Override
    public void delete(ObjectId id) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = Filters.eq("_id", id);

            reviewCollection.deleteOne(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    public void deleteByMedia(ObjectId mediaId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = Filters.or(
                    Filters.eq("anime.id", mediaId),
                    Filters.eq("manga.id", mediaId)
            );

            reviewCollection.deleteMany(filter);
        } catch (Exception e) {
            throw new DAOException("Error while deleting review", e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(ObjectId userId) throws DAOException {
        try (MongoClient mongoClient = getConnection()) {
            MongoCollection<Document> reviewCollection = mongoClient.getDatabase("mangaVerse").getCollection("review");

            Bson filter = Filters.eq("user.id", userId);
            Bson projection = Projections.exclude("user");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(filter).projection(projection).forEach(document -> {
                ReviewDTO review = documentToReviewDTO(document);
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

            Bson filter = Filters.or(
                    Filters.eq("anime.id", mediaId),
                    Filters.eq("manga.id", mediaId)
            );
            Bson projection = Projections.exclude("anime", "manga");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(filter).projection(projection).forEach(document -> {
                ReviewDTO review = documentToReviewDTO(document);
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

            Bson filter = Filters.and(
                    Filters.eq("user.id", userId),
                    Filters.or(
                            Filters.eq("anime.id", mediaId),
                            Filters.eq("manga.id", mediaId)
                    )
            );
            Bson projection = Projections.exclude("user", "anime", "manga");

            List<ReviewDTO> result = new ArrayList<>();
            reviewCollection.find(filter).projection(projection).forEach(document -> {
                ReviewDTO review = documentToReviewDTO(document);
                result.add(review);
            });

            return result;
        } catch (Exception e) {
            throw new DAOException("Error while finding reviews by user and media", e);
        }
    }

    private Document reviewDTOToDocument(ReviewDTO review) {
        Document reviewDocument = new Document()
                .append("user", new Document()
                        .append("id", review.getUser().getId())
                        .append("username", review.getUser().getUsername())
                        .append("picture", review.getUser().getProfilePicUrl()))
                .append("date", ConverterUtils.convertLocalDateToDate(LocalDate.now()));
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

    private ReviewDTO documentToReviewDTO(Document reviewDoc) {
        ObjectId reviewId = reviewDoc.getObjectId("_id");
        LocalDate date = ConverterUtils.convertDateToLocalDate(reviewDoc.getDate("date"));
        String comment = reviewDoc.getString("comment");
        Integer rating = reviewDoc.getInteger("rating");

        MediaContentDTO mediaDTO = null;
        Document mediaDoc;
        if ((mediaDoc = reviewDoc.get("anime", Document.class)) != null) {
            mediaDTO = new AnimeDTO(mediaDoc.getObjectId("id"), mediaDoc.getString("title"), mediaDoc.getString("image"));
        } else if ((mediaDoc = reviewDoc.get("manga", Document.class)) != null) {
            mediaDTO = new MangaDTO(mediaDoc.getObjectId("id"), mediaDoc.getString("title"), mediaDoc.getString("image"));
        }

        Document userDoc = reviewDoc.get("user", Document.class);
        RegisteredUserDTO userDTO = (userDoc != null) ? new RegisteredUserDTO(userDoc.getObjectId("id"), userDoc.getString("username"), userDoc.getString("picture")) : null;

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }
}