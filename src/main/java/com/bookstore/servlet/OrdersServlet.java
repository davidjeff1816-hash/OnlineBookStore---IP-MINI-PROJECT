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

@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {

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

                <title>My Orders - BookNest</title>

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

                    .box {
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

                    .status {
                        color: green;
                        font-weight: bold;
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

                <div class="box">

                    <h2>My Orders</h2>

                    <table>

                        <tr>
                            <th>Order ID</th>
                            <th>Total Amount</th>
                            <th>Date</th>
                            <th>Status</th>
                        </tr>
            """);

        String sql =
                "SELECT id, total_amount, order_date, status " +
                "FROM orders " +
                "WHERE user_id = ? " +
                "ORDER BY order_date DESC";

        boolean hasOrders = false;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                hasOrders = true;

                int orderId =
                        resultSet.getInt("id");

                double total =
                        resultSet.getDouble("total_amount");

                String date =
                        resultSet.getString("order_date");

                String status =
                        resultSet.getString("status");

                response.getWriter().println(
                    "<tr>" +

                    "<td>#" +
                    orderId +
                    "</td>" +

                    "<td>₹" +
                    String.format("%.2f", total) +
                    "</td>" +

                    "<td>" +
                    date +
                    "</td>" +

                    "<td class='status'>" +
                    escapeHtml(status) +
                    "</td>" +

                    "</tr>"
                );
            }

        } catch (Exception e) {

            response.getWriter().println(
                "<tr><td colspan='4'>" +
                "Unable to load orders: " +
                escapeHtml(e.getMessage()) +
                "</td></tr>"
            );
        }

        response.getWriter().println("</table>");

        if (!hasOrders) {

            response.getWriter().println("""
                <div class="empty">
                    <p>You haven't placed any orders yet.</p>

                    <a href="books">
                        Browse Books
                    </a>
                </div>
                """);
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