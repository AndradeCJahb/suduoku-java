package suduoku.Board;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import static suduoku.Constants.DB_URL;

/**
 * Represents a complete 9x9 Sudoku board with solution tracking.
 * Manages the current state, solution, and incorrect cells.
 */
public class SudokuBoard {
    private final int puzzleId;
    private String title;
    private String difficulty;
    private Cell[][] board;
    private Cell[][] solution;

    @Getter
    private final List<int[]> incorrectCells;

    /**
     * Loads a Sudoku puzzle from the database.
     *
     * @param puzzleId The ID of the puzzle to load
     */
    public SudokuBoard(int puzzleId) {
        this.puzzleId = puzzleId;
        this.incorrectCells = new ArrayList<>();
        loadPuzzleFromDB();
        loadSolutionFromDB();
    }

    /**
     * Loads the puzzle (board state) from the database.
     */
    private void loadPuzzleFromDB() {
        String query = "SELECT title, difficulty, sdx, COALESCE(candidates, '') as candidates FROM puzzles WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, puzzleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.board = BoardSerializer.convertSDXToBoard(rs.getString("sdx"));
                this.title = rs.getString("title");
                this.difficulty = rs.getString("difficulty");

                // Load candidates if they exist
                String candidatesStr = rs.getString("candidates");
                if (candidatesStr != null && !candidatesStr.isEmpty()) {
                    CandidatesSerializer.convertCandidatesStringToBoard(candidatesStr, this.board);
                }
            } else {
                this.board = new Cell[9][9];
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the solution from the database.
     */
    private void loadSolutionFromDB() {
        String query = "SELECT sdx_solution FROM puzzles WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, puzzleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.solution = BoardSerializer.convertSDXToBoard(rs.getString("sdx_solution"));
            } else {
                this.solution = new Cell[9][9];
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts the board to JSON format for sending to the frontend.
     * Includes cell values, editability, and candidates.
     */
    public JSONObject getBoardJSON() {
        JSONObject jsonResponse = new JSONObject();
        JSONArray rows = new JSONArray();

        for (int i = 0; i < 9; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < 9; j++) {
                JSONObject cell = new JSONObject();
                Cell boardCell = board[i][j];

                cell.put("value", boardCell.getValue() == null ? "" : boardCell.getValue());
                cell.put("isEditable", boardCell.isEditable());

                // Include candidates as an array
                JSONArray candidatesArray = new JSONArray(boardCell.getCandidates());
                cell.put("candidates", candidatesArray);

                row.put(cell);
            }
            rows.put(row);
        }

        jsonResponse.put("type", "updatePuzzle");
        jsonResponse.put("board", rows);
        jsonResponse.put("title", this.title + "  " + this.difficulty.toUpperCase());
        return jsonResponse;
    }

    /**
     * Sets a cell value and updates candidates.
     *
     * @param row   The row index (0-8)
     * @param col   The column index (0-8)
     * @param value The value to set (1-9, or 0 to clear)
     */
    public void setCell(int row, int col, int value) {
        if (board[row][col].isEditable()) {
            board[row][col].setValue(value);
            updateDB();
        }
    }

    /**
     * Toggles a candidate for a cell.
     *
     * @param row       The row index (0-8)
     * @param col       The column index (0-8)
     * @param candidate The candidate to toggle (1-9)
     */
    public void toggleCandidate(int row, int col, int candidate) {
        if (board[row][col].isEditable()) {
            board[row][col].toggleCandidate(candidate);
            updateDB();
        }
    }

    /**
     * Clears all editable cells.
     */
    public void clearBoard() {
        for(Cell[] row : board) {
            for(Cell cell : row) {
                if(cell.isEditable()) {
                    cell.setValue(null);
                    cell.clearCell();
                }
            }
        }
        updateDB();
    }

    /**
     * Persists the board state to the database.
     */
    private void updateDB() {
        String sdx = BoardSerializer.convertBoardToSDX(board);
        String candidates = CandidatesSerializer.convertBoardToCandidatesString(board);
        String query = "UPDATE puzzles SET sdx = ?, candidates = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sdx);
            stmt.setString(2, candidates);
            stmt.setInt(3, this.puzzleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the list of incorrect cells by comparing with the solution.
     */
    public void updateIncorrectCells() {
        incorrectCells.clear();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Integer boardValue = board[i][j].getValue();
                Integer solutionValue = solution[i][j].getValue();
                if ((boardValue != null && boardValue != 0) && !boardValue.equals(solutionValue)) {
                    incorrectCells.add(new int[] { i, j });
                }
            }
        }
    }

    /**
     * Clears the incorrect cells list.
     */
    public void clearIncorrectCells() {
        this.incorrectCells.clear();
    }

    /**
     * Removes a specific cell from the incorrect cells list.
     */
    public void removeIncorrectCell(int[] cellToRemove) {
        for (int i = 0; i < incorrectCells.size(); i++) {
            if (Arrays.equals(incorrectCells.get(i), cellToRemove)) {
                incorrectCells.remove(i);
                return;
            }
        }
    }

    /**
     * Checks if the puzzle is completely solved.
     */
    public boolean isSolved() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Integer boardValue = board[i][j].getValue();
                Integer solutionValue = solution[i][j].getValue();
                if (boardValue == null || !boardValue.equals(solutionValue)) {
                    return false;
                }
            }
        }
        return true;
    }
}

