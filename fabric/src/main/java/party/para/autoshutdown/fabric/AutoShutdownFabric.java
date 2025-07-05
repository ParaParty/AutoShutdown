package party.para.autoshutdown.fabric;

import net.fabricmc.api.ModInitializer;

import party.para.autoshutdown.AutoShutdown;

public final class AutoShutdownFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        AutoShutdown.init();
    }
}
