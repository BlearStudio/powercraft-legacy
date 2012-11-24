package net.minecraft.src;

import java.util.Random;

import net.minecraftforge.common.ChestGenHooks;

public class WeightedRandomChestContent extends WeightedRandomItem
{
    private int theItemId;

    private int theItemDamage;

    private int theMinimumChanceToGenerateItem;

    private int theMaximumChanceToGenerateItem;

    public final ItemStack itemStack;

    public WeightedRandomChestContent(int par1, int par2, int par3, int par4, int par5)
    {
        super(par5);
        this.theItemId = par1;
        this.theItemDamage = par2;
        this.theMinimumChanceToGenerateItem = par3;
        this.theMaximumChanceToGenerateItem = par4;
        itemStack = new ItemStack(par1, 1, par2);
    }

    public WeightedRandomChestContent(ItemStack stack, int min, int max, int weight)
    {
        super(weight);
        itemStack = stack;
        theMinimumChanceToGenerateItem = min;
        theMaximumChanceToGenerateItem = max;
    }

    public static void generateChestContents(Random par0Random, WeightedRandomChestContent[] par1ArrayOfWeightedRandomChestContent, TileEntityChest par2TileEntityChest, int par3)
    {
        for (int var4 = 0; var4 < par3; ++var4)
        {
            WeightedRandomChestContent var5 = (WeightedRandomChestContent)WeightedRandom.getRandomItem(par0Random, par1ArrayOfWeightedRandomChestContent);
            ItemStack[] stacks = ChestGenHooks.generateStacks(par0Random, var5.itemStack, var5.theMinimumChanceToGenerateItem, var5.theMinimumChanceToGenerateItem);

            for (ItemStack item : stacks)
            {
                par2TileEntityChest.setInventorySlotContents(par0Random.nextInt(par2TileEntityChest.getSizeInventory()), item);
            }
        }
    }

    public static void generateDispenserContents(Random par0Random, WeightedRandomChestContent[] par1ArrayOfWeightedRandomChestContent, TileEntityDispenser par2TileEntityDispenser, int par3)
    {
        for (int var4 = 0; var4 < par3; ++var4)
        {
            WeightedRandomChestContent var5 = (WeightedRandomChestContent)WeightedRandom.getRandomItem(par0Random, par1ArrayOfWeightedRandomChestContent);
            ItemStack[] stacks = ChestGenHooks.generateStacks(par0Random, var5.itemStack, var5.theMinimumChanceToGenerateItem, var5.theMinimumChanceToGenerateItem);

            for (ItemStack item : stacks)
            {
                par2TileEntityDispenser.setInventorySlotContents(par0Random.nextInt(par2TileEntityDispenser.getSizeInventory()), item);
            }
        }
    }
}
