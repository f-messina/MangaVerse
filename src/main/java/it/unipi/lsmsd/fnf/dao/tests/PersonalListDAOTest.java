package it.unipi.lsmsd.fnf.dao.tests;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.PersonalListDAOImpl;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

public class PersonalListDAOTest {

    public static void main(String[] args) {
        PersonalListDAOImpl personalListDAO = new PersonalListDAOImpl();

        // Test insert method
        PersonalListDTO listToInsert = createSampleList();
        ObjectId id = testInsert(personalListDAO, listToInsert);
        listToInsert.setId(id);

        // Test update method
        PersonalListDTO updatedList = createUpdatedList(listToInsert);
        testUpdate(personalListDAO, updatedList);

        // Test addToList method
        AnimeDTO animeToAdd = createSampleAnime();
        testAddToList(personalListDAO, updatedList.getId(), animeToAdd);

        // Test removeFromList method
        testRemoveFromList(personalListDAO, updatedList.getId(), animeToAdd.getId(), MediaContentType.ANIME);

        // Test updateItem method
        AnimeDTO updatedAnime = createUpdatedAnime(animeToAdd);
        testUpdateItem(personalListDAO, updatedAnime);

        // Test removeItem method
        testRemoveItem(personalListDAO, updatedAnime.getId());

        // Test delete method
        testDelete(personalListDAO, updatedList.getId());

        // Test deleteByUser method
        testDeleteByUser(personalListDAO, new ObjectId("65b01ff258ac67490d21d561"));
        
        // Test findByUser method
        testFindByUser(personalListDAO, new ObjectId("65b01ff258ac67490d21d561"));
    }

    private static ObjectId testInsert(PersonalListDAOImpl personalListDAO, PersonalListDTO list) {
        try {
            System.out.println("Inserting personal list: " + list.getName());
            ObjectId id = personalListDAO.insert(list);
            System.out.println("Personal list inserted successfully!");
            return id;
        } catch (DAOException e) {
            System.err.println("Error inserting personal list: " + e.getMessage());
            return null;
        }
    }

    private static void testUpdate(PersonalListDAOImpl personalListDAO, PersonalListDTO list) {
        try {
            System.out.println("Updating personal list: " + list.getName());
            personalListDAO.update(list);
            System.out.println("Personal list updated successfully!");
        } catch (DAOException e) {
            System.err.println("Error updating personal list: " + e.getMessage());
        }
    }

    private static void testAddToList(PersonalListDAOImpl personalListDAO, ObjectId listId, MediaContentDTO item) {
        try {
            System.out.println("Adding item to personal list: " + item.getTitle());
            personalListDAO.addToList(listId, item);
            System.out.println("Item added to personal list successfully!");
        } catch (DAOException e) {
            System.err.println("Error adding item to personal list: " + e.getMessage());
        }
    }

    private static void testRemoveFromList(PersonalListDAOImpl personalListDAO, ObjectId listId, ObjectId itemId, MediaContentType type) {
        try {
            System.out.println("Removing item from personal list with ID: " + itemId);
            personalListDAO.removeFromList(listId, itemId, type);
            System.out.println("Item removed from personal list successfully!");
        } catch (DAOException e) {
            System.err.println("Error removing item from personal list: " + e.getMessage());
        }
    }

    private static void testUpdateItem(PersonalListDAOImpl personalListDAO, AnimeDTO anime) {
        try {
            System.out.println("Updating item in personal list: " + anime.getTitle());
            personalListDAO.updateItem(anime);
            System.out.println("Item in personal list updated successfully!");
        } catch (DAOException e) {
            System.err.println("Error updating item in personal list: " + e.getMessage());
        }
    }

    private static void testRemoveItem(PersonalListDAOImpl personalListDAO, ObjectId itemId) {
        try {
            System.out.println("Removing item from all personal lists with ID: " + itemId);
            personalListDAO.removeItem(itemId);
            System.out.println("Item removed from all personal lists successfully!");
        } catch (DAOException e) {
            System.err.println("Error removing item from all personal lists: " + e.getMessage());
        }
    }

    private static void testDelete(PersonalListDAOImpl personalListDAO, ObjectId listId) {
        try {
            System.out.println("Deleting personal list with ID: " + listId);
            personalListDAO.delete(listId);
            System.out.println("Personal list deleted successfully!");
        } catch (DAOException e) {
            System.err.println("Error deleting personal list: " + e.getMessage());
        }
    }

    private static void testDeleteByUser(PersonalListDAOImpl personalListDAO, ObjectId userId) {
        try {
            System.out.println("Deleting all personal lists for user with ID: " + userId);
            personalListDAO.deleteByUser(userId);
            System.out.println("All personal lists for the user deleted successfully!");
        } catch (DAOException e) {
            System.err.println("Error deleting personal lists by user: " + e.getMessage());
        }
    }

    private static void testFindByUser(PersonalListDAOImpl personalListDAO, ObjectId userId) {
        try {
            System.out.println("Finding personal lists for user with ID: " + userId);
            List<PersonalListDTO> personalLists = personalListDAO.findByUser(userId);
            if (!personalLists.isEmpty()) {
                System.out.println("Personal lists found:");
                for (PersonalListDTO list : personalLists) {
                    System.out.println("List ID: " + list.getId() + ", Name: " + list.getName());
                }
            } else {
                System.out.println("No personal lists found for the user.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding personal lists by user: " + e.getMessage());
        }
    }

    private static void testFindAll(PersonalListDAOImpl personalListDAO) {
        try {
            System.out.println("Finding all personal lists...");
            List<PersonalListDTO> personalLists = personalListDAO.findAll();
            if (!personalLists.isEmpty()) {
                System.out.println("All personal lists:");
                for (PersonalListDTO list : personalLists) {
                    System.out.println("List ID: " + list.getId() + ", Name: " + list.getName());
                }
            } else {
                System.out.println("No personal lists found.");
            }
        } catch (DAOException e) {
            System.err.println("Error finding all personal lists: " + e.getMessage());
        }
    }

    private static PersonalListDTO createSampleList() {
        RegisteredUserDTO user = createSampleUser();
        PersonalListDTO list = new PersonalListDTO();
        list.setName("MyAnimeList");
        list.setUser(user);
        return list;
    }

    private static PersonalListDTO createUpdatedList(PersonalListDTO list) {
        PersonalListDTO updatedList = new PersonalListDTO();
        updatedList.setId(list.getId());
        updatedList.setName("UpdatedList");
        updatedList.setUser(list.getUser());
        return updatedList;
    }

    private static RegisteredUserDTO createSampleUser() {
        RegisteredUserDTO user = new RegisteredUserDTO();
        user.setId(new ObjectId("65b01ff258ac67490d21d561"));
        user.setLocation("Sample City");
        user.setBirthday(LocalDate.of(1990,1,1));
        user.setAge(30);
        return user;
    }

    private static AnimeDTO createSampleAnime() {
        AnimeDTO anime = new AnimeDTO();
        anime.setId(new ObjectId("65b016e34af6e42a70fa3ca5"));
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample_anime.jpg");
        return anime;
    }

    private static AnimeDTO createUpdatedAnime(AnimeDTO anime) {
        AnimeDTO updatedAnime = new AnimeDTO();
        updatedAnime.setId(anime.getId());
        updatedAnime.setTitle("Updated Anime");
        updatedAnime.setImageUrl("updated_anime.jpg");
        return updatedAnime;
    }
}

