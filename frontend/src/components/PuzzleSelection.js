import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from './Header';
import webSocketManager from './WebSocketManager';

function PuzzleSelection() {
  const [puzzles, setPuzzles] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const handleMessage = (data) => {
      if (data.type === 'puzzles') {
        setPuzzles(data.puzzles);
      }
    };
  
    webSocketManager.addListener(handleMessage);
    webSocketManager.send({ type: 'fetchPuzzles' });
  
    return () => {
      webSocketManager.removeListener(handleMessage);
    };
  }, []);

  const handlePuzzleSelect = (puzzleId) => {
    navigate(`/puzzle/${puzzleId}`);
  };

  return (
    <div>
      <Header />
      <div className="container">
        <div className="puzzle-grid">
          {puzzles.map((puzzle) => (
            <div
              key={puzzle.id}
              className="puzzle-card"
              onClick={() => handlePuzzleSelect(puzzle.id)}
            >
              <h3>{puzzle.title}</h3>
              <div className="puzzle-meta">
                <span className="difficulty">{puzzle.difficulty || 'Medium'}</span>
                <span className="status">{puzzle.status || 'New'}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default PuzzleSelection;