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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String sql =
                "SELECT id, name FROM users " +
                "WHERE email = ? AND password = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                int userId = resultSet.getInt("id");
                String name = resultSet.getString("name");

                HttpSession session = request.getSession();

                session.setAttribute("userId", userId);
                session.setAttribute("userName", name);
                session.setAttribute("userEmail", email);

                response.sendRedirect("books");

            } else {

                response.setContentType("text/html;charset=UTF-8");

                response.getWriter().println("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Login Failed</title>

                        <style>
                            body {
                                font-family: Arial;
                                background: #f5f5f5;
                                text-align: center;
                                padding-top: 100px;
                            }

                            .box {
                                background: white;
                                border: 1px solid #ddd;
                                width: 400px;
                                margin: auto;
                                padding: 30px;
                            }

                            .error {
                                color: #c00;
                            }

                            a {
                                display: inline-block;
                                margin-top: 20px;
                                background: #333;
                                color: white;
                                padding: 10px 20px;
                                text-decoration: none;
                            }
                        </style>
                    </head>

                    <body>

                        <div class="box">

                            <h2 class="error">
                                Login Failed
                            </h2>

                            <p>
                                Invalid email or password.
                            </p>

                            <a href="login.html">
                                Try Again
                            </a>

                        </div>

                    </body>
                    </html>
                    """);
            }

        } catch (Exception e) {

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h2>Login Error</h2>" +
                    "<p>" + e.getMessage() + "</p>"
            );
        }
    }
}