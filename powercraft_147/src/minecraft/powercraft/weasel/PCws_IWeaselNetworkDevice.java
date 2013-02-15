package powercraft.weasel;

import powercraft.management.PC_Color;
import weasel.WeaselFunctionManager;
import weasel.obj.WeaselObject;

public interface PCws_IWeaselNetworkDevice {

	public boolean doesProvideFunctionOnEngine(String functionName);

	public void callFunctionOnEngine(String functionName, WeaselObject... args);

	public void setNetwork(int i);

	public String getName();

	public int getID();

	public WeaselFunctionManager makePluginProvider();

	public void setNetworkName(String name);

	public void setNetworkColor(PC_Color copy);

	public PCws_WeaselNetwork getNetwork();

}
