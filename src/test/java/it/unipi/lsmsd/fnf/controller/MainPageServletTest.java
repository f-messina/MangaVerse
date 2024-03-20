package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import org.junit.Test;

public class MainPageServletTest {

    /*@Test
    public void getSuggestions() throws DAOException {
        BaseMongoDBDAO.openConnection();
        String suggestionParameter = "birthday";
        if (suggestionParameter.equals("birthday")) {
            ReviewService reviewService = ServiceLocator.getReviewService();
            UserService userService = ServiceLocator.getUserService();
            try {
                User user = userService.getUserInfoForSuggestions("6577877be68376234760585e");
                PageDTO<MediaContentDTO> suggestions = reviewService.suggestTopMediaContent(MediaContentType.MANGA, user.getBirthday().toString(), "birthday");
                for (MediaContentDTO mediaContentDTO : suggestions.getEntries()) {
                    if (mediaContentDTO instanceof MangaDTO) {
                        System.out.println((mediaContentDTO).getTitle());
                    }
                }
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
        BaseMongoDBDAO.closeConnection();
    }*/

}