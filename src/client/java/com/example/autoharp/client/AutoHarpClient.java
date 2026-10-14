package com.example.autoharp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-solver for Melody's Harp (Minecraft 26.1.2).
 *
 * Watches every slot of the harp GUI and clicks a slot the moment
 * it turns into a QUARTZ_BLOCK (a note that is ready to be hit).
 *
 * DEBUG = true writes what the mod sees to the game log
 * (Modrinth -> Logs tab, search for "AutoHarp").
 */
public class AutoHarpClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger("AutoHarp");
	private static final boolean DEBUG = false;

	private static final String TITLE_MARKER = "Harp";
	private static final int CLICK_BUTTON = 0; // try 2 with ContainerInput.CLONE if PICKUP misbehaves

	private boolean[] wasQuartz = new boolean[0];
	private int currentContainerId = -1;
	private Screen lastScreen = null;

	@Override
	public void onInitializeClient() {
		LOGGER.info("AutoHarp loaded");
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(Minecraft mc) {
		if (mc.player == null || mc.gameMode == null) return;

		Screen current = mc.screen;
		if (DEBUG && current != lastScreen && current != null) {
			LOGGER.info("Screen opened: {}", current.getClass().getName());
		}
		lastScreen = current;

		if (!(current instanceof ContainerScreen screen)) {
			currentContainerId = -1;
			return;
		}

		ChestMenu menu = screen.getMenu();
		String title = screen.getTitle().getString().replaceAll("§.", "");
		int size = menu.getRowCount() * 9;

		// New window -> reset state and log what it is
		if (menu.containerId != currentContainerId) {
			currentContainerId = menu.containerId;
			wasQuartz = new boolean[size];
			if (DEBUG) {
				LOGGER.info("Container '{}' with {} rows (harp: {})",
						title, menu.getRowCount(), title.contains(TITLE_MARKER));
			}
		}

		if (!title.contains(TITLE_MARKER)) return;

		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = menu.getContainer().getItem(slot);
			boolean quartz = stack.is(Items.QUARTZ_BLOCK);

			// Click once when a slot turns into quartz
			if (quartz && !wasQuartz[slot]) {
				mc.gameMode.handleContainerInput(
						menu.containerId, slot, CLICK_BUTTON, ContainerInput.PICKUP, mc.player);
				if (DEBUG) {
					LOGGER.info("Clicked slot {} (row {}, col {})", slot, slot / 9, slot % 9);
				}
			}
			wasQuartz[slot] = quartz;
		}
	}
}
