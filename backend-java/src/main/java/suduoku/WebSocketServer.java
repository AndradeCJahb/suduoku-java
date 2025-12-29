package suduoku;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import lombok.extern.log4j.Log4j2;

import static suduoku.Constants.DB_URL;

@Log4j2
@ServerEndpoint("/ws")
public class WebSocketServer {
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private static final Map<Integer, Board> boards = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket connection opened: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String requestType = jsonMessage.getString("type");
            
            log.debug("Received message type '{}' from session {}", requestType, session.getId());

            switch (requestType) {
                case "fetchPuzzles":
                    handleFetchPuzzles(session);
                    break;
                case "fetchIdentity":
                    handleIdentity(session, jsonMessage);
                    break;
                case "fetchPuzzle":
                    handleFetchPuzzle(jsonMessage);
                    break;
                case "fetchChat":
                    handleFetchChat(session, jsonMessage);
                    break;
                case "sendChat":
                    handleSendChat(jsonMessage);
                    break;
                case "sendPlayerPosition":
                    handleSendPlayerPosition(session, jsonMessage);
                    break;
                case "sendCellChange":
                    handleSendCellChange(jsonMessage);
                    break;
                case "sendClearBoard":
                    handleSendClearBoard(jsonMessage);
                    break;
                case "sendCheckSolution":
                    handleSendCheckSolution(jsonMessage);
                    break;
                case "sendLeaveRoom":
                    handleSendLeaveRoom(jsonMessage);
                    break;
                case "sendIncorrectCellsUpdate":
                    handleSendIncorrectCellsChange(jsonMessage);
                    break;
                case "sendElapsedTime":
                    handleSendElapsedTime(jsonMessage);
                    break;
                default:
                    log.warn("Unknown request type received: {}", requestType);
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", message, e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket connection closed: {}", session.getId());
        for(Player currPlayer : players.values()) {
            if (currPlayer.getSession().equals(session)) {
                int puzzleId = currPlayer.getCurrentPuzzleId();
                currPlayer.setCurrentPuzzleId(-1);
                currPlayer.setSelectedCol(-1);
                currPlayer.setSelectedRow(-1);

                if (puzzleId != -1) {
                    broadcastPlayerPosition(puzzleId);
                    broadcastPlayers(puzzleId);
                }

                break;
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Error on WebSocket session {}: {}", session.getId(), throwable.getMessage(), throwable);
    }

    private void handleFetchChat(Session session, JSONObject jsonMessage) {
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

    private void handleFetchPuzzles(Session session) {
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

    private void handleIdentity (Session session, JSONObject jsonMessage) {
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

    private void handleFetchPuzzle(JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        int puzzleId = jsonMessage.getInt("puzzleId");

        players.get(clientId).setCurrentPuzzleId(puzzleId);
        
        log.info("Player {} joined puzzle {}", clientId, puzzleId);

        if (!boards.containsKey(puzzleId)) {
            boards.put(puzzleId, new Board(puzzleId));
            log.info("Created new board for puzzle {}", puzzleId);
        }

        broadcastBoard(puzzleId);
        broadcastIncorrectCells(puzzleId);
        broadcastPlayerPosition(puzzleId);
        broadcastPlayers(puzzleId);
    }

    private void broadcastBoard(int puzzleId) {
        Board board = boards.get(puzzleId);
        JSONObject boardJson = board.getBoardJSON();

        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();

            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                try {
                    currentSession.getBasicRemote().sendText(boardJson.toString());
                } catch (IOException e) {
                    log.error("Error broadcasting board to session {}: {}", currentSession.getId(), e.getMessage(), e);
                }
            }
        }
    }

    private void handleSendChat(JSONObject jsonMessage) {
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

    private void broadcastChat(int puzzleId) {
        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();

            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                JSONObject JSONPuzzleId = new JSONObject();
                JSONPuzzleId.put("puzzleId", puzzleId);
                handleFetchChat(currentSession, JSONPuzzleId);
            }
        }
    }

    private void broadcastPlayers(int puzzleId) {
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

    private List<Player> getPlayersInPuzzle(int puzzleId) {
        List<Player> playersInPuzzle = new ArrayList<>();
        for(Player currentPlayer : players.values()) {
            if (currentPlayer.getCurrentPuzzleId() == puzzleId) {
                playersInPuzzle.add(currentPlayer);
            }
        }
        return playersInPuzzle;
    }

    private void handleSendPlayerPosition(Session session, JSONObject jsonMessage) {
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
        broadcastPlayerPosition(puzzleId);
    }

    private void broadcastPlayerPosition (int puzzleId) {
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

    private void handleSendCellChange(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        int row = jsonMessage.getInt("row");
        int col = jsonMessage.getInt("col");
        int value = jsonMessage.getInt("value");

        log.debug("Cell change: puzzle={}, row={}, col={}, value={}", puzzleId, row, col, value);

        Board board = boards.get(puzzleId);
        board.setCell(row, col, value);
        broadcastBoard(puzzleId);
        if (board.isSolved()) {
            log.info("Puzzle {} has been solved!", puzzleId);
            broadcastSolvedBoard(puzzleId);
        }
    }

    private void handleSendClearBoard(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        Board board = boards.get(puzzleId);
        board.clearBoard();
        board.clearIncorrectCells();

        broadcastIncorrectCells(puzzleId);
        broadcastBoard(puzzleId);
    }

    private void handleSendCheckSolution(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        Board board = boards.get(puzzleId);
        board.updateIncorrectCells();
        broadcastIncorrectCells(puzzleId);
    }

    private void broadcastIncorrectCells(int puzzleId) {
        JSONArray incorrectCellsJson = new JSONArray();
        ArrayList<int[]> incorrectCells = boards.get(puzzleId).getIncorrectCells();
        for (int[] cell : incorrectCells) {
            JSONObject cellJson = new JSONObject();
            cellJson.put("row", cell[0]);
            cellJson.put("col", cell[1]);
            incorrectCellsJson.put(cellJson);
        }

        JSONObject response = new JSONObject();
        response.put("type", "updateIncorrectCells");
        response.put("incorrectCells", incorrectCellsJson);
        response.put("puzzleId", puzzleId);

        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();
            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                try {
                    currentSession.getBasicRemote().sendText(response.toString());
                } catch (IOException e) {
                    log.error("Error broadcasting incorrect cells to session {}: {}", currentSession.getId(), e.getMessage(), e);
                }
            }
        }
    }

    private void broadcastSolvedBoard(int puzzleId) {
        JSONObject response = new JSONObject();
        response.put("type", "updatePuzzleSolved");
        for (Player currentPlayer : players.values()) {
            Session currentSession = currentPlayer.getSession();
            if (currentSession.isOpen() && currentPlayer.getCurrentPuzzleId() == puzzleId) {
                try {
                    currentSession.getBasicRemote().sendText(response.toString());
                } catch (IOException e) {
                    log.error("Error sending updatePuzzleSolved to session {}: {}", currentSession.getId(), e.getMessage(), e);
                }
            }
        }
    }

    private void handleSendLeaveRoom(JSONObject jsonMessage) {
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
            broadcastPlayerPosition(puzzleId);
            broadcastPlayers(puzzleId);
        }
    }

    private void handleSendIncorrectCellsChange(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");

        Board board = boards.get(puzzleId);
        int row = jsonMessage.getInt("row");
        int col = jsonMessage.getInt("col");

        board.removeIncorrectCell(new int[]{row, col});
        broadcastIncorrectCells(puzzleId);
    }

    private void handleSendElapsedTime(JSONObject jsonMessage) {
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

