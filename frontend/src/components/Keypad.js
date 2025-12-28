import React from "react";
import "../css/Keypad.css";

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
						<div
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
		</div>
	);
}

export default Keypad;
