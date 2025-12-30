package suduoku.Board;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles serialization and deserialization of the Sudoku board to/from SDX format.
 * SDX format: space-separated tokens where each token is either:
 * - "u<digit>" for uneditable cells (given values)
 * - "<digit>" for editable filled cells
 * - "0" for empty editable cells
 */
public class BoardSerializer {

    /**
     * Converts an SDX string to a Cell board.
     *
     * @param sdx The SDX format string
     * @return A 9x9 Cell array
     */
    public static Cell[][] convertSDXToBoard(String sdx) {
        String[] tokens = sdx.split(" ");
        Cell[][] board = new Cell[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String token = tokens[i * 9 + j];
                if (token.charAt(0) == 'u') {
                    // Uneditable cell
                    int value = Integer.parseInt(token.substring(1));
                    board[i][j] = new Cell(value, false);
                } else {
                    // Editable cell
                    Integer value = token.equals("0") ? null : Integer.valueOf(token);
                    board[i][j] = new Cell(value, true);
                }
            }
        }
        return board;
    }

    /**
     * Converts a Cell board back to SDX format string.
     *
     * @param board The 9x9 Cell array
     * @return The SDX format string
     */
    public static String convertBoardToSDX(Cell[][] board) {
        StringBuilder sdx = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!board[i][j].isEditable()) {
                    sdx.append("u").append(board[i][j].getValue()).append(" ");
                } else {
                    Integer value = board[i][j].getValue();
                    sdx.append(value == null ? "0" : value).append(" ");
                }
            }
        }
        return sdx.toString().trim();
    }
}

