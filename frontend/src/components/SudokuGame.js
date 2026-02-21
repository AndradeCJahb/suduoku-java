import React, { useState, useRef } from "react";
import { useParams } from "react-router-dom";

import "../css/SudokuGame.css";

import Header from "./Header";
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

  const [puzzleTitle, setPuzzleTitle] = useState("Loading puzzle...");
  const [gridData, setGridData] = useState(
    Array(9).fill(
      Array(9).fill({ value: "", isEditable: true, candidates: [] }),
    ),
  );

  const [clientInfo, setClientInfo] = useState({ name: "", color: "" });
  const [players, setPlayers] = useState([]);
  const [playerPositions, setPlayerPositions] = useState([]);

  const [focusedCell, setFocusedCell] = useState({ row: 5, col: 5 });

  const [chatInput, setChatInput] = useState("");
  const [chatMessages, setChatMessages] = useState([]);

  const [incorrectCells, setIncorrectCells] = useState([]);
  const [puzzleSolved, setPuzzleSolved] = useState(false);
  const [candidateMode, setCandidateMode] = useState(false);

  const gridDataRef = useRef(gridData);
  const focusedCellRef = useRef(focusedCell);
  const chatLogRef = useRef(null);

  React.useEffect(() => {
    focusedCellRef.current = focusedCell;
  }, [focusedCell]);

  const clientId = getOrCreateClientId();

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
    (row, col, value) => handleCellChange(row, col, value),
    (row, col, candidate) => handleCandidateToggle(row, col, candidate),
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

  usePlayerPosition(focusedCell, clientId, puzzleId);
  usePuzzleInitialization(puzzleId, clientId);

  // Chat scroll management
  React.useEffect(() => {
    if (chatLogRef.current) {
      chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
    }
  }, [chatMessages]);

  // Handle board clearing with proper state updates
  const handleClearBoardWrapper = () => {
    handleClearBoard();
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
          incorrectCells={incorrectCells}
          playerPositions={playerPositions}
          setFocusedCell={setFocusedCell}
          clientId={clientId}
          candidateMode={candidateMode}
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
