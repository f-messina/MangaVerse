package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.service.MediaContentService;
import it.unipi.lsmsd.fnf.service.PersonalListService;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(urlPatterns = {"/manga", "/anime"})
public class MediaContentPageServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MediaContentPageServlet.class);
    private static final MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
    private static final PersonalListService personalListService = ServiceLocator.getPersonalListService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        MediaContentType mediaType = MediaContentType.valueOf(request.getServletPath().substring(1).toUpperCase());
        String mediaId = request.getParameter("id");
        String targetJSP = "tests/media_content_test.jsp";

        if (mediaId == null) {
            response.sendRedirect("/mainPage");
            return;
        }
        try {
            MediaContent mediaContent = mediaContentService.getMediaContentById(mediaId, mediaType);
            if (mediaContent == null) {
                request.setAttribute("error", "Media not found");
                targetJSP = "error.jsp";
                request.getRequestDispatcher(targetJSP).forward(request, response);
            }
            request.setAttribute("media", mediaContentService.getMediaContentById(mediaId, mediaType));
            request.setAttribute("lists", (SecurityUtils.getAuthenticatedUser(request).getLists()));
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            targetJSP = "error.jsp";
        }

        switch (action) {
            case "addToList" -> handleAddToList(request, response);
            case null, default -> request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }

    private void handleAddToList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaId = request.getParameter("id");
        String listId = request.getParameter("listId");
        String mediaType = request.getParameter("mediaType");
        String targetJSP = "tests/media_content_test.jsp";

        if (mediaId == null || listId == null || mediaType == null) {
            request.setAttribute("error", "Invalid request");
            targetJSP = "error.jsp";
            request.getRequestDispatcher(targetJSP).forward(request, response);
            return;
        }

        try {
            personalListService.addToList(listId, (MediaContentDTO) request.getAttribute("media"));
            request.setAttribute("success", "Media added to list");
        } catch (Exception e) {
            logger.error("Error while processing request", e);
            request.setAttribute("error", "Error while adding media to list");
            targetJSP = "error.jsp";
        }

        request.getRequestDispatcher(targetJSP).forward(request, response);
    }
}
