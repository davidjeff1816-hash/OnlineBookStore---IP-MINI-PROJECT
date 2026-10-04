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
import java.sql.ResultSet;

@WebServlet("/place-order")
public class OrderServlet extends HttpServlet {

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

        Connection connection = null;

        try {

            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            String cartSql =
                    "SELECT c.book_id, c.quantity, " +
                    "b.price, b.stock " +
                    "FROM cart c " +
                    "JOIN books b ON c.book_id = b.id " +
                    "WHERE c.user_id = ?";

            PreparedStatement cartStatement =
                    connection.prepareStatement(cartSql);

            cartStatement.setInt(1, userId);

            ResultSet cartResult =
                    cartStatement.executeQuery();

            double total = 0;
            boolean hasItems = false;

            while (cartResult.next()) {

                hasItems = true;

                int quantity =
                        cartResult.getInt("quantity");

                double price =
                        cartResult.getDouble("price");

                int stock =
                        cartResult.getInt("stock");

                if (quantity > stock) {

                    connection.rollback();

                    response.setContentType(
                            "text/html;charset=UTF-8");

                    response.getWriter().println(
                            "<h2>Order Failed</h2>" +
                            "<p>Not enough stock available.</p>" +
                            "<a href='cart'>Back to Cart</a>"
                    );

                    return;
                }

                total += price * quantity;
            }

            cartResult.close();
            cartStatement.close();

            if (!hasItems) {

                connection.rollback();

                response.sendRedirect("cart");

                return;
            }

            // Create order

            String orderSql =
                    "INSERT INTO orders " +
                    "(user_id, total_amount, status) " +
                    "VALUES (?, ?, 'Placed')";

            PreparedStatement orderStatement =
                    connection.prepareStatement(
                            orderSql,
                            java.sql.Statement.RETURN_GENERATED_KEYS
                    );

            orderStatement.setInt(1, userId);
            orderStatement.setDouble(2, total);

            orderStatement.executeUpdate();

            ResultSet generatedKeys =
                    orderStatement.getGeneratedKeys();

            generatedKeys.next();

            int orderId =
                    generatedKeys.getInt(1);

            generatedKeys.close();
            orderStatement.close();

            // Get cart items again

            cartStatement =
                    connection.prepareStatement(cartSql);

            cartStatement.setInt(1, userId);

            cartResult =
                    cartStatement.executeQuery();

            while (cartResult.next()) {

                int bookId =
                        cartResult.getInt("book_id");

                int quantity =
                        cartResult.getInt("quantity");

                double price =
                        cartResult.getDouble("price");

                // Insert order item

                String itemSql =
                        "INSERT INTO order_items " +
                        "(order_id, book_id, quantity, price) " +
                        "VALUES (?, ?, ?, ?)";

                PreparedStatement itemStatement =
                        connection.prepareStatement(itemSql);

                itemStatement.setInt(1, orderId);
                itemStatement.setInt(2, bookId);
                itemStatement.setInt(3, quantity);
                itemStatement.setDouble(4, price);

                itemStatement.executeUpdate();

                itemStatement.close();

                // Reduce stock

                String stockSql =
                        "UPDATE books " +
                        "SET stock = stock - ? " +
                        "WHERE id = ? AND stock >= ?";

                PreparedStatement stockStatement =
                        connection.prepareStatement(stockSql);

                stockStatement.setInt(1, quantity);
                stockStatement.setInt(2, bookId);
                stockStatement.setInt(3, quantity);

                int updated =
                        stockStatement.executeUpdate();

                stockStatement.close();

                if (updated == 0) {

                    connection.rollback();

                    response.setContentType(
                            "text/html;charset=UTF-8");

                    response.getWriter().println(
                            "<h2>Order Failed</h2>" +
                            "<p>Stock is no longer available.</p>" +
                            "<a href='cart'>Back to Cart</a>"
                    );

                    return;
                }
            }

            cartResult.close();
            cartStatement.close();

            // Clear cart

            String deleteCartSql =
                    "DELETE FROM cart WHERE user_id = ?";

            PreparedStatement deleteStatement =
                    connection.prepareStatement(
                            deleteCartSql
                    );

            deleteStatement.setInt(1, userId);

            deleteStatement.executeUpdate();

            deleteStatement.close();

            // Complete transaction

            connection.commit();

            response.sendRedirect("orders");

        } catch (Exception e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception ignored) {
            }

            response.setContentType(
                    "text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h2>Order Failed</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "<a href='cart'>Back to Cart</a>"
            );

        } finally {

            try {

                if (connection != null) {
                    connection.close();
                }

            } catch (Exception ignored) {
            }
        }
    }
}