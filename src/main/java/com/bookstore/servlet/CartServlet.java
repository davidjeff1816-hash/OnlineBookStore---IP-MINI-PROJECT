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

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
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

        response.setContentType("text/html;charset=UTF-8");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>
            <head>

                <title>Shopping Cart - BookNest</title>

                <style>

                    body {
                        font-family: Arial, sans-serif;
                        background: #f5f5f5;
                        margin: 0;
                        color: #222;
                    }

                    header {
                        background: white;
                        border-bottom: 1px solid #ddd;
                        padding: 18px 10%;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }

                    .logo {
                        font-size: 22px;
                        font-weight: bold;
                    }

                    nav a {
                        margin-left: 20px;
                        text-decoration: none;
                        color: #333;
                    }

                    .container {
                        width: 80%;
                        max-width: 900px;
                        margin: 30px auto;
                    }

                    .cart-box {
                        background: white;
                        border: 1px solid #ddd;
                        padding: 25px;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                    }

                    th, td {
                        padding: 12px;
                        border-bottom: 1px solid #ddd;
                        text-align: left;
                    }

                    th {
                        background: #f5f5f5;
                    }

                    .total {
                        text-align: right;
                        font-size: 20px;
                        font-weight: bold;
                        margin-top: 20px;
                    }

                    .remove {
                        background: #c00;
                        color: white;
                        border: none;
                        padding: 7px 12px;
                        cursor: pointer;
                    }

                    .order {
                        background: #333;
                        color: white;
                        border: none;
                        padding: 11px 20px;
                        cursor: pointer;
                        margin-top: 15px;
                    }

                    .empty {
                        text-align: center;
                        padding: 30px;
                    }

                </style>

            </head>

            <body>

            <header>

                <div class="logo">
                    📚 BookNest
                </div>

                <nav>

                    <a href="books">Books</a>

                    <a href="cart">Cart</a>

                    <a href="orders">My Orders</a>

                    <a href="logout">Logout</a>

                </nav>

            </header>

            <div class="container">

                <div class="cart-box">

                    <h2>Shopping Cart</h2>
            """);

        String sql =
                "SELECT c.id, b.title, b.author, b.price, " +
                "c.quantity, (b.price * c.quantity) AS subtotal " +
                "FROM cart c " +
                "JOIN books b ON c.book_id = b.id " +
                "WHERE c.user_id = ?";

        double total = 0;
        boolean hasItems = false;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet resultSet =
                    statement.executeQuery();

            response.getWriter().println("""
                <table>

                    <tr>
                        <th>Book</th>
                        <th>Author</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Subtotal</th>
                        <th>Action</th>
                    </tr>
                """);

            while (resultSet.next()) {

                hasItems = true;

                int cartId =
                        resultSet.getInt("id");

                String title =
                        resultSet.getString("title");

                String author =
                        resultSet.getString("author");

                double price =
                        resultSet.getDouble("price");

                int quantity =
                        resultSet.getInt("quantity");

                double subtotal =
                        resultSet.getDouble("subtotal");

                total += subtotal;

                response.getWriter().println(
                    "<tr>" +

                    "<td>" + escapeHtml(title) + "</td>" +

                    "<td>" + escapeHtml(author) + "</td>" +

                    "<td>₹" +
                    String.format("%.2f", price) +
                    "</td>" +

                    "<td>" +
                    quantity +
                    "</td>" +

                    "<td>₹" +
                    String.format("%.2f", subtotal) +
                    "</td>" +

                    "<td>" +

                    "<form action='remove-from-cart' method='post'>" +

                    "<input type='hidden' " +
                    "name='cartId' value='" +
                    cartId + "'>" +

                    "<button class='remove' " +
                    "type='submit'>" +
                    "Remove" +
                    "</button>" +

                    "</form>" +

                    "</td>" +

                    "</tr>"
                );
            }

            response.getWriter().println("</table>");

        } catch (Exception e) {

            response.getWriter().println(
                "<p>Unable to load cart: " +
                escapeHtml(e.getMessage()) +
                "</p>"
            );
        }

        if (!hasItems) {

            response.getWriter().println("""
                <div class="empty">

                    <p>Your cart is empty.</p>

                    <a href="books">
                        Continue Shopping
                    </a>

                </div>
                """);

        } else {

            response.getWriter().println(
                "<div class='total'>" +
                "Total: ₹" +
                String.format("%.2f", total) +
                "</div>" +

                "<form action='place-order' method='post'>" +

                "<button class='order' type='submit'>" +
                "Place Order" +
                "</button>" +

                "</form>"
            );
        }

        response.getWriter().println("""
                </div>

            </div>

            </body>
            </html>
            """);
    }

    private static String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}