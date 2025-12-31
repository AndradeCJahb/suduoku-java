package suduoku.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.websocket.Session;
import lombok.extern.log4j.Log4j2;
import suduoku.Board.SudokuBoard;
import suduoku.Player;

/**
 * Handles board-related WebSocket operations (cell changes, candidates, board state)
 */
@Log4j2
public class BoardHandler {
    private final Map<Integer, SudokuBoard> boards;
    private final Map<UUID, Player> players;
    private final PlayerHandler playerHandler;

    public BoardHandler(Map<Integer, SudokuBoard> boards, Map<UUID, Player> players, PlayerHandler playerHandler) {
        this.boards = boards;
        this.players = players;
        this.playerHandler = playerHandler;
    }

    public void handleCellChange(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        int row = jsonMessage.getInt("row");
        int col = jsonMessage.getInt("col");
        int value = jsonMessage.getInt("value");

        log.debug("Cell change: puzzle={}, row={}, col={}, value={}", puzzleId, row, col, value);

        SudokuBoard board = boards.get(puzzleId);
        board.setCell(row, col, value);
        broadcastBoard(puzzleId);

        if (board.isSolved()) {
            log.info("Puzzle {} has been solved!", puzzleId);
            broadcastSolved(puzzleId);
        }
    }

    public void handleCandidateToggle(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        int row = jsonMessage.getInt("row");
        int col = jsonMessage.getInt("col");
        int candidate = jsonMessage.getInt("candidate");

        log.debug("Candidate toggle: puzzle={}, row={}, col={}, candidate={}", puzzleId, row, col, candidate);

        SudokuBoard board = boards.get(puzzleId);
        board.toggleCandidate(row, col, candidate);
        broadcastBoard(puzzleId);
    }

    public void handleClearBoard(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        SudokuBoard board = boards.get(puzzleId);
        board.clearBoard();
        board.clearIncorrectCells();

        broadcastIncorrectCells(puzzleId);
        broadcastBoard(puzzleId);
    }

    public void handleCheckSolution(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        SudokuBoard board = boards.get(puzzleId);
        board.updateIncorrectCells();
        broadcastIncorrectCells(puzzleId);
    }

    public void handleIncorrectCellsUpdate(JSONObject jsonMessage) {
        int puzzleId = jsonMessage.getInt("puzzleId");
        SudokuBoard board = boards.get(puzzleId);
        int row = jsonMessage.getInt("row");
        int col = jsonMessage.getInt("col");

        board.removeIncorrectCell(new int[]{row, col});
        broadcastIncorrectCells(puzzleId);
    }

    public void broadcastBoard(int puzzleId) {
        SudokuBoard board = boards.get(puzzleId);
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

    public void broadcastIncorrectCells(int puzzleId) {
        JSONArray incorrectCellsJson = new JSONArray();
        List<int[]> incorrectCells = boards.get(puzzleId).getIncorrectCells();
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

    public void broadcastSolved(int puzzleId) {
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

    public void handlePuzzleFetch(JSONObject jsonMessage) {
        String clientIdStr = jsonMessage.getString("clientId");
        int puzzleId = jsonMessage.getInt("puzzleId");

        UUID clientId = UUID.fromString(clientIdStr);
        players.get(clientId).setCurrentPuzzleId(puzzleId);

        log.info("Player {} joined puzzle {}", clientId, puzzleId);

        if (!boards.containsKey(puzzleId)) {
            boards.put(puzzleId, new SudokuBoard(puzzleId));
            log.info("Created new board for puzzle {}", puzzleId);
        }

        broadcastBoard(puzzleId);
        broadcastIncorrectCells(puzzleId);
        playerHandler.broadcastPlayerPositions(puzzleId);
        playerHandler.broadcastPlayersList(puzzleId);
    }
}

