package suduoku.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.websocket.Session;
import lombok.extern.log4j.Log4j2;
import suduoku.Player;

import static suduoku.Constants.DB_URL;

/**
 * Handles chat-related WebSocket operations
 */
@Log4j2
public class ChatHandler {
    private final Map<UUID, Player> players;

    public ChatHandler(Map<UUID, Player> players) {
        this.players = players;
    }

    public void fetchChat(Session session, JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        String query = "SELECT user, color, message, time FROM chat_logs WHERE puzzle_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, puzzleId);

            try (ResultSet rs = stmt.executeQuery()) {
                JSONArray messages = new JSONArray();

                while (rs.next()) {
                    JSONObject message = new JSONObject();
                    message.put("user", rs.getString("user"));
                    message.put("color", rs.getString("color"));
                    message.put("message", rs.getString("message"));
                    message.put("time", rs.getLong("time"));
                    messages.put(message);
                }

                JSONObject response = new JSONObject();
                response.put("type", "updateChat");
                response.put("messages", messages);

                session.getBasicRemote().sendText(response.toString());
            }
        } catch (SQLException e) {
            log.error("Error fetching chat history for puzzle {}: {}", puzzleId, e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error sending chat response for session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    public void sendChat(JSONObject jsonMessage) {
        JSONObject incomingChat = jsonMessage.getJSONObject("message");
        String clientName = incomingChat.getString("user");
        String clientColor = incomingChat.getString("color");
        String chatText = incomingChat.getString("text");
        int puzzleId = incomingChat.getInt("puzzleId");

        String query = "INSERT INTO chat_logs (puzzle_id, user, color, message, time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, puzzleId);
            stmt.setString(2, clientName);
            stmt.setString(3, clientColor);
            stmt.setString(4, chatText);
            stmt.setLong(5, System.currentTimeMillis());

            stmt.executeUpdate();
            broadcastChat(puzzleId);
        } catch (SQLException e) {
            log.error("Error saving chat message for puzzle {}: {}", puzzleId, e.getMessage(), e);
        }
    }

    public void broadcastChat(int puzzleId) {
        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();

            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                JSONObject JSONPuzzleId = new JSONObject();
                JSONPuzzleId.put("puzzleId", puzzleId);
                fetchChat(currentSession, JSONPuzzleId);
            }
        }
    }
}

