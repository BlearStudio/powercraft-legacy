package net.minecraft.network;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.crypto.SecretKey;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet205ClientCommand;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet252SharedKey;
import net.minecraft.network.packet.Packet253ServerAuthData;
import net.minecraft.network.packet.Packet254ServerPing;
import net.minecraft.network.packet.Packet255KickDisconnect;
import net.minecraft.network.packet.Packet2ClientProtocol;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServerListenThread;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.StringUtils;

public class NetLoginHandler extends NetHandler
{
    /** The Random object used to generate serverId hex strings. */
    private static Random rand = new Random();

    /** The 4 byte verify token read from a Packet252SharedKey */
    private byte[] verifyToken;

    /** Reference to the MinecraftServer object. */
    private final MinecraftServer mcServer;
    public final TcpConnection myTCPConnection;
    public boolean connectionComplete = false;
    private int connectionTimer = 0;
    public String clientUsername = null;
    private volatile boolean field_72544_i = false;

    /** server ID that is randomly generated by this login handler. */
    private String loginServerId = "";
    private boolean field_92079_k = false;

    /** Secret AES key obtained from the client's Packet252SharedKey */
    private SecretKey sharedKey = null;

    public NetLoginHandler(MinecraftServer par1MinecraftServer, Socket par2Socket, String par3Str) throws IOException
    {
        this.mcServer = par1MinecraftServer;
        this.myTCPConnection = new TcpConnection(par1MinecraftServer.getLogAgent(), par2Socket, par3Str, this, par1MinecraftServer.getKeyPair().getPrivate());
        this.myTCPConnection.field_74468_e = 0;
    }

    /**
     * Logs the user in if a login packet is found, otherwise keeps processing network packets unless the timeout has
     * occurred.
     */
    public void tryLogin()
    {
        if (this.field_72544_i)
        {
            this.initializePlayerConnection();
        }

        if (this.connectionTimer++ == 6000)
        {
            this.raiseErrorAndDisconnect("Took too long to log in");
        }
        else
        {
            this.myTCPConnection.processReadPackets();
        }
    }

    public void raiseErrorAndDisconnect(String par1Str)
    {
        try
        {
            this.mcServer.getLogAgent().logInfo("Disconnecting " + this.getUsernameAndAddress() + ": " + par1Str);
            this.myTCPConnection.addToSendQueue(new Packet255KickDisconnect(par1Str));
            this.myTCPConnection.serverShutdown();
            this.connectionComplete = true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void handleClientProtocol(Packet2ClientProtocol par1Packet2ClientProtocol)
    {
        this.clientUsername = par1Packet2ClientProtocol.getUsername();

        if (!this.clientUsername.equals(StringUtils.stripControlCodes(this.clientUsername)))
        {
            this.raiseErrorAndDisconnect("Invalid username!");
        }
        else
        {
            PublicKey publickey = this.mcServer.getKeyPair().getPublic();

            if (par1Packet2ClientProtocol.getProtocolVersion() != 60)
            {
                if (par1Packet2ClientProtocol.getProtocolVersion() > 60)
                {
                    this.raiseErrorAndDisconnect("Outdated server!");
                }
                else
                {
                    this.raiseErrorAndDisconnect("Outdated client!");
                }
            }
            else
            {
                this.loginServerId = this.mcServer.isServerInOnlineMode() ? Long.toString(rand.nextLong(), 16) : "-";
                this.verifyToken = new byte[4];
                rand.nextBytes(this.verifyToken);
                this.myTCPConnection.addToSendQueue(new Packet253ServerAuthData(this.loginServerId, publickey, this.verifyToken));
            }
        }
    }

    public void handleSharedKey(Packet252SharedKey par1Packet252SharedKey)
    {
        PrivateKey privatekey = this.mcServer.getKeyPair().getPrivate();
        this.sharedKey = par1Packet252SharedKey.getSharedKey(privatekey);

        if (!Arrays.equals(this.verifyToken, par1Packet252SharedKey.getVerifyToken(privatekey)))
        {
            this.raiseErrorAndDisconnect("Invalid client reply");
        }

        this.myTCPConnection.addToSendQueue(new Packet252SharedKey());
    }

    public void handleClientCommand(Packet205ClientCommand par1Packet205ClientCommand)
    {
        if (par1Packet205ClientCommand.forceRespawn == 0)
        {
            if (this.field_92079_k)
            {
                this.raiseErrorAndDisconnect("Duplicate login");
                return;
            }

            this.field_92079_k = true;

            if (this.mcServer.isServerInOnlineMode())
            {
                (new ThreadLoginVerifier(this)).start();
            }
            else
            {
                this.field_72544_i = true;
            }
        }
    }

    public void handleLogin(Packet1Login par1Packet1Login)
    {
        FMLNetworkHandler.handleLoginPacketOnServer(this, par1Packet1Login);
    }

    /**
     * on success the specified username is connected to the minecraftInstance, otherwise they are packet255'd
     */
    public void initializePlayerConnection()
    {
        FMLNetworkHandler.onConnectionReceivedFromClient(this, this.mcServer, this.myTCPConnection.getSocketAddress(), this.clientUsername);
    }

    public void completeConnection(String s)
    {

        if (s != null)
        {
            this.raiseErrorAndDisconnect(s);
        }
        else
        {
            EntityPlayerMP entityplayermp = this.mcServer.getConfigurationManager().createPlayerForUser(this.clientUsername);

            if (entityplayermp != null)
            {
                this.mcServer.getConfigurationManager().initializeConnectionToPlayer(this.myTCPConnection, entityplayermp);
            }
        }

        this.connectionComplete = true;
    }

    public void handleErrorMessage(String par1Str, Object[] par2ArrayOfObj)
    {
        this.mcServer.getLogAgent().logInfo(this.getUsernameAndAddress() + " lost connection");
        this.connectionComplete = true;
    }

    /**
     * Handle a server ping packet.
     */
    public void handleServerPing(Packet254ServerPing par1Packet254ServerPing)
    {
        try
        {
            ServerConfigurationManager serverconfigurationmanager = this.mcServer.getConfigurationManager();
            String s = null;

            if (par1Packet254ServerPing.field_82559_a == 1)
            {
                List list = Arrays.asList(new Serializable[] {Integer.valueOf(1), Integer.valueOf(60), this.mcServer.getMinecraftVersion(), this.mcServer.getMOTD(), Integer.valueOf(serverconfigurationmanager.getCurrentPlayerCount()), Integer.valueOf(serverconfigurationmanager.getMaxPlayers())});
                Object object;

                for (Iterator iterator = list.iterator(); iterator.hasNext(); s = s + object.toString().replaceAll("\u0000", ""))
                {
                    object = iterator.next();

                    if (s == null)
                    {
                        s = "\u00a7";
                    }
                    else
                    {
                        s = s + "\u0000";
                    }
                }
            }
            else
            {
                s = this.mcServer.getMOTD() + "\u00a7" + serverconfigurationmanager.getCurrentPlayerCount() + "\u00a7" + serverconfigurationmanager.getMaxPlayers();
            }

            InetAddress inetaddress = null;

            if (this.myTCPConnection.getSocket() != null)
            {
                inetaddress = this.myTCPConnection.getSocket().getInetAddress();
            }

            this.myTCPConnection.addToSendQueue(new Packet255KickDisconnect(s));
            this.myTCPConnection.serverShutdown();

            if (inetaddress != null && this.mcServer.getNetworkThread() instanceof DedicatedServerListenThread)
            {
                ((DedicatedServerListenThread)this.mcServer.getNetworkThread()).func_71761_a(inetaddress);
            }

            this.connectionComplete = true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Default handler called for packets that don't have their own handlers in NetClientHandler; currentlly does
     * nothing.
     */
    public void unexpectedPacket(Packet par1Packet)
    {
        this.raiseErrorAndDisconnect("Protocol error");
    }

    public String getUsernameAndAddress()
    {
        return this.clientUsername != null ? this.clientUsername + " [" + this.myTCPConnection.getSocketAddress().toString() + "]" : this.myTCPConnection.getSocketAddress().toString();
    }

    /**
     * determine if it is a server handler
     */
    public boolean isServerHandler()
    {
        return true;
    }

    /**
     * Returns the server Id randomly generated by this login handler.
     */
    static String getServerId(NetLoginHandler par0NetLoginHandler)
    {
        return par0NetLoginHandler.loginServerId;
    }

    /**
     * Returns the reference to Minecraft Server.
     */
    static MinecraftServer getLoginMinecraftServer(NetLoginHandler par0NetLoginHandler)
    {
        return par0NetLoginHandler.mcServer;
    }

    /**
     * Return the secret AES sharedKey
     */
    static SecretKey getSharedKey(NetLoginHandler par0NetLoginHandler)
    {
        return par0NetLoginHandler.sharedKey;
    }

    /**
     * Returns the connecting client username.
     */
    static String getClientUsername(NetLoginHandler par0NetLoginHandler)
    {
        return par0NetLoginHandler.clientUsername;
    }

    public static boolean func_72531_a(NetLoginHandler par0NetLoginHandler, boolean par1)
    {
        return par0NetLoginHandler.field_72544_i = par1;
    }
    

    public void handleCustomPayload(Packet250CustomPayload par1Packet250CustomPayload)
    {
        FMLNetworkHandler.handlePacket250Packet(par1Packet250CustomPayload, myTCPConnection, this);
    }

    @Override
    public void handleVanilla250Packet(Packet250CustomPayload payload)
    {
        // NOOP for login
    }

    @Override
    public EntityPlayer getPlayer()
    {
        return null;
    }
}
