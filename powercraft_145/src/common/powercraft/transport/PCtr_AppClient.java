package powercraft.transport;

import java.util.List;

import powercraft.management.PC_IClientModule;
import powercraft.management.PC_LangEntry;
import powercraft.management.PC_Utils;

public class PCtr_AppClient extends PCtr_App implements PC_IClientModule {



    @Override
	public List<String> loadTextureFiles(List<String> textures)
    {
		textures.add(PC_Utils.getTerrainFile(this));
		return textures;
    }

	@Override
	public List<PC_LangEntry> initLanguage(List<PC_LangEntry> lang) {
		lang.add(new PC_LangEntry("pc.gui.separationBelt.group", "Ignore subtypes of"));
		lang.add(new PC_LangEntry("pc.gui.separationBelt.groupLogs", "Logs"));
		lang.add(new PC_LangEntry("pc.gui.separationBelt.groupPlanks", "Planks"));
		lang.add(new PC_LangEntry("pc.gui.separationBelt.groupAll", "All"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeEjectTitle", "Ejection mode:"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeStacks", "Whole stacks"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeItems", "Single items"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeAll", "All contents at once"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeSelectTitle", "Method of selection:"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeSelectFirst", "First slot"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeSelectLast", "Last slot"));
		lang.add(new PC_LangEntry("pc.gui.ejector.modeSelectRandom", "Random slot"));
        return lang;
	}

	@Override
	public List<String> addSplashes(List<String> list) {
		// TODO Auto-generated method stub
		return null;
	}
}