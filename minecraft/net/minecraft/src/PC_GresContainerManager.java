package net.minecraft.src;


/**
 * Container used by GRES for inventory slots in the GUI.
 * 
 * @author XOR19
 */
public class PC_GresContainerManager extends Container {

	/** The player who opened the gui */
	public EntityPlayer thePlayer;
	/** the open gui */
	public PC_IGresGui gresGui;

	private static final int playerSlots = 9 * 4;
	/** Upper part of the player's inventory (3x9) */
	public Slot[][] inventoryPlayerUpper = new Slot[9][3];
	/** Lower part of the player's inventory (1x9, aka QuickBar) */
	public Slot[][] inventoryPlayerLower = new Slot[9][1];
	
	/**
	 * Gres Container for player
	 * 
	 * @param player the player
	 */
	public PC_GresContainerManager(EntityPlayer player) {
		thePlayer = player;
		if (thePlayer != null) {
			// lower player inventory
			for (int i = 0; i < 9; i++) {
				inventoryPlayerLower[i][0] = new Slot(player.inventory, i, -3000, 0);
				addSlot(inventoryPlayerLower[i][0]);
			}

			// upper player inventory
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 3; j++) {
					inventoryPlayerUpper[i][j] = new Slot(player.inventory, i + j * 9 + 9, -3000, 0);
					addSlot(inventoryPlayerUpper[i][j]);
				}
			}
		}

	}

	/**
	 * Set the GRES GUI (gui which uses this container)
	 * 
	 * @param gresGui
	 */
	public void setGresGui(PC_IGresGui gresGui) {
		this.gresGui = gresGui;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void addSlot(Slot slot) {
		super.addSlot(slot);
	}

	@Override
	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer) {

		if (((PC_GresGui) gresGui).gui instanceof PCco_GuiCraftingTool) {
			if ((PC_Utils.isCreative() || PCco_SlotDirectCrafting.survivalCheating)) {
				return;
			}
		}

		super.retrySlotClick(par1, par2, par3, par4EntityPlayer);
	}

	@Override
	public ItemStack transferStackInSlot(int slotIndex) {

		// if (((PC_GresGui) gresGui).gui instanceof PCco_GuiCraftingTool){
		// }

		if (slotIndex < playerSlots && !gresGui.canShiftTransfer()) {
			return null;
		}

		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotIndex < playerSlots) {

				if (!mergeItemStack(itemstack1, playerSlots, inventorySlots.size(), false)) {
					return null;
				} else {
					slot.onPickupFromSlot(itemstack);
				}

			} else if (!mergeItemStack(itemstack1, 0, playerSlots, false)) {
				return null;
			} else {
				slot.onPickupFromSlot(itemstack);
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack(null);
				slot.onSlotChanged();
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	/**
	 * Set a slot at position in inventorySlots list. Slot's slotNumber will be
	 * set to the ID.
	 * 
	 * @param id position in the list
	 * @param newSlot the added slot.
	 */
	public void setSlot(int id, Slot newSlot) {
		if (newSlot != null) {
			inventorySlots.set(id, newSlot);
			inventoryItemStacks.set(id, null);
			newSlot.slotNumber = id;
		}
	}

	/**
	 * Remove a slot with ID
	 * 
	 * @param id slot id
	 */
	public void removeSlot(int id) {
		int i = id;
		for (; i < inventorySlots.size() - 1; i++) {
			Slot s = (Slot) inventorySlots.get(i + 1);
			if (s != null) {
				s.slotNumber = i;
			}
			inventorySlots.set(i, s);
			inventoryItemStacks.set(i, inventorySlots.get(i + 1));
		}
		if (i < inventorySlots.size()) {
			inventorySlots.remove(i);
			inventoryItemStacks.remove(i);
		}
	}


	private int getLimit(Slot slot, int a, boolean flag) {
		if (flag) {
			return a;
		}
		return Math.min(a, slot.inventory.getInventoryStackLimit());
	}

	@Override
	protected boolean mergeItemStack(ItemStack itemstack, int i, int j, boolean flag) {
		boolean flag1 = false;
		int k = i;
		if (flag) {
			k = j - 1;
		}
		if (itemstack.isStackable()) {
			while (itemstack.stackSize > 0 && (!flag && k < j || flag && k >= i)) {
				Slot slot = (Slot) inventorySlots.get(k);
				ItemStack itemstack1 = slot.getStack();
				if (itemstack1 != null && slot.isItemValid(itemstack) && (flag || itemstack1.stackSize < slot.inventory.getInventoryStackLimit())
						&& itemstack1.itemID == itemstack.itemID
						&& (!itemstack.getHasSubtypes() || itemstack.getItemDamage() == itemstack1.getItemDamage())) {
					int i1 = itemstack1.stackSize + itemstack.stackSize;
					if (i1 <= getLimit(slot, itemstack.getMaxStackSize(), flag)) {
						itemstack.stackSize = 0;
						itemstack1.stackSize = i1;
						slot.onSlotChanged();
						flag1 = true;
					} else if (itemstack1.stackSize < getLimit(slot, itemstack.getMaxStackSize(), flag)) {
						itemstack.stackSize -= getLimit(slot, itemstack.getMaxStackSize(), flag) - itemstack1.stackSize;
						itemstack1.stackSize = getLimit(slot, itemstack.getMaxStackSize(), flag);
						slot.onSlotChanged();
						flag1 = true;
					}
				}
				if (flag) {
					k--;
				} else {
					k++;
				}
			}
		}
		if (itemstack.stackSize > 0) {
			int l;
			if (flag) {
				l = j - 1;
			} else {
				l = i;
			}
			do {
				if ((flag || l >= j) && (!flag || l < i)) {
					break;
				}
				Slot slot = (Slot) inventorySlots.get(l);
				ItemStack itemstack2 = slot.getStack();
				if (itemstack2 == null && slot.isItemValid(itemstack)) {
					// empty field in inventory
					ItemStack toStore = itemstack.copy();
					toStore.stackSize = getLimit(slot, toStore.stackSize, flag);
					itemstack.stackSize -= toStore.stackSize;

					slot.putStack(toStore);
					slot.onSlotChanged();

					if (itemstack.stackSize <= 0) {
						flag1 = true;
						itemstack.stackSize = 0;
						break;
					}
				}
				if (flag) {
					l--;
				} else {
					l++;
				}
			} while (true);
		}
		return flag1;
	}

	@Override
	public void onCraftMatrixChanged(IInventory iinventory) {
		gresGui.onCraftMatrixChanged(iinventory);
	}

}
