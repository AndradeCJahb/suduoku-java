package suduoku.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.websocket.Session;
import lombok.extern.log4j.Log4j2;
import suduoku.Player;

/**
 * Handles player-related WebSocket operations (identity, positions, player list)
 */
@Log4j2
public class PlayerHandler {
    private final Map<UUID, Player> players;

    public PlayerHandler(Map<UUID, Player> players) {
        this.players = players;
    }

    public void handleIdentity(Session session, JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));

        if (!players.containsKey(clientId)) {
            players.put(clientId, new Player(session));
        } else {
            players.get(clientId).setSession(session);
        }

        Player clientPlayer = players.get(clientId);
        String clientName = clientPlayer.getName();
        String clientColor = clientPlayer.getColor();

        JSONObject clientInfo = new JSONObject();
        clientInfo.put("name", clientName);
        clientInfo.put("color", clientColor);

        JSONObject response = new JSONObject();
        response.put("type", "updateIdentity");
        response.put("client", clientInfo);

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            log.error("Error sending identity response for session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    public void handlePlayerPosition(Session session, JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        Player player = players.get(clientId);

        if (player == null) {
            handleIdentity(session, jsonMessage);
            player = players.get(clientId);
        }

        JSONObject position = jsonMessage.getJSONObject("position");
        int row = position.getInt("row");
        int col = position.getInt("col");

        player.setSelectedRow(row);
        player.setSelectedCol(col);

        int puzzleId = player.getCurrentPuzzleId();
        broadcastPlayerPositions(puzzleId);
    }

    public void broadcastPlayerPositions(int puzzleId) {
        List<Player> playersInPuzzle = getPlayersInPuzzle(puzzleId);
        JSONArray playerPositions = new JSONArray();

        for (Player currPlayer : playersInPuzzle) {
            JSONObject playerJson = new JSONObject();
            playerJson.put("name", currPlayer.getName());
            playerJson.put("color", currPlayer.getColor());
            playerJson.put("position", new JSONObject()
                .put("row", currPlayer.getSelectedRow())
                .put("col", currPlayer.getSelectedCol())
            );
            playerPositions.put(playerJson);
        }

        JSONObject response = new JSONObject();
        response.put("type", "updatePlayerPositions");
        response.put("positions", playerPositions);

        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();

            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                try {
                    currentSession.getBasicRemote().sendText(response.toString());
                } catch (IOException e) {
                    log.error("Error broadcasting player positions to session {}: {}", currentSession.getId(), e.getMessage(), e);
                }
            }
        }
    }

    public void broadcastPlayersList(int puzzleId) {
        List<Player> playersInPuzzle = getPlayersInPuzzle(puzzleId);
        JSONArray playersArray = new JSONArray();
        for (Player player : playersInPuzzle) {
            JSONObject playerJson = new JSONObject();
            playerJson.put("name", player.getName());
            playerJson.put("color", player.getColor());
            playersArray.put(playerJson);
        }

        JSONObject response = new JSONObject();
        response.put("type", "updatePlayers");
        response.put("players", playersArray);

        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();

            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                try {
                    currentSession.getBasicRemote().sendText(response.toString());
                } catch (IOException e) {
                    log.error("Error broadcasting players to session {}: {}", currentSession.getId(), e.getMessage(), e);
                }
            }
        }
    }

    public void handleLeaveRoom(JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        Player player = players.get(clientId);
        if (player == null) {
            return;
        }
        int puzzleId = jsonMessage.getInt("puzzleId");

        log.info("Player {} left puzzle {}", clientId, puzzleId);

        player.setCurrentPuzzleId(-1);
        player.setSelectedCol(-1);
        player.setSelectedRow(-1);

        if (puzzleId != -1) {
            broadcastPlayerPositions(puzzleId);
            broadcastPlayersList(puzzleId);
        }
    }

    public List<Player> getPlayersInPuzzle(int puzzleId) {
        List<Player> playersInPuzzle = new ArrayList<>();
        for(Player currentPlayer : players.values()) {
            if (currentPlayer.getCurrentPuzzleId() == puzzleId) {
                playersInPuzzle.add(currentPlayer);
            }
        }
        return playersInPuzzle;
    }
}

