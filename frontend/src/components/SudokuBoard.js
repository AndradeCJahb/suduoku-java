import React from "react";
import webSocketManager from "./WebSocketManager";

function Cell({
    value,
    isEditable,
    onChange,
    isIncorrect,
    row,
    col,
    playerPositions,
    setFocusedCell,
    clientId,
}) {
    const handleChange = (event) => {
        const inputValue = event.target.value.slice(-1);
        if (/^[1-9]?$/.test(inputValue)) {
            onChange(inputValue);
        }
    };

    // Send position to server and update focusedCell when cell is focused
    const handleFocus = () => {
        setFocusedCell({ row, col }); // Update the focusedCell state
        webSocketManager.send({
            type: "sendPlayerPosition",
            position: { row, col },
            clientId: clientId,
        });
    };

    // Determine the CSS class based on editable state and incorrect state
    let cellClass = isEditable ? "cell" : "non-editable-cell";
    if (isIncorrect) {
        cellClass += " incorrect-cell";
    }

    // Add player position highlights
    const playerHighlights =
        playerPositions
            .filter(
                (player) =>
                    player.position.row === row && player.position.col === col,
            )
            .map((player) => {
                return {
                    boxShadow: `inset 0 0 0 3px ${player.color}`,
                    zIndex: 1,
                    position: "relative",
                };
            })[0] || {};

    return (
        <input
            type="text"
            value={value}
            onChange={isEditable ? handleChange : undefined}
            onFocus={handleFocus}
            readOnly={!isEditable}
            maxLength="2"
            className={cellClass}
            style={playerHighlights}
            data-row={row}
            data-col={col}
            inputMode="none"
        />
    );
}

function ThreeGrid({
    gridData,
    onCellChange,
    rowOffset,
    colOffset,
    incorrectCells,
    playerPositions,
    setFocusedCell,
    clientId,
}) {
    const transposedGridData = Array.from({ length: 3 }, (_, i) =>
        Array.from({ length: 3 }, (_, j) => gridData[j][i]),
    );

    return (
        <div className="threeGrid">
            {transposedGridData.map((row, rowIndex) => (
                <div key={rowIndex} className="grid-row">
                    {row.map((cell, colIndex) => {
                        const globalRow = rowOffset + colIndex;
                        const globalCol = colOffset + rowIndex;

                        const isIncorrect = incorrectCells.some(
                            (cell) =>
                                cell.col === globalRow &&
                                cell.row === globalCol,
                        );

                        return (
                            <Cell
                                key={colIndex}
                                value={cell.value}
                                isEditable={cell.isEditable}
                                isIncorrect={isIncorrect}
                                row={globalRow}
                                col={globalCol}
                                onChange={(value) =>
                                    onCellChange(globalRow, globalCol, value)
                                }
                                playerPositions={playerPositions}
                                setFocusedCell={setFocusedCell}
                                clientId={clientId}
                            />
                        );
                    })}
                </div>
            ))}
        </div>
    );
}

function FinalGrid({
    gridData,
    onCellChange,
    incorrectCells,
    playerPositions,
    setFocusedCell,
    clientId,
}) {
    return (
        <div className="finalGrid">
            {Array.from({ length: 3 }, (_, gridRow) => (
                <div key={gridRow} className="grid-row">
                    {Array.from({ length: 3 }, (_, gridCol) => (
                        <ThreeGrid
                            key={gridCol}
                            gridData={gridData
                                .slice(gridRow * 3, gridRow * 3 + 3)
                                .map((row) =>
                                    row.slice(gridCol * 3, gridCol * 3 + 3),
                                )}
                            onCellChange={onCellChange}
                            rowOffset={gridRow * 3}
                            colOffset={gridCol * 3}
                            incorrectCells={incorrectCells}
                            playerPositions={playerPositions}
                            setFocusedCell={setFocusedCell}
                            clientId={clientId}
                        />
                    ))}
                </div>
            ))}
        </div>
    );
}

function SudokuBoard({
    gridData,
    handleCellChange,
    incorrectCells,
    playerPositions,
    setFocusedCell,
    clientId,
    puzzleTitle,
}) {
    return (
        <div className="sudoku-container">
            <div className="sudokuTitle">{puzzleTitle}</div>
            <FinalGrid
                gridData={gridData}
                onCellChange={handleCellChange}
                incorrectCells={incorrectCells}
                playerPositions={playerPositions}
                setFocusedCell={setFocusedCell}
                clientId={clientId}
            />
        </div>
    );
}

export default SudokuBoard;
