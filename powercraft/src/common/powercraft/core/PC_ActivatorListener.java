package powercraft.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class PC_ActivatorListener implements PC_IActivatorListener {

	private static List<PC_IActivatorListener> listeners = new ArrayList<PC_IActivatorListener>();
	
	static{
		registerListener(new PC_ActivatorListener());
	}
	
	private PC_ActivatorListener(){}
	
	@Override
	public boolean onActivatorUsedOnBlock(ItemStack stack, EntityPlayer player, World world, PC_CoordI pos) {
		
		
		if (pos.getId(world) == Block.mobSpawner.blockID) {

			if(world.isRemote)
				PC_Utils.openGres("SpawnerEditor", player, pos.x, pos.y, pos.z);

			stack.damageItem(1, player);

			return true;
		}
		
		return false;
	}

	/**
	 * Register another listener to this item.<br>
	 * Call the corresponding method on PC_Module to register custom listener.
	 * 
	 * @param listener the listener
	 */
	public static void registerListener(PC_IActivatorListener listener) {
		listeners.add(listener);
	}
	
	public static List<PC_IActivatorListener> getListeners() {
		return new ArrayList<PC_IActivatorListener>(listeners);
	}
	
}
