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

@ServerEndpoint("/ws")
public class WebSocketServer {
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private static final Map<Integer, Board> boards = new ConcurrentHashMap<>();
    private static final String DB_URL = "jdbc:sqlite:sudokugames.db";

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connection opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String requestType = jsonMessage.getString("type");
            
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
                    handleSendPlayerPosition(jsonMessage);
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
                default:
                    System.out.println("Unknown request type: " + requestType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Connection closed: " + session.getId());
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
        System.err.println("Error on session " + session.getId() + ": " + throwable.getMessage());
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
            System.err.println("Error fetching chat history: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error sending chat response: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error fetching puzzles: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error sending puzzles response: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error sending identity response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleFetchPuzzle(JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        int puzzleId = jsonMessage.getInt("puzzleId");

        players.get(clientId).setCurrentPuzzleId(puzzleId);
        
        if (!boards.containsKey(puzzleId)) {
            boards.put(puzzleId, new Board(puzzleId));
        }

        broadcastBoard(puzzleId);
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
                    System.err.println("Error broadcasting board: " + e.getMessage());
                    e.printStackTrace();
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
            System.err.println("Error saving chat message: " + e.getMessage());
            e.printStackTrace();
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
                    System.err.println("Error broadcasting players: " + e.getMessage());
                    e.printStackTrace();
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

    private void handleSendPlayerPosition(JSONObject jsonMessage) {
        UUID clientId = UUID.fromString(jsonMessage.getString("clientId"));
        Player player = players.get(clientId);
        
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
                    System.err.println("Error broadcasting players: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
    }

    private void handleSendCellChange(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        int row = jsonMessage.getInt("row");
        int col = jsonMessage.getInt("col");
        int value = jsonMessage.getInt("value");

        Board board = boards.get(puzzleId);
        board.setCell(row, col, value);
        broadcastBoard(puzzleId);
        if (board.isSolved()) {
            broadcastSolvedBoard(puzzleId);
        }
    }

    private void handleSendClearBoard(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        Board board = boards.get(puzzleId);
        board.clearBoard();

        broadcastIncorrectCells(new ArrayList<int[]>(), puzzleId);
        broadcastBoard(puzzleId);
    }

    private void handleSendCheckSolution(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        Board board = boards.get(puzzleId);
        broadcastIncorrectCells(board.getIncorrectCells(), puzzleId);
    }

    private void broadcastIncorrectCells(List<int[]> incorrectCells, int puzzleId) {
        JSONArray incorrectCellsJson = new JSONArray();
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
                    System.err.println("Error broadcasting incorrect cells: " + e.getMessage());
                    e.printStackTrace();
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
                    System.err.println("Error sending updatePuzzleSolved" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}

