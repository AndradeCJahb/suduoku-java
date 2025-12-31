package suduoku;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import lombok.extern.log4j.Log4j2;
import suduoku.Board.SudokuBoard;
import suduoku.handlers.BoardHandler;
import suduoku.handlers.ChatHandler;
import suduoku.handlers.PlayerHandler;
import suduoku.handlers.PuzzleHandler;
import suduoku.handlers.TimeHandler;

/**
 * WebSocket server endpoint for Sudoku game
 * Delegates message handling to specialized handler classes
 */
@Log4j2
@ServerEndpoint("/ws")
public class WebSocketServer {
    private static final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private static final Map<Integer, SudokuBoard> boards = new ConcurrentHashMap<>();

    private final PuzzleHandler puzzleHandler = new PuzzleHandler();
    private final ChatHandler chatHandler = new ChatHandler(players);
    private final PlayerHandler playerHandler = new PlayerHandler(players);
    private final BoardHandler boardHandler = new BoardHandler(boards, players, playerHandler);
    private final TimeHandler timeHandler = new TimeHandler(players);

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

            handleMessage(requestType, jsonMessage, session);
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", message, e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket connection closed: {}", session.getId());
        handlePlayerDisconnect(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Error on WebSocket session {}: {}", session.getId(), throwable.getMessage(), throwable);
    }

    /**
     * Routes messages to appropriate handlers based on message type
     */
    private void handleMessage(String requestType, JSONObject jsonMessage, Session session) {
        switch (requestType) {
            // Puzzle operations
            case "fetchPuzzles":
                puzzleHandler.fetchPuzzles(session);
                break;
            case "fetchPuzzle":
                boardHandler.handlePuzzleFetch(jsonMessage);
                break;

            // Player operations
            case "fetchIdentity":
                playerHandler.handleIdentity(session, jsonMessage);
                break;
            case "sendPlayerPosition":
                playerHandler.handlePlayerPosition(session, jsonMessage);
                break;
            case "sendLeaveRoom":
                playerHandler.handleLeaveRoom(jsonMessage);
                break;

            // Chat operations
            case "fetchChat":
                chatHandler.fetchChat(session, jsonMessage);
                break;
            case "sendChat":
                chatHandler.sendChat(jsonMessage);
                break;
            case "sendCellChange":
                boardHandler.handleCellChange(jsonMessage);
                break;
            case "sendCandidateToggle":
                boardHandler.handleCandidateToggle(jsonMessage);
                break;
            case "sendClearBoard":
                boardHandler.handleClearBoard(jsonMessage);
                break;
            case "sendCheckSolution":
                boardHandler.handleCheckSolution(jsonMessage);
                break;
            case "sendIncorrectCellsUpdate":
                boardHandler.handleIncorrectCellsUpdate(jsonMessage);
                break;
            case "sendElapsedTime":
                timeHandler.handleElapsedTime(jsonMessage);
                break;

            default:
                log.warn("Unknown request type received: {}", requestType);
        }
    }

    /**
     * Handles player disconnection cleanup
     */
    private void handlePlayerDisconnect(Session session) {
        for(Player currPlayer : players.values()) {
            if (currPlayer.getSession().equals(session)) {
                int puzzleId = currPlayer.getCurrentPuzzleId();
                currPlayer.setCurrentPuzzleId(-1);
                currPlayer.setSelectedCol(-1);
                currPlayer.setSelectedRow(-1);

                if (puzzleId != -1) {
                    playerHandler.broadcastPlayerPositions(puzzleId);
                    playerHandler.broadcastPlayersList(puzzleId);
                }

                break;
            }
        }
    }
}

