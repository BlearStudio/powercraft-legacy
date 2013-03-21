package powercraft.machines;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import powercraft.api.PC_Utils;
import powercraft.api.PC_Utils.GameInfo;
import powercraft.api.PC_Utils.ValueWriting;
import powercraft.api.PC_VecI;
import powercraft.api.annotation.PC_BlockInfo;
import powercraft.api.block.PC_Block;
import powercraft.api.item.PC_IItemInfo;
import powercraft.api.registry.PC_GresRegistry;
import powercraft.api.registry.PC_MSGRegistry;
import powercraft.api.renderer.PC_Renderer;
import powercraft.api.tileentity.PC_TileEntity;

@PC_BlockInfo(tileEntity=PCma_TileEntityTransmutabox.class)
public class PCma_BlockTransmutabox extends PC_Block implements PC_IItemInfo
{
    public PCma_BlockTransmutabox(int id)
    {
        super(id, Material.rock, "transmutabox");
        setHardness(1.5F);
        setResistance(50.0F);
        setStepSound(Block.soundMetalFootstep);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    public void receivePower(IBlockAccess world, int x, int y, int z, float power)
    {
        PCma_TileEntityTransmutabox te = GameInfo.getTE(world, x, y, z, blockID);

        if (te != null)
        {
            te.addEnergy((int)power);
        }
    }

    @Override
    public TileEntity newTileEntity(World world, int metadata) {
        return new PCma_TileEntityTransmutabox();
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y,
            int z, EntityPlayer player, int par6, float par7,
            float par8, float par9)
    {
        PC_GresRegistry.openGres("Transmutabox", player, GameInfo.<PC_TileEntity>getTE(world, x, y, z));
        return true;
    }

    public void renderInventoryBlock(Block block, int metadata, int modelID, Object renderer)
    {
        ValueWriting.setBlockBounds(block,0.1f, 0.1f, 0.1f, 0.9f, 0.9f, 0.9f);
        PC_Renderer.renderInvBox(renderer, block, metadata);
        ValueWriting.setBlockBounds(block,0.0f, 0.0f, 0.0f, 0.2f, 0.2f, 0.2f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.8f, 0.0f, 0.0f, 1.0f, 0.2f, 0.2f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.8f, 0.8f, 0.0f, 1.0f, 1.0f, 0.2f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.8f, 0.8f, 0.8f, 1.0f, 1.0f, 1.0f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.0f, 0.8f, 0.8f, 0.2f, 1.0f, 1.0f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.0f, 0.0f, 0.8f, 0.2f, 0.2f, 1.0f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.0f, 0.8f, 0.0f, 0.2f, 1.0f, 0.2f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0.8f, 0.0f, 0.8f, 1.0f, 0.2f, 1.0f);
        PC_Renderer.renderInvBoxWithTexture(renderer, block, Block.blockSteel.getBlockTextureFromSide(0));
        ValueWriting.setBlockBounds(block,0, 0, 0, 1, 1, 1);
    }

    public void renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, Object renderer)
    {
        PC_Renderer.tessellatorDraw();
        PC_Renderer.tessellatorStartDrawingQuads();
        ValueWriting.setBlockBounds(block,0.1f, 0.1f, 0.1f, 0.9f, 0.9f, 0.9f);
        PC_Renderer.renderStandardBlock(renderer, block, x, y, z);
        ValueWriting.setBlockBounds(block,0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        
        ValueWriting.setBlockBounds(Block.blockSteel,0.0f, 0.0f, 0.0f, 0.2f, 0.2f, 0.2f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.8f, 0.0f, 0.0f, 1.0f, 0.2f, 0.2f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.8f, 0.8f, 0.0f, 1.0f, 1.0f, 0.2f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.8f, 0.8f, 0.8f, 1.0f, 1.0f, 1.0f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.0f, 0.8f, 0.8f, 0.2f, 1.0f, 1.0f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.0f, 0.0f, 0.8f, 0.2f, 0.2f, 1.0f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.0f, 0.8f, 0.0f, 0.2f, 1.0f, 0.2f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.8f, 0.0f, 0.8f, 1.0f, 0.2f, 1.0f);
        PC_Renderer.renderStandardBlock(renderer, Block.blockSteel, x, y, z);
        ValueWriting.setBlockBounds(Block.blockSteel,0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        PC_Renderer.tessellatorDraw();
        PC_Renderer.tessellatorStartDrawingQuads();
    }

    @Override
    public List<ItemStack> getItemStacks(List<ItemStack> arrayList)
    {
        arrayList.add(new ItemStack(this));
        return arrayList;
    }

	@Override
	public Object msg(IBlockAccess world, PC_VecI pos, int msg, Object... obj) {
		switch (msg){
		case PC_MSGRegistry.MSG_DEFAULT_NAME:
			return "Transmutabox";
		case PC_MSGRegistry.MSG_ITEM_FLAGS:{
			List<String> list = (List<String>)obj[1];
			list.add(PC_Utils.NO_BUILD);
			return list;
		}case PC_MSGRegistry.MSG_BLOCK_FLAGS:{
			List<String> list = (List<String>)obj[0];
	   		list.add(PC_Utils.NO_HARVEST);
	   		list.add(PC_Utils.NO_PICKUP);
	   		list.add(PC_Utils.HARVEST_STOP);
	   		return list;
		}case PC_MSGRegistry.MSG_RENDER_INVENTORY_BLOCK:{
			renderInventoryBlock((Block)obj[0], (Integer)obj[1], (Integer)obj[2], obj[3]);
			return true;
		}case PC_MSGRegistry.MSG_RENDER_WORLD_BLOCK:{
			renderWorldBlock(world, pos.x, pos.y, pos.z, (Block)obj[0], (Integer)obj[1], obj[2]);
			return true;
		}case PC_MSGRegistry.MSG_CAN_RECIVE_POWER:{
			return true;
		}case PC_MSGRegistry.MSG_RECIVE_POWER:{
			receivePower(world, (Integer)obj[0], (Integer)obj[1], (Integer)obj[2], (Float)obj[3]);
		}
		}
		return null;
	}
    
}
