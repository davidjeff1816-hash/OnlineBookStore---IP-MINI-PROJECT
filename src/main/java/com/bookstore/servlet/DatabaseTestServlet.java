package com.bookstore.servlet;

import com.bookstore.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/database-test")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            response.getWriter().println(
                    "<html>" +
                    "<head>" +
                    "<title>Database Test</title>" +
                    "<style>" +
                    "body {" +
                    "font-family: Arial;" +
                    "background: #f5f7fb;" +
                    "text-align: center;" +
                    "padding-top: 100px;" +
                    "}" +

                    ".box {" +
                    "display: inline-block;" +
                    "background: white;" +
                    "padding: 40px;" +
                    "border-radius: 15px;" +
                    "box-shadow: 0 5px 20px rgba(0,0,0,0.15);" +
                    "}" +

                    "h1 {" +
                    "color: #198754;" +
                    "}" +
                    "</style>" +
                    "</head>" +

                    "<body>" +

                    "<div class='box'>" +
                    "<h1>Database Connected Successfully!</h1>" +
                    "<p>Online Book Store database is working.</p>" +
                    "</div>" +

                    "</body>" +
                    "</html>"
            );

            connection.close();

        } catch (Exception e) {

            response.getWriter().println(
                    "<h1>Database Connection Failed</h1>" +
                    "<p>" + e.getMessage() + "</p>"
            );
        }
    }
}