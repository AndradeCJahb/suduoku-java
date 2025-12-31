package suduoku.Board;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single cell in the Sudoku board.
 * Each cell has a value (1-9, or null for empty) and a set of candidates.
 */
@Getter
public class Cell {
    @Setter
    private Integer value;
    private final boolean isEditable;
    private final Set<Integer> candidates;

    /**
     * Creates a cell with the given value and editability.
     *
     * @param value      The cell value (1-9) or null for empty
     * @param isEditable Whether this cell can be edited by the player
     */
    public Cell(Integer value, boolean isEditable) {
        this.value = value;
        this.isEditable = isEditable;
        this.candidates = new HashSet<>();
    }

    /**
     * Toggles a candidate on or off.
     *
     * @param candidate The candidate number (1-9)
     */
    public void toggleCandidate(int candidate) {
        if (candidate < 1 || candidate > 9) {
            throw new IllegalArgumentException("Candidate must be between 1 and 9");
        }

        if (candidates.contains(candidate)) {
            candidates.remove(candidate);
        } else {
            candidates.add(candidate);
        }
    }

    /**
     * Sets multiple candidates at once.
     *
     * @param candidates The set of candidates to set
     */
    public void setCandidates(Set<Integer> candidates) {
        this.candidates.clear();
        this.candidates.addAll(candidates);
    }

    /**
     * Clears cell candidates and value.
     */
    public void clearCell() {
        this.value = null;
        this.candidates.clear();
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.valueOf(value);
        }
        return candidates.isEmpty() ? "." : candidates.toString();
    }
}

