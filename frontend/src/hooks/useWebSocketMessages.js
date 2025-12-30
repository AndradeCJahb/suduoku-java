import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import webSocketManager from "../components/WebSocketManager";

/**
 * Custom hook for WebSocket message handling
 * Manages all incoming WebSocket events and state updates
 */
export const useWebSocketMessages = (
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
) => {
  const navigate = useNavigate();

  useEffect(() => {
    const wsUrl = "ws://localhost:8080/ws";
    console.log(`Connecting to WebSocket at ${wsUrl}`);
    webSocketManager.connect(wsUrl);

    const handleMessage = (data) => {
      switch (data.type) {
        case "updatePuzzle":
          // Transpose the grid data and use server's candidates as authoritative
          setGridData((currentGridData) => {
            const updatedGrid = Array.from({ length: 9 }, (_, rowIndex) =>
              Array.from({ length: 9 }, (_, colIndex) => {
                const serverCell = data.board[colIndex][rowIndex];

                return {
                  value: serverCell.value,
                  isEditable: serverCell.isEditable,
                  candidates: serverCell.candidates || [],
                };
              }),
            );
            return updatedGrid;
          });
          setPuzzleTitle(data.title);
          break;

        case "updatePlayers":
          setPlayers(data.players);
          break;

        case "updateIdentity":
          setClientInfo(data.client);
          break;

        case "updateChat":
          setChatMessages(data.messages);
          break;

        case "updatePuzzleSolved":
          setPuzzleSolved(true);
          break;

        case "updateIncorrectCells":
          setIncorrectCells(data.incorrectCells);
          break;

        case "updatePlayerPositions":
          setPlayerPositions(data.positions);
          break;

        case "updateElapsedTime":
          // Server elapsed time is authoritative - sync local timer
          setElapsedTime(data.elapsedTime);
          break;

        case "puzzleNotFound":
          alert("Puzzle not found. Returning to puzzle selection.");
          navigate("/");
          break;

        default:
          console.warn("Unknown message type received:", data.type);
      }
    };

    webSocketManager.addListener(handleMessage);

    return () => {
      webSocketManager.removeListener(handleMessage);
    };
  }, [
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
    navigate,
  ]);
};
