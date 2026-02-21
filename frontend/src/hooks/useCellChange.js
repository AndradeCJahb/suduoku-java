import { useCallback } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for handling cell value changes
 * Manages normal mode cell updates
 */
export const useCellChange = (puzzleId, elapsedTimeRef) => {
  const handleCellChange = useCallback(
    (row, col, value) => {
      webSocketManager.send({
        type: "sendIncorrectCellsUpdate",
        puzzleId: puzzleId,
        row: row,
        col: col,
      });

      webSocketManager.send({
        type: "sendCellChange",
        puzzleId: puzzleId,
        row: row,
        col: col,
        value: value || 0,
      });

      webSocketManager.send({
        type: "sendElapsedTime",
        puzzleId: puzzleId,
        elapsedTime: elapsedTimeRef.current,
      });
    },
    [puzzleId, elapsedTimeRef],
  );

  return { handleCellChange };
};
