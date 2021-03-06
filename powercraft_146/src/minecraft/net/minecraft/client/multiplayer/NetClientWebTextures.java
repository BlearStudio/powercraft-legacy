package net.minecraft.client.multiplayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
class NetClientWebTextures extends GuiScreen
{
    /** The Texture Pack's name. */
    final String texturePackName;

    /** Initialises Web Textures? */
    final NetClientHandler netClientHandlerWebTextures;

    NetClientWebTextures(NetClientHandler par1NetClientHandler, String par2Str)
    {
        this.netClientHandlerWebTextures = par1NetClientHandler;
        this.texturePackName = par2Str;
    }

    public void confirmClicked(boolean par1, int par2)
    {
        this.mc = Minecraft.getMinecraft();

        if (this.mc.getServerData() != null)
        {
            this.mc.getServerData().setAcceptsTextures(par1);
            ServerList.func_78852_b(this.mc.getServerData());
        }

        if (par1)
        {
            this.mc.texturePackList.requestDownloadOfTexture(this.texturePackName);
        }

        this.mc.displayGuiScreen((GuiScreen)null);
    }
}
