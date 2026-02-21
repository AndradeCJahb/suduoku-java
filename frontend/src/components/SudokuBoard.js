import React from "react";
import webSocketManager from "./WebSocketManager";
import "../css/SudokuBoard.css";

/**
 * Candidate cell - displays a single candidate number (1-9)
 * Used within the 3x3 candidate grid in each main cell
 */
function Candidate({ number, isCandidate, onToggle, candidateMode }) {
  return (
    <div className={`candidate ${isCandidate ? "active" : ""}`}>
      {isCandidate ? number : ""}
    </div>
  );
}

/**
 * Main cell - contains the value and 3x3 candidate grid
 */
function Cell({
  value,
  candidates,
  isEditable,
  isIncorrect,
  row,
  col,
  playerPositions,
  setFocusedCell,
  clientId,
  onValueChange,
  candidateMode,
}) {
  const handleValueChange = (event) => {
    const inputValue = event.target.value.slice(-1);
    if (/^[1-9]?$/.test(inputValue)) {
      onValueChange(row, col, inputValue);
    }
  };

  const handleFocus = () => {
    setFocusedCell({ row, col });
    webSocketManager.send({
      type: "sendPlayerPosition",
      position: { row, col },
      clientId: clientId,
    });
  };

  const playerHighlight =
    playerPositions
      .filter((p) => p.position.row === row && p.position.col === col)
      .map((p) => ({ boxShadow: `inset 0 0 0 3px ${p.color}` }))[0] || {};

  const cellClass = [
    "cell",
    !isEditable && "non-editable-cell",
    isIncorrect && "incorrect-cell",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div
      className={cellClass}
      data-row={row}
      data-col={col}
      style={playerHighlight}
      onFocus={handleFocus}
      tabIndex="0"
    >
      {value ? (
        <div className="cell-value">{value}</div>
      ) : (
        <div className="candidates-grid">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
            <Candidate
              key={num}
              number={num}
              isCandidate={candidates && candidates.includes(num)}
              candidateMode={candidateMode}
            />
          ))}
        </div>
      )}
      {isEditable && (
        <input
          type="hidden"
          value={value}
          onChange={handleValueChange}
          maxLength="1"
          className="cell-input"
        />
      )}
    </div>
  );
}

/**
 * Box - represents a 3x3 box in the sudoku grid
 * Contains 9 cells
 */
function Box({
  boxData,
  boxRow,
  boxCol,
  incorrectCells,
  playerPositions,
  setFocusedCell,
  clientId,
  onCellChange,
  candidateMode,
}) {
  const startRow = boxRow * 3;
  const startCol = boxCol * 3;

  return (
    <div className="box">
      {Array.from({ length: 3 }, (_, cellRow) =>
        Array.from({ length: 3 }, (_, cellCol) => {
          const globalRow = startRow + cellRow;
          const globalCol = startCol + cellCol;
          const cellData = boxData[cellRow][cellCol];

          const isIncorrect = incorrectCells.some(
            (c) => c.row === globalRow && c.col === globalCol,
          );

          return (
            <Cell
              key={`${globalRow}-${globalCol}`}
              value={cellData.value}
              candidates={cellData.candidates || []}
              isEditable={cellData.isEditable}
              isIncorrect={isIncorrect}
              row={globalRow}
              col={globalCol}
              playerPositions={playerPositions}
              setFocusedCell={setFocusedCell}
              clientId={clientId}
              onValueChange={onCellChange}
              candidateMode={candidateMode}
            />
          );
        }),
      )}
    </div>
  );
}

/**
 * Main Sudoku Board
 * 9x9 grid composed of 3x3 boxes
 */
function SudokuBoard({
  gridData,
  handleCellChange,
  incorrectCells,
  playerPositions,
  setFocusedCell,
  clientId,
  candidateMode,
}) {
  // Extract 3x3 box data from the 9x9 grid
  const getBoxData = (boxRow, boxCol) => {
    const startRow = boxRow * 3;
    const startCol = boxCol * 3;
    return Array.from({ length: 3 }, (_, i) =>
      Array.from({ length: 3 }, (_, j) => gridData[startRow + i][startCol + j]),
    );
  };

  return (
    <div className="sudoku-container">
      {Array.from({ length: 3 }, (_, boxRow) =>
        Array.from({ length: 3 }, (_, boxCol) => (
          <Box
            key={`box-${boxRow}-${boxCol}`}
            boxData={getBoxData(boxRow, boxCol)}
            boxRow={boxRow}
            boxCol={boxCol}
            incorrectCells={incorrectCells}
            playerPositions={playerPositions}
            setFocusedCell={setFocusedCell}
            clientId={clientId}
            onCellChange={handleCellChange}
            candidateMode={candidateMode}
          />
        )),
      )}
    </div>
  );
}

export default SudokuBoard;
