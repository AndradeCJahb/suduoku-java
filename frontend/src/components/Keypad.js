import React from "react";

function Keypad({
    onNumberClick,
    onBackspaceClick,
    handleClearBoard,
    handleCheckSolution,
    gridData,
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

    return (
        <div className="controls-container">
            <div className="keypad">
                {numbers.map((number) => {
                    const disabled = counts[number] >= 9;
                    return (
                        <button
                            key={number}
                            className="keypad-button"
                            onClick={() => onNumberClick(number.toString())}
                            style={
                                disabled
                                    ? {
                                          color: "#bbbbbb",
                                          fontWeight: "lighter",
                                      }
                                    : {}
                            }
                        >
                            {number}
                        </button>
                    );
                })}
                <span
                    className="keypad-button keypad-backspace"
                    onClick={onBackspaceClick}
                >
                    &times;
                </span>
            </div>

            <div className="board-controls-section">
                <button className="clearBoardBtn" onClick={handleClearBoard}>
                    Clear
                </button>
                <button
                    className="checkSolutionBtn"
                    onClick={handleCheckSolution}
                >
                    Check
                </button>
            </div>
        </div>
    );
}

export default Keypad;
