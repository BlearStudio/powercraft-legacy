package mods.betterworld.DeCB.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.betterworld.CB.core.BWCB_BlockList;
import mods.betterworld.DeCB.blocks.BWDeCB_BlockFenceWood;
import mods.betterworld.DeCB.tileEntity.BWDeCB_TileEntityBlockFenceWood;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.ForceChunkEvent;

public class BWDeCB_ItemBlockFenceWood extends ItemBlock {

	public BWDeCB_ItemBlockFenceWood(int id) {
		super(id);
		this.setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("BlockFenceWood");

	}

	/*
	 * @Override public int getMetadata (int damageValue) { return damageValue;
	 * }
	 */
	public int getItemDamageVal(ItemStack itemstack) {
		return itemstack.getItemDamage();
	}

	String[] a = new String[BWCB_BlockList.blockPlanksNormalName.size()];

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "BlockFence."
				+ BWCB_BlockList.blockPlanksNormalName.get(itemStack
						.getItemDamage());

	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) {
		return super.getIconFromDamage(par1);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player,
			World world, int x, int y, int z, int side, float hitX, float hitY,
			float hitZ, int metadata) {

		if (super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY,
				hitZ, metadata)) {
			((BWDeCB_TileEntityBlockFenceWood) world.getBlockTileEntity(x, y, z))
					.setBlockDamageID(stack.getItemDamage());
			((BWDeCB_TileEntityBlockFenceWood) world.getBlockTileEntity(x, y, z))
					.setUserName(player.username);
			((BWDeCB_TileEntityBlockFenceWood) world.getBlockTileEntity(x, y, z))
					.setLightValue(BWCB_BlockList.blockPlanksNormalLight
							.get(stack.getItemDamage()));
			((BWDeCB_TileEntityBlockFenceWood) world.getBlockTileEntity(x, y, z))
					.setBlockLocked(false);
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int var1, CreativeTabs var2, List var3) {
		BWDeCB_BlockFenceWood block = (BWDeCB_BlockFenceWood) Block.blocksList[getBlockID()];
		for (int i = 0; i < block.getSubBlockCount(); i++) {
			var3.add(new ItemStack(var1, 1, i));

		}
	}
}