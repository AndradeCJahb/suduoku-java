import { useCallback } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for board-level operations
 * Manages clear board, check solution, and state synchronization
 */
export const useBoardOperations = (puzzleId) => {
  const handleCheckSolution = useCallback(() => {
    webSocketManager.send({
      type: "sendCheckSolution",
      puzzleId: puzzleId,
    });
  }, [puzzleId]);

  const handleClearBoard = useCallback(() => {
    webSocketManager.send({
      type: "sendClearBoard",
      puzzleId: puzzleId,
    });
  }, [puzzleId]);

  return { handleCheckSolution, handleClearBoard };
};
