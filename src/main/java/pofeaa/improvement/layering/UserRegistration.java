package pofeaa.improvement.layering;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserRegistration {
    public static void main(String[] args) throws Exception {
        // Derby組み込みDB接続
        String dbUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE users (username VARCHAR(255) PRIMARY KEY, password VARCHAR(255))"
            );
        } catch (SQLException e) {
            // すでにテーブルがある場合は無視
            if (!e.getSQLState().equals("X0Y32")) throw e;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/register", exchange -> handleExchange(exchange, conn));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8080/register");
    }

    private static void handleExchange(HttpExchange exchange, Connection conn) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String page = "<html><body>"
                    + "<h2>User Registration</h2>"
                    + "<form method='POST' action='/register'>"
                    + "Username: <input name='username'/><br/>"
                    + "Password: <input type='password' name='password'/><br/>"
                    + "<input type='submit' value='Register'/>"
                    + "</form></body></html>";
            sendResponse(exchange, page);
        } else if ("POST".equalsIgnoreCase(method)) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            String body = new BufferedReader(isr).lines().reduce("", (a, b) -> a + b);
            Map<String, String> params = new HashMap<>();
            for (String pair : body.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    params.put(key, val);
                }
            }
            String response;
            if (params.get("username") == null) {
                response = "<html><body><h3>Username and password must not be empty.</h3><a href='/register'>Back</a></body></html>";
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users(username, email) VALUES(?,?)")) {
                    ps.setString(1, params.get("username"));
                    ps.setString(2, params.get("email"));
                    ps.executeUpdate();
                    response = "<html><body><h3>Registration successful for user: " + params.get("username") + "</h3><a href='/register'>Register another</a></body></html>";
                } catch (SQLIntegrityConstraintViolationException e) {
                    response = "<html><body><h3>User '" + params.get("username") + "' already exists.</h3><a href='/register'>Back</a></body></html>";
                } catch (SQLException e) {
                    response = "<html><body><h3>Database error: " + e.getMessage() + "</h3></body></html>";
                }
            }
            sendResponse(exchange, response);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private static void sendResponse(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
