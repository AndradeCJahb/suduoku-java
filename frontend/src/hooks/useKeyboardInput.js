import { useCallback, useEffect } from "react";

/**
 * Custom hook for keyboard input handling
 * Manages number keys, arrow keys, shift (mode toggle), and backspace
 */
export const useKeyboardInput = (
  focusedCellRef,
  gridDataRef,
  candidateMode,
  handleCellChange,
  handleCandidateToggle,
  setFocusedCell,
  setCandidateMode,
  focusedCell,
) => {
  const handleKeyboardInput = useCallback(
    (event) => {
      const { row, col } = focusedCellRef.current;
      const currentCell = gridDataRef.current[row]?.[col];

      // Check if focus is in chat input
      const isChatInput =
        event.target?.tagName === "INPUT" ||
        event.target?.tagName === "TEXTAREA";

      // Handle Shift key to toggle candidate mode (only if not in chat)
      if (event.key === "Shift" && !isChatInput) {
        setCandidateMode((prev) => !prev);
        event.preventDefault();
        return;
      }

      // Handle number keys (1-9) (only if not in chat)
      if (/^[1-9]$/.test(event.key) && !isChatInput) {
        if (currentCell?.isEditable) {
          if (candidateMode) {
            // Candidate mode: toggle candidate
            handleCandidateToggle(
              row,
              col,
              parseInt(event.key),
              gridDataRef.current,
              (setGridData) => {},
            );
          } else {
            // Normal mode: set cell value
            handleCellChange(
              row,
              col,
              event.key,
              gridDataRef.current,
              (setGridData) => {},
            );
          }
          event.preventDefault();
        }
        return;
      }

      // Handle backspace/delete (only for sudoku board, not chat)
      if (event.key === "Backspace" || event.key === "Delete") {
        if (isChatInput) {
          return; // Allow backspace to work in chat input
        }
        if (currentCell?.isEditable) {
          handleCellChange(
            row,
            col,
            "",
            gridDataRef.current,
            (setGridData) => {},
          );
          event.preventDefault();
        }
        return;
      }

      // Handle arrow navigation (only if not in chat)
      if (!isChatInput) {
        switch (event.key) {
          case "ArrowUp":
            if (row > 0) setFocusedCell({ row: row - 1, col: col });
            event.preventDefault();
            break;
          case "ArrowDown":
            if (row < 8) setFocusedCell({ row: row + 1, col: col });
            event.preventDefault();
            break;
          case "ArrowLeft":
            if (col > 0) setFocusedCell({ row: row, col: col - 1 });
            event.preventDefault();
            break;
          case "ArrowRight":
            if (col < 8) setFocusedCell({ row: row, col: col + 1 });
            event.preventDefault();
            break;
          default:
            break;
        }
      }
    },
    [
      candidateMode,
      handleCellChange,
      handleCandidateToggle,
      setFocusedCell,
      setCandidateMode,
    ],
  );

  useEffect(() => {
    window.addEventListener("keydown", handleKeyboardInput);

    // Focus the input for the currently focused cell
    const { row, col } = focusedCellRef.current;
    const targetCell = document.querySelector(
      `input[data-row="${row}"][data-col="${col}"]`,
    );
    if (targetCell) {
      targetCell.focus();
      const valueLength = targetCell.value.length;
      setTimeout(() => {
        targetCell.setSelectionRange(valueLength, valueLength);
      }, 0);
    }

    return () => {
      window.removeEventListener("keydown", handleKeyboardInput);
    };
  }, [handleKeyboardInput, focusedCellRef, focusedCell]);

  return { handleKeyboardInput };
};
