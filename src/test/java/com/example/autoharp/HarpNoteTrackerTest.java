package com.example.autoharp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HarpNoteTrackerTest {
	private final HarpNoteTracker tracker = new HarpNoteTracker();
	private static final int SLOT = HarpNoteTracker.FIRST_SLOT;

	@Test
	void firstNoteIsConsumedOnlyOnce() {
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
		assertFalse(tracker.consumePending(SLOT));
	}

	@Test
	void repeatedQuartzUpdatesAndFullResyncDoNotDoubleHit() {
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
		for (int i = 0; i < 100; i++) {
			tracker.observeServerSlot(SLOT, true, false);
			assertFalse(tracker.consumePending(SLOT));
		}
	}

	@Test
	void disappearanceAndRestorationDoNotRearmTheNote() {
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
		tracker.observeServerSlot(SLOT, false, true);
		tracker.observeServerSlot(SLOT, true, false);
		assertFalse(tracker.consumePending(SLOT));
	}

	@Test
	void serverBackgroundAllowsTheNextNoteWithoutCooldown() {
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
		tracker.observeServerSlot(SLOT, false, false);
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
	}

	@Test
	void chordKeysAreIndependent() {
		for (int slot = SLOT; slot < SLOT + HarpNoteTracker.KEY_COUNT; slot++) {
			tracker.observeServerSlot(slot, true, false);
		}
		for (int slot = SLOT; slot < SLOT + HarpNoteTracker.KEY_COUNT; slot++) {
			assertTrue(tracker.consumePending(slot));
			assertFalse(tracker.consumePending(slot));
		}
	}

	@Test
	void fallingNotesBordersAndPlayerInventoryAreIgnored() {
		for (int slot : new int[]{-1, 0, 28, 36, 44, 54, 80}) {
			tracker.observeServerSlot(slot, true, false);
			assertFalse(tracker.consumePending(slot));
		}
	}

	@Test
	void noteCanWaitUntilTheCursorIsEmpty() {
		tracker.observeServerSlot(SLOT, true, false);
		// No consume while the cursor is occupied. Repeated packets don't lose the pending note.
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
		assertFalse(tracker.consumePending(SLOT));
	}

	@Test
	void expiredPendingNoteIsCancelledByServerBackground() {
		tracker.observeServerSlot(SLOT, true, false);
		tracker.observeServerSlot(SLOT, false, false);
		assertFalse(tracker.consumePending(SLOT));
	}

	@Test
	void reopeningTheMenuResetsConsumedNotes() {
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
		tracker.reset();
		assertFalse(tracker.consumePending(SLOT));
		tracker.observeServerSlot(SLOT, true, false);
		assertTrue(tracker.consumePending(SLOT));
	}

	@Test
	void reopeningAlsoCancelsPendingNotes() {
		tracker.observeServerSlot(SLOT, true, false);
		tracker.reset();
		assertFalse(tracker.consumePending(SLOT));
	}
}
