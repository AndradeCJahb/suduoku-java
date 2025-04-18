# Suduoku

A real-time collaborative Sudoku game where multiple players can solve puzzles together.

![Suduoku Logo](./frontend/public/favicon.ico)

## Features

- 🎮 Real-time collaborative puzzle solving
- 💬 Live chat with other players
- 🧩 Multiple puzzles with varying difficulty levels
- 🔄 Automatic synchronization across all connected clients
- ✅ Solution verification and error highlighting
- 🎨 Unique player colors and auto-generated names

## Project Structure

```
suduoku-java/
├── backend-java/
│   ├── src/ 
│   │   └── main/java/suduoku
│   │       ├── Board.java              
│   │       ├── Player.java
│   │       ├── SuduokuBackend.java
│   │       └── WebSocketServer.java
│   ├── pom.xml                         
│   └── sudokugames.db                  
├── frontend/                           # React frontend
│   ├── public/                         
│   │   ├── favicon.ico
│   │   └── index.html
│   └── src/
│       ├── components/
│       │   ├── Header.js
│       │   ├── PuzzleSelection.js
│       │   └── SudokuGame.js
│       ├── index.js                    # Main React application
│       └── index.css                   
└── sudoku_scraper/                     
    └── sudoku_scraper.py
```

## Technologies

- **Frontend**: React.js, CSS
- **Backend**: Java, Maven, Python, SQLite

---

*Suduoku is a collaborative project created as a way to enjoy Sudoku with friends no matter where they are.*