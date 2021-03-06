package powercraft.logic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import powercraft.core.PC_ItemBlock;
import powercraft.core.PC_MathHelper;
import powercraft.core.PC_Utils;

public class PClo_ItemBlockRepeater extends PC_ItemBlock
{
    public PClo_ItemBlockRepeater(int id)
    {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public String[] getDefaultNames()
    {
        List<String> s =  new ArrayList<String>();

        for (int i = 0; i < PClo_RepeaterType.TOTAL_REPEATER_COUNT; i++)
        {
            s.add(getItemName() + ".repeater" + i);
            s.add(PClo_RepeaterType.names[i] + " repeater");
        };

        s.add(getItemName());

        s.add("repeater");

        return s.toArray(new String[0]);
    }

    @Override
    public List<ItemStack> getItemStacks(List<ItemStack> arrayList)
    {
        for (int i = 0; i < PClo_RepeaterType.TOTAL_REPEATER_COUNT; i++)
        {
            arrayList.add(new ItemStack(this, 1, i));
        }

        return arrayList;
    }

    @Override
    public int getIconFromDamage(int i)
    {
        return mod_PowerCraftLogic.repeater.getBlockTextureFromSideAndMetadata(1, 0);
    }

    @Override
    public String getItemNameIS(ItemStack itemstack)
    {
        return getItemName() + ".repeater" + itemstack.getItemDamage();
    }

    @Override
    public boolean isFull3D()
    {
        return false;
    }

    @Override
    public boolean shouldRotateAroundWhenRendering()
    {
        return false;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean b)
    {
        list.add(getDescriptionForRepeater(itemStack.getItemDamage()));
    }

    public static String getDescriptionForRepeater(int dmg)
    {
        return PC_Utils.tr("pc.repeater." + PClo_RepeaterType.names[PC_MathHelper.clamp_int(dmg, 0, PClo_RepeaterType.TOTAL_REPEATER_COUNT - 1)] + ".desc");
    }
}
