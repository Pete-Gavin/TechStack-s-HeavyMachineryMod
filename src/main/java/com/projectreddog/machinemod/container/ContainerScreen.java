package com.projectreddog.machinemod.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.projectreddog.machinemod.tileentities.TileEntityScreen;

public class ContainerScreen extends Container {

	protected TileEntityScreen screen;
	private int lastFuelStorage;

	public ContainerScreen(InventoryPlayer inventoryPlayer, TileEntityScreen screen) {
		this.screen = screen;
		// slot 0 = north
		// slot 1 = east
		// slot 2 = south
		// slot 3 = west
		addSlotToContainer(new Slot(screen, 0, 81, 30));// north = 0

		addSlotToContainer(new Slot(screen, 1, 114, 64)); // east

		addSlotToContainer(new Slot(screen, 2, 81, 98)); // south

		addSlotToContainer(new Slot(screen, 3, 44, 64)); // west

		addSlotToContainer(new Slot(screen, 4, 81, 64));// / center

		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return screen.isUseableByPlayer(player);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 139 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 197));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			// merges the item into player inventory since its in the Entity
			if (slot < 5) {
				if (!this.mergeItemStack(stackInSlot, 5, this.inventorySlots.size(), true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else if (!this.mergeItemStack(stackInSlot, 4, 5, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
	
	
	
	
	
	
	
	
	
	

}
