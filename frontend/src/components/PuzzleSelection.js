import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from './Header';
import webSocketManager from './WebSocketManager';

function PuzzleSelection() {
  const [puzzles, setPuzzles] = useState([]);
  const [allPuzzles, setAllPuzzles] = useState([]); // Store all puzzles for client-side filtering
  const navigate = useNavigate();
  const [searchFilter, setSearchFilter] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState(''); 

  const [loadedPuzzles, setLoadedPuzzles] = useState(false); 

  useEffect(() => {
    const handleMessage = (data) => {
      if (data.type === 'puzzles') {
        setPuzzles(data.puzzles);
        setAllPuzzles(data.puzzles); // Store all puzzles for filtering
        setLoadedPuzzles(true); // Set loadedPuzzles to true when puzzles are received
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

  useEffect(() => {
    let filteredResults = [...allPuzzles];
    
    // Apply difficulty filter
    if (difficultyFilter) {
      filteredResults = filteredResults.filter(
        puzzle => puzzle.difficulty && puzzle.difficulty.toLowerCase() === difficultyFilter.toLowerCase()
      );
    }
    
    // Apply search filter
    if (searchFilter) {
      const searchLower = searchFilter.toLowerCase();
      filteredResults = filteredResults.filter(
        puzzle => puzzle.title && puzzle.title.toLowerCase().includes(searchLower)
      );
    }
    
    setPuzzles(filteredResults);
  }, [difficultyFilter, searchFilter, allPuzzles]);

  const resetFilters = () => {
    setDifficultyFilter('');
    setSearchFilter('');
  };
  
  return (
    <div>
      <Header />
      
      <div className = "filter-container">
        <div className="filter-options">
          <span className="filter-difficulty-button" onClick={() => setDifficultyFilter("easy")}>Easy</span>
          <span className="filter-difficulty-button" onClick={() => setDifficultyFilter("medium")}>Medium</span>
          <span className="filter-difficulty-button" onClick={() => setDifficultyFilter("hard")}>Hard</span>
          
          <div className="filter-search">
            <input
              type="text"
              value={searchFilter}
              onChange={(e) => setSearchFilter(e.target.value)}
              placeholder="Search"
            />
          </div>

          <span className="filter-difficulty-button" onClick={resetFilters}>x</span>
        </div>
      </div>

      <div className="container">
        {!loadedPuzzles ? (
            <div className="loading-container">
              <div className="loading-spinner"></div>
              <p className="loading-message">Loading puzzles...</p>
            </div>
          ) : (
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
        )}





        
      </div>
    </div>
  );
}

export default PuzzleSelection;