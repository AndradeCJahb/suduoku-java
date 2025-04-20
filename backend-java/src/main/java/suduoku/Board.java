package suduoku;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Board {
    private static final String DB_URL = "jdbc:sqlite:/app/sudokugames.db";
    private int puzzleId;
    private String title;
    private String difficulty;
    private Integer[][][] board;
    private Integer[][][] solution;
    private ArrayList<int[]> incorrectCells;
    
    public Board(int puzzleId) {
        this.puzzleId = puzzleId;
        this.incorrectCells = new ArrayList<int[]>();

        String query = "SELECT title, difficulty, sdx FROM puzzles WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, puzzleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.board = convertSDXToBoard(rs.getString("sdx"));
                this.title = rs.getString("title");
                this.difficulty = rs.getString("difficulty");
            } else {
                this.board = new Integer[9][9][2];
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String solutionQuery = "SELECT sdx_solution FROM puzzles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(solutionQuery)) {
            stmt.setInt(1, puzzleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.solution = convertSDXToBoard(rs.getString("sdx_solution"));
            } else {
                this.solution = new Integer[9][9][2];
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer[][][] convertSDXToBoard(String sdx) {
        String[] tokens = sdx.split(" ");
        Integer[][][] board = new Integer[9][9][2];
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String token = tokens[i * 9 + j];
                if (token.charAt(0) == 'u') {
                    board[i][j][0] = Integer.parseInt(token.substring(1));
                    board[i][j][1] = 1; // 1 means uneditable
                } else {
                    board[i][j][0] = token.equals("0") ? null : Integer.valueOf(token);
                    board[i][j][1] = 0; // 0 means editable
                }
            }
        }
        return board;
    }

    private String convertBoardToSDX() {
        StringBuilder sdx = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j][1] == 1) {
                    sdx.append("u").append(board[i][j][0]).append(" ");
                } else {
                    sdx.append(board[i][j][0] == null ? "0" : board[i][j][0]).append(" ");
                }
            }
        }
        return sdx.toString().trim();
    }

    public JSONObject getBoardJSON() {
        JSONObject jsonResponse = new JSONObject();
        JSONArray rows = new JSONArray();
        
        // Convert the board to JSON format
        for (int i = 0; i < 9; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < 9; j++) {
                JSONObject cell = new JSONObject();
                
                // Convert null values to empty strings
                String value = (board[i][j][0] == null) ? "" : board[i][j][0].toString();
                // 0 means editable, 1 means uneditable
                boolean isEditable = (board[i][j][1] == 0);
                
                cell.put("value", value);
                cell.put("isEditable", isEditable);
                
                row.put(cell);
            }
            rows.put(row);
        }
        
        jsonResponse.put("type", "updatePuzzle");
        jsonResponse.put("board", rows);
        jsonResponse.put("title", this.title + "  " + this.difficulty.toUpperCase());
        return jsonResponse;
    }

    public void setCell(int row, int col, int value) {
        board[row][col][0] = (value == 0) ? null : value;
        updateDB();
    }

    public void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j][1] == 0) { // Only clear editable cells
                    board[i][j][0] = null;
                }
            }
        }
        updateDB();
    }

    private void updateDB() {
        String sdx = convertBoardToSDX();
        String query = "UPDATE puzzles SET sdx = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sdx);
            stmt.setInt(2, this.puzzleId); // Assuming you want to update the puzzle with ID 1
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateIncorrectCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if(board[i][j][0] != null){
                    if (!board[i][j][0].equals(solution[i][j][0])) { // Only check editable cells
                        incorrectCells.add(new int[]{i, j});
                    }
                }
            }
        }
    }

    public void setIncorrectCells(ArrayList<int[]> incorrectCells) {
        this.incorrectCells = incorrectCells;
    }

    public ArrayList<int[]> getIncorrectCells() {
        return this.incorrectCells;
    }

    public void clearIncorrectCells() {
        this.incorrectCells.clear();
    }

    public void incorrectCellsChange(int row, int col) {
        for (int i = 0; i < incorrectCells.size(); i++) {
            int[] cell = incorrectCells.get(i);
            if (cell[0] == row && cell[1] == col) {
                incorrectCells.remove(i);
                return;
            }
        }
    }

    public boolean isSolved() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j][0] == null || !board[i][j][0].equals(solution[i][j][0])) {
                    return false;
                }
            }
        }
        return true;
    }
}

