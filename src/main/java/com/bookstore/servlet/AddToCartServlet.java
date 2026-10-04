package com.bookstore.servlet;

import com.bookstore.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/add-to-cart")
public class AddToCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.sendRedirect("login.html");
            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        int bookId =
                Integer.parseInt(request.getParameter("bookId"));

        int quantity =
                Integer.parseInt(request.getParameter("quantity"));

        if (quantity < 1) {
            quantity = 1;
        }

        String sql =
                "INSERT INTO cart (user_id, book_id, quantity) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "quantity = quantity + VALUES(quantity)";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, bookId);
            statement.setInt(3, quantity);

            statement.executeUpdate();

            response.sendRedirect("cart");

        } catch (Exception e) {

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h2>Unable to add book to cart</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "<a href='books'>Back to Books</a>"
            );
        }
    }
}