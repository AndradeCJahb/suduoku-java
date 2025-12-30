import { useCallback } from "react";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for handling candidate toggles
 * Manages candidate mode operations, including overwriting values
 */
export const useCandidateToggle = (puzzleId) => {
  const handleCandidateToggle = useCallback(
    (row, col, candidate, gridData, setGridData) => {
      setGridData((prevGridData) => {
        // First check if the cell is editable
        const currentCell = prevGridData[row][col];
        if (!currentCell.isEditable) {
          return prevGridData; // Exit if cell is not editable
        }

        // If cell has a value, clear it and add the candidate instead
        if (currentCell.value) {
          const newGrid = prevGridData.map((r, rowIndex) =>
            r.map((cell, colIndex) => {
              if (rowIndex === row && colIndex === col) {
                return {
                  ...cell,
                  value: "", // Clear the value
                  candidates: [candidate], // Add the candidate
                };
              }
              return cell;
            }),
          );

          // Send updates to server
          webSocketManager.send({
            type: "sendCellChange",
            puzzleId: puzzleId,
            row: row,
            col: col,
            value: 0, // Clear the value
          });

          webSocketManager.send({
            type: "sendCandidateToggle",
            puzzleId: puzzleId,
            row: row,
            col: col,
            candidate: candidate,
          });

          return newGrid;
        }

        // Update local candidates (cell is empty)
        const newGrid = prevGridData.map((r, rowIndex) =>
          r.map((cell, colIndex) => {
            if (rowIndex === row && colIndex === col) {
              const newCandidates = cell.candidates.includes(candidate)
                ? cell.candidates.filter((n) => n !== candidate)
                : [...cell.candidates, candidate];
              return { ...cell, candidates: newCandidates };
            }
            return cell;
          }),
        );

        // Send candidate toggle to server
        webSocketManager.send({
          type: "sendCandidateToggle",
          puzzleId: puzzleId,
          row: row,
          col: col,
          candidate: candidate,
        });

        return newGrid;
      });
    },
    [puzzleId],
  );

  return { handleCandidateToggle };
};
