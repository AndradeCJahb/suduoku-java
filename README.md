# Suduoku

A real-time collaborative Sudoku game where multiple players can solve puzzles together.

Hosted at https://suduoku.vercel.app/.

<img src="./frontend/public/favicon.ico" alt="Suduoku Logo" width="64" height="64">

## Features

- 🎮 Real-time collaborative puzzle solving
- 💬 Live chat with other players
- 🧩 Multiple puzzles with varying difficulty levels
- 🔄 Automatic synchronization across all connected clients
- ✅ Solution verification and error highlighting
- 🎨 Unique player colors and auto-generated names

## Contributing

### Getting Started

1. Install `mvn`, `docker`, `npm`

2. Run each with `-v` to ensure install

    - Docker v28.0.4 used
    - Apache Maven v3.9.9 w/ java 21 used
    - npm v10.9.2 used

3. Clone repository and build using docker

    -   `git clone https://github.com/AndradeCJahb/suduoku-java.git`

    -   `cd backend-java`

    -   `docker build -t suduoku-backend .`

4. Begin local Docker Host

    -   `docker run -p 8080:8080 suduoku-backend`

5. Navigate to `frontend/` and install dependencies

    -   `cd ../frontend`

    -   `npm install`

6. Run frontend server `npm start`

## Technologies

- **Frontend**: React.js, CSS
- **Backend**: Java, Maven, Python, SQLite, Docker

## License

[MIT License](LICENSE)

---

*Suduoku is a collaborative project created as a way to enjoy Sudoku with friends no matter where they are.*