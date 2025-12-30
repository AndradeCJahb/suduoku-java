import { useCallback } from "react";

/**
 * Custom hook for keypad and UI interactions
 * Manages number clicks and backspace from keypad
 */
export const useUIInteractions = (
  focusedCell,
  handleCellChange,
  handleCandidateToggle,
) => {
  const handleNumberClick = useCallback(
    (number, mode, gridData, setGridData) => {
      if (focusedCell) {
        const { row, col } = focusedCell;
        if (mode) {
          // Candidate mode: toggle candidate
          handleCandidateToggle(
            row,
            col,
            parseInt(number),
            gridData,
            setGridData,
          );
        } else {
          // Normal mode: set cell value
          handleCellChange(row, col, number, gridData, setGridData);
        }
      }
    },
    [focusedCell, handleCellChange, handleCandidateToggle],
  );

  const handleBackspaceClick = useCallback(
    (gridData, setGridData) => {
      if (focusedCell) {
        const { row, col } = focusedCell;
        handleCellChange(row, col, "", gridData, setGridData);
      }
    },
    [focusedCell, handleCellChange],
  );

  return { handleNumberClick, handleBackspaceClick };
};
