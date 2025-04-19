import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from './Header';
import '../index.css';
import webSocketManager from './WebSocketManager';

function Cell({ value, isEditable, onChange, isIncorrect, row, col, playerPositions, setFocusedCell, clientId }) {  const handleChange = (event) => {
    const inputValue = event.target.value.slice(-1);
    if (/^[1-9]?$/.test(inputValue)) {
      onChange(inputValue);
    }
  };

  // Send position to server and update focusedCell when cell is focused
  const handleFocus = () => {
    setFocusedCell({ row, col }); // Update the focusedCell state
    webSocketManager.send({
      type: 'sendPlayerPosition',
      position: { row, col },
      clientId: clientId
    });
  };

  // Determine the CSS class based on editable state and incorrect state
  let cellClass = isEditable ? 'cell' : 'non-editable-cell';
  if (isIncorrect) {
    cellClass += ' incorrect-cell';
  }

  // Add player position highlights
  const playerHighlights = playerPositions
    .filter(player => player.position.row === row && player.position.col === col)
    .map(player => {
      return {
        boxShadow: `inset 0 0 0 3px ${player.color}`,
        zIndex: 1,
        position: 'relative'
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
    />
  );
}

function ThreeGrid({ gridData, onCellChange, rowOffset, colOffset, incorrectCells, playerPositions, setFocusedCell }) {
  const transposedGridData = Array.from({ length: 3 }, (_, i) =>
    Array.from({ length: 3 }, (_, j) => gridData[j][i])
  );

  return (
    <div className="threeGrid">
      {transposedGridData.map((row, rowIndex) => (
        <div key={rowIndex} className="grid-row">
          {row.map((cell, colIndex) => {
            const globalRow = rowOffset + colIndex;
            const globalCol = colOffset + rowIndex;

            const isIncorrect = incorrectCells.some(
              cell => cell.col === globalRow && cell.row === globalCol
            );

            return (
              <Cell
                key={colIndex}
                value={cell.value}
                isEditable={cell.isEditable}
                isIncorrect={isIncorrect}
                row={globalRow}
                col={globalCol}
                onChange={(value) => onCellChange(globalRow, globalCol, value)}
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

function FinalGrid({ gridData, onCellChange, incorrectCells, playerPositions, setFocusedCell }) {
  return (
    <div className="finalGrid">
      {Array.from({ length: 3 }, (_, gridRow) => (
        <div key={gridRow} className="grid-row">
          {Array.from({ length: 3 }, (_, gridCol) => (
            <ThreeGrid
              key={gridCol}
              gridData={gridData.slice(gridRow * 3, gridRow * 3 + 3).map((row) =>
                row.slice(gridCol * 3, gridCol * 3 + 3)
              )}
              onCellChange={onCellChange}
              rowOffset={gridRow * 3}
              colOffset={gridCol * 3}
              incorrectCells={incorrectCells}
              playerPositions={playerPositions}
              setFocusedCell={setFocusedCell}
            />
          ))}
        </div>
      ))}
    </div>
  );
}

// Check if a client ID exists in localStorage
let clientId = localStorage.getItem('clientId');
if (!clientId) {
  clientId = crypto.randomUUID();
  localStorage.setItem('clientId', clientId);
}

function SudokuGame() {
  const navigate = useNavigate();
  const { puzzleId: urlPuzzleId } = useParams();
  const [puzzleId] = useState(parseInt(urlPuzzleId) || null);
  const [puzzleTitle, setPuzzleTitle] = useState('Loading puzzle...');
  const [gridData, setGridData] = useState(Array(9).fill(Array(9).fill({ value: '', isEditable: true })));
  const [players, setPlayers] = useState([]);
  const [playerPositions, setPlayerPositions] = useState([]);
  const [focusedCell, setFocusedCell] = useState({ row: 5, col: 5 });
  const [clientInfo, setClientInfo] = useState({ name: '', color: '' });
  const [chatInput, setChatInput] = useState('');
  const [chatMessages, setChatMessages] = useState([]);
  const [incorrectCells, setIncorrectCells] = useState([]);
  const [puzzleSolved, setPuzzleSolved] = useState(false);
  const chatLogRef = useRef(null);

  useEffect(() => {
    // Scroll to the bottom of the chat log whenever messages are updated
    if (chatLogRef.current) {
      chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
    }
  }, [chatMessages]);

  useEffect(() => {
    const wsUrl = 'ws://localhost:8080/ws';
    console.log(`Connecting to WebSocket at ${wsUrl}`);
    webSocketManager.connect(wsUrl);

    const handleMessage = (data) => {
      if (data.type === 'updatePuzzle') {
        const updatedGrid = Array.from({ length: 9 }, (_, rowIndex) =>
          Array.from({ length: 9 }, (_, colIndex) => ({
            value: data.board[colIndex][rowIndex].value, // Swap row and column indices
            isEditable: data.board[colIndex][rowIndex].isEditable, // Swap row and column indices
          }))
        );
        setGridData(updatedGrid);
        setPuzzleTitle(data.title);
      } else if (data.type === 'updatePlayers') {
        setPlayers(data.players);
      } else if (data.type === 'updateIdentity') {
        setClientInfo(data.client);
      } else if (data.type === 'updateChat') {
        setChatMessages(data.messages);
      } else if (data.type === 'updatePuzzleSolved') {
        setPuzzleSolved(true);
      } else if (data.type === 'updateIncorrectCells') {
        setIncorrectCells(data.incorrectCells);
      } else if (data.type === 'updatePlayerPositions') {
        setPlayerPositions(data.positions);
      } else if (data.type === 'puzzleNotFound') {
        alert('Puzzle not found. Returning to puzzle selection.');
        navigate('/');
      }
    };
  
    webSocketManager.addListener(handleMessage);
  
    // Send initial messages
    webSocketManager.send({ type: 'fetchIdentity', clientId });
    webSocketManager.send({ type: 'fetchPuzzle', clientId, puzzleId });
    webSocketManager.send({ type: 'fetchChat', puzzleId });
  
    return () => {
      webSocketManager.removeListener(handleMessage);
      webSocketManager.send({ 
        type: 'sendLeaveRoom', 
        clientId: clientId,
        puzzleId: puzzleId
      });
    };
  }, [puzzleId, navigate]);

  const sendChatMessage = () => {
    if (chatInput.trim() !== '') {
      const message = {
        user: clientInfo.name,
        color: clientInfo.color,
        text: chatInput,
        puzzleId: puzzleId,
      };

      webSocketManager.send({ type: 'sendChat', message });
      setChatInput('');
    }
  };

  const handleKeyDown = (event) => {
    const { row, col } = focusedCell;
      switch (event.key) {
        case 'ArrowUp':
          if (col > 0) setFocusedCell({ row: row , col: col - 1});
          break;
        case 'ArrowDown':
          if (col < 8) setFocusedCell({ row: row , col: col + 1});
          break;
        case 'ArrowLeft':
          if (row > 0) setFocusedCell({ row: row - 1, col: col });
          break;
        case 'ArrowRight':
          if (row < 8) setFocusedCell({ row: row + 1, col: col });
          break;
          default:
            break;
        }
    };

  useEffect(() => {
    const handleKeyPress = (event) => handleKeyDown(event);
  
    window.addEventListener('keydown', handleKeyPress);
    return () => {
      window.removeEventListener('keydown', handleKeyPress);
    };
  });
  
  useEffect(() => {
    const { row, col } = focusedCell;
    const targetCell = document.querySelector(
      `input[data-row="${row}"][data-col="${col}"]`
    );
    if (targetCell) {
      targetCell.focus();
      // Ensure the cursor is always at the end of the input value
      const valueLength = targetCell.value.length;
      setTimeout(() => {
        targetCell.setSelectionRange(valueLength, valueLength);
      }, 0); // Use a timeout to ensure this runs after the focus event
    }
  }, [focusedCell]);
  
  const handleCheckSolution = () => {
    webSocketManager.send({
      type: 'sendCheckSolution',
      puzzleId: puzzleId,
    });
  };

  const handleCellChange = (row, col, value) => {
    const newGrid = gridData.map((r, rowIndex) =>
      r.map((cell, colIndex) =>
        rowIndex === row && colIndex === col
          ? { ...cell, value }
          : cell
      )
    );
  
    setGridData(newGrid);
    setPuzzleSolved(false);

    setIncorrectCells((prev) =>
      prev.filter((cell) => !(cell.row === col && cell.col === row))
    );
  
    webSocketManager.send({
      type: 'sendCellChange',
      puzzleId: puzzleId,
      row: col,
      col: row,
      value: value || 0,
    });
  };

  const handleClearBoard = () => {
    // Create a new grid with only locked cells
    const clearedGrid = gridData.map(row =>
      row.map(cell => ({
        ...cell,
        value: cell.isEditable ? '' : cell.value
      }))
    );
    
    // Update local state
    setGridData(clearedGrid);
    setIncorrectCells([]);
    setPuzzleSolved(false);
    
    webSocketManager.send({
      type: 'sendClearBoard',
      puzzleId: puzzleId,
    });
  };

  return (
    <div>
      <Header />
      <div className="sudokuTitle">{puzzleTitle}</div>
      
      <div className="app-container">
        <div className="left-section">
        <div className={`board-section ${puzzleSolved ? 'solved' : ''}`}>
        <FinalGrid
          gridData={gridData}
          onCellChange={handleCellChange}
          incorrectCells={incorrectCells}
          playerPositions={playerPositions}
          setFocusedCell={setFocusedCell}
        />
        </div>
          
          <div className="board-controls-section">
            <button 
              className="clearBoardBtn" 
              onClick={handleClearBoard}
            >
              Clear Board
            </button>
            <button 
              className="checkSolutionBtn" 
              onClick={handleCheckSolution}
            >
              Check Solution
            </button>
          </div>
        </div>
        
        <div className="right-section">
          <div className="chatBox">
            <div className="chatLog" ref={chatLogRef}>
              {chatMessages.map((msg, index) => (
                <div key={index}>
                  <strong style={{ color: msg.color || '#000' }}>{msg.user}:</strong>
                  <span className="message">{msg.message}</span>
                  <span className="time">
                    {new Date(msg.time).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                      hour12: true,
                    })}
                  </span>
                </div>
              ))}
            </div>

            <div className="chatInput">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    sendChatMessage();
                  }
                }}
                placeholder="Type to chat"
              />
            </div>
          </div>
          
          <div className="players-section">
            <div className="clientInfo">
              <span>You are:</span>
              <span style={{ color: clientInfo.color }}> {clientInfo.name}</span>
            </div>

            <h3 className="playerHeader">Connected Players:</h3>

            <div className="playerList">
              <ul>
                {players.map((player, index) => (
                  <li key={index} style={{ color: player.color }}>
                    {player.name}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SudokuGame;