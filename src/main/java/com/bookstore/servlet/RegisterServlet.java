package com.bookstore.servlet;

import com.bookstore.database.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String sql =
                "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);

            statement.executeUpdate();

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Registration Successful</title>

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

                        <h2>Registration Successful</h2>

                        <p>
                            Your BookNest account has been created.
                        </p>

                        <a href="login.html">
                            Login Now
                        </a>

                    </div>

                </body>
                </html>
                """);

        } catch (Exception e) {

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h2>Registration Failed</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "<a href='register.html'>Go Back</a>"
            );
        }
    }
}