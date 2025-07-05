package party.para.autoshutdown.impl;

import java.time.Instant;

public interface IMinecraftServerMixin {

    Instant autoshutdown_getPendingShutdownTime();

    void autoshutdown_setPendingShutdownTime(Instant pendingShutdownTime);

    boolean autoshutdown_isShutdownImmediately();

    void autoshutdown_setShutdownImmediately(boolean shutdownImmediately);
}
