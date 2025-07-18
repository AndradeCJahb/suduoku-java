import React from "react";

function InfoPopup({ visible, onClose  }) {
    if (!visible) return null;
    
    return (
        <div className="info-popup">
            <div className="popup-content">
                <span className="close-popup" onClick={onClose}>
                    &times;
                </span>
                <h2 className="info-title">About Suduoku</h2>
                <p className="info-text">
                    Suduoku is a real-time collaborative Sudoku game
                    where multiple players can solve puzzles together.
                </p>
                <p className="info-text">
                    For more information or to report any issues,
                    check out the project on{" "}
                    <a
                        href="https://github.com/AndradeCJahb/suduoku-java"
                        target="_blank"
                        rel="noreferrer"
                    >
                        GitHub
                    </a>
                    .
                </p>
            </div>
        </div>
    );
}

export default InfoPopup;