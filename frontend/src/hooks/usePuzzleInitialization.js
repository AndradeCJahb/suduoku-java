import { useEffect } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for initializing puzzle session
 * Sends initial WebSocket messages and handles cleanup on unmount
 */
export const usePuzzleInitialization = (puzzleId, clientId) => {
  useEffect(() => {
    // Send initial messages
    webSocketManager.send({ type: "fetchIdentity", clientId });
    webSocketManager.send({ type: "fetchPuzzle", clientId, puzzleId });
    webSocketManager.send({ type: "fetchChat", puzzleId });

    return () => {
      webSocketManager.send({
        type: "sendLeaveRoom",
        clientId: clientId,
        puzzleId: puzzleId,
      });
    };
  }, [puzzleId, clientId]);
};
