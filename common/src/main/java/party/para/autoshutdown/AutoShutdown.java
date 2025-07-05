package party.para.autoshutdown;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import party.para.autoshutdown.command.AutoshutdownCommand;
import party.para.autoshutdown.impl.IMinecraftServerMixin;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AutoShutdown {
    public static final String MOD_ID = "auto_shutdown";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(AutoShutdown::onServerBeforeStart);
        CommandRegistrationEvent.EVENT.register(AutoshutdownCommand::register);
        PlayerEvent.PLAYER_JOIN.register(AutoShutdown::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(AutoShutdown::onPlayerQuit);
        TickEvent.SERVER_POST.register(AutoShutdown::onTickServerPost);
    }

    private static void onServerBeforeStart(MinecraftServer server) {
        String motd = server.getMotd();
        motd += " " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
        server.setMotd(motd);
    }

    private static void onPlayerJoin(ServerPlayer player) {
        MinecraftServer server = player.server;
        IMinecraftServerMixin serverMixin = (IMinecraftServerMixin) server;

        if (serverMixin.autoshutdown_getPendingShutdownTime() != null) {
            LOGGER.info("Auto shutdown cancelled.");
        }

        if (serverMixin.autoshutdown_isShutdownImmediately()) {
            server.getPlayerList().broadcastSystemMessage(
                    Component.literal("服务器将会在在线人数持续 10 分钟为 0 时关闭。"),
                    false
            );
        }

        serverMixin.autoshutdown_setPendingShutdownTime(null);
        serverMixin.autoshutdown_setShutdownImmediately(false);
    }

    private static void onPlayerQuit(ServerPlayer player) {
        IMinecraftServerMixin serverMixin = (IMinecraftServerMixin) player.server;

        if (player.server.getPlayerCount() == 0) {
            Instant pendingShutdownTime = serverMixin.autoshutdown_isShutdownImmediately()
                    ? Instant.now()
                    : Instant.now().plus(Duration.ofMinutes(10));

            LOGGER.info("Auto shutdown pending.");
            serverMixin.autoshutdown_setPendingShutdownTime(pendingShutdownTime);
        }
    }

    private static void onTickServerPost(MinecraftServer server) {
        IMinecraftServerMixin serverMixin = (IMinecraftServerMixin) server;

        Instant pendingShutdownTime = serverMixin.autoshutdown_getPendingShutdownTime();

        if (pendingShutdownTime == null)
            return;

        if (Instant.now().isBefore(pendingShutdownTime))
            return;

        LOGGER.info("Auto shutdown.");
        server.halt(false);
    }
}
