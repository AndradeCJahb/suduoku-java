import { useEffect } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for managing player position broadcasts
 * Sends player focus position to other players whenever it changes
 */
export const usePlayerPosition = (focusedCell, clientId, puzzleId) => {
  useEffect(() => {
    webSocketManager.send({
      type: "sendPlayerPosition",
      position: { row: focusedCell.row, col: focusedCell.col },
      clientId: clientId,
      puzzleId: puzzleId,
    });
  }, [focusedCell, clientId, puzzleId]);
};
