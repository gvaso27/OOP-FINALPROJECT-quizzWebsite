package servlets;

import database.DatabaseInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DatabaseInitServlet", urlPatterns = {"/init-db"}, loadOnStartup = 1)
public class DatabaseInitServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("Initializing database...");
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize database", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            DatabaseInitializer.initializeDatabase();
            response.getWriter().println("Database initialized successfully!");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Failed to initialize database: " + e.getMessage());
        }
    }
}