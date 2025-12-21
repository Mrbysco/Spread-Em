package com.mrbysco.spreadem;

import com.mojang.logging.LogUtils;
import com.mrbysco.spreadem.config.SpreadConfig;
import com.mrbysco.spreadem.handlers.SpreadHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SpreadEm.MOD_ID)
public class SpreadEm {
	public static final String MOD_ID = "spreadem";

	public static final Logger LOGGER = LogUtils.getLogger();

	public SpreadEm(IEventBus eventBus, Dist dist, ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, SpreadConfig.commonSpec);
		eventBus.register(SpreadConfig.class);

		NeoForge.EVENT_BUS.register(new SpreadHandler());

		if (dist.isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}
}
