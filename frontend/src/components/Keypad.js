import React from "react";
import "../css/Keypad.css";

function Keypad({
  onNumberClick,
  onBackspaceClick,
  handleClearBoard,
  handleCheckSolution,
  gridData,
  onCandidateModeChange,
  candidateMode,
}) {
  const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9];

  const counts = {};
  for (let row of gridData) {
    for (let cell of row) {
      if (cell.value && /^[1-9]$/.test(cell.value)) {
        counts[cell.value] = (counts[cell.value] || 0) + 1;
      }
    }
  }

  const handleCandidateModeToggle = () => {
    const newMode = !candidateMode;
    if (onCandidateModeChange) {
      onCandidateModeChange(newMode);
    }
  };

  return (
    <div className="controls-container">
      <div className="keypad">
        {numbers.map((number) => {
          const disabled = counts[number] >= 9;
          return (
            <div
              key={number}
              className="keypad-button"
              onClick={() => onNumberClick(number.toString(), candidateMode)}
              style={
                disabled && !candidateMode
                  ? {
                      color: "#bbbbbb",
                      fontWeight: "lighter",
                    }
                  : {}
              }
            >
              {number}
            </div>
          );
        })}
        <div className="keypad-button clearBoardBtn" onClick={handleClearBoard}>
          Clear
        </div>
        <div
          className="keypad-button checkSolutionBtn"
          onClick={handleCheckSolution}
        >
          Check
        </div>
        <div className="keypad-button backspaceBtn" onClick={onBackspaceClick}>
          &times;
        </div>
      </div>
      <div className="mode-toggle">
        <div
          className={`mode-button normal-mode ${!candidateMode ? "active" : ""}`}
          onClick={handleCandidateModeToggle}
        >
          Normal
        </div>
        <div
          className={`mode-button candidate-mode ${candidateMode ? "active" : ""}`}
          onClick={handleCandidateModeToggle}
        >
          Candidate
        </div>
      </div>
    </div>
  );
}

export default Keypad;
