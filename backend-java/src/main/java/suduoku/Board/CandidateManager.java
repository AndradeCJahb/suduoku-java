package suduoku.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages candidate logic and validation for Sudoku solving.
 */
public class CandidateManager {

    /**
     * Updates candidates for all empty cells based on Sudoku rules.
     * A candidate is valid if it doesn't appear in the same row, column, or 3x3 box.
     *
     * @param board The 9x9 Sudoku board
     */
    public static void updateAllCandidates(Cell[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col].isEmpty()) {
                    updateCandidatesForCell(board, row, col);
                }
            }
        }
    }

    /**
     * Updates candidates for a single cell.
     *
     * @param board The 9x9 Sudoku board
     * @param row   The row index (0-8)
     * @param col   The column index (0-8)
     */
    public static void updateCandidatesForCell(Cell[][] board, int row, int col) {
        if (!board[row][col].isEmpty()) {
            board[row][col].clearCandidates();
            return;
        }

        Set<Integer> invalid = getInvalidCandidates(board, row, col);
        Set<Integer> candidates = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        candidates.removeAll(invalid);
        board[row][col].setCandidates(candidates);
    }

    /**
     * Gets the set of numbers that cannot be in this cell.
     *
     * @param board The 9x9 Sudoku board
     * @param row   The row index (0-8)
     * @param col   The column index (0-8)
     * @return Set of invalid candidates
     */
    private static Set<Integer> getInvalidCandidates(Cell[][] board, int row, int col) {
        Set<Integer> invalid = new HashSet<>();

        // Check row
        for (int j = 0; j < 9; j++) {
            if (board[row][j].getValue() != null) {
                invalid.add(board[row][j].getValue());
            }
        }

        // Check column
        for (int i = 0; i < 9; i++) {
            if (board[i][col].getValue() != null) {
                invalid.add(board[i][col].getValue());
            }
        }

        // Check 3x3 box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if (board[i][j].getValue() != null) {
                    invalid.add(board[i][j].getValue());
                }
            }
        }

        return invalid;
    }

    /**
     * Gets all possible candidates for a cell (used for initialization or validation).
     *
     * @param board The 9x9 Sudoku board
     * @param row   The row index (0-8)
     * @param col   The column index (0-8)
     * @return Set of valid candidates for this cell
     */
    public static Set<Integer> getPossibleCandidates(Cell[][] board, int row, int col) {
        Set<Integer> possible = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        possible.removeAll(getInvalidCandidates(board, row, col));
        return possible;
    }

    /**
     * Eliminates a candidate from a cell.
     *
     * @param board     The 9x9 Sudoku board
     * @param row       The row index (0-8)
     * @param col       The column index (0-8)
     * @param candidate The candidate to eliminate (1-9)
     */
    public static void eliminateCandidate(Cell[][] board, int row, int col, int candidate) {
        if (board[row][col].isEmpty()) {
            board[row][col].getCandidates().remove(candidate);
        }
    }

    /**
     * Returns the number of candidates in a cell.
     *
     * @param board The 9x9 Sudoku board
     * @param row   The row index (0-8)
     * @param col   The column index (0-8)
     * @return Number of candidates
     */
    public static int getCandidateCount(Cell[][] board, int row, int col) {
        return board[row][col].getCandidates().size();
    }

    /**
     * Checks if a value is a valid candidate for a cell.
     *
     * @param board The 9x9 Sudoku board
     * @param row   The row index (0-8)
     * @param col   The column index (0-8)
     * @param value The value to check (1-9)
     * @return True if the value is a valid candidate
     */
    public static boolean isValidCandidate(Cell[][] board, int row, int col, int value) {
        Set<Integer> invalid = getInvalidCandidates(board, row, col);
        return value >= 1 && value <= 9 && !invalid.contains(value);
    }
}

