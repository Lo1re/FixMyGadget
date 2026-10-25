package model;

import com.fixmygadget.model.RepairStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.fixmygadget.model.RepairStatus.ACCEPTED;
import static com.fixmygadget.model.RepairStatus.DONE;
import static com.fixmygadget.model.RepairStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepairStatusTest {

    @Test
    void acceptedCanMoveOnlyToInProgress() {
        assertTrue(ACCEPTED.canMoveTo(IN_PROGRESS));
        assertFalse(ACCEPTED.canMoveTo(DONE));
        assertFalse(ACCEPTED.canMoveTo(ACCEPTED));
    }

    @Test
    void inProgressCanMoveOnlyToDone() {
        assertTrue(IN_PROGRESS.canMoveTo(DONE));
        assertFalse(IN_PROGRESS.canMoveTo(ACCEPTED));
        assertFalse(IN_PROGRESS.canMoveTo(IN_PROGRESS));
    }

    @Test
    void doneIsFinal() {
        assertFalse(DONE.canMoveTo(ACCEPTED));
        assertFalse(DONE.canMoveTo(IN_PROGRESS));
        assertFalse(DONE.canMoveTo(DONE));
    }

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "ACCEPTED, ACCEPTED, false",
            "ACCEPTED, IN_PROGRESS, true",
            "ACCEPTED, DONE, false",
            "IN_PROGRESS, ACCEPTED, false",
            "IN_PROGRESS, IN_PROGRESS, false",
            "IN_PROGRESS, DONE, true",
            "DONE, ACCEPTED, false",
            "DONE, IN_PROGRESS, false",
            "DONE, DONE, false"
    })
    void fullTransitionMatrix(RepairStatus from, RepairStatus to, boolean expected) {
        assertEquals(expected, from.canMoveTo(to));
    }

    @Test
    void nullTargetIsNotAllowed() {
        assertFalse(ACCEPTED.canMoveTo(null));
        assertFalse(IN_PROGRESS.canMoveTo(null));
        assertFalse(DONE.canMoveTo(null));
    }

    @Test
    void toStringReturnsLabel() {
        assertEquals("In progress", IN_PROGRESS.toString());
    }
}
