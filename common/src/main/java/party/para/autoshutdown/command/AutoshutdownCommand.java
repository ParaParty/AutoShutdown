package party.para.autoshutdown.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import party.para.autoshutdown.impl.IMinecraftServerMixin;

public class AutoshutdownCommand {
    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registry,
            Commands.CommandSelection selection
    ) {
        dispatcher.register(
                Commands.literal("autoshutdown")
                        .requires(commandSourceStack -> {
                             if (commandSourceStack.hasPermission(4))
                                 return commandSourceStack.getServer().getPlayerCount() > 0;

                             if (commandSourceStack.getPlayer() != null)
                                 return commandSourceStack.getServer().getPlayerCount() == 1;

                             return false;
                        })
                        .executes(commandContext -> {
                            MinecraftServer server = commandContext.getSource().getServer();
                            IMinecraftServerMixin serverMixin = (IMinecraftServerMixin) server;

                            server.getPlayerList().broadcastSystemMessage(
                                    Component.literal("服务器将会在所有玩家退出后关闭。"),
                                    false
                            );
                            serverMixin.autoshutdown_setShutdownImmediately(true);

                            return 1;
                        })
        );
    }
}
