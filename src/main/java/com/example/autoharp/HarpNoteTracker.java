package com.example.autoharp;

import java.util.Arrays;

/** Tracks authoritative note transitions, never the client's predicted inventory. */
public final class HarpNoteTracker {
	// Seven playable keys in the fifth row of the six-row harp menu.
	public static final int FIRST_SLOT = 37;
	public static final int KEY_COUNT = 7;

	private final boolean[] consumed = new boolean[KEY_COUNT];
	private final boolean[] pending = new boolean[KEY_COUNT];

	public void observeServerSlot(int slot, boolean quartz, boolean empty) {
		int key = slot - FIRST_SLOT;
		if (key < 0 || key >= KEY_COUNT || empty) return;

		if (quartz) {
			if (!consumed[key]) pending[key] = true;
		} else {
			// Only a real, nonempty server replacement rearms the key.
			consumed[key] = false;
			pending[key] = false;
		}
	}

	public boolean consumePending(int slot) {
		int key = slot - FIRST_SLOT;
		if (key < 0 || key >= KEY_COUNT || !pending[key] || consumed[key]) return false;
		pending[key] = false;
		consumed[key] = true;
		return true;
	}

	public void reset() {
		Arrays.fill(consumed, false);
		Arrays.fill(pending, false);
	}
}
