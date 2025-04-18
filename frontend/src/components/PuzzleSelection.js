import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from './Header';

function PuzzleSelection() {
  const [puzzles, setPuzzles] = useState([]);
  const navigate = useNavigate();
  const wsRef = useRef(null);

  useEffect(() => {
    const wsUrl = 'ws://localhost:8080/ws';
    wsRef.current = new WebSocket(wsUrl);

    wsRef.current.onopen = () => {
      console.log('Connected to WebSocket server');
      wsRef.current.send(JSON.stringify({ type: 'fetchPuzzles' }));
    };

    wsRef.current.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log('Received data from server:', data);

      if (data.type === 'puzzles') {
        setPuzzles(data.puzzles);
      }
    };

    wsRef.current.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    wsRef.current.onclose = () => {
      console.log('WebSocket connection closed');
    };

    // Cleanup WebSocket connection on component unmount
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
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