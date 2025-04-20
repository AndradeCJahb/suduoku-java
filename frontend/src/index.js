import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './index.css';
import SudokuGame from './components/SudokuGame';
import PuzzleSelection from './components/PuzzleSelection';
import webSocketManager from './components/WebSocketManager';

const wsUrl = 'wss://suduoku-java.onrender.com/ws';
//const wsUrl = 'ws://localhost:8080/ws';
console.log(`Connecting to WebSocket server at ${wsUrl}`);
webSocketManager.connect(wsUrl);

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PuzzleSelection />} />
        <Route path="/puzzle/:puzzleId" element={<SudokuGame />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);