package suduoku.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.websocket.Session;
import lombok.extern.log4j.Log4j2;

import static suduoku.Constants.DB_URL;

/**
 * Handles puzzle-related WebSocket operations
 */
@Log4j2
public class PuzzleHandler {

    public void fetchPuzzles(Session session) {
        String query = "SELECT id, title, difficulty, status FROM puzzles ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {

            JSONArray puzzles = new JSONArray();

            while (rs.next()) {
                JSONObject puzzle = new JSONObject();
                puzzle.put("id", rs.getInt("id"));
                puzzle.put("title", rs.getString("title"));
                puzzle.put("difficulty", rs.getString("difficulty"));
                puzzle.put("status", rs.getString("status"));
                puzzles.put(puzzle);
            }

            JSONObject response = new JSONObject();
            response.put("type", "puzzles");
            response.put("puzzles", puzzles);
            session.getBasicRemote().sendText(response.toString());
        } catch (SQLException e) {
            log.error("Error fetching puzzles: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error sending puzzles response for session {}: {}", session.getId(), e.getMessage(), e);
        }
    }
}

