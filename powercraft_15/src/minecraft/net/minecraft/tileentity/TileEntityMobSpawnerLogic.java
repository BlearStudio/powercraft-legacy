package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.world.World;

class TileEntityMobSpawnerLogic extends MobSpawnerBaseLogic
{
    final TileEntityMobSpawner field_98295_a;

    TileEntityMobSpawnerLogic(TileEntityMobSpawner par1TileEntityMobSpawner)
    {
        this.field_98295_a = par1TileEntityMobSpawner;
    }

    public void func_98267_a(int par1)
    {
        this.field_98295_a.worldObj.addBlockEvent(this.field_98295_a.xCoord, this.field_98295_a.yCoord, this.field_98295_a.zCoord, Block.mobSpawner.blockID, par1, 0);
    }

    public World func_98271_a()
    {
        return this.field_98295_a.worldObj;
    }

    public int func_98275_b()
    {
        return this.field_98295_a.xCoord;
    }

    public int func_98274_c()
    {
        return this.field_98295_a.yCoord;
    }

    public int func_98266_d()
    {
        return this.field_98295_a.zCoord;
    }

    public void func_98277_a(WeightedRandomMinecart par1WeightedRandomMinecart)
    {
        super.func_98277_a(par1WeightedRandomMinecart);

        if (this.func_98271_a() != null)
        {
            this.func_98271_a().markBlockForUpdate(this.field_98295_a.xCoord, this.field_98295_a.yCoord, this.field_98295_a.zCoord);
        }
    }
}