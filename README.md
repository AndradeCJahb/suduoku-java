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

## Table of Contents

- Project Structure
- Technologies
- License

## Project Structure

```
suduoku/
├── backend/                    # Backend server code
│   ├── server.js               # WebSocket server implementation
│   └── sudokugames.db          # SQLite database
├── frontend/                   # React frontend
│   ├── public/                 # Static assets
│   └── src/                    # Source code
│       ├── index.js            # Main React application
│       └── index.css           # Styles
└── sudoku_conversion/          # Tools for puzzle conversion and import
    ├── sudoku_sdx/             # Puzzle files in SDX format
    └── sudoku_sdx_solutions/   # Solution files
```

## Technologies

- **Frontend**: React.js, CSS
- **Backend**: Node.js, WebSocket (ws), SQLite
- **Database**: SQLite3
- **Deployment**: Ngrok (for development sharing)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

*Suduoku is a collaborative project created as a way to enjoy Sudoku with friends no matter where they are.*
