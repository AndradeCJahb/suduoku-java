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

  const handleClearBoard = useCallback(
    (gridData, setGridData, setIncorrectCells, setPuzzleSolved) => {
      // Create a new grid with only locked cells
      const clearedGrid = gridData.map((row) =>
        row.map((cell) => ({
          ...cell,
          value: cell.isEditable ? "" : cell.value,
          candidates: cell.isEditable ? [] : cell.candidates,
        })),
      );

      // Update local state
      setGridData(clearedGrid);
      setIncorrectCells([]);
      setPuzzleSolved(false);

      webSocketManager.send({
        type: "sendClearBoard",
        puzzleId: puzzleId,
      });
    },
    [puzzleId],
  );

  return { handleCheckSolution, handleClearBoard };
};
