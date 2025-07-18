import React, { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "./Header";
import "../index.css";
import webSocketManager from "./WebSocketManager";
import SudokuBoard from "./SudokuBoard";
import PlayerChat from "./PlayerChat";
import Keypad from "./Keypad";
import SolvedPopup from "./SolvedPopup";


// Check if a client ID exists in localStorage
let clientId = localStorage.getItem("clientId");
if (!clientId) {
    clientId = generateUUID();
    localStorage.setItem("clientId", clientId);
}

function generateUUID() {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (c) {
            var r = (Math.random() * 16) | 0,
                v = c === "x" ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        },
    );
}

function SudokuGame() {
    const navigate = useNavigate();
    const { puzzleId: urlPuzzleId } = useParams();
    const [puzzleId] = useState(parseInt(urlPuzzleId) || null);
    const [puzzleTitle, setPuzzleTitle] = useState("Loading puzzle...");
    const [gridData, setGridData] = useState(
        Array(9).fill(Array(9).fill({ value: "", isEditable: true })),
    );
    const [players, setPlayers] = useState([]);
    const [playerPositions, setPlayerPositions] = useState([]);
    const [focusedCell, setFocusedCell] = useState({ row: 5, col: 5 });
    const [clientInfo, setClientInfo] = useState({ name: "", color: "" });
    const [chatInput, setChatInput] = useState("");
    const [chatMessages, setChatMessages] = useState([]);
    const [incorrectCells, setIncorrectCells] = useState([]);
    const [puzzleSolved, setPuzzleSolved] = useState(false);
    const chatLogRef = useRef(null);

    const [elapsedTime, setElapsedTime] = useState(0);
    const timerRef = useRef(null);

    useEffect(() => {
        // Scroll to the bottom of the chat log whenever messages are updated
        if (chatLogRef.current) {
            chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
        }
    }, [chatMessages]);

    useEffect(() => {
        if (!puzzleSolved) {
            timerRef.current = setInterval(() => {
                setElapsedTime((prev) => prev + 1);
            }, 1000);
        } else if (timerRef.current) {
            clearInterval(timerRef.current);
        }
        return () => clearInterval(timerRef.current);
    }, [puzzleSolved]);

    // Reset timer when puzzle changes
    useEffect(() => {
        setElapsedTime(0);
    }, [puzzleId]);

    // Format time as mm:ss
    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60)
            .toString()
            .padStart(2, "0");
        const s = (seconds % 60).toString().padStart(2, "0");
        return `${m}:${s}`;
    };



    useEffect(() => {
        const wsUrl = "ws://localhost:8080/ws";
        console.log(`Connecting to WebSocket at ${wsUrl}`);
        webSocketManager.connect(wsUrl);

        const handleMessage = (data) => {
            if (data.type === "updatePuzzle") {
                // Transpose the grid data
                const updatedGrid = Array.from({ length: 9 }, (_, rowIndex) =>
                    Array.from({ length: 9 }, (_, colIndex) => ({
                        value: data.board[colIndex][rowIndex].value, // Swap row and column indices
                        isEditable: data.board[colIndex][rowIndex].isEditable, // Swap row and column indices
                    })),
                );

                // Update the grid data
                setGridData(updatedGrid);
                setPuzzleTitle(data.title);
            } else if (data.type === "updatePlayers") {
                setPlayers(data.players);
            } else if (data.type === "updateIdentity") {
                setClientInfo(data.client);
            } else if (data.type === "updateChat") {
                setChatMessages(data.messages);
            } else if (data.type === "updatePuzzleSolved") {
                setPuzzleSolved(true);
            } else if (data.type === "updateIncorrectCells") {
                setIncorrectCells(data.incorrectCells);
            } else if (data.type === "updatePlayerPositions") {
                setPlayerPositions(data.positions);
            } else if (data.type === "puzzleNotFound") {
                alert("Puzzle not found. Returning to puzzle selection.");
                navigate("/");
            }
        };

        webSocketManager.addListener(handleMessage);

        // Send initial messages
        webSocketManager.send({ type: "fetchIdentity", clientId });
        webSocketManager.send({ type: "fetchPuzzle", clientId, puzzleId });
        webSocketManager.send({ type: "fetchChat", puzzleId });

        return () => {
            webSocketManager.removeListener(handleMessage);
            webSocketManager.send({
                type: "sendLeaveRoom",
                clientId: clientId,
                puzzleId: puzzleId,
            });
        };
    }, [puzzleId, navigate]);

    // Handles key navigation and cell focus
    useEffect(() => {
        const handleKeyPress = (event) => {
            const { row, col } = focusedCell;
            switch (event.key) {
                case "ArrowUp":
                    if (col > 0) setFocusedCell({ row: row, col: col - 1 });
                    break;
                case "ArrowDown":
                    if (col < 8) setFocusedCell({ row: row, col: col + 1 });
                    break;
                case "ArrowLeft":
                    if (row > 0) setFocusedCell({ row: row - 1, col: col });
                    break;
                case "ArrowRight":
                    if (row < 8) setFocusedCell({ row: row + 1, col: col });
                    break;
                default:
                    break;
            }
        };

        window.addEventListener("keydown", handleKeyPress);

        // Focus the input for the currently focused cell
        const { row, col } = focusedCell;
        const targetCell = document.querySelector(
            `input[data-row="${row}"][data-col="${col}"]`
        );
        if (targetCell) {
            targetCell.focus();
            const valueLength = targetCell.value.length;
            setTimeout(() => {
                targetCell.setSelectionRange(valueLength, valueLength);
            }, 0);
        }

        return () => {
            window.removeEventListener("keydown", handleKeyPress);
        };
    }, [focusedCell]);

    const handleCheckSolution = () => {
        webSocketManager.send({
            type: "sendCheckSolution",
            puzzleId: puzzleId,
        });
    };

    const handleCellChange = (row, col, value) => {
        // First check if the cell is editable
        const currentCell = gridData[row][col];
        if (!currentCell.isEditable) {
            return; // Exit early if the cell is not editable
        }

        // Continue with update since the cell is editable
        const newGrid = gridData.map((r, rowIndex) =>
            r.map((cell, colIndex) =>
                rowIndex === row && colIndex === col
                    ? { ...cell, value }
                    : cell,
            ),
        );

        setGridData(newGrid);
        setPuzzleSolved(false);

        // Send updates to server
        webSocketManager.send({
            type: "sendIncorrectCellsUpdate",
            puzzleId: puzzleId,
            row: col,
            col: row,
        });

        webSocketManager.send({
            type: "sendCellChange",
            puzzleId: puzzleId,
            row: col,
            col: row,
            value: value || 0,
        });

        webSocketManager.send({
            type: "sendElapsedTime",
            puzzleId: puzzleId,
            elapsedTime: elapsedTime
        });
    };

    const handleClearBoard = () => {
        // Create a new grid with only locked cells
        const clearedGrid = gridData.map((row) =>
            row.map((cell) => ({
                ...cell,
                value: cell.isEditable ? "" : cell.value,
            })),
        );

        // Update local state
        setGridData(clearedGrid);
        setIncorrectCells([]);
        setPuzzleSolved(false);

        webSocketManager.send({
            type: "sendClearBoard",
            puzzleId: puzzleId,
        });
    };

    const handleNumberClick = (number) => {
        if (focusedCell) {
            const { row, col } = focusedCell;
            handleCellChange(row, col, number);
        }
    };

    const handleBackspaceClick = () => {
        if (focusedCell) {
            const { row, col } = focusedCell;
            handleCellChange(row, col, "");
        }
    };

    return (
        <div>
            <Header />

            <SolvedPopup
                visible={puzzleSolved}
                onClose={() => setPuzzleSolved(false)}
            />

            <div className="app-container">
                <div className="left-section">
                    <Keypad
                        onNumberClick={handleNumberClick}
                        onBackspaceClick={handleBackspaceClick}
                        handleClearBoard={handleClearBoard}
                        handleCheckSolution={handleCheckSolution}
                        gridData={gridData}
                    />

                    <SudokuBoard
                        puzzleTitle={puzzleTitle}
                        gridData={gridData}
                        handleCellChange={handleCellChange}
                        incorrectCells={incorrectCells}
                        playerPositions={playerPositions}
                        setFocusedCell={setFocusedCell}
                        clientId={clientId}
                        elapsedTime={elapsedTime}
                        formatTime={formatTime}
                    />
                </div>

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
