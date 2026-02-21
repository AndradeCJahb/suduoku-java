import { useCallback } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for handling candidate toggles
 * Manages candidate mode operations, including overwriting values
 */
export const useCandidateToggle = (puzzleId) => {
  const handleCandidateToggle = useCallback(
    (row, col, candidate) => {
      // Send updates to server
      webSocketManager.send({
        type: "sendCellChange",
        puzzleId: puzzleId,
        row: row,
        col: col,
        value: 0,
      });

      webSocketManager.send({
        type: "sendCandidateToggle",
        puzzleId: puzzleId,
        row: row,
        col: col,
        candidate: candidate,
      });
    },
    [puzzleId],
  );

  return { handleCandidateToggle };
};
