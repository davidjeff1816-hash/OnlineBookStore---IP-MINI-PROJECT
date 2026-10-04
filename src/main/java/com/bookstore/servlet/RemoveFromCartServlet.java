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

@WebServlet("/remove-from-cart")
public class RemoveFromCartServlet extends HttpServlet {

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

        int cartId =
                Integer.parseInt(
                        request.getParameter("cartId")
                );

        String sql =
                "DELETE FROM cart " +
                "WHERE id = ? AND user_id = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, cartId);
            statement.setInt(2, userId);

            statement.executeUpdate();

            response.sendRedirect("cart");

        } catch (Exception e) {

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h2>Unable to remove item</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "<a href='cart'>Back to Cart</a>"
            );
        }
    }
}