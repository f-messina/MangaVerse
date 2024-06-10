package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.*;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import it.unipi.lsmsd.fnf.model.registeredUser.Manager;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentUtils {
    /**
     * Appends a key-value pair to a MongoDB document if the value is not null or empty.
     *
     * @param doc   MongoDB document to which the key-value pair is to be appended.
     * @param key   Key of the key-value pair.
     * @param value Value of the key-value pair.
     */
    public static void appendIfNotNull(Document doc, String key, Object value) {
        if (value != null &&
                !(value instanceof String && (value.equals(Constants.NULL_STRING) || value.equals(Gender.UNKNOWN.toString()))) &&
                !(value instanceof Date && value.equals(ConverterUtils.localDateToDate(Constants.NULL_DATE))) &&
                (StringUtils.isNotBlank(value.toString()) ||
                        (value instanceof List && CollectionUtils.isNotEmpty((List<?>) value)))) {
            doc.append(key, value);
        }
    }

    // Model / DTO to Document conversion methods

    /**
     * Converts an Anime object into a Document for MongoDB storage.
     *
     * @param anime The Anime object to convert.
     * @return A Document representing the Anime.
     */
    public static Document animeToDocument(Anime anime) {
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

        List<Document> reviewsDocuments = Optional.ofNullable(anime.getLatestReviews())
                .orElse(Collections.emptyList())
                .stream()
                .map(review -> reviewDTOToNestedDocument(review.toDTO()))
                .toList();

        appendIfNotNull(doc, "latest_reviews", reviewsDocuments);

        return doc;
    }




    /**
     * Converts a Manga object to a MongoDB Document.
     *
     * @param manga The Manga object to be converted.
     * @return The MongoDB Document representation of the Manga object.
     */
    public static Document mangaToDocument(Manga manga) {
        Document doc = new Document();

        appendIfNotNull(doc, "title", manga.getTitle());
        appendIfNotNull(doc, "status", manga.getStatus());
        if (manga.getType() != null) {
            appendIfNotNull(doc, "type", manga.getType().name());
        }
        appendIfNotNull(doc, "picture", manga.getImageUrl());
        appendIfNotNull(doc, "genres", manga.getGenres());
        appendIfNotNull(doc, "start_date", ConverterUtils.localDateToDate(manga.getStartDate()));
        appendIfNotNull(doc, "end_date", ConverterUtils.localDateToDate(manga.getEndDate()));
        appendIfNotNull(doc, "demographics", manga.getDemographics());
        appendIfNotNull(doc, "serializations", manga.getSerializations());
        appendIfNotNull(doc, "synopsis", manga.getSynopsis());
        appendIfNotNull(doc, "themes", manga.getThemes());
        appendIfNotNull(doc, "background", manga.getBackground());
        appendIfNotNull(doc, "title_english", manga.getTitleEnglish());
        appendIfNotNull(doc, "title_japanese", manga.getTitleJapanese());
        appendIfNotNull(doc, "average_rating", manga.getAverageRating());
        appendIfNotNull(doc, "volumes", manga.getVolumes());
        appendIfNotNull(doc, "chapters", manga.getChapters());

        List<Document> authorsDocument = Optional.ofNullable(manga.getAuthors())
                .orElse(Collections.emptyList())
                .stream()
                .map(DocumentUtils::AuthorToNestedDocument)
                .toList();
        appendIfNotNull(doc, "authors", authorsDocument);

        List<Document> reviewsDocuments = Optional.ofNullable(manga.getLatestReviews())
                .orElse(Collections.emptyList())
                .stream()
                .map(review -> reviewDTOToNestedDocument(review.toDTO()))
                .toList();

        appendIfNotNull(doc, "latest_reviews", reviewsDocuments);

        return doc;
    }

    private static Document AuthorToNestedDocument(MangaAuthor author) {
        return new Document()
                .append("id", author.getId())
                .append("name", author.getName())
                .append("role", author.getRole());
    }

    public static Document reviewDTOToNestedDocument(ReviewDTO reviewDTO) {
        Document reviewDocument = new Document();
        appendIfNotNull(reviewDocument, "id", new ObjectId(reviewDTO.getId()));
        appendIfNotNull(reviewDocument, "comment", reviewDTO.getComment());
        appendIfNotNull(reviewDocument, "date", ConverterUtils.localDateTimeToDate(reviewDTO.getDate()));
        appendIfNotNull(reviewDocument, "rating", reviewDTO.getRating());
        Document userDocument = new Document();
        appendIfNotNull(userDocument, "id", new ObjectId(reviewDTO.getUser().getId()));
        appendIfNotNull(userDocument, "username", reviewDTO.getUser().getUsername());
        appendIfNotNull(userDocument, "picture", reviewDTO.getUser().getProfilePicUrl());
        appendIfNotNull(reviewDocument, "user", userDocument);
        return reviewDocument;
    }

    /**
     * Converts a ReviewDTO object to a MongoDB document for storage in the database.
     *
     * @param reviewDTO The ReviewDTO object to be converted.
     * @return A MongoDB Document representing the ReviewDTO object.
     */
    public static Document reviewDTOToDocument(ReviewDTO reviewDTO) {
        Document reviewDocument = new Document();
        Document userDocument = new Document();
        appendIfNotNull(userDocument, "id", new ObjectId(reviewDTO.getUser().getId()));
        appendIfNotNull(userDocument, "username", reviewDTO.getUser().getUsername());
        appendIfNotNull(userDocument, "picture", reviewDTO.getUser().getProfilePicUrl());
        appendIfNotNull(userDocument, "location", reviewDTO.getUser().getLocation());
        appendIfNotNull(userDocument, "birthday", ConverterUtils.localDateToDate(reviewDTO.getUser().getBirthDate()));
        appendIfNotNull(reviewDocument, "user", userDocument);
        appendIfNotNull(reviewDocument, "date", ConverterUtils.localDateTimeToDate(reviewDTO.getDate()));
        appendIfNotNull(reviewDocument, "comment", reviewDTO.getComment());
        appendIfNotNull(reviewDocument, "rating", reviewDTO.getRating());
        boolean isAnime = reviewDTO.getMediaContent() instanceof AnimeDTO;
        Document mediaDocument = new Document();
        appendIfNotNull(mediaDocument, "id", new ObjectId(reviewDTO.getMediaContent().getId()));
        appendIfNotNull(mediaDocument, "title", reviewDTO.getMediaContent().getTitle());
        appendIfNotNull(reviewDocument, isAnime ? "anime" : "manga", mediaDocument);

        return reviewDocument;
    }

    public static Document RegisteredUserToDocument(UserRegistrationDTO user) {
        return createUserDocument(user.getPassword(), user.getEmail(), LocalDate.now(),
                user.getFullname(), null, user.getUsername(),
                user.getBirthday(), null, user.getGender(), user.getLocation());
    }

    public static Document RegisteredUserToDocument(User user) {
        return createUserDocument(user.getPassword(), user.getEmail(), user.getJoinedDate(),
                user.getFullname(), user.getProfilePicUrl(), user.getUsername(),
                user.getBirthday(), user.getDescription(), user.getGender(), user.getLocation());
    }

    private static Document createUserDocument(String password, String email, LocalDate joinedDate, String fullname, String profilePicUrl, String username, LocalDate birthday, String description, Gender gender, String location) {
        Document doc = new Document();
        appendIfNotNull(doc, "password", password);
        appendIfNotNull(doc, "email", email);

        if (joinedDate != null) {
            appendIfNotNull(doc, "joined_on", ConverterUtils.localDateToDate(joinedDate));
        }
        appendIfNotNull(doc, "fullname", fullname);
        appendIfNotNull(doc, "picture", profilePicUrl);
        appendIfNotNull(doc, "username", username);
        appendIfNotNull(doc, "birthday", ConverterUtils.localDateToDate(birthday));
        appendIfNotNull(doc, "description", description);
        appendIfNotNull(doc, "gender", gender != null ? gender.toString() : null);
        appendIfNotNull(doc, "location", location);

        return doc;
    }

    // Document to Model / DTO conversion methods

    /**
     * Converts a Document from MongoDB storage into an Anime object.
     *
     * @param doc The Document to convert.
     * @return An Anime object representing the Document.
     */
    public static Anime documentToAnime(Document doc) {
        //Add anime doc.getlist(review_ids)
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
                .map(DocumentUtils::nestedDocumentToReview)
                .toList();
        anime.setLatestReviews(reviewList);

        anime.setLikes(doc.getInteger("likes"));

        anime.setReviewIds(doc.getList("review_ids", String.class));
        return anime;
    }

    private static Review nestedDocumentToReview(Document doc) {
        Review review = new Review();
        User reviewer = new User();
        Document userDocument = doc.get("user", Document.class);
        reviewer.setId(userDocument.getObjectId("id").toString());
        reviewer.setUsername(userDocument.getString("username"));
        reviewer.setProfilePicUrl(userDocument.getString("picture"));
        review.setUser(reviewer);
        review.setId(doc.getObjectId("id").toString());
        review.setRating(doc.getInteger("rating"));
        review.setComment(doc.getString("comment"));
        review.setDate(ConverterUtils.dateToLocalDateTime(doc.getDate("date")));
        return review;
    }

    /**
     * Converts a Document from MongoDB storage into an AnimeDTO object.
     *
     * @param doc The Document to convert.
     * @return An AnimeDTO object representing the Document.
     */
    public static AnimeDTO documentToAnimeDTO(Document doc) {
        AnimeDTO anime = new AnimeDTO();
        anime.setId(doc.getObjectId("_id").toString());
        anime.setTitle(doc.getString("title"));
        anime.setImageUrl(doc.getString("picture"));
        Object averageRatingObj = doc.get("average_rating");
        anime.setAverageRating(
                (averageRatingObj instanceof Integer) ? ((Integer) averageRatingObj).doubleValue() :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : 0.0
        );
        if ((doc.get("anime_season", Document.class) != null)) {
            anime.setYear(doc.get("anime_season", Document.class).getInteger("year"));
            anime.setSeason(doc.get("anime_season", Document.class).getString("season"));
        }
        anime.setLikes(doc.getInteger("likes"));

        return anime;
    }

    /**
     * Converts a MongoDB document representing a review to a ReviewDTO object.
     *
     * @param reviewDoc The MongoDB document representing the review.
     * @return A ReviewDTO object representing the MongoDB document.
     */
    public static ReviewDTO documentToReviewDTO(Document reviewDoc) {
        String reviewId = reviewDoc.getObjectId("_id").toString();
        LocalDateTime date = ConverterUtils.dateToLocalDateTime(reviewDoc.getDate("date"));
        String comment = reviewDoc.getString("comment");
        Integer rating = reviewDoc.getInteger("rating");

        MediaContentDTO mediaDTO = null;
        Document mediaDoc;
        if ((mediaDoc = reviewDoc.get("anime", Document.class)) != null) {
            mediaDTO = new AnimeDTO(mediaDoc.getObjectId("id").toString(), mediaDoc.getString("title"));
        } else if ((mediaDoc = reviewDoc.get("manga", Document.class)) != null) {
            mediaDTO = new MangaDTO(mediaDoc.getObjectId("id").toString(), mediaDoc.getString("title"));
        }

        Document userDoc = reviewDoc.get("user", Document.class);
        UserSummaryDTO userDTO = (userDoc != null) ? new UserSummaryDTO(userDoc.getObjectId("id").toString(), userDoc.getString("username"), userDoc.getString("picture")) : null;

        return new ReviewDTO(reviewId, date, comment, rating, mediaDTO, userDTO);
    }

    /**
     * Converts a MongoDB Document to a Manga object.
     *
     * @param document The MongoDB Document to be converted.
     * @return The Manga object representation of the MongoDB Document.
     */
    public static Manga documentToManga(Document document) {
        Manga manga = new Manga();
        manga.setId(document.getObjectId("_id").toString());
        manga.setTitle(document.getString("title"));
        manga.setType(MangaType.fromString(document.getString("type")));
        manga.setStatus(MangaStatus.valueOf(document.getString("status")));
        manga.setThemes(document.getList("themes", String.class));
        manga.setGenres(document.getList("genres", String.class));
        manga.setImageUrl(document.getString("picture"));
        manga.setDemographics(Optional.ofNullable(document.getList("demographics", String.class)).stream()
                .flatMap(List::stream)
                .map(MangaDemographics::fromString)
                .collect(Collectors.toList()));
        manga.setSerializations(document.getString("serializations"));
        manga.setBackground(document.getString("background"));
        manga.setTitleEnglish(document.getString("title_english"));
        manga.setTitleJapanese(document.getString("title_japanese"));
        manga.setStartDate(ConverterUtils.dateToLocalDate(document.getDate("start_date")));
        manga.setEndDate(ConverterUtils.dateToLocalDate(document.getDate("end_date")));
        manga.setVolumes(document.getInteger("volumes"));
        manga.setChapters(document.getInteger("chapters"));
        Object averageRatingObj = document.get("average_rating");
        manga.setAverageRating(
                (averageRatingObj instanceof Integer) ? ((Integer) averageRatingObj).doubleValue() :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : 0.0
        );

        Optional.ofNullable(document.getList("authors", Document.class))
                .ifPresent(authors -> {
                    List<MangaAuthor> authorsList = authors.stream()
                            .map(authorDocument -> {
                                MangaAuthor author = new MangaAuthor();
                                author.setId(authorDocument.getInteger("id"));
                                author.setName(authorDocument.getString("name"));
                                author.setRole(authorDocument.getString("role"));
                                return author;
                            })
                            .toList();
                    manga.setAuthors(authorsList);
                });

        List<Review> reviewList = Optional.ofNullable(document.getList("latest_reviews", Document.class))
                .orElse(Collections.emptyList())
                .stream()
                .map(DocumentUtils::nestedDocumentToReview)
                .toList();
        manga.setLatestReviews(reviewList);

        manga.setLikes(document.getInteger("likes"));

        manga.setReviewIds(document.getList("review_ids", String.class));

        return manga;
    }

    /**
     * Converts a MongoDB Document to a MangaDTO object.
     *
     * @param doc The MongoDB Document to be converted.
     * @return The MangaDTO object representation of the MongoDB Document.
     */
    public static MangaDTO documentToMangaDTO(Document doc) {
        MangaDTO manga = new MangaDTO();
        manga.setId(doc.getObjectId("_id").toString());
        manga.setTitle(doc.getString("title"));
        manga.setImageUrl(doc.getString("picture"));
        Object averageRatingObj = doc.get("average_rating");
        manga.setAverageRating(
                (averageRatingObj instanceof Integer) ? Double.valueOf(((Integer) averageRatingObj)) :
                        (averageRatingObj instanceof Double) ? (Double) averageRatingObj : null
        );
        manga.setStartDate(ConverterUtils.dateToLocalDate(doc.getDate("start_date")));
        manga.setEndDate(ConverterUtils.dateToLocalDate(doc.getDate("end_date")));
        manga.setLikes(doc.getInteger("likes"));
        return manga;
    }

    public static UserSummaryDTO documentToUserSummaryDTO(Document doc) {
        UserSummaryDTO user = new UserSummaryDTO();
        user.setId(doc.getObjectId("_id").toString());
        user.setUsername(doc.getString("username"));
        user.setProfilePicUrl(doc.getString("picture"));
        user.setLocation(doc.getString("location"));
        Date birthDate = doc.getDate("birthday");
        if (birthDate != null)
            user.setBirthDate(ConverterUtils.dateToLocalDate(birthDate));
        return user;
    }

    public static RegisteredUser documentToRegisteredUser(Document doc) {
        RegisteredUser user;

        if (doc.getBoolean("is_manager") != null) {
            Manager manager = new Manager();
            manager.setHiredDate(ConverterUtils.dateToLocalDate(doc.getDate("hired_on")));
            user = manager;
        } else {
            User normalUser = new User();
            normalUser.setUsername(doc.getString("username"));
            normalUser.setBirthday(ConverterUtils.dateToLocalDate(doc.getDate("birthday")));
            normalUser.setDescription(doc.getString("description"));
            normalUser.setGender(Gender.fromString(doc.getString("gender")));
            normalUser.setLocation(doc.getString("location"));
            normalUser.setFollowers(doc.getInteger("followers"));
            normalUser.setFollowed(doc.getInteger("followed"));
            normalUser.setReviewIds(doc.getList("review_ids", String.class));
            normalUser.setAppRating(doc.getInteger("app_rating"));
            user = normalUser;
        }

        user.setId(doc.getObjectId("_id").toString());
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setJoinedDate(ConverterUtils.dateToLocalDate(doc.getDate("joined_on")));
        user.setFullname(doc.getString("fullname"));
        user.setProfilePicUrl(doc.getString("picture"));
        return user;
    }

    public static Document UserToUnsetUserFieldsDocument(User registeredUser) {
        Document doc = new Document();
        if (registeredUser.getFullname() != null && registeredUser.getFullname().equals(Constants.NULL_STRING))
            doc.append("fullname", 1);
        if (registeredUser.getBirthday() != null && registeredUser.getBirthday().equals(Constants.NULL_DATE))
            doc.append("birthday", 1);
        if (registeredUser.getLocation() != null && registeredUser.getLocation().equals(Constants.NULL_STRING))
            doc.append("location", 1);
        if (registeredUser.getDescription() != null && registeredUser.getDescription().equals(Constants.NULL_STRING))
            doc.append("description", 1);
        if (registeredUser.getGender() != null && registeredUser.getGender().equals(Gender.UNKNOWN))
            doc.append("gender", 1);
        if (registeredUser.getProfilePicUrl() != null && registeredUser.getProfilePicUrl().equals(Constants.NULL_STRING))
            doc.append("picture", 1);
        return doc;
    }

    public static Document animeToUnsetAnimeFieldsDocument(Anime anime) {
        Document doc = new Document();
        if (anime.getEpisodeCount() != null && anime.getEpisodeCount().equals(Constants.NULL_INT))
            doc.append("episodes", 1);
        if (anime.getStatus() != null && anime.getStatus().equals(AnimeStatus.UNKNOWN))
            doc.append("status", 1);
        if (anime.getImageUrl() != null && anime.getImageUrl().equals(Constants.NULL_STRING))
            doc.append("picture", 1);
        if (anime.getAverageRating() != null && anime.getAverageRating().equals(Constants.NULL_DOUBLE))
            doc.append("average_rating", 1);
        if (anime.getType() != null && anime.getType().equals(AnimeType.UNKNOWN))
            doc.append("type", 1);
        if (anime.getProducers() != null && anime.getProducers().equals(Constants.NULL_STRING))
            doc.append("producers", 1);
        if (anime.getStudios() != null && anime.getStudios().equals(Constants.NULL_STRING))
            doc.append("studios", 1);
        if (anime.getSynopsis() != null && anime.getSynopsis().equals(Constants.NULL_STRING))
            doc.append("synopsis", 1);
        if (anime.getTags() != null && anime.getTags().equals(Constants.NULL_LIST))
            doc.append("tags", 1);
        if (anime.getRelatedAnime() != null && anime.getRelatedAnime().equals(Constants.NULL_LIST))
            doc.append("relations", 1);
        if (anime.getSeason() != null && anime.getSeason().equals(Constants.NULL_STRING))
            doc.append("anime_season", 1);
        if (anime.getYear() != null && anime.getYear().equals(Constants.NULL_INT))
            doc.append("anime_season", 1);
        return doc;
    }

    public static Document mangaToUnsetMangaFieldsDocument(Manga manga) {
        Document doc = new Document();
        if (manga.getStatus() != null && manga.getStatus().equals(MangaStatus.UNKNOWN))
            doc.append("status", 1);
        if (manga.getType() != null && manga.getType().equals(MangaType.UNKNOWN))
            doc.append("type", 1);
        if (manga.getThemes() != null && manga.getThemes().equals(Constants.NULL_LIST))
            doc.append("themes", 1);
        if (manga.getGenres() != null && manga.getGenres().equals(Constants.NULL_LIST))
            doc.append("genres", 1);
        if (manga.getImageUrl() != null && manga.getImageUrl().equals(Constants.NULL_STRING))
            doc.append("picture", 1);
        if (manga.getDemographics() != null && manga.getDemographics().contains(MangaDemographics.UNKNOWN))
            doc.append("demographics", 1);
        if (manga.getSerializations() != null && manga.getSerializations().equals(Constants.NULL_STRING))
            doc.append("serializations", 1);
        if (manga.getBackground() != null && manga.getBackground().equals(Constants.NULL_STRING))
            doc.append("background", 1);
        if (manga.getTitleEnglish() != null && manga.getTitleEnglish().equals(Constants.NULL_STRING))
            doc.append("title_english", 1);
        if (manga.getTitleJapanese() != null && manga.getTitleJapanese().equals(Constants.NULL_STRING))
            doc.append("title_japanese", 1);
        if (manga.getStartDate() != null && manga.getStartDate().equals(Constants.NULL_DATE))
            doc.append("start_date", 1);
        if (manga.getEndDate() != null && manga.getEndDate().equals(Constants.NULL_DATE))
            doc.append("end_date", 1);
        if (manga.getVolumes() != null && manga.getVolumes().equals(Constants.NULL_INT))
            doc.append("volumes", 1);
        if (manga.getChapters() != null && manga.getChapters().equals(Constants.NULL_INT))
            doc.append("chapters", 1);
        if (manga.getAverageRating() != null && manga.getAverageRating().equals(Constants.NULL_DOUBLE))
            doc.append("average_rating", 1);
        if (manga.getAuthors() != null && manga.getAuthors().equals(Constants.NULL_LIST_AUTHOR))
            doc.append("authors", 1);
        return doc;
    }
}
