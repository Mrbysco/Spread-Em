package com.mrbysco.spreadem.config;

import com.mrbysco.spreadem.SpreadEm;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SpreadConfig {

	public static class Common {
		public final ForgeConfigSpec.IntValue spreadDistance;
		public final ForgeConfigSpec.BooleanValue blacklistOceans;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> biomeBlacklist;

		Common(ForgeConfigSpec.Builder builder) {
			//General settings
			builder.comment("General settings")
					.push("general");

			spreadDistance = builder
					.comment("The distance used to spread players in blocks (default: 2000)")
					.defineInRange("spreadDistance", 1, 2000, Integer.MAX_VALUE);

			builder.pop();

			//Blacklist settings
			builder.comment("Blacklist Settings")
					.push("blacklist");

			blacklistOceans = builder
					.comment("If this is set to true, the mod will use the tag for ocean biomes to blacklist them (default: true)")
					.define("blacklistOceans", true);

			biomeBlacklist = builder
					.comment("Biomes in this list will be blacklisted from having players spawn in them. By default the mod uses a tag for ocean biomes.")
					.defineListAllowEmpty("biomeBlacklist", ArrayList::new, o -> (o instanceof String));

			builder.pop();
		}
	}

	public static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		SpreadEm.LOGGER.debug("Loaded Spread Em's config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		SpreadEm.LOGGER.warn("Spread Em's config just got changed on the file system!");
	}
}
