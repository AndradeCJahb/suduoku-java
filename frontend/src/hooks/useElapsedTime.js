import { useState, useEffect, useRef } from "react";

/**
 * Custom hook for managing elapsed time timer
 * Handles timer initialization, stopping, and state syncing with ref
 */
export const useElapsedTime = (puzzleSolved, puzzleId) => {
  const [elapsedTime, setElapsedTime] = useState(0);
  const timerRef = useRef(null);
  const elapsedTimeRef = useRef(elapsedTime);

  // Keep elapsedTimeRef in sync with elapsedTime state
  useEffect(() => {
    elapsedTimeRef.current = elapsedTime;
  }, [elapsedTime]);

  // Timer interval management
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

  return { elapsedTime, setElapsedTime, elapsedTimeRef };
};
