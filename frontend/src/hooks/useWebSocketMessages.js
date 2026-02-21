import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import webSocketManager from "../components/WebSocketManager";
import { WS_URL } from "../config/wsConfig";

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
    webSocketManager.connect(WS_URL);

    const handleMessage = (data) => {
      switch (data.type) {
        case "updatePuzzle":
          setGridData(
            data.board.map((row) =>
              row.map((cell) => ({
                value: cell.value,
                isEditable: cell.isEditable,
                candidates: cell.candidates || [],
              })),
            ),
          );
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
