package powercraft.light;

import java.io.IOException;

import net.minecraft.src.Block;
import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.Entity;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import powercraft.core.PC_BeamTracer;
import powercraft.core.PC_Color;
import powercraft.core.PC_CoordI;
import powercraft.core.PC_IBeamHandler;
import powercraft.core.PC_ITileEntityRenderer;
import powercraft.core.PC_Renderer;
import powercraft.core.PC_TileEntity;
import powercraft.core.PC_Utils;

public class PCli_TileEntityLaser extends PC_TileEntity implements PC_IBeamHandler, PC_ITileEntityRenderer
{
	private static PCli_ModelLaser modelLaser = new PCli_ModelLaser();
    private boolean active = false;
    private ItemStack itemstack;
    private PC_BeamTracer laser;
    private boolean isKiller = false;
    
    public boolean isKiller()
    {
        return isKiller;
    }

    
	public boolean isActive()
    {
        return active;
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    @Override
    public void updateEntity()
    {
    	if(laser==null){
	    	laser = new PC_BeamTracer(worldObj, this);
	    	laser.setStartCoord(getCoord());
	    	int metadata = PC_Utils.getMD(worldObj, xCoord, yCoord, zCoord);
	    	laser.setStartMove(metadata == 4?1:metadata == 5?-1:0, 0, metadata == 2?1:metadata == 3?-1:0);
	    	laser.setColor(PCli_ItemLaserComposition.getColorForItemStack(itemstack));
	    	laser.setStartLength(PCli_ItemLaserComposition.getLengthLimit(itemstack));
	    	laser.setDetectEntities(true);
    	}
    	laser.flash();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
        active = nbttagcompound.getBoolean("on");
        isKiller = nbttagcompound.getBoolean("isKiller");
        itemstack = null;
        if(nbttagcompound.hasKey("itemstack")){
        	itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("itemstack"));
        	laser = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("on", active);
        nbttagcompound.setBoolean("isKiller", isKiller);
        if(itemstack!=null){
	        NBTTagCompound nbttag = new NBTTagCompound();
	        itemstack.writeToNBT(nbttag);
	        nbttagcompound.setCompoundTag("itemstack", nbttag);
        }
    }

	@Override
	public boolean onBlockHit(PC_BeamTracer beamTracer, Block block, PC_CoordI coord) {
		return PCli_ItemLaserComposition.onBlockHit(beamTracer, block, coord, itemstack);
	}

	@Override
	public boolean onEntityHit(PC_BeamTracer beamTracer, Entity entity, PC_CoordI coord) {
		return PCli_ItemLaserComposition.onEntityHit(beamTracer, entity, coord, itemstack);
	}

	public ItemStack getItemStack() {
		return itemstack;
	}
	
	public void setItemStack(ItemStack itemstack) {
		this.itemstack = itemstack;
		laser = null;
	}

	@Override
	public void setData(Object[] o) {
		int p = 0;

        while (p < o.length)
        {
            String var = (String)o[p++];

            if (var.equals("nbt"))
            {
            	try {
					readFromNBT(CompressedStreamTools.decompress((byte[])o[p++]));
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
   
        }
	}

	@Override
	public Object[] getData() {
		NBTTagCompound nbt = new NBTTagCompound("PCPacket");
		writeToNBT(nbt);
		try {
			return new Object[]{
				"nbt", CompressedStreamTools.compress(nbt)
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void renderTileEntityAt(double x, double y, double z, float rot) {
		modelLaser.laserParts[0].showModel = modelLaser.laserParts[1].showModel = modelLaser.laserParts[2].showModel = modelLaser.laserParts[3].showModel = 
				isKiller();

		modelLaser.laserParts[7].showModel = itemstack != null;

		PC_Renderer.glPushMatrix();
		float f = 1.0F;

		PC_Renderer.glTranslatef((float) x + 0.5F, (float) y + 0.5F /* *f0 */, (float) z + 0.5F);

		int[] meta2angle = { 0, 0, 90, 270, 0, 180 };

		float f1 = meta2angle[getBlockMetadata()];

		PC_Renderer.bindTexture(mod_PowerCraftLight.getInstance().getTextureDirectory() + "laser.png");

		PC_Renderer.glPushMatrix();
		PC_Renderer.glRotatef(-f1, 0.0F, 1.0F, 0.0F);
		PC_Renderer.glScalef(f, -f, -f);
		modelLaser.renderLaser();
		PC_Color color = PCli_ItemLaserComposition.getColorForItemStack(itemstack);
		PC_Renderer.glColor4f((float)color.r, (float)color.g, (float)color.b, 1.0F);
		modelLaser.renderLens();
		PC_Renderer.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		PC_Renderer.glPopMatrix();

		PC_Renderer.glPopMatrix();
	}
	
}

