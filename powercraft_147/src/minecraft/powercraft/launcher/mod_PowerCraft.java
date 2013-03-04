package powercraft.launcher;

import java.util.List;

import powercraft.management.PC_ClientPacketHandler;
import powercraft.management.PC_PacketHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;

@Mod(modid = "PowerCraft", name = "PowerCraft", version = "3.5.0AlphaJ", dependencies = "after:*")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "PowerCraft" }, packetHandler = PC_ClientPacketHandler.class), serverPacketHandlerSpec = @SidedPacketHandler(channels = { "PowerCraft" }, packetHandler = PC_PacketHandler.class))
public class mod_PowerCraft{
	
	@SidedProxy(clientSide = "powercraft.launcher.PC_ClientProxy", serverSide = "powercraft.launcher.PC_CommonProxy")
	public static PC_CommonProxy proxy;

	private static final String updateInfoPath = "https://dl.dropbox.com/s/nrkmh98nchr7nrj/VersionInfo.xml?dl=1";
	
	private static mod_PowerCraft instance;
	
	private static PC_Launcher launcher = new PC_Launcher();
	
	public static mod_PowerCraft getInstance(){
		return instance;
	}
	
	public mod_PowerCraft(){
		instance = this;
	}
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		proxy.initUtils();
		launcher.preInit();
	}

	@Init
	public void init(FMLInitializationEvent event) {
		launcher.init();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		launcher.postInit();
	}

 	public ModContainer getModContainer()
    {
        List<ModContainer> modContainers = Loader.instance().getModList();

        for (ModContainer modContainer: modContainers)
        {
            if (modContainer.matches(this))
            {
                return modContainer;
            }
        }

        return null;
    }

    public ModMetadata getModMetadata()
    {
        return getModContainer().getMetadata();
    }
	
    /*public void hackInfo(){
    	 ModMetadata mm = getModMetadata();
         mm.autogenerated = false;
         mm.authorList = new ArrayList<String>();
         mm.authorList.add("XOR");
         mm.authorList.add("Rapus");
         mm.credits = "MightyPork, RxD, LOLerul2";
         mm.description = "";
         mm.logoFile = PC_TextureRegistry.getPowerCraftLoaderImageDir() + "PowerCraft.png";
         mm.url = "http://powercrafting.net/";
    }
    
	public static void registerBlock(Block block, Class<? extends ItemBlock> itemBlock){
		if(itemBlock==null)
			GameRegistry.registerBlock(block);
		else
			GameRegistry.registerBlock(block, itemBlock);
	}

	public static void registerTileEntity(Class<? extends TileEntity> tileEntityClass) {
		GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getName());
	}
	
	public static void addStringLocalization(String key, String lang, String value) {
		LanguageRegistry.instance().addStringLocalization(key, lang, value);
	}*/

	public String getVersion() {
		return getModMetadata().version;
	}
	
}
