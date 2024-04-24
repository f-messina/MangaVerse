package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents an this media content.
 */
public class Anime extends MediaContent {
    private Integer year;
    private String season;
    private Integer episodeCount;
    private List<String> tags = new ArrayList<>();
    private List<String> relatedAnime = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private String producers;
    private String studios;
    private AnimeType type;
    private AnimeStatus status;

    public Integer getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getRelatedAnime() {
        return relatedAnime;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public String getProducers() {
        return producers;
    }

    public String getStudios() {
        return studios;
    }

    public AnimeType getType() {
        return type;
    }

    public AnimeStatus getStatus() {
        return status;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setRelatedAnime(List<String> relatedAnime) {
        this.relatedAnime = relatedAnime;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setProducers(String producers) {
        this.producers = producers;
    }

    public void setStudios(String studios) {
        this.studios = studios;
    }

    public void setType(AnimeType type) {
        this.type = type;
    }

    public void setStatus(AnimeStatus status) {
        this.status = status;
    }


    /**
     * Adds a review to the list of reviews for this this.
     * @param review The review to add.
     */
    public void addReview(Review review) {
        this.reviews.add(review);
    }

    /**
     * Removes a review from the list of reviews for this this.
     * @param review The review to remove.
     */
    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    /**
     * Returns a string representation of the Anime object.
     * @return A string representation of the Anime object.
     */
    @Override
    public String toString() {
        return "Anime{" +
                super.toString() +
                ", year=" + year +
                ", season='" + season + '\'' +
                ", episodeCount='" + episodeCount + '\'' +
                ", tags=" + tags +
                ", relatedAnime=" + relatedAnime +
                ", reviews=" + reviews +
                ", producers='" + producers + '\'' +
                ", studios='" + studios + '\'' +
                '}';
    }

    public AnimeDTO toDTO() {
        return new AnimeDTO(this.getId(), this.getTitle(), this.getImageUrl(), this.getAverageRating(), this.getYear(), this.getSeason());
    }

    /**
     * Converts a Document from MongoDB storage into an Anime object.
     *
     * @param doc The Document to convert.
     * @return An Anime object representing the Document.
     */
    private static Anime fromDocument(Document doc) {
        Anime anime = new Anime();
        anime.setId(doc.getObjectId("_id").toString());
        anime.setTitle(doc.getString("title"));
        anime.setEpisodeCount(doc.getInteger("episodes"));
        anime.setStatus(AnimeStatus.valueOf(doc.getString("status")));
        anime.setImageUrl(doc.getString("picture"));
        Object averageRatingObj = doc.get("average_rating");
        anime.setAverageRating(
                (averageRatingObj instanceof Integer) ? ((Integer) averageRatingObj).doubleValue() :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : 0.0
        );
        anime.setType(AnimeType.fromString(doc.getString("type")));
        anime.setRelatedAnime(doc.getList("relations", String.class));
        anime.setTags(doc.getList("tags", String.class));
        anime.setProducers(doc.getString("producers"));
        anime.setStudios(doc.getString("studios"));
        anime.setSynopsis(doc.getString("synopsis"));

        Optional.ofNullable(doc.get("anime_season", Document.class))
                .ifPresent(seasonDocument -> {
                    anime.setSeason(seasonDocument.getString("season"));
                    anime.setYear(seasonDocument.getInteger("year"));
                });

        List<Review> reviewList = Optional.ofNullable(doc.getList("latest_reviews", Document.class))
                .orElse(Collections.emptyList())
                .stream()
                .map(reviewDocument -> {
                    Review review = new Review();
                    User reviewer = new User();
                    Document userDocument = reviewDocument.get("user", Document.class);
                    reviewer.setId(userDocument.getObjectId("id").toString());
                    reviewer.setUsername(userDocument.getString("username"));
                    reviewer.setProfilePicUrl(userDocument.getString("picture"));
                    review.setUser(reviewer);
                    review.setId(reviewDocument.getObjectId("id").toString());
                    review.setComment(reviewDocument.getString("comment"));
                    review.setDate(ConverterUtils.dateToLocalDate(reviewDocument.getDate("date")));
                    return review;
                })
                .toList();
        anime.setReviews(reviewList);

        return anime;
    }

    /**
     * Converts an Anime object into a Document for MongoDB storage.
     *
     * @param anime The Anime object to convert.
     * @return A Document representing the Anime.
     */
    private Document toDocument(Anime anime) {
        Document doc = new Document();
        appendIfNotNull(doc, "title", anime.getTitle());
        appendIfNotNull(doc, "episodes", anime.getEpisodeCount());
        appendIfNotNull(doc, "status", anime.getStatus());
        appendIfNotNull(doc, "picture", anime.getImageUrl());
        appendIfNotNull(doc, "average_score", anime.getAverageRating());
        appendIfNotNull(doc, "type", anime.getType());
        appendIfNotNull(doc, "producers", anime.getProducers());
        appendIfNotNull(doc, "studios", anime.getStudios());
        appendIfNotNull(doc, "synopsis", anime.getSynopsis());
        appendIfNotNull(doc, "tags", anime.getTags());
        appendIfNotNull(doc, "relations", anime.getRelatedAnime());

        if (anime.getSeason() != null || anime.getYear() != null) {
            Document seasonDocument = new Document();
            appendIfNotNull(seasonDocument, "season", anime.getSeason());
            appendIfNotNull(seasonDocument, "year", anime.getYear());
            doc.append("anime_season", seasonDocument);
        }

        List<Document> reviewsDocuments = Optional.ofNullable(anime.getReviews())
                .orElse(Collections.emptyList())
                .stream()
                .map(review -> {
                    ReviewDTO reviewDTO = review.toDTO();
                    return reviewDTOtoDocument(reviewDTO);
                })
                .toList();

        appendIfNotNull(doc, "latest_reviews", reviewsDocuments);

        return doc;
    }
}
