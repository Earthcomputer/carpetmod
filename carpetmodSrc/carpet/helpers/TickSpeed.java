package carpet.helpers;

import carpet.CarpetServer;
import carpet.utils.Messenger;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class TickSpeed
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static float tickrate = 20.0f;
    public static long mspt = 50l;
    public static long warp_temp_mspt = 1l;
    public static long time_bias = 0;
    public static long time_warp_start_time = 0;
    public static long time_warp_scheduled_ticks = 0;
    public static EntityPlayer time_advancerer = null;
    public static String tick_warp_callback = null;
    public static ICommandSender tick_warp_sender = null;
    public static boolean is_superHot = false;
    public static boolean is_paused_setting = false;
    public static boolean is_paused = false;
    public static int ticks_to_run_in_pause = 0;
    private static int frozenTickCounter = 0;

    public static void tickrate(float rate)
    {
        tickrate = rate;
        mspt = (long)(1000.0/tickrate);
        if (mspt <=0)
        {
            mspt = 1l;
            tickrate = 1000.0f;
        }
    }

    public static String tickrate_advance(EntityPlayer player, long advance, String callback, ICommandSender icommandsender)
    {
        if (0 == advance)
        {
            tick_warp_callback = null;
            tick_warp_sender = null;
            finish_time_warp();
            return "Warp interrupted";
        }
        if (time_bias > 0)
        {
            return "Another player is already advancing time at the moment. Try later or talk to them";
        }
        time_advancerer = player;
        time_warp_start_time = System.nanoTime();
        time_warp_scheduled_ticks = advance;
        time_bias = advance;
        tick_warp_callback = callback;
        tick_warp_sender = icommandsender;
        return "Warp speed ....";
    }

    public static void finish_time_warp()
    {

        long completed_ticks = time_warp_scheduled_ticks - time_bias;
        double milis_to_complete = System.nanoTime()-time_warp_start_time;
        if (milis_to_complete == 0.0)
        {
            milis_to_complete = 1.0;
        }
        milis_to_complete /= 1000000.0;
        int tps = (int) (1000.0D*completed_ticks/milis_to_complete);
        double mspt = (1.0*milis_to_complete)/completed_ticks;
        time_warp_scheduled_ticks = 0;
        time_warp_start_time = 0;
        if (tick_warp_callback != null)
        {
            ICommandManager icommandmanager = tick_warp_sender.getServer().getCommandManager();
            try
            {
                int j = icommandmanager.executeCommand(tick_warp_sender, tick_warp_callback);

                if (j < 1)
                {
                    if (time_advancerer != null)
                    {
                        Messenger.m(time_advancerer, "r Command Callback failed: ", "rb /"+tick_warp_callback,"/"+tick_warp_callback);
                    }
                }
            }
            catch (Throwable var23)
            {
                if (time_advancerer != null)
                {
                    Messenger.m(time_advancerer, "r Command Callback failed - unknown error: ", "rb /"+tick_warp_callback,"/"+tick_warp_callback);
                }
            }
            tick_warp_callback = null;
            tick_warp_sender = null;
        }
        if (time_advancerer != null)
        {
            Messenger.m(time_advancerer, String.format("gi ... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
            time_advancerer = null;
        }
        else
        {
            Messenger.print_server_message(CarpetServer.minecraft_server, String.format("... Time warp completed with %d tps, or %.2f mspt",tps, mspt ));
        }
        time_bias = 0;

    }

    public static boolean continueWarp()
    {
        if (time_bias > 0)
        {
            if (time_bias == time_warp_scheduled_ticks) //first call after previous tick, adjust start time
            {
                time_warp_start_time = System.nanoTime();
            }
            time_bias -= 1;
            return true;
        }
        else
        {
            finish_time_warp();
            return false;
        }
    }

    public static void pausedTick(MinecraftServer server)
    {
        if (ticks_to_run_in_pause > 0)
        {
            ticks_to_run_in_pause--;
            is_paused = false;
            server.tick();
            is_paused = is_paused_setting;
        }

        frozenTickCounter++;
        long time = System.nanoTime();

        // Player updates and packets
        synchronized (server.futureTaskQueue)
        {
            while (!server.futureTaskQueue.isEmpty())
            {
                Util.runTask(server.futureTaskQueue.poll(), LOGGER);
            }
        }

        for (WorldServer world : server.worlds)
        {
            // Time sync
            if (frozenTickCounter % 20 == 0)
            {
                server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(
                                world.getTotalWorldTime(),
                                world.getWorldTime(),
                                false),
                        world.provider.getDimensionType().getId());
            }

            //TODO: part of playerchunkmap tick to send loaded chunks to clients
            // Send movement of other players to other clients
            world.getEntityTracker().tick();
            // Player movement and stuff
            world.tickPlayers();
        }

        // Update server status response
        if (time - server.nanoTimeSinceStatusRefresh >= 5000000000L)
        {
            server.nanoTimeSinceStatusRefresh = time;
            server.getServerStatusResponse().setPlayers(new ServerStatusResponse.Players(server.getMaxPlayers(), server.getCurrentPlayerCount()));
            GameProfile[] gameProfiles = new GameProfile[Math.min(server.getCurrentPlayerCount(), 12)];
            int startIndex = MathHelper.getInt(new Random(), 0, server.getCurrentPlayerCount() - gameProfiles.length);

            for (int i = 0; i < gameProfiles.length; i++)
            {
                gameProfiles[i] = server.getPlayerList().getPlayers().get(startIndex + i).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(gameProfiles));
            server.getServerStatusResponse().getPlayers().setPlayers(gameProfiles);
        }

        server.getNetworkSystem().networkTick();
        server.getPlayerList().onTick();
    }

    public static void add_ticks_to_run_in_pause(int advance)
    {
        ticks_to_run_in_pause += advance;
    }
}
