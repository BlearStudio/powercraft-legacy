package powercraft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public abstract class PC_Module{
	
	private static HashMap<String, PC_Module> modules = new HashMap<String, PC_Module>();
	private static List<String> splashes = new ArrayList<String>();
	private File cfgdir=null;
	private Configuration config=null;
	private PC_Proxy proxy;
	private boolean updateAvailable = false;
	private String updateText;
	private String updateModVersion = null;
	private String lastIgnoredUpdateVersion;
	
	public ModContainer getModContainer(){
		List<ModContainer> modContainers = Loader.instance().getActiveModList();
		for(ModContainer modContainer:modContainers){
			if(modContainer.matches(this))
				return modContainer;
		}
		return null;
	}
	
	public ModMetadata getModMetadata(){
		return getModContainer().getMetadata();
	}

	public String getName(){
		return getModMetadata().name;
	}
	
	public String getNameWithoutPowerCraft(){
		String name=getName();
		return name.substring(name.indexOf('-')+1);
	}
	
	public String getVersion() {
		return getModMetadata().version;
	}
	
	public String getTextureDirectory(){
		return "/powercraft/"+getNameWithoutPowerCraft().toLowerCase()+"/textures/";
	}
	
	public String getTerrainFile() {
		return getTextureDirectory()+"tiles.png";
	}
	
	protected void registerModule(){
		modules.put(getName().toLowerCase(), this);
		
		PC_Utils.loadLanguage(this);
		
		ModMetadata mm = getModMetadata();
		mm.autogenerated = false;
		mm.authorList = new ArrayList<String>();
		mm.authorList.add("XOR");
		mm.authorList.add("Rapus");
		mm.credits = "MightyPork, RxD";
		mm.description = "";
		mm.logoFile = "";
		mm.url = "http://powercrafting.net/";
		PC_Module core = getModule("Core");
		if(this!=core){
			mm.parent = "PowerCraft-Core";
		}
		
	}
	
	public static PC_Module getModule(String module){
		module = module.toLowerCase();
		if(modules.containsKey(module))
			return modules.get(module);
		module = "powercraft-"+module;
		if(modules.containsKey(module))
			return modules.get(module);
		return null;
	}
	
	protected void setCfgdir(File cfgdir){
		this.cfgdir = cfgdir;
	}
	
	public Configuration getConfig(){
		if(config==null){
			config = new Configuration(cfgdir);
			config.load();
		}
		return config;
	}

	protected void preInit(FMLPreInitializationEvent event, PC_Proxy proxy){
		this.proxy = proxy;
		registerModule();
		setCfgdir(event.getSuggestedConfigurationFile());
		lastIgnoredUpdateVersion = PC_Utils.getConfigString(getConfig(), Configuration.CATEGORY_GENERAL, "lastIgnoredUpdateVersion", "");
		initProperties(getConfig());
		proxy.registerRenderer();
		List<String> textures = loadTextureFiles(new ArrayList<String>());
		if(textures!=null && textures.size()>0)
			PC_Utils.registerTextureFiles(textures.toArray(new String[0]));
		initLanguage();
	}
	
	protected abstract void initProperties(Configuration config);
	protected abstract List<String> loadTextureFiles(List<String> textures);
	protected abstract void initLanguage();
	
	protected void init(){
		proxy.init();
		initBlocks();
		initItems();
		initRecipes();
		PC_Utils.registerGresArray(proxy.registerGuis());
		PC_PacketHandler.registerPackethandlers(proxy.registerPackethandlers());
		proxy.registerTileEntitySpecialRenderers();
		List<String> list = addSplashes(new ArrayList<String>());
		if(list!=null)
			splashes.addAll(list);
	}
	
	protected abstract void initBlocks();
	protected abstract void initItems();
	protected abstract void initRecipes();
	protected abstract List<String> addSplashes(List<String> list);
	
	protected void postInit(){
		PC_Utils.saveLanguage(this);
		getConfig().save();
	}

	public static String getPowerCraftFile() {
		return "/PowerCraft/";
	}
	
	public static String getRandomSplash(Random rand){
		return splashes.get(rand.nextInt(splashes.size()));
	}

	public boolean updateInfo(String sModuleVersion, String sInfo) {
		
		if(PC_Utils.isVersionNewer(getVersion(), sModuleVersion) && !lastIgnoredUpdateVersion.equalsIgnoreCase(sModuleVersion)){
				
			updateModVersion = sModuleVersion;
			updateText = sInfo.trim();
			
			return true;
			
		}
		
		return false;
	}
	
	public String getUpdateModVersion(){
		return updateModVersion;
	}
	
	public String getUpdateText(){
		return updateText;
	}
	
	public boolean isUpdateAvailable(){
		return updateModVersion!=null;
	}
	
	public void ignoreUpdateVersion(){
		if(isUpdateAvailable()){
			Configuration config = getConfig();
			config.get(Configuration.CATEGORY_GENERAL, "lastIgnoredUpdateVersion", "").value = updateModVersion;
			config.save();
		}
	}
	
	public int getLangVersion() {
		return PC_Utils.getConfigInt(getConfig(), Configuration.CATEGORY_GENERAL, "langVersion", 0);
	}
	
	public void setLangVersion(int langVersion) {
		getConfig().get(Configuration.CATEGORY_GENERAL, "langVersion", 0).value = ""+langVersion;
		getConfig().save();
	}
	
	public static void ignoreALLUpdateVersion(){
		for(PC_Module module: modules.values()){
			module.ignoreUpdateVersion();
		}
	}

	public static PC_Module[] getAllModules() {
		return modules.values().toArray(new PC_Module[0]);
	}
	
}
