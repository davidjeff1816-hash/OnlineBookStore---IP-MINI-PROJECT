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

@WebServlet("/books")
public class BookServlet extends HttpServlet {

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

        response.setContentType("text/html;charset=UTF-8");

        String userName =
                (String) session.getAttribute("userName");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>
            <head>

                <title>Books - BookNest</title>

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
                        max-width: 1100px;
                        margin: 30px auto;
                    }

                    .welcome {
                        background: white;
                        border: 1px solid #ddd;
                        padding: 20px;
                        margin-bottom: 25px;
                    }

                    .books {
                        display: grid;
                        grid-template-columns:
                            repeat(3, 1fr);
                        gap: 20px;
                    }

                    .book {
                        background: white;
                        border: 1px solid #ddd;
                        padding: 20px;
                    }

                    .book h3 {
                        margin-top: 0;
                    }

                    .author {
                        color: #666;
                    }

                    .category {
                        font-size: 14px;
                        color: #777;
                    }

                    .price {
                        font-size: 20px;
                        font-weight: bold;
                        margin: 15px 0;
                    }

                    .stock {
                        color: #555;
                        font-size: 14px;
                    }

                    button {
                        background: #333;
                        color: white;
                        border: none;
                        padding: 10px 15px;
                        cursor: pointer;
                    }

                    button:hover {
                        background: #555;
                    }

                    input {
                        width: 50px;
                        padding: 8px;
                    }

                    @media(max-width: 800px) {
                        .books {
                            grid-template-columns: 1fr;
                        }
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

                <div class="welcome">

                    <h2>Welcome, USER_NAME!</h2>

                    <p>
                        Browse our available books.
                    </p>

                </div>

                <div class="books">
            """.replace("USER_NAME", escapeHtml(userName)));

        String sql =
                "SELECT id, title, author, category, " +
                "price, stock, description " +
                "FROM books ORDER BY id";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String category = resultSet.getString("category");
                double price = resultSet.getDouble("price");
                int stock = resultSet.getInt("stock");
                String description =
                        resultSet.getString("description");

                response.getWriter().println(

                    "<div class='book'>" +

                    "<h3>" +
                    escapeHtml(title) +
                    "</h3>" +

                    "<p class='author'>" +
                    "Author: " +
                    escapeHtml(author) +
                    "</p>" +

                    "<p class='category'>" +
                    "Category: " +
                    escapeHtml(category) +
                    "</p>" +

                    "<p>" +
                    escapeHtml(description) +
                    "</p>" +

                    "<div class='price'>" +
                    "₹" +
                    String.format("%.2f", price) +
                    "</div>" +

                    "<p class='stock'>" +
                    "Stock: " +
                    stock +
                    "</p>" +

                    (stock > 0

                        ? "<form action='add-to-cart' method='post'>" +

                          "<input type='hidden' " +
                          "name='bookId' value='" +
                          id + "'>" +

                          "<input type='number' " +
                          "name='quantity' value='1' " +
                          "min='1' max='" +
                          stock + "'>" +

                          "<button type='submit'>" +
                          "Add to Cart" +
                          "</button>" +

                          "</form>"

                        : "<p>Out of Stock</p>") +

                    "</div>"
                );
            }

        } catch (Exception e) {

            response.getWriter().println(
                "<p>Unable to load books: " +
                escapeHtml(e.getMessage()) +
                "</p>"
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