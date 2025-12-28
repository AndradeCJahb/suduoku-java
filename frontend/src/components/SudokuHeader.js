import React from "react";
import "../css/SudokuHeader.css";

function sudokuHeader({ puzzleTitle, elapsedTime }) {
	return (
		<div className="sudoku-header-row">
			<div className="sudoku-title">{puzzleTitle}</div>
			<div className="sudoku-timer">{formatTime(elapsedTime)}</div>
		</div>
	);
}

const formatTime = (seconds) => {
	const m = Math.floor(seconds / 60)
		.toString()
		.padStart(2, "0");
	const s = (seconds % 60).toString().padStart(2, "0");
	return `${m}:${s}`;
};

export default sudokuHeader;
