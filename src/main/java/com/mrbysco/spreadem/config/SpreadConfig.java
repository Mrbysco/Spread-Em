package com.mrbysco.spreadem.config;

import java.util.ArrayList;

import com.mrbysco.spreadem.Reference;
import com.mrbysco.spreadem.SpreadEm;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Config(modid = Reference.MOD_ID, name = "SpreadEm", category = "")
@Config.LangKey("spreadem.config.title")
public class SpreadConfig {
    @Config.Comment({"General settings"})
    public static General general = new General();

    public static class General{
        @Config.Comment("Sets the dimension the player is spread in [0 = overworld] [default: 0]")
        public int spreadDimension = 0;

        @Config.Comment("Sets the distance it uses to spread players in blocks [default: 2000]")
        @Config.RangeInt(min = 1)
        public int spreadDistance = 2000;
    }

    @Config.Comment({"Blacklist settings"})
    public static BlackList blacklist = new BlackList();

    public static class BlackList{
        @Config.Comment("Adding biome names to this list will stop the player from being spawned in that biome")
        public String[] biomeBlacklist = new String[]
                {
                        "minecraft:ocean",
                        "minecraft:deep_ocean",
                        "minecraft:frozen_ocean",
                        "minecraft:river",
                        "minecraft:frozen_river"
                };
    }
    

    @Config.Ignore
    public static ArrayList<ResourceLocation> _blacklistedBiomes = new ArrayList<>();
    
    public static void initBlacklist()
    {
    	_blacklistedBiomes.clear();
    	
    	for(String name : SpreadConfig.blacklist.biomeBlacklist) {
    		name.trim();
    		if(!name.isEmpty()) {
    			final ResourceLocation resource = new ResourceLocation(name);
    			if(ForgeRegistries.BIOMES.containsKey(resource)) {
    				_blacklistedBiomes.add(resource);
    			} else {
    				SpreadEm.logger.error("Config: Failed to locate biome by the name of " + name);
    			}
    		}
    	}
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Reference.MOD_ID)) {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
                initBlacklist();
            }
        }
    }
}
