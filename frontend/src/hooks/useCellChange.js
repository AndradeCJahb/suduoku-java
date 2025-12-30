import { useCallback } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for handling cell value changes
 * Manages normal mode cell updates
 */
export const useCellChange = (puzzleId, elapsedTimeRef) => {
  const handleCellChange = useCallback(
    (row, col, value, gridData, setGridData) => {
      // First check if the cell is editable
      setGridData((prevGridData) => {
        const currentCell = prevGridData[row][col];
        if (!currentCell.isEditable) {
          return prevGridData; // Exit early if the cell is not editable
        }

        // Continue with update since the cell is editable
        const newGrid = prevGridData.map((r, rowIndex) =>
          r.map((cell, colIndex) =>
            rowIndex === row && colIndex === col ? { ...cell, value } : cell,
          ),
        );

        // Send updates to server
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

        return newGrid;
      });
    },
    [puzzleId, elapsedTimeRef],
  );

  return { handleCellChange };
};
