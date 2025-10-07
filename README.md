# MangaVerse

<div align="center">

A comprehensive web platform for exploring, discovering, and interacting with manga and anime content, while fostering community engagement among enthusiasts.

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-4.11.1-green.svg)](https://www.mongodb.com/)
[![Neo4j](https://img.shields.io/badge/Neo4j-5.17.0-blue.svg)](https://neo4j.com/)

</div>

---

## 📋 Table of Contents

- [About the Project](#about-the-project)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [User Roles](#user-roles)
- [Contributors](#contributors)
- [License](#license)
- [Acknowledgments](#acknowledgments)

---

## 🎯 About the Project

**MangaVerse** is a web application developed for the Large-scale and Multi-structured Databases course at the University of Pisa. This platform aims to create a comprehensive environment for exploring a vast collection of **manga** and **anime**, while fostering interaction among users.

The website is accessible without requiring login, offering limited functionalities. Once users log in, they gain access to a wide range of features designed to personalize their experience, particularly through social interactions and personalized recommendations.

> **Note**: This application is not an active production service, but rather a university project that works with local databases. It can be extended by integrating remote-access databases, but in its current form, it does not include synchronization mechanisms for manga or anime data, nor does it provide a complete database of titles. Therefore, it should be seen as a foundation or starting point for building such an application.

---

## ✨ Key Features

### 📚 Comprehensive Media Database
- Vast collection of manga and anime entries
- Detailed information, ratings, and reviews
- Complete metadata including genres, authors, and publication dates

### 🔍 Search and Filter Functionality
- Advanced search capabilities for titles, genres, and authors
- Powerful filtering options to refine search results
- Browse trending and popular content

### 👤 User Profiles and Social Features
- Create personalized user profiles
- Track activity and engagement
- Follow other users with similar interests
- Like and review media content
- Share opinions and connect with the community

### 🎯 Intelligent Recommendations
- Tailored content suggestions based on user interactions
- Personalized recommendations using user preferences
- Discover new manga, anime, and users

### 📊 Analytics Dashboard (Manager Role)
- Comprehensive user analytics and distribution
- Media content trends and ratings analysis
- Monitor platform engagement and activity
- Add, update, or remove media content entries

### 🔐 Content Management
- Efficient media content management
- User account administration
- Platform moderation capabilities

---

## 🛠️ Technology Stack

### Backend
- **Java 21** - Core programming language
- **Jakarta EE 10** - Enterprise application framework
- **Apache Tomcat** - Web server and servlet container
- **Maven** - Build automation and dependency management

### Databases
- **MongoDB 4.11.1** - Document database for media content and user data
- **Neo4j 5.17.0** - Graph database for relationships (likes, follows, reviews)

### Frontend
- **JSP (JavaServer Pages)** - Dynamic web pages
- **JavaScript** - Client-side interactivity
- **CSS** - Styling and layout
- **Chart.js** - Analytics visualizations

### Testing
- **JUnit** - Unit testing framework

### Additional Libraries
- **Gson** - JSON processing
- **Jackson** - Data binding and serialization
- **Apache Commons** - Utility libraries
- **SLF4J** - Logging facade

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java Development Kit (JDK) 21** or higher
  - [Download JDK](https://openjdk.org/)
  - Verify installation: `java -version`

- **Apache Maven 3.6+**
  - [Download Maven](https://maven.apache.org/download.cgi)
  - Verify installation: `mvn -version`

- **Apache Tomcat 10.1+**
  - [Download Tomcat](https://tomcat.apache.org/download-10.cgi)
  - Compatible with Jakarta EE 10

- **MongoDB 4.11+**
  - [Download MongoDB](https://www.mongodb.com/try/download/community)
  - Verify installation: `mongod --version`

- **Neo4j 5.17+**
  - [Download Neo4j](https://neo4j.com/download/)
  - Verify installation: `neo4j version`

---

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/f-messina/MangaVerse.git
cd MangaVerse
```

### 2. Install Dependencies

```bash
mvn clean install
```

This command will:
- Download all required dependencies
- Compile the project
- Package the application as a WAR file

---

## ⚙️ Configuration

### Database Configuration

#### MongoDB Setup

1. **Start MongoDB** (default configuration expects MongoDB replica set on ports 27018, 27019, 27020):

```bash
# Start MongoDB instances for replica set
mongod --port 27018 --dbpath /data/db1 --replSet rs0
mongod --port 27019 --dbpath /data/db2 --replSet rs0
mongod --port 27020 --dbpath /data/db3 --replSet rs0
```

2. **Initialize Replica Set**:

```bash
mongosh --port 27018
```

```javascript
rs.initiate({
  _id: "rs0",
  members: [
    { _id: 0, host: "localhost:27018" },
    { _id: 1, host: "localhost:27019" },
    { _id: 2, host: "localhost:27020" }
  ]
})
```

3. **Create Database**:

```javascript
use mangaVerse
```

> **Note**: The database configuration is located in `src/main/java/it/unipi/lsmsd/fnf/dao/mongo/BaseMongoDBDAO.java`. You can modify connection settings there if needed.

#### Neo4j Setup

1. **Start Neo4j** (default configuration expects Neo4j on default port 7687):

```bash
neo4j start
```

2. **Access Neo4j Browser** at `http://localhost:7474`

3. **Set Initial Password** (default username: `neo4j`)

> **Note**: Neo4j configuration is located in `src/main/java/it/unipi/lsmsd/fnf/dao/neo4j/BaseNeo4JDAO.java`. Update credentials and connection settings there if needed.

### Application Configuration

The main configuration is handled by `AppServletContextListener`, which manages:
- Database connections
- TaskManager for asynchronous operations
- PeriodicExecutorTaskService for scheduled tasks

---

## 🏃 Running the Application

### Option 1: Deploy to Tomcat

1. **Build the WAR file**:

```bash
mvn clean package
```

2. **Deploy to Tomcat**:

Copy the generated WAR file from `target/MangaVerse-1.0-SNAPSHOT.war` to your Tomcat `webapps` directory:

```bash
cp target/MangaVerse-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/
```

3. **Start Tomcat**:

```bash
$TOMCAT_HOME/bin/startup.sh  # On Unix/Linux/Mac
$TOMCAT_HOME/bin/startup.bat  # On Windows
```

4. **Access the Application**:

Open your browser and navigate to:
```
http://localhost:8080/MangaVerse-1.0-SNAPSHOT/
```

### Option 2: Using Maven Tomcat Plugin

Add the following plugin to your `pom.xml` (if not already present) and run:

```bash
mvn tomcat7:run
```

---

## 📁 Project Structure

```
MangaVerse/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── it/unipi/lsmsd/fnf/
│   │   │       ├── configuration/    # Application configuration
│   │   │       ├── controller/       # Servlets and controllers
│   │   │       ├── dao/              # Data Access Objects
│   │   │       │   ├── mongo/        # MongoDB implementations
│   │   │       │   └── neo4j/        # Neo4j implementations
│   │   │       ├── dto/              # Data Transfer Objects
│   │   │       ├── model/            # Domain models
│   │   │       ├── service/          # Business logic layer
│   │   │       └── utils/            # Utility classes
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── jsp/              # JavaServer Pages
│   │       │   └── web.xml           # Web application descriptor
│   │       ├── css/                  # Stylesheets
│   │       ├── js/                   # JavaScript files
│   │       └── images/               # Static images
│   └── test/
│       └── java/                     # Unit tests
├── documentation/                    # Project documentation
│   ├── chapters/                     # Documentation chapters
│   └── Media/                        # Documentation images
├── pom.xml                          # Maven configuration
├── LICENSE                          # MIT License
└── README.md                        # This file
```

---

## 👥 User Roles

### 🔓 Unregistered User

- Browse manga and anime collections
- Search and filter media content
- View media content details and trends
- View media reviews
- Register for an account

### 👤 Registered User

All unregistered user features, plus:

- **Profile Management**: Edit personal information, profile picture, and bio
- **Social Interactions**: 
  - Like/unlike manga and anime
  - Follow/unfollow other users
  - Explore other user profiles
- **Reviews**: Add, edit, and delete reviews with ratings
- **Personalized Recommendations**: 
  - Get media content suggestions based on preferences
  - Discover similar users with common interests

### 👔 Manager

All registered user features, plus:

- **Analytics Dashboard**:
  - View user distribution and app ratings
  - Monitor manga and anime trends
  - Analyze average ratings and engagement
- **Content Management**:
  - Add new manga and anime entries
  - Update existing media content
  - Remove media content entries
- **User Management**: Monitor and manage user accounts

---

## 👨‍💻 Contributors

- **Flavio Messina** ([@f-messina](https://github.com/f-messina)) - Project Author and Developer

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Flavio Messina

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

- **University of Pisa** - Large-scale and Multi-structured Databases Course
- **Data Sources**: [MyAnimeList](https://myanimelist.net/), [AniList](https://anilist.co/), [Kitsu](https://kitsu.io/)
- **Chart.js** - For analytics visualizations
- **MongoDB & Neo4j** - Database technologies
- All contributors and maintainers

---

<div align="center">

**[⬆ Back to Top](#mangaverse)**

Made with ❤️ for manga and anime enthusiasts

</div>
