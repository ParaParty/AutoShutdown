package party.para.autoshutdown;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Mod("auto-shutdown")
public class AutoShutdown {
    private static final Logger LOGGER = LogManager.getLogger();

    private Date theLastPlayerLogoutAt = null;

    private boolean shutdownImmediately = false;

    private static final int MILLIS_TO_SHUTDOWN = 1000 * 60 * 10;

    private final Date startTime = new Date();

    public AutoShutdown() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerAboutToStart(FMLServerAboutToStartEvent t) {
        String motd = t.getServer().getMOTD();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getDefault());
        String text = sdf.format(startTime);
        motd = motd + " " + text;
        t.getServer().setMOTD(motd);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent t) {
        playerCountWatchDog();
        shutdownWatchDog(t);
    }

    private void shutdownWatchDog(TickEvent.ServerTickEvent t) {
        if (!t.side.isServer()) {
            return;
        }

        if (!t.phase.equals(TickEvent.Phase.START)) {
            return;
        }

        if (theLastPlayerLogoutAt == null) {
            return;
        }

        long diff = new Date().getTime() - theLastPlayerLogoutAt.getTime();
        if (shutdownImmediately || (diff > MILLIS_TO_SHUTDOWN)) {
            LOGGER.info("Auto shutdown.");
            ServerLifecycleHooks.getCurrentServer().initiateShutdown(true);
        }
    }

    int lastPlayerCount = 0;

    private void playerCountWatchDog() {
        int nowPlayerCount = ServerLifecycleHooks.getCurrentServer().getPlayerList().getCurrentPlayerCount();

        if (nowPlayerCount == 0 && lastPlayerCount > 0) {
            theLastPlayerLogoutAt = new Date();
            LOGGER.info("lastPlayerLogoutAt: {}", theLastPlayerLogoutAt);
        }

        lastPlayerCount = nowPlayerCount;
    }

    @SubscribeEvent
    public void onRegisterCommandEvent(RegisterCommandsEvent t) {
        CommandDispatcher<CommandSource> commandManager = t.getDispatcher();
        commandManager.register(Commands.literal("autoshutdown").requires((source) -> {
            try {
                source.asPlayer();
            } catch (Exception e) {
                return false;
            }
            return ServerLifecycleHooks.getCurrentServer().getPlayerList().getCurrentPlayerCount() == 1;
        }).executes((source) -> {
            ServerLifecycleHooks.getCurrentServer().getPlayerList().func_232641_a_(new StringTextComponent("服务器将会在人数变为 0 后关闭。"), ChatType.SYSTEM, Util.DUMMY_UUID);
            shutdownImmediately = true;
            return Command.SINGLE_SUCCESS;
        }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent t) {
        if (theLastPlayerLogoutAt != null) {
            LOGGER.info("Auto shutdown cancelled.");
        }
        if (shutdownImmediately) {
            ServerLifecycleHooks.getCurrentServer().getPlayerList().func_232641_a_(new StringTextComponent("服务器将会在在线人数持续 10 分钟为 0 时关闭。"), ChatType.SYSTEM, Util.DUMMY_UUID);
        }

        theLastPlayerLogoutAt = null;
        shutdownImmediately = false;
    }
}
