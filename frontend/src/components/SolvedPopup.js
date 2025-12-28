import React from "react";
import "../css/SolvedPopup.css";

function SolvedPopup({ visible, onClose }) {
	if (!visible) return null;

	return (
		<div className="solved-popup" style={{ display: "flex" }}>
			<div className="solved-popup-content">
				<span className="close-solved" onClick={onClose}>
					&times;
				</span>
				<h2 className="solved-title">Puzzle Solved!</h2>
				<p className="solved-text">
					Congratulations! You've successfully completed the puzzle.
				</p>
			</div>
		</div>
	);
}

export default SolvedPopup;
