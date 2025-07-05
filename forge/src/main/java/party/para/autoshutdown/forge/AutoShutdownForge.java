package party.para.autoshutdown.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import party.para.autoshutdown.AutoShutdown;

@Mod(AutoShutdown.MOD_ID)
public final class AutoShutdownForge {
    public AutoShutdownForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(AutoShutdown.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        AutoShutdown.init();
    }
}
