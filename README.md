# Manga Universe

This web application aims to provide users with a comprehensive platform to explore, search, and interact with a vast collection of manga. Users can register, personalize their profiles, and engage in a manga community. The platform also offers a set of features for registered users, such as liking manga, following other users, adding reviews, and managing their manga lists. Additionally, users receive personalized suggestions based on their preferences and current trends. The site administrator has access to detailed analytics about manga and user activities. 

Note: This application is not an active production service, but rather a university project that works with local databases. It can be extended by integrating a remote-access database, but in its current form it does not include any synchronization mechanisms for manga or anime data, nor does it provide a complete database of titles. Therefore, if you are looking for a ready-to-use application to manage your personal manga list, this repository is not the right fit. Instead, it should be seen as a foundation or starting point for building such an application.


## FUNCTIONAL REQUIREMENTS

### Unregistered User:

    - Browse Manga:
        - View a list of available manga on the home page.
        - Access basic details about each manga without logging in.

    - Search and Filter:
        - Use the search bar to find specific manga by title, genre, or author.
        - Utilize basic filtering options to refine the manga list.

    - View Manga Details:
        - Click on a manga to view detailed information, including synopsis and genre.

    - Register/Login:
        - Access a registration page to create a new account.
        - Use valid credentials to log into the account.

    - Explore Features:
        - Access information about the features available to registered users.
        - Receive prompts to register for additional benefits.

### Registered User:

    - Browse Manga:
        - View a list of available manga on the home page.
        - Access basic details about each manga without logging in.

    - Search and Filter:
        - Use the search bar to find specific manga by title, genre, or author.
        - Utilize basic filtering options to refine the manga list.

    - View Manga Details:
        - Click on a manga to view detailed information, including synopsis and genre.
        - 
    - Logout:
        - Ends the user's session.

    - Profile Management:
        - Edit and update personal information (e.g., profile picture, bio).
        - Change account password.

    - Explore Other User Profiles:
        - View profiles of other registered users.
        - See their liked manga, reviews, and lists.

### Customer (Registered User with Additional Features):
   
    - Interact with Manga/Users:
        - Like or dislike manga to indicate preferences.
        - Follow/unfollow other users for a personalized feed.

    - Review Manga:
        - Add reviews and ratings to manga titles.
        - View and edit own reviews.

    - Personal Lists:
        - Create and manage lists like "to read", "dropped" and "finished".
        - Add and remove manga from personal lists.

    - Advanced Recommendations:
        - Receive more refined manga suggestions based on detailed user interactions.
        - Opt to enable or disable personalized recommendations.

### Manager (Registered User with Administrative Features):

    - Analytics Dashboard:
        - Access a comprehensive analytics dashboard with data on user engagement, popular manga, and trends.

    - User Management:
        - View and manage user accounts, including account activation and deactivation.

    - Content Management:
        - Manage manga entries, including adding new manga, updating information, and removing entries if necessary.

    - Monitor Trends:
        - Monitor trends in user interactions, popular genres, and trending manga.

## NON FUNCTIONAL REQUIREMENTS

    - Performance:

      - Response Time: The system should have low latency, with pages loading within an acceptable timeframe.
      - Scalability: The system should be able to handle an increasing number of users and data without significant degradation in performance.
      - Concurrency: The application should support multiple users simultaneously without performance bottlenecks; For very high traffic scenarios, acceptable delays may be introduced.

    - Security:

      - Data Encryption: All user data, including passwords, should be securely encrypted during transmission and storage.

    - User Interface:

      - Responsiveness (?): The user interface should be responsive, providing a consistent and seamless experience across various devices and screen sizes.
      - Intuitiveness: The interface should be user-friendly, with clear navigation and easily understandable features.
