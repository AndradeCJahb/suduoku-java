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

      const isChatInput =
        event.target?.tagName === "INPUT" ||
        event.target?.tagName === "TEXTAREA";

      if (event.key === "Shift" && !isChatInput) {
        setCandidateMode((prev) => !prev);
        event.preventDefault();
        return;
      }

      if (/^[1-9]$/.test(event.key) && !isChatInput) {
        if (candidateMode) {
          handleCandidateToggle(row, col, parseInt(event.key));
        } else {
          handleCellChange(row, col, event.key);
        }
        event.preventDefault();

        return;
      }

      // Handle backspace/delete (only for sudoku board, not chat)
      if (event.key === "Backspace" || event.key === "Delete") {
        if (isChatInput) {
          return;
        }
        handleCellChange(row, col, "");
        event.preventDefault();
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
      focusedCellRef,
      setCandidateMode,
      candidateMode,
      handleCandidateToggle,
      handleCellChange,
      setFocusedCell,
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
