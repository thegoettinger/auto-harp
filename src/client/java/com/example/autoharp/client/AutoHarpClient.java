package com.example.autoharp.client;

import com.example.autoharp.HarpNoteTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Plays harp notes once per server-confirmed appearance, using the Q action. */
public class AutoHarpClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("AutoHarp");
	private static final int DROP_ONE = 0;
	private static AutoHarpClient instance;

	private final HarpNoteTracker notes = new HarpNoteTracker();
	private ChestMenu trackedMenu;

	@Override
	public void onInitializeClient() {
		instance = this;
		LOGGER.info("AutoHarp loaded (server-confirmed note tracking)");
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			ChestMenu menu = activeMenu(mc);
			if (menu != null) playPending(mc, menu);
		});
	}

	/** Called after vanilla has applied a server slot update on the client thread. */
	public static void onServerSlot(ClientboundContainerSetSlotPacket packet) {
		if (instance == null) return;
		Minecraft mc = Minecraft.getInstance();
		ChestMenu menu = instance.activeMenu(mc);
		if (menu == null || packet.getContainerId() != menu.containerId) return;

		ItemStack stack = packet.getItem();
		instance.notes.observeServerSlot(packet.getSlot(), stack.is(Items.QUARTZ_BLOCK), stack.isEmpty());
		instance.playPending(mc, menu);
	}

	/** Full resyncs must use the same latch as individual slot updates. */
	public static void onServerContent(ClientboundContainerSetContentPacket packet) {
		if (instance == null) return;
		Minecraft mc = Minecraft.getInstance();
		ChestMenu menu = instance.activeMenu(mc);
		if (menu == null || packet.containerId() != menu.containerId) return;

		for (int slot = HarpNoteTracker.FIRST_SLOT;
				slot < HarpNoteTracker.FIRST_SLOT + HarpNoteTracker.KEY_COUNT; slot++) {
			if (slot >= packet.items().size()) break;
			ItemStack stack = packet.items().get(slot);
			instance.notes.observeServerSlot(slot, stack.is(Items.QUARTZ_BLOCK), stack.isEmpty());
		}
		instance.playPending(mc, menu);
	}

	private ChestMenu activeMenu(Minecraft mc) {
		ChestMenu menu = null;
		if (mc.player != null && mc.gameMode != null
				&& mc.screen instanceof ContainerScreen screen
				&& screen.getMenu().getRowCount() == 6
				&& screen.getTitle().getString().replaceAll("§.", "").startsWith("Harp -")
				&& mc.player.containerMenu == screen.getMenu()) {
			menu = screen.getMenu();
		}
		// Compare menu identity, not just the ID (IDs can be reused after reopening).
		if (menu != trackedMenu) {
			notes.reset();
			trackedMenu = menu;
		}
		return menu;
	}

	private void playPending(Minecraft mc, ChestMenu menu) {
		// Vanilla ignores THROW while carrying an item. Don't consume a note then.
		if (!menu.getCarried().isEmpty()) return;
		for (int slot = HarpNoteTracker.FIRST_SLOT;
				slot < HarpNoteTracker.FIRST_SLOT + HarpNoteTracker.KEY_COUNT; slot++) {
			if (menu.getContainer().getItem(slot).is(Items.QUARTZ_BLOCK)
					&& notes.consumePending(slot)) {
				mc.gameMode.handleContainerInput(
						menu.containerId, slot, DROP_ONE, ContainerInput.THROW, mc.player);
			}
		}
	}
}
