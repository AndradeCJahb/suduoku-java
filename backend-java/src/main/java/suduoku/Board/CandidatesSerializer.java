package suduoku.Board;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles serialization and deserialization of candidates to/from a compact format.
 * Format: space-separated cells, each cell format is "row,col:1,2,3" (colon-separated candidates)
 * Example: "0,0:1,2,3 0,1:5,6"
 */
public class CandidatesSerializer {

    /**
     * Converts candidates from a Cell board to a compact string format.
     *
     * @param board The 9x9 Cell array
     * @return Compact string representation of candidates (or empty string if none exist)
     */
    public static String convertBoardToCandidatesString(Cell[][] board) {
        StringBuilder sb = new StringBuilder();
        boolean hasAny = false;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Cell cell = board[i][j];
                if (!cell.getCandidates().isEmpty()) {
                    if (hasAny) {
                        sb.append(" ");
                    }
                    sb.append(i).append(",").append(j).append(":");
                    cell.getCandidates().stream()
                            .sorted()
                            .forEach(c -> sb.append(c).append(","));
                    // Remove trailing comma
                    sb.setLength(sb.length() - 1);
                    hasAny = true;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Converts a candidates string back to a Cell board.
     *
     * @param candidatesString The compact string representation
     * @param board The 9x9 Cell array to populate with candidates
     */
    public static void convertCandidatesStringToBoard(String candidatesString, Cell[][] board) {
        if (candidatesString == null || candidatesString.isEmpty()) {
            return;
        }

        String[] cells = candidatesString.split(" ");
        for (String cellStr : cells) {
            String[] parts = cellStr.split(":");
            if (parts.length == 2) {
                String[] coords = parts[0].split(",");
                int row = Integer.parseInt(coords[0]);
                int col = Integer.parseInt(coords[1]);

                Set<Integer> candidates = new HashSet<>();
                String[] candidateStrs = parts[1].split(",");
                for (String candStr : candidateStrs) {
                    candidates.add(Integer.parseInt(candStr));
                }

                board[row][col].setCandidates(candidates);
            }
        }
    }
}

