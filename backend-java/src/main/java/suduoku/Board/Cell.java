package suduoku.Board;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single cell in the Sudoku board.
 * Each cell has a value (1-9, or null for empty) and a set of candidates.
 */
public class Cell {
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

    // Getters and Setters
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = (value == 0) ? null : value;
        // Clear candidates when a value is set
        if (this.value != null) {
            this.candidates.clear();
        }
    }

    public boolean isEditable() {
        return isEditable;
    }

    public Set<Integer> getCandidates() {
        return candidates;
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

        // Don't allow candidates if cell has a value
        if (this.value != null) {
            return;
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
        if (this.value == null) {
            this.candidates.clear();
            this.candidates.addAll(candidates);
        }
    }

    /**
     * Clears all candidates.
     */
    public void clearCandidates() {
        this.candidates.clear();
    }

    /**
     * Returns whether this cell is empty (no value).
     */
    public boolean isEmpty() {
        return this.value == null;
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.valueOf(value);
        }
        return candidates.isEmpty() ? "." : candidates.toString();
    }
}

