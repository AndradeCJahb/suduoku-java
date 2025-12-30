import React, { useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "./Header";
import "../index.css";
import SudokuBoard from "./SudokuBoard";
import PlayerChat from "./PlayerChat";
import Keypad from "./Keypad";
import SolvedPopup from "./SolvedPopup";
import SudokuHeader from "./SudokuHeader";

import { useCellChange } from "../hooks/useCellChange";
import { useCandidateToggle } from "../hooks/useCandidateToggle";
import { useKeyboardInput } from "../hooks/useKeyboardInput";
import { useWebSocketMessages } from "../hooks/useWebSocketMessages";
import { useBoardOperations } from "../hooks/useBoardOperations";
import { usePlayerPosition } from "../hooks/usePlayerPosition";
import { useElapsedTime } from "../hooks/useElapsedTime";
import { usePuzzleInitialization } from "../hooks/usePuzzleInitialization";
import { useUIInteractions } from "../hooks/useUIInteractions";

import { getOrCreateClientId } from "../utils/clientIdManager";

/**
 * SudokuGame Component
 * Main game interface with refactored logic using custom hooks
 */
function SudokuGame() {
  const { puzzleId: urlPuzzleId } = useParams();
  const [puzzleId] = useState(parseInt(urlPuzzleId) || null);

  // State management
  const [puzzleTitle, setPuzzleTitle] = useState("Loading puzzle...");
  const [gridData, setGridData] = useState(
    Array(9).fill(
      Array(9).fill({ value: "", isEditable: true, candidates: [] }),
    ),
  );
  const [players, setPlayers] = useState([]);
  const [playerPositions, setPlayerPositions] = useState([]);
  const [focusedCell, setFocusedCell] = useState({ row: 5, col: 5 });
  const [clientInfo, setClientInfo] = useState({ name: "", color: "" });
  const [chatInput, setChatInput] = useState("");
  const [chatMessages, setChatMessages] = useState([]);
  const [incorrectCells, setIncorrectCells] = useState([]);
  const [puzzleSolved, setPuzzleSolved] = useState(false);
  const [candidateMode, setCandidateMode] = useState(false);

  // Refs for state synchronization
  const gridDataRef = useRef(gridData);
  const focusedCellRef = useRef(focusedCell);
  const chatLogRef = useRef(null);

  // Sync refs with state
  React.useEffect(() => {
    gridDataRef.current = gridData;
  }, [gridData]);

  React.useEffect(() => {
    focusedCellRef.current = focusedCell;
  }, [focusedCell]);

  // Client ID
  const clientId = getOrCreateClientId();

  // Custom hooks for business logic
  const { elapsedTime, setElapsedTime, elapsedTimeRef } = useElapsedTime(
    puzzleSolved,
    puzzleId,
  );

  const { handleCellChange } = useCellChange(puzzleId, elapsedTimeRef);
  const { handleCandidateToggle } = useCandidateToggle(puzzleId);
  const { handleCheckSolution, handleClearBoard } =
    useBoardOperations(puzzleId);
  const { handleNumberClick, handleBackspaceClick } = useUIInteractions(
    focusedCell,
    handleCellChange,
    handleCandidateToggle,
  );

  // Keyboard input handling
  useKeyboardInput(
    focusedCellRef,
    gridDataRef,
    candidateMode,
    (row, col, value) =>
      handleCellChange(row, col, value, gridData, setGridData),
    (row, col, candidate) =>
      handleCandidateToggle(row, col, candidate, gridData, setGridData),
    setFocusedCell,
    setCandidateMode,
    focusedCell,
  );

  // WebSocket message handling
  useWebSocketMessages(
    puzzleId,
    setGridData,
    setPuzzleTitle,
    setPlayers,
    setPlayerPositions,
    setClientInfo,
    setChatMessages,
    setIncorrectCells,
    setPuzzleSolved,
    setElapsedTime,
  );

  // Player position broadcasting
  usePlayerPosition(focusedCell, clientId, puzzleId);

  // Puzzle initialization
  usePuzzleInitialization(puzzleId, clientId);

  // Chat scroll management
  React.useEffect(() => {
    if (chatLogRef.current) {
      chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
    }
  }, [chatMessages]);

  // Handle board clearing with proper state updates
  const handleClearBoardWrapper = () => {
    handleClearBoard(gridData, setGridData, setIncorrectCells, setPuzzleSolved);
  };

  // Handle keypad number click with state
  const handleNumberClickWrapper = (number, mode) => {
    handleNumberClick(number, mode, gridData, setGridData);
  };

  // Handle keypad backspace with state
  const handleBackspaceClickWrapper = () => {
    handleBackspaceClick(gridData, setGridData);
  };

  return (
    <div>
      <Header />

      <SolvedPopup
        visible={puzzleSolved}
        onClose={() => setPuzzleSolved(false)}
      />

      <SudokuHeader puzzleTitle={puzzleTitle} elapsedTime={elapsedTime} />

      <div className="app-container">
        <Keypad
          onNumberClick={handleNumberClickWrapper}
          onBackspaceClick={handleBackspaceClickWrapper}
          handleClearBoard={handleClearBoardWrapper}
          handleCheckSolution={handleCheckSolution}
          gridData={gridData}
          onCandidateModeChange={setCandidateMode}
          candidateMode={candidateMode}
        />

        <SudokuBoard
          puzzleTitle={puzzleTitle}
          gridData={gridData}
          handleCellChange={handleCellChange}
          handleCandidateToggle={handleCandidateToggle}
          incorrectCells={incorrectCells}
          playerPositions={playerPositions}
          setFocusedCell={setFocusedCell}
          clientId={clientId}
          candidateMode={candidateMode}
          focusedCell={focusedCell}
        />

        <PlayerChat
          chatMessages={chatMessages}
          chatInput={chatInput}
          setChatInput={setChatInput}
          chatLogRef={chatLogRef}
          clientInfo={clientInfo}
          players={players}
          puzzleId={puzzleId}
        />
      </div>
    </div>
  );
}

export default SudokuGame;
