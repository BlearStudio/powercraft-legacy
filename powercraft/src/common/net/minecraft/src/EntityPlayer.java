package net.minecraft.src;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.network.FMLNetworkHandler;

import java.util.Iterator;
import java.util.List;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public abstract class EntityPlayer extends EntityLiving implements ICommandSender
{
    public InventoryPlayer inventory = new InventoryPlayer(this);
    private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();

    public Container inventorySlots;

    public Container craftingInventory;

    protected FoodStats foodStats = new FoodStats();

    protected int flyToggleTimer = 0;
    public byte field_71098_bD = 0;
    public float prevCameraYaw;
    public float cameraYaw;
    public String username;
    @SideOnly(Side.CLIENT)
    public String playerCloakUrl;

    public int xpCooldown = 0;
    public double field_71091_bM;
    public double field_71096_bN;
    public double field_71097_bO;
    public double field_71094_bP;
    public double field_71095_bQ;
    public double field_71085_bR;

    protected boolean sleeping;

    public ChunkCoordinates playerLocation;
    private int sleepTimer;
    public float field_71079_bU;
    @SideOnly(Side.CLIENT)
    public float field_71082_cx;
    public float field_71089_bV;

    private ChunkCoordinates spawnChunk;

    private boolean spawnForced;

    private ChunkCoordinates startMinecartRidingCoordinate;

    public PlayerCapabilities capabilities = new PlayerCapabilities();

    public int experienceLevel;

    public int experienceTotal;

    public float experience;

    private ItemStack itemInUse;

    private int itemInUseCount;
    protected float speedOnGround = 0.1F;
    protected float speedInAir = 0.02F;
    private int field_82249_h = 0;

    public EntityFishHook fishEntity = null;

    public EntityPlayer(World par1World)
    {
        super(par1World);
        this.inventorySlots = new ContainerPlayer(this.inventory, !par1World.isRemote, this);
        this.craftingInventory = this.inventorySlots;
        this.yOffset = 1.62F;
        ChunkCoordinates var2 = par1World.getSpawnPoint();
        this.setLocationAndAngles((double)var2.posX + 0.5D, (double)(var2.posY + 1), (double)var2.posZ + 0.5D, 0.0F, 0.0F);
        this.entityType = "humanoid";
        this.field_70741_aB = 180.0F;
        this.fireResistance = 20;
        this.texture = "/mob/char.png";
    }

    public int getMaxHealth()
    {
        return 20;
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(17, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(18, Integer.valueOf(0));
    }

    @SideOnly(Side.CLIENT)

    public ItemStack getItemInUse()
    {
        return this.itemInUse;
    }

    @SideOnly(Side.CLIENT)

    public int getItemInUseCount()
    {
        return this.itemInUseCount;
    }

    public boolean isUsingItem()
    {
        return this.itemInUse != null;
    }

    @SideOnly(Side.CLIENT)

    public int getItemInUseDuration()
    {
        return this.isUsingItem() ? this.itemInUse.getMaxItemUseDuration() - this.itemInUseCount : 0;
    }

    public void stopUsingItem()
    {
        if (this.itemInUse != null)
        {
            this.itemInUse.onPlayerStoppedUsing(this.worldObj, this, this.itemInUseCount);
        }

        this.clearItemInUse();
    }

    public void clearItemInUse()
    {
        this.itemInUse = null;
        this.itemInUseCount = 0;

        if (!this.worldObj.isRemote)
        {
            this.setEating(false);
        }
    }

    public boolean isBlocking()
    {
        return this.isUsingItem() && Item.itemsList[this.itemInUse.itemID].getItemUseAction(this.itemInUse) == EnumAction.block;
    }

    public void onUpdate()
    {
        FMLCommonHandler.instance().onPlayerPreTick(this);

        if (this.itemInUse != null)
        {
            ItemStack var1 = this.inventory.getCurrentItem();

            if (var1 == this.itemInUse)
            {
                itemInUse.getItem().onUsingItemTick(itemInUse, this, itemInUseCount);

                if (this.itemInUseCount <= 25 && this.itemInUseCount % 4 == 0)
                {
                    this.updateItemUse(var1, 5);
                }

                if (--this.itemInUseCount == 0 && !this.worldObj.isRemote)
                {
                    this.onItemUseFinish();
                }
            }
            else
            {
                this.clearItemInUse();
            }
        }

        if (this.xpCooldown > 0)
        {
            --this.xpCooldown;
        }

        if (this.isPlayerSleeping())
        {
            ++this.sleepTimer;

            if (this.sleepTimer > 100)
            {
                this.sleepTimer = 100;
            }

            if (!this.worldObj.isRemote)
            {
                if (!this.isInBed())
                {
                    this.wakeUpPlayer(true, true, false);
                }
                else if (this.worldObj.isDaytime())
                {
                    this.wakeUpPlayer(false, true, true);
                }
            }
        }
        else if (this.sleepTimer > 0)
        {
            ++this.sleepTimer;

            if (this.sleepTimer >= 110)
            {
                this.sleepTimer = 0;
            }
        }

        super.onUpdate();

        if (!this.worldObj.isRemote && this.craftingInventory != null && !this.craftingInventory.canInteractWith(this))
        {
            this.closeScreen();
            this.craftingInventory = this.inventorySlots;
        }

        if (this.isBurning() && this.capabilities.disableDamage)
        {
            this.extinguish();
        }

        this.field_71091_bM = this.field_71094_bP;
        this.field_71096_bN = this.field_71095_bQ;
        this.field_71097_bO = this.field_71085_bR;
        double var9 = this.posX - this.field_71094_bP;
        double var3 = this.posY - this.field_71095_bQ;
        double var5 = this.posZ - this.field_71085_bR;
        double var7 = 10.0D;

        if (var9 > var7)
        {
            this.field_71091_bM = this.field_71094_bP = this.posX;
        }

        if (var5 > var7)
        {
            this.field_71097_bO = this.field_71085_bR = this.posZ;
        }

        if (var3 > var7)
        {
            this.field_71096_bN = this.field_71095_bQ = this.posY;
        }

        if (var9 < -var7)
        {
            this.field_71091_bM = this.field_71094_bP = this.posX;
        }

        if (var5 < -var7)
        {
            this.field_71097_bO = this.field_71085_bR = this.posZ;
        }

        if (var3 < -var7)
        {
            this.field_71096_bN = this.field_71095_bQ = this.posY;
        }

        this.field_71094_bP += var9 * 0.25D;
        this.field_71085_bR += var5 * 0.25D;
        this.field_71095_bQ += var3 * 0.25D;
        this.addStat(StatList.minutesPlayedStat, 1);

        if (this.ridingEntity == null)
        {
            this.startMinecartRidingCoordinate = null;
        }

        if (!this.worldObj.isRemote)
        {
            this.foodStats.onUpdate(this);
        }

        FMLCommonHandler.instance().onPlayerPostTick(this);
    }

    public int getMaxInPortalTime()
    {
        return this.capabilities.disableDamage ? 0 : 80;
    }

    public int getPortalCooldown()
    {
        return 10;
    }

    protected void func_85030_a(String par1Str, float par2, float par3)
    {
        this.worldObj.func_85173_a(this, par1Str, par2, par3);
    }

    protected void updateItemUse(ItemStack par1ItemStack, int par2)
    {
        if (par1ItemStack.getItemUseAction() == EnumAction.drink)
        {
            this.func_85030_a("random.drink", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (par1ItemStack.getItemUseAction() == EnumAction.eat)
        {
            for (int var3 = 0; var3 < par2; ++var3)
            {
                Vec3 var4 = this.worldObj.getWorldVec3Pool().getVecFromPool(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                var4.rotateAroundX(-this.rotationPitch * (float)Math.PI / 180.0F);
                var4.rotateAroundY(-this.rotationYaw * (float)Math.PI / 180.0F);
                Vec3 var5 = this.worldObj.getWorldVec3Pool().getVecFromPool(((double)this.rand.nextFloat() - 0.5D) * 0.3D, (double)(-this.rand.nextFloat()) * 0.6D - 0.3D, 0.6D);
                var5.rotateAroundX(-this.rotationPitch * (float)Math.PI / 180.0F);
                var5.rotateAroundY(-this.rotationYaw * (float)Math.PI / 180.0F);
                var5 = var5.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
                this.worldObj.spawnParticle("iconcrack_" + par1ItemStack.getItem().shiftedIndex, var5.xCoord, var5.yCoord, var5.zCoord, var4.xCoord, var4.yCoord + 0.05D, var4.zCoord);
            }

            this.func_85030_a("random.eat", 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    protected void onItemUseFinish()
    {
        if (this.itemInUse != null)
        {
            this.updateItemUse(this.itemInUse, 16);
            int var1 = this.itemInUse.stackSize;
            ItemStack var2 = this.itemInUse.onFoodEaten(this.worldObj, this);

            if (var2 != this.itemInUse || var2 != null && var2.stackSize != var1)
            {
                this.inventory.mainInventory[this.inventory.currentItem] = var2;

                if (var2.stackSize == 0)
                {
                    this.inventory.mainInventory[this.inventory.currentItem] = null;
                }
            }

            this.clearItemInUse();
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte par1)
    {
        if (par1 == 9)
        {
            this.onItemUseFinish();
        }
        else
        {
            super.handleHealthUpdate(par1);
        }
    }

    protected boolean isMovementBlocked()
    {
        return this.getHealth() <= 0 || this.isPlayerSleeping();
    }

    public void closeScreen()
    {
        this.craftingInventory = this.inventorySlots;
    }

    public void updateRidden()
    {
        double var1 = this.posX;
        double var3 = this.posY;
        double var5 = this.posZ;
        float var7 = this.rotationYaw;
        float var8 = this.rotationPitch;
        super.updateRidden();
        this.prevCameraYaw = this.cameraYaw;
        this.cameraYaw = 0.0F;
        this.addMountedMovementStat(this.posX - var1, this.posY - var3, this.posZ - var5);

        if (this.ridingEntity instanceof EntityLiving && ((EntityLiving)ridingEntity).shouldRiderFaceForward(this))
        {
            this.rotationPitch = var8;
            this.rotationYaw = var7;
            this.renderYawOffset = ((EntityLiving)this.ridingEntity).renderYawOffset;
        }
    }

    @SideOnly(Side.CLIENT)

    public void preparePlayerToSpawn()
    {
        this.yOffset = 1.62F;
        this.setSize(0.6F, 1.8F);
        super.preparePlayerToSpawn();
        this.setEntityHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    protected void updateEntityActionState()
    {
        this.updateArmSwingProgress();
    }

    public void onLivingUpdate()
    {
        if (this.flyToggleTimer > 0)
        {
            --this.flyToggleTimer;
        }

        if (this.worldObj.difficultySetting == 0 && this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 * 12 == 0)
        {
            this.heal(1);
        }

        this.inventory.decrementAnimations();
        this.prevCameraYaw = this.cameraYaw;
        super.onLivingUpdate();
        this.landMovementFactor = this.capabilities.getWalkSpeed();
        this.jumpMovementFactor = this.speedInAir;

        if (this.isSprinting())
        {
            this.landMovementFactor = (float)((double)this.landMovementFactor + (double)this.capabilities.getWalkSpeed() * 0.3D);
            this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3D);
        }

        float var1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float var2 = (float)Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;

        if (var1 > 0.1F)
        {
            var1 = 0.1F;
        }

        if (!this.onGround || this.getHealth() <= 0)
        {
            var1 = 0.0F;
        }

        if (this.onGround || this.getHealth() <= 0)
        {
            var2 = 0.0F;
        }

        this.cameraYaw += (var1 - this.cameraYaw) * 0.4F;
        this.cameraPitch += (var2 - this.cameraPitch) * 0.8F;

        if (this.getHealth() > 0)
        {
            List var3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(1.0D, 0.5D, 1.0D));

            if (var3 != null)
            {
                for (int var4 = 0; var4 < var3.size(); ++var4)
                {
                    Entity var5 = (Entity)var3.get(var4);

                    if (!var5.isDead)
                    {
                        this.collideWithPlayer(var5);
                    }
                }
            }
        }
    }

    private void collideWithPlayer(Entity par1Entity)
    {
        par1Entity.onCollideWithPlayer(this);
    }

    public int getScore()
    {
        return this.dataWatcher.getWatchableObjectInt(18);
    }

    public void func_85040_s(int par1)
    {
        this.dataWatcher.updateObject(18, Integer.valueOf(par1));
    }

    public void func_85039_t(int par1)
    {
        int var2 = this.getScore();
        this.dataWatcher.updateObject(18, Integer.valueOf(var2 + par1));
    }

    public void onDeath(DamageSource par1DamageSource)
    {
        super.onDeath(par1DamageSource);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.10000000149011612D;
        captureDrops = true;
        capturedDrops.clear();

        if (this.username.equals("Notch"))
        {
            this.dropPlayerItemWithRandomChoice(new ItemStack(Item.appleRed, 1), true);
        }

        if (!this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
        {
            this.inventory.dropAllItems();
        }

        captureDrops = false;

        if (!worldObj.isRemote)
        {
            PlayerDropsEvent event = new PlayerDropsEvent(this, par1DamageSource, capturedDrops, recentlyHit > 0);

            if (!MinecraftForge.EVENT_BUS.post(event))
            {
                for (EntityItem item : capturedDrops)
                {
                    joinEntityItemWithWorld(item);
                }
            }
        }

        if (par1DamageSource != null)
        {
            this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
            this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
        }
        else
        {
            this.motionX = this.motionZ = 0.0D;
        }

        this.yOffset = 0.1F;
        this.addStat(StatList.deathsStat, 1);
    }

    public void addToPlayerScore(Entity par1Entity, int par2)
    {
        this.func_85039_t(par2);

        if (par1Entity instanceof EntityPlayer)
        {
            this.addStat(StatList.playerKillsStat, 1);
        }
        else
        {
            this.addStat(StatList.mobKillsStat, 1);
        }
    }

    public EntityItem dropOneItem()
    {
        ItemStack stack = inventory.getCurrentItem();

        if (stack == null)
        {
            return null;
        }

        if (stack.getItem().onDroppedByPlayer(stack, this))
        {
            return ForgeHooks.onPlayerTossEvent(this, inventory.decrStackSize(inventory.currentItem, 1));
        }

        return null;
    }

    public EntityItem dropPlayerItem(ItemStack par1ItemStack)
    {
        return ForgeHooks.onPlayerTossEvent(this, par1ItemStack);
    }

    public EntityItem dropPlayerItemWithRandomChoice(ItemStack par1ItemStack, boolean par2)
    {
        if (par1ItemStack == null)
        {
            return null;
        }
        else
        {
            EntityItem var3 = new EntityItem(this.worldObj, this.posX, this.posY - 0.30000001192092896D + (double)this.getEyeHeight(), this.posZ, par1ItemStack);
            var3.delayBeforeCanPickup = 40;
            float var4 = 0.1F;
            float var5;

            if (par2)
            {
                var5 = this.rand.nextFloat() * 0.5F;
                float var6 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                var3.motionX = (double)(-MathHelper.sin(var6) * var5);
                var3.motionZ = (double)(MathHelper.cos(var6) * var5);
                var3.motionY = 0.20000000298023224D;
            }
            else
            {
                var4 = 0.3F;
                var3.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * var4);
                var3.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * var4);
                var3.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI) * var4 + 0.1F);
                var4 = 0.02F;
                var5 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                var4 *= this.rand.nextFloat();
                var3.motionX += Math.cos((double)var5) * (double)var4;
                var3.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
                var3.motionZ += Math.sin((double)var5) * (double)var4;
            }

            this.joinEntityItemWithWorld(var3);
            this.addStat(StatList.dropStat, 1);
            return var3;
        }
    }

    public void joinEntityItemWithWorld(EntityItem par1EntityItem)
    {
        if (captureDrops)
        {
            capturedDrops.add(par1EntityItem);
        }
        else
        {
            this.worldObj.spawnEntityInWorld(par1EntityItem);
        }
    }

    @Deprecated
    public float getCurrentPlayerStrVsBlock(Block par1Block)
    {
        return getCurrentPlayerStrVsBlock(par1Block, 0);
    }

    public float getCurrentPlayerStrVsBlock(Block par1Block, int meta)
    {
        ItemStack stack = inventory.getCurrentItem();
        float var2 = (stack == null ? 1.0F : stack.getItem().getStrVsBlock(stack, par1Block, meta));
        int var3 = EnchantmentHelper.getEfficiencyModifier(this);

        if (var3 > 0 && ForgeHooks.canHarvestBlock(par1Block, this, meta))
        {
            var2 += (float)(var3 * var3 + 1);
        }

        if (this.isPotionActive(Potion.digSpeed))
        {
            var2 *= 1.0F + (float)(this.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (this.isPotionActive(Potion.digSlowdown))
        {
            var2 *= 1.0F - (float)(this.getActivePotionEffect(Potion.digSlowdown).getAmplifier() + 1) * 0.2F;
        }

        if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this))
        {
            var2 /= 5.0F;
        }

        if (!this.onGround)
        {
            var2 /= 5.0F;
        }

        var2 = ForgeEventFactory.getBreakSpeed(this, par1Block, meta, var2);
        return (var2 < 0 ? 0 : var2);
    }

    public boolean canHarvestBlock(Block par1Block)
    {
        return ForgeEventFactory.doPlayerHarvestCheck(this, par1Block, inventory.canHarvestBlock(par1Block));
    }

    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        NBTTagList var2 = par1NBTTagCompound.getTagList("Inventory");
        this.inventory.readFromNBT(var2);
        this.sleeping = par1NBTTagCompound.getBoolean("Sleeping");
        this.sleepTimer = par1NBTTagCompound.getShort("SleepTimer");
        this.experience = par1NBTTagCompound.getFloat("XpP");
        this.experienceLevel = par1NBTTagCompound.getInteger("XpLevel");
        this.experienceTotal = par1NBTTagCompound.getInteger("XpTotal");
        this.func_85040_s(par1NBTTagCompound.getInteger("Score"));

        if (this.sleeping)
        {
            this.playerLocation = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
            this.wakeUpPlayer(true, true, false);
        }

        if (par1NBTTagCompound.hasKey("SpawnX") && par1NBTTagCompound.hasKey("SpawnY") && par1NBTTagCompound.hasKey("SpawnZ"))
        {
            this.spawnChunk = new ChunkCoordinates(par1NBTTagCompound.getInteger("SpawnX"), par1NBTTagCompound.getInteger("SpawnY"), par1NBTTagCompound.getInteger("SpawnZ"));
            this.spawnForced = par1NBTTagCompound.getBoolean("SpawnForced");
        }

        this.foodStats.readNBT(par1NBTTagCompound);
        this.capabilities.readCapabilitiesFromNBT(par1NBTTagCompound);

        if (par1NBTTagCompound.hasKey("EnderItems"))
        {
            NBTTagList var3 = par1NBTTagCompound.getTagList("EnderItems");
            this.theInventoryEnderChest.loadInventoryFromNBT(var3);
        }
    }

    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        par1NBTTagCompound.setBoolean("Sleeping", this.sleeping);
        par1NBTTagCompound.setShort("SleepTimer", (short)this.sleepTimer);
        par1NBTTagCompound.setFloat("XpP", this.experience);
        par1NBTTagCompound.setInteger("XpLevel", this.experienceLevel);
        par1NBTTagCompound.setInteger("XpTotal", this.experienceTotal);
        par1NBTTagCompound.setInteger("Score", this.getScore());

        if (this.spawnChunk != null)
        {
            par1NBTTagCompound.setInteger("SpawnX", this.spawnChunk.posX);
            par1NBTTagCompound.setInteger("SpawnY", this.spawnChunk.posY);
            par1NBTTagCompound.setInteger("SpawnZ", this.spawnChunk.posZ);
            par1NBTTagCompound.setBoolean("SpawnForced", this.spawnForced);
        }

        this.foodStats.writeNBT(par1NBTTagCompound);
        this.capabilities.writeCapabilitiesToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setTag("EnderItems", this.theInventoryEnderChest.saveInventoryToNBT());
    }

    public void displayGUIChest(IInventory par1IInventory) {}

    public void displayGUIEnchantment(int par1, int par2, int par3) {}

    public void displayGUIAnvil(int par1, int par2, int par3) {}

    public void displayGUIWorkbench(int par1, int par2, int par3) {}

    public float getEyeHeight()
    {
        return 0.12F;
    }

    protected void resetHeight()
    {
        this.yOffset = 1.62F;
    }

    public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
    {
        if (this.func_85032_ar())
        {
            return false;
        }
        else if (this.capabilities.disableDamage && !par1DamageSource.canHarmInCreative())
        {
            return false;
        }
        else
        {
            this.entityAge = 0;

            if (this.getHealth() <= 0)
            {
                return false;
            }
            else
            {
                if (this.isPlayerSleeping() && !this.worldObj.isRemote)
                {
                    this.wakeUpPlayer(true, true, false);
                }

                if (par1DamageSource.func_76350_n())
                {
                    if (this.worldObj.difficultySetting == 0)
                    {
                        par2 = 0;
                    }

                    if (this.worldObj.difficultySetting == 1)
                    {
                        par2 = par2 / 2 + 1;
                    }

                    if (this.worldObj.difficultySetting == 3)
                    {
                        par2 = par2 * 3 / 2;
                    }
                }

                if (par2 == 0)
                {
                    return false;
                }
                else
                {
                    Entity var3 = par1DamageSource.getEntity();

                    if (var3 instanceof EntityArrow && ((EntityArrow)var3).shootingEntity != null)
                    {
                        var3 = ((EntityArrow)var3).shootingEntity;
                    }

                    if (var3 instanceof EntityLiving)
                    {
                        this.alertWolves((EntityLiving)var3, false);
                    }

                    this.addStat(StatList.damageTakenStat, par2);
                    return super.attackEntityFrom(par1DamageSource, par2);
                }
            }
        }
    }

    protected int applyPotionDamageCalculations(DamageSource par1DamageSource, int par2)
    {
        int var3 = super.applyPotionDamageCalculations(par1DamageSource, par2);

        if (var3 <= 0)
        {
            return 0;
        }
        else
        {
            int var4 = EnchantmentHelper.getEnchantmentModifierDamage(this.inventory.armorInventory, par1DamageSource);

            if (var4 > 20)
            {
                var4 = 20;
            }

            if (var4 > 0 && var4 <= 20)
            {
                int var5 = 25 - var4;
                int var6 = var3 * var5 + this.carryoverDamage;
                var3 = var6 / 25;
                this.carryoverDamage = var6 % 25;
            }

            return var3;
        }
    }

    protected boolean isPVPEnabled()
    {
        return false;
    }

    protected void alertWolves(EntityLiving par1EntityLiving, boolean par2)
    {
        if (!(par1EntityLiving instanceof EntityCreeper) && !(par1EntityLiving instanceof EntityGhast))
        {
            if (par1EntityLiving instanceof EntityWolf)
            {
                EntityWolf var3 = (EntityWolf)par1EntityLiving;

                if (var3.isTamed() && this.username.equals(var3.getOwnerName()))
                {
                    return;
                }
            }

            if (!(par1EntityLiving instanceof EntityPlayer) || this.isPVPEnabled())
            {
                List var6 = this.worldObj.getEntitiesWithinAABB(EntityWolf.class, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
                Iterator var4 = var6.iterator();

                while (var4.hasNext())
                {
                    EntityWolf var5 = (EntityWolf)var4.next();

                    if (var5.isTamed() && var5.getEntityToAttack() == null && this.username.equals(var5.getOwnerName()) && (!par2 || !var5.isSitting()))
                    {
                        var5.setSitting(false);
                        var5.setTarget(par1EntityLiving);
                    }
                }
            }
        }
    }

    protected void damageArmor(int par1)
    {
        this.inventory.damageArmor(par1);
    }

    public int getTotalArmorValue()
    {
        return this.inventory.getTotalArmorValue();
    }

    public float func_82243_bO()
    {
        int var1 = 0;
        ItemStack[] var2 = this.inventory.armorInventory;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4)
        {
            ItemStack var5 = var2[var4];

            if (var5 != null)
            {
                ++var1;
            }
        }

        return (float)var1 / (float)this.inventory.armorInventory.length;
    }

    protected void damageEntity(DamageSource par1DamageSource, int par2)
    {
        if (!this.func_85032_ar())
        {
            par2 = ForgeHooks.onLivingHurt(this, par1DamageSource, par2);

            if (par2 <= 0)
            {
                return;
            }

            if (!par1DamageSource.isUnblockable() && this.isBlocking())
            {
                par2 = 1 + par2 >> 1;
            }

            par2 = ArmorProperties.ApplyArmor(this, inventory.armorInventory, par1DamageSource, par2);

            if (par2 <= 0)
            {
                return;
            }

            par2 = this.applyPotionDamageCalculations(par1DamageSource, par2);
            this.addExhaustion(par1DamageSource.getHungerDamage());
            this.health -= par2;
        }
    }

    public void displayGUIFurnace(TileEntityFurnace par1TileEntityFurnace) {}

    public void displayGUIDispenser(TileEntityDispenser par1TileEntityDispenser) {}

    public void displayGUIEditSign(TileEntity par1TileEntity) {}

    public void displayGUIBrewingStand(TileEntityBrewingStand par1TileEntityBrewingStand) {}

    public void displayGUIBeacon(TileEntityBeacon par1TileEntityBeacon) {}

    public void displayGUIMerchant(IMerchant par1IMerchant) {}

    public void displayGUIBook(ItemStack par1ItemStack) {}

    public boolean interactWith(Entity par1Entity)
    {
        if (MinecraftForge.EVENT_BUS.post(new EntityInteractEvent(this, par1Entity)))
        {
            return false;
        }

        if (par1Entity.interact(this))
        {
            return true;
        }
        else
        {
            ItemStack var2 = this.getCurrentEquippedItem();

            if (var2 != null && par1Entity instanceof EntityLiving)
            {
                if (this.capabilities.isCreativeMode)
                {
                    var2 = var2.copy();
                }

                if (var2.interactWith((EntityLiving)par1Entity))
                {
                    if (var2.stackSize <= 0 && !this.capabilities.isCreativeMode)
                    {
                        this.destroyCurrentEquippedItem();
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public ItemStack getCurrentEquippedItem()
    {
        return this.inventory.getCurrentItem();
    }

    public void destroyCurrentEquippedItem()
    {
        ItemStack orig = getCurrentEquippedItem();
        this.inventory.setInventorySlotContents(this.inventory.currentItem, (ItemStack)null);
        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this, orig));
    }

    public double getYOffset()
    {
        return (double)(this.yOffset - 0.5F);
    }

    public void attackTargetEntityWithCurrentItem(Entity par1Entity)
    {
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(this, par1Entity)))
        {
            return;
        }

        ItemStack stack = getCurrentEquippedItem();

        if (stack != null && stack.getItem().onLeftClickEntity(stack, this, par1Entity))
        {
            return;
        }

        if (par1Entity.canAttackWithItem())
        {
            if (!par1Entity.func_85031_j(this))
            {
                int var2 = this.inventory.getDamageVsEntity(par1Entity);

                if (this.isPotionActive(Potion.damageBoost))
                {
                    var2 += 3 << this.getActivePotionEffect(Potion.damageBoost).getAmplifier();
                }

                if (this.isPotionActive(Potion.weakness))
                {
                    var2 -= 2 << this.getActivePotionEffect(Potion.weakness).getAmplifier();
                }

                int var3 = 0;
                int var4 = 0;

                if (par1Entity instanceof EntityLiving)
                {
                    var4 = EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLiving)par1Entity);
                    var3 += EnchantmentHelper.getKnockbackModifier(this, (EntityLiving)par1Entity);
                }

                if (this.isSprinting())
                {
                    ++var3;
                }

                if (var2 > 0 || var4 > 0)
                {
                    boolean var5 = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && par1Entity instanceof EntityLiving;

                    if (var5)
                    {
                        var2 += this.rand.nextInt(var2 / 2 + 2);
                    }

                    var2 += var4;
                    boolean var6 = false;
                    int var7 = EnchantmentHelper.func_90036_a(this);

                    if (par1Entity instanceof EntityLiving && var7 > 0 && !par1Entity.isBurning())
                    {
                        var6 = true;
                        par1Entity.setFire(1);
                    }

                    boolean var8 = par1Entity.attackEntityFrom(DamageSource.causePlayerDamage(this), var2);

                    if (var8)
                    {
                        if (var3 > 0)
                        {
                            par1Entity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)var3 * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)var3 * 0.5F));
                            this.motionX *= 0.6D;
                            this.motionZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        if (var5)
                        {
                            this.onCriticalHit(par1Entity);
                        }

                        if (var4 > 0)
                        {
                            this.onEnchantmentCritical(par1Entity);
                        }

                        if (var2 >= 18)
                        {
                            this.triggerAchievement(AchievementList.overkill);
                        }

                        this.setLastAttackingEntity(par1Entity);
                    }

                    ItemStack var9 = this.getCurrentEquippedItem();

                    if (var9 != null && par1Entity instanceof EntityLiving)
                    {
                        var9.hitEntity((EntityLiving)par1Entity, this);

                        if (var9.stackSize <= 0)
                        {
                            this.destroyCurrentEquippedItem();
                        }
                    }

                    if (par1Entity instanceof EntityLiving)
                    {
                        if (par1Entity.isEntityAlive())
                        {
                            this.alertWolves((EntityLiving)par1Entity, true);
                        }

                        this.addStat(StatList.damageDealtStat, var2);

                        if (var7 > 0 && var8)
                        {
                            par1Entity.setFire(var7 * 4);
                        }
                        else if (var6)
                        {
                            par1Entity.extinguish();
                        }
                    }

                    this.addExhaustion(0.3F);
                }
            }
        }
    }

    public void onCriticalHit(Entity par1Entity) {}

    public void onEnchantmentCritical(Entity par1Entity) {}

    @SideOnly(Side.CLIENT)
    public void respawnPlayer() {}

    public void setDead()
    {
        super.setDead();
        this.inventorySlots.onCraftGuiClosed(this);

        if (this.craftingInventory != null)
        {
            this.craftingInventory.onCraftGuiClosed(this);
        }
    }

    public boolean isEntityInsideOpaqueBlock()
    {
        return !this.sleeping && super.isEntityInsideOpaqueBlock();
    }

    public boolean func_71066_bF()
    {
        return false;
    }

    public EnumStatus sleepInBedAt(int par1, int par2, int par3)
    {
        PlayerSleepInBedEvent event = new PlayerSleepInBedEvent(this, par1, par2, par3);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.result != null)
        {
            return event.result;
        }

        if (!this.worldObj.isRemote)
        {
            if (this.isPlayerSleeping() || !this.isEntityAlive())
            {
                return EnumStatus.OTHER_PROBLEM;
            }

            if (!this.worldObj.provider.isSurfaceWorld())
            {
                return EnumStatus.NOT_POSSIBLE_HERE;
            }

            if (this.worldObj.isDaytime())
            {
                return EnumStatus.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(this.posX - (double)par1) > 3.0D || Math.abs(this.posY - (double)par2) > 2.0D || Math.abs(this.posZ - (double)par3) > 3.0D)
            {
                return EnumStatus.TOO_FAR_AWAY;
            }

            double var4 = 8.0D;
            double var6 = 5.0D;
            List var8 = this.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)par1 - var4, (double)par2 - var6, (double)par3 - var4, (double)par1 + var4, (double)par2 + var6, (double)par3 + var4));

            if (!var8.isEmpty())
            {
                return EnumStatus.NOT_SAFE;
            }
        }

        this.setSize(0.2F, 0.2F);
        this.yOffset = 0.2F;

        if (this.worldObj.blockExists(par1, par2, par3))
        {
            int var9 = this.worldObj.getBlockMetadata(par1, par2, par3);
            int var5 = BlockBed.getDirection(var9);
            Block block = Block.blocksList[worldObj.getBlockId(par1, par2, par3)];

            if (block != null)
            {
                var5 = block.getBedDirection(worldObj, par1, par2, par3);
            }

            float var10 = 0.5F;
            float var7 = 0.5F;

            switch (var5)
            {
                case 0:
                    var7 = 0.9F;
                    break;

                case 1:
                    var10 = 0.1F;
                    break;

                case 2:
                    var7 = 0.1F;
                    break;

                case 3:
                    var10 = 0.9F;
            }

            this.func_71013_b(var5);
            this.setPosition((double)((float)par1 + var10), (double)((float)par2 + 0.9375F), (double)((float)par3 + var7));
        }
        else
        {
            this.setPosition((double)((float)par1 + 0.5F), (double)((float)par2 + 0.9375F), (double)((float)par3 + 0.5F));
        }

        this.sleeping = true;
        this.sleepTimer = 0;
        this.playerLocation = new ChunkCoordinates(par1, par2, par3);
        this.motionX = this.motionZ = this.motionY = 0.0D;

        if (!this.worldObj.isRemote)
        {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        return EnumStatus.OK;
    }

    private void func_71013_b(int par1)
    {
        this.field_71079_bU = 0.0F;
        this.field_71089_bV = 0.0F;

        switch (par1)
        {
            case 0:
                this.field_71089_bV = -1.8F;
                break;

            case 1:
                this.field_71079_bU = 1.8F;
                break;

            case 2:
                this.field_71089_bV = 1.8F;
                break;

            case 3:
                this.field_71079_bU = -1.8F;
        }
    }

    public void wakeUpPlayer(boolean par1, boolean par2, boolean par3)
    {
        this.setSize(0.6F, 1.8F);
        this.resetHeight();
        ChunkCoordinates var4 = this.playerLocation;
        ChunkCoordinates var5 = this.playerLocation;
        Block block = (var4 == null ? null : Block.blocksList[worldObj.getBlockId(var4.posX, var4.posY, var4.posZ)]);

        if (var4 != null && block != null && block.isBed(worldObj, var4.posX, var4.posY, var4.posZ, this))
        {
            block.setBedOccupied(this.worldObj, var4.posX, var4.posY, var4.posZ, this, false);
            var5 = block.getBedSpawnPosition(worldObj, var4.posX, var4.posY, var4.posZ, this);

            if (var5 == null)
            {
                var5 = new ChunkCoordinates(var4.posX, var4.posY + 1, var4.posZ);
            }

            this.setPosition((double)((float)var5.posX + 0.5F), (double)((float)var5.posY + this.yOffset + 0.1F), (double)((float)var5.posZ + 0.5F));
        }

        this.sleeping = false;

        if (!this.worldObj.isRemote && par2)
        {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        if (par1)
        {
            this.sleepTimer = 0;
        }
        else
        {
            this.sleepTimer = 100;
        }

        if (par3)
        {
            this.setSpawnChunk(this.playerLocation, false);
        }
    }

    private boolean isInBed()
    {
        ChunkCoordinates c = playerLocation;
        int blockID = worldObj.getBlockId(c.posX, c.posY, c.posZ);
        return Block.blocksList[blockID] != null && Block.blocksList[blockID].isBed(worldObj, c.posX, c.posY, c.posZ, this);
    }

    public static ChunkCoordinates verifyRespawnCoordinates(World par0World, ChunkCoordinates par1ChunkCoordinates, boolean par2)
    {
        IChunkProvider var3 = par0World.getChunkProvider();
        var3.loadChunk(par1ChunkCoordinates.posX - 3 >> 4, par1ChunkCoordinates.posZ - 3 >> 4);
        var3.loadChunk(par1ChunkCoordinates.posX + 3 >> 4, par1ChunkCoordinates.posZ - 3 >> 4);
        var3.loadChunk(par1ChunkCoordinates.posX - 3 >> 4, par1ChunkCoordinates.posZ + 3 >> 4);
        var3.loadChunk(par1ChunkCoordinates.posX + 3 >> 4, par1ChunkCoordinates.posZ + 3 >> 4);
        ChunkCoordinates c = par1ChunkCoordinates;
        Block block = Block.blocksList[par0World.getBlockId(c.posX, c.posY, c.posZ)];

        if (block == null || !block.isBed(par0World, c.posX, c.posY, c.posZ, null))
        {
            ChunkCoordinates var8 = block.getBedSpawnPosition(par0World, c.posX, c.posY, c.posZ, null);
            return var8;
        }
        else
        {
            Material var4 = par0World.getBlockMaterial(par1ChunkCoordinates.posX, par1ChunkCoordinates.posY, par1ChunkCoordinates.posZ);
            Material var5 = par0World.getBlockMaterial(par1ChunkCoordinates.posX, par1ChunkCoordinates.posY + 1, par1ChunkCoordinates.posZ);
            boolean var6 = !var4.isSolid() && !var4.isLiquid();
            boolean var7 = !var5.isSolid() && !var5.isLiquid();
            return par2 && var6 && var7 ? par1ChunkCoordinates : null;
        }
    }

    @SideOnly(Side.CLIENT)

    public float getBedOrientationInDegrees()
    {
        if (this.playerLocation != null)
        {
            int x = playerLocation.posX;
            int y = playerLocation.posY;
            int z = playerLocation.posZ;
            Block block = Block.blocksList[worldObj.getBlockId(x, y, z)];
            int var2 = (block == null ? 0 : block.getBedDirection(worldObj, x, y, z));

            switch (var2)
            {
                case 0:
                    return 90.0F;

                case 1:
                    return 0.0F;

                case 2:
                    return 270.0F;

                case 3:
                    return 180.0F;
            }
        }

        return 0.0F;
    }

    public boolean isPlayerSleeping()
    {
        return this.sleeping;
    }

    public boolean isPlayerFullyAsleep()
    {
        return this.sleeping && this.sleepTimer >= 100;
    }

    @SideOnly(Side.CLIENT)
    public int getSleepTimer()
    {
        return this.sleepTimer;
    }

    @SideOnly(Side.CLIENT)
    protected boolean func_82241_s(int par1)
    {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1 << par1) != 0;
    }

    protected void func_82239_b(int par1, boolean par2)
    {
        byte var3 = this.dataWatcher.getWatchableObjectByte(16);

        if (par2)
        {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte)(var3 | 1 << par1)));
        }
        else
        {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte)(var3 & ~(1 << par1))));
        }
    }

    public void addChatMessage(String par1Str) {}

    public ChunkCoordinates getSpawnChunk()
    {
        return this.spawnChunk;
    }

    public boolean func_82245_bX()
    {
        return this.spawnForced;
    }

    public void setSpawnChunk(ChunkCoordinates par1ChunkCoordinates, boolean par2)
    {
        if (par1ChunkCoordinates != null)
        {
            this.spawnChunk = new ChunkCoordinates(par1ChunkCoordinates);
            this.spawnForced = par2;
        }
        else
        {
            this.spawnChunk = null;
            this.spawnForced = false;
        }
    }

    public void triggerAchievement(StatBase par1StatBase)
    {
        this.addStat(par1StatBase, 1);
    }

    public void addStat(StatBase par1StatBase, int par2) {}

    protected void jump()
    {
        super.jump();
        this.addStat(StatList.jumpStat, 1);

        if (this.isSprinting())
        {
            this.addExhaustion(0.8F);
        }
        else
        {
            this.addExhaustion(0.2F);
        }
    }

    public void moveEntityWithHeading(float par1, float par2)
    {
        double var3 = this.posX;
        double var5 = this.posY;
        double var7 = this.posZ;

        if (this.capabilities.isFlying && this.ridingEntity == null)
        {
            double var9 = this.motionY;
            float var11 = this.jumpMovementFactor;
            this.jumpMovementFactor = this.capabilities.getFlySpeed();
            super.moveEntityWithHeading(par1, par2);
            this.motionY = var9 * 0.6D;
            this.jumpMovementFactor = var11;
        }
        else
        {
            super.moveEntityWithHeading(par1, par2);
        }

        this.addMovementStat(this.posX - var3, this.posY - var5, this.posZ - var7);
    }

    public void addMovementStat(double par1, double par3, double par5)
    {
        if (this.ridingEntity == null)
        {
            int var7;

            if (this.isInsideOfMaterial(Material.water))
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5) * 100.0F);

                if (var7 > 0)
                {
                    this.addStat(StatList.distanceDoveStat, var7);
                    this.addExhaustion(0.015F * (float)var7 * 0.01F);
                }
            }
            else if (this.isInWater())
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par5 * par5) * 100.0F);

                if (var7 > 0)
                {
                    this.addStat(StatList.distanceSwumStat, var7);
                    this.addExhaustion(0.015F * (float)var7 * 0.01F);
                }
            }
            else if (this.isOnLadder())
            {
                if (par3 > 0.0D)
                {
                    this.addStat(StatList.distanceClimbedStat, (int)Math.round(par3 * 100.0D));
                }
            }
            else if (this.onGround)
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par5 * par5) * 100.0F);

                if (var7 > 0)
                {
                    this.addStat(StatList.distanceWalkedStat, var7);

                    if (this.isSprinting())
                    {
                        this.addExhaustion(0.099999994F * (float)var7 * 0.01F);
                    }
                    else
                    {
                        this.addExhaustion(0.01F * (float)var7 * 0.01F);
                    }
                }
            }
            else
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par5 * par5) * 100.0F);

                if (var7 > 25)
                {
                    this.addStat(StatList.distanceFlownStat, var7);
                }
            }
        }
    }

    private void addMountedMovementStat(double par1, double par3, double par5)
    {
        if (this.ridingEntity != null)
        {
            int var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5) * 100.0F);

            if (var7 > 0)
            {
                if (this.ridingEntity instanceof EntityMinecart)
                {
                    this.addStat(StatList.distanceByMinecartStat, var7);

                    if (this.startMinecartRidingCoordinate == null)
                    {
                        this.startMinecartRidingCoordinate = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
                    }
                    else if ((double)this.startMinecartRidingCoordinate.getDistanceSquared(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) >= 1000000.0D)
                    {
                        this.addStat(AchievementList.onARail, 1);
                    }
                }
                else if (this.ridingEntity instanceof EntityBoat)
                {
                    this.addStat(StatList.distanceByBoatStat, var7);
                }
                else if (this.ridingEntity instanceof EntityPig)
                {
                    this.addStat(StatList.distanceByPigStat, var7);
                }
            }
        }
    }

    protected void fall(float par1)
    {
        if (!this.capabilities.allowFlying)
        {
            if (par1 >= 2.0F)
            {
                this.addStat(StatList.distanceFallenStat, (int)Math.round((double)par1 * 100.0D));
            }

            super.fall(par1);
        }
    }

    public void onKillEntity(EntityLiving par1EntityLiving)
    {
        if (par1EntityLiving instanceof IMob)
        {
            this.triggerAchievement(AchievementList.killEnemy);
        }
    }

    public void setInWeb()
    {
        if (!this.capabilities.isFlying)
        {
            super.setInWeb();
        }
    }

    @SideOnly(Side.CLIENT)

    public int getItemIcon(ItemStack par1ItemStack, int par2)
    {
        int var3 = super.getItemIcon(par1ItemStack, par2);

        if (par1ItemStack.itemID == Item.fishingRod.shiftedIndex && this.fishEntity != null)
        {
            var3 = par1ItemStack.getIconIndex() + 16;
        }
        else
        {
            if (par1ItemStack.getItem().requiresMultipleRenderPasses())
            {
                return par1ItemStack.getItem().getIconFromItemStackForMultiplePasses(par1ItemStack, par2);
            }

            if (this.itemInUse != null && par1ItemStack.itemID == Item.bow.shiftedIndex)
            {
                int var4 = par1ItemStack.getMaxItemUseDuration() - this.itemInUseCount;

                if (var4 >= 18)
                {
                    return 133;
                }

                if (var4 > 13)
                {
                    return 117;
                }

                if (var4 > 0)
                {
                    return 101;
                }
            }

            var3 = par1ItemStack.getItem().getIconIndex(par1ItemStack, par2, this, itemInUse, itemInUseCount);
        }

        return var3;
    }

    public ItemStack getCurrentArmor(int par1)
    {
        return this.inventory.armorItemInSlot(par1);
    }

    protected void func_82164_bB() {}

    protected void func_82162_bC() {}

    public void addExperience(int par1)
    {
        this.func_85039_t(par1);
        int var2 = Integer.MAX_VALUE - this.experienceTotal;

        if (par1 > var2)
        {
            par1 = var2;
        }

        this.experience += (float)par1 / (float)this.xpBarCap();

        for (this.experienceTotal += par1; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap())
        {
            this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
            this.addExperienceLevel(1);
        }
    }

    public void addExperienceLevel(int par1)
    {
        this.experienceLevel += par1;

        if (this.experienceLevel < 0)
        {
            this.experienceLevel = 0;
        }

        if (par1 > 0 && this.experienceLevel % 5 == 0 && (float)this.field_82249_h < (float)this.ticksExisted - 100.0F)
        {
            float var2 = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
            this.func_85030_a("random.levelup", var2 * 0.75F, 1.0F);
            this.field_82249_h = this.ticksExisted;
        }
    }

    public int xpBarCap()
    {
        return this.experienceLevel >= 30 ? 62 + (this.experienceLevel - 30) * 7 : (this.experienceLevel >= 15 ? 17 + (this.experienceLevel - 15) * 3 : 17);
    }

    public void addExhaustion(float par1)
    {
        if (!this.capabilities.disableDamage)
        {
            if (!this.worldObj.isRemote)
            {
                this.foodStats.addExhaustion(par1);
            }
        }
    }

    public FoodStats getFoodStats()
    {
        return this.foodStats;
    }

    public boolean canEat(boolean par1)
    {
        return (par1 || this.foodStats.needFood()) && !this.capabilities.disableDamage;
    }

    public boolean shouldHeal()
    {
        return this.getHealth() > 0 && this.getHealth() < this.getMaxHealth();
    }

    public void setItemInUse(ItemStack par1ItemStack, int par2)
    {
        if (par1ItemStack != this.itemInUse)
        {
            this.itemInUse = par1ItemStack;
            this.itemInUseCount = par2;

            if (!this.worldObj.isRemote)
            {
                this.setEating(true);
            }
        }
    }

    public boolean canCurrentToolHarvestBlock(int par1, int par2, int par3)
    {
        if (this.capabilities.allowEdit)
        {
            return true;
        }
        else
        {
            int var4 = this.worldObj.getBlockId(par1, par2, par3);

            if (var4 > 0)
            {
                Block var5 = Block.blocksList[var4];

                if (var5.blockMaterial.func_85157_q())
                {
                    return true;
                }

                if (this.getCurrentEquippedItem() != null)
                {
                    ItemStack var6 = this.getCurrentEquippedItem();

                    if (var6.canHarvestBlock(var5) || var6.getStrVsBlock(var5) > 1.0F)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean func_82247_a(int par1, int par2, int par3, int par4, ItemStack par5ItemStack)
    {
        return this.capabilities.allowEdit ? true : (par5ItemStack != null ? par5ItemStack.func_82835_x() : false);
    }

    protected int getExperiencePoints(EntityPlayer par1EntityPlayer)
    {
        if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
        {
            return 0;
        }
        else
        {
            int var2 = this.experienceLevel * 7;
            return var2 > 100 ? 100 : var2;
        }
    }

    protected boolean isPlayer()
    {
        return true;
    }

    public String getEntityName()
    {
        return this.username;
    }

    public void clonePlayer(EntityPlayer par1EntityPlayer, boolean par2)
    {
        if (par2)
        {
            this.inventory.copyInventory(par1EntityPlayer.inventory);
            this.health = par1EntityPlayer.health;
            this.foodStats = par1EntityPlayer.foodStats;
            this.experienceLevel = par1EntityPlayer.experienceLevel;
            this.experienceTotal = par1EntityPlayer.experienceTotal;
            this.experience = par1EntityPlayer.experience;
            this.func_85040_s(par1EntityPlayer.getScore());
            this.field_82152_aq = par1EntityPlayer.field_82152_aq;
        }
        else if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
        {
            this.inventory.copyInventory(par1EntityPlayer.inventory);
            this.experienceLevel = par1EntityPlayer.experienceLevel;
            this.experienceTotal = par1EntityPlayer.experienceTotal;
            this.experience = par1EntityPlayer.experience;
            this.func_85040_s(par1EntityPlayer.getScore());
        }

        this.theInventoryEnderChest = par1EntityPlayer.theInventoryEnderChest;
    }

    protected boolean canTriggerWalking()
    {
        return !this.capabilities.isFlying;
    }

    public void sendPlayerAbilities() {}

    public void sendGameTypeToPlayer(EnumGameType par1EnumGameType) {}

    public String getCommandSenderName()
    {
        return this.username;
    }

    public StringTranslate getTranslator()
    {
        return StringTranslate.getInstance();
    }

    public String translateString(String par1Str, Object ... par2ArrayOfObj)
    {
        return this.getTranslator().translateKeyFormat(par1Str, par2ArrayOfObj);
    }

    public InventoryEnderChest getInventoryEnderChest()
    {
        return this.theInventoryEnderChest;
    }

    public ItemStack getCurrentItemOrArmor(int par1)
    {
        return par1 == 0 ? this.inventory.getCurrentItem() : this.inventory.armorInventory[par1 - 1];
    }

    public ItemStack getHeldItem()
    {
        return this.inventory.getCurrentItem();
    }

    public void setCurrentItemOrArmor(int par1, ItemStack par2ItemStack)
    {
        this.inventory.armorInventory[par1] = par2ItemStack;
    }

    public ItemStack[] getLastActiveItems()
    {
        return this.inventory.armorInventory;
    }

    @SideOnly(Side.CLIENT)
    public boolean func_82238_cc()
    {
        return this.func_82241_s(1);
    }

    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z)
    {
        FMLNetworkHandler.openGui(this, mod, modGuiId, world, x, y, z);
    }
}
