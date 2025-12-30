package suduoku.handlers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import jakarta.websocket.Session;
import lombok.extern.log4j.Log4j2;
import suduoku.Player;

/**
 * Handles time-related WebSocket operations
 */
@Log4j2
public class TimeHandler {
    private final Map<UUID, Player> players;

    public TimeHandler(Map<UUID, Player> players) {
        this.players = players;
    }

    public void handleElapsedTime(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        long elapsedTime = jsonMessage.getLong("elapsedTime");

        JSONObject response = new JSONObject();
        response.put("type", "updateElapsedTime");
        response.put("elapsedTime", elapsedTime);
        response.put("puzzleId", puzzleId);

        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();
            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                try {
                    currentSession.getBasicRemote().sendText(response.toString());
                } catch (IOException e) {
                    log.error("Error sending updateElapsedTime to session {}: {}", currentSession.getId(), e.getMessage(), e);
                }
            }
        }
    }
}

