package party.para.autoshutdown.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import party.para.autoshutdown.impl.IMinecraftServerMixin;

import java.time.Instant;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IMinecraftServerMixin {
    @Unique
    private Instant autoshutdown_pendingShutdownTime;

    @Override
    public Instant autoshutdown_getPendingShutdownTime() {
        return autoshutdown_pendingShutdownTime;
    }

    @Override
    public void autoshutdown_setPendingShutdownTime(Instant pendingShutdownTime) {
        autoshutdown_pendingShutdownTime = pendingShutdownTime;
    }

    @Unique
    private boolean autoshutdown_shutdownImmediately = false;

    @Override
    public boolean autoshutdown_isShutdownImmediately() {
        return autoshutdown_shutdownImmediately;
    }

    @Override
    public void autoshutdown_setShutdownImmediately(boolean shutdownImmediately) {
        autoshutdown_shutdownImmediately = shutdownImmediately;
    }
}
