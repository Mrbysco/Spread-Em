package com.mrbysco.spreadem;

import com.mojang.logging.LogUtils;
import com.mrbysco.spreadem.config.SpreadConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SpreadEm.MOD_ID)
public class SpreadEm {
	public static final String MOD_ID = "spreadem";
	public static final String knownPlayer = MOD_ID + ".knownPlayer";

	public static final Logger LOGGER = LogUtils.getLogger();

	public SpreadEm() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpreadConfig.commonSpec);
		eventBus.register(SpreadConfig.class);

		MinecraftForge.EVENT_BUS.register(this);
	}
}
