package powercraft.logic;

import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import powercraft.core.PC_Block;
import powercraft.core.PC_Module;
import powercraft.core.PC_Utils;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid="PowerCraft-Logik", name="PowerCraft-Logik", version="0.0.1Alpha", dependencies="required-after:PowerCraft-Core")
@NetworkMod(clientSideRequired=true, serverSideRequired=true)
public class mod_PowerCraftLogic extends PC_Module {

	@SidedProxy(clientSide = "powercraft.logic.PClo_ClientProxy", serverSide = "powercraft.logic.PClo_CommonProxy")
	public static PClo_CommonProxy proxy;
	
	public static PC_Block pulsar;
	public static PC_Block gateOff;
	public static PC_Block gateOn;
	
	public static mod_PowerCraftLogic getInstance(){
		return (mod_PowerCraftLogic)PC_Module.getModule("PowerCraft-Logik");
	}
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event){
		
		preInit(event, proxy);
		
	}
	
	@Init
	public void init(FMLInitializationEvent event){
		
		init();
		
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event){
		
		postInit();
		
	}
	
	@Override
	protected void initProperties(Configuration config) {
		
	}

	@Override
	protected List<String> loadTextureFiles(List<String> textures) {
		textures.add(getTerrainFile());
		return textures;
	}

	@Override
	protected void initLanguage() {
		PC_Utils.registerLanguage(this,
				"pc.pulsar.clickMsg", "Period %s ticks (%s s)",
				"pc.pulsar.clickMsgTime", "Period %s ticks (%s s), remains %s",
				"pc.gui.pulsar.silent", "Silent",
				"pc.gui.pulsar.paused", "Pause",
				"pc.gui.pulsar.delay", "Delay (sec)",
				"pc.gui.pulsar.hold", "Hold time (sec)",
				"pc.gui.pulsar.ticks", "ticks",
				"pc.gui.pulsar.errDelay", "Bad delay time!",
				"pc.gui.pulsar.errHold", "Bad hold time!"
		);
	}

	@Override
	protected void initBlocks() {
		pulsar = (PC_Block)PC_Utils.register(this, 461, PClo_BlockPulsar.class, PClo_TileEntityPulsar.class);
		gateOff = (PC_Block)PC_Utils.register(this, 462, PClo_BlockPulsar.class, PClo_ItemBlockGate.class);
		gateOn = (PC_Block)PC_Utils.register(this, 463, PClo_BlockPulsar.class);
	}

	@Override
	protected void initItems() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void initRecipes() {
		// *** pulsar ***
		
		PC_Utils.addRecipe(new ItemStack(pulsar, 1, 0),
				new Object[] { " r ", "ror", " r ",
				'r', Item.redstone, 'o', Block.obsidian });
	}

}