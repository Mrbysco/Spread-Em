package com.mrbysco.spreadem.config;

import com.mrbysco.spreadem.SpreadEm;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SpreadConfig {

	public static class Common {
		public final ModConfigSpec.IntValue spreadDistance;
		public final ModConfigSpec.BooleanValue blacklistOceans;
		public final ModConfigSpec.ConfigValue<List<? extends String>> biomeBlacklist;

		Common(ModConfigSpec.Builder builder) {
			//General settings
			builder.comment("General settings")
					.push("general");

			spreadDistance = builder
					.comment("The distance used to spread players in blocks (default: 2000)")
					.defineInRange("spreadDistance", 2000, 1, Integer.MAX_VALUE);

			builder.pop();

			//Blacklist settings
			builder.comment("Blacklist Settings")
					.push("blacklist");

			blacklistOceans = builder
					.comment("If this is set to true, the mod will use the tag for ocean biomes to blacklist them (default: true)")
					.define("blacklistOceans", true);

			biomeBlacklist = builder
					.comment("Biomes in this list will be blacklisted from having players spawn in them. By default the mod uses a tag for ocean biomes.")
					.defineListAllowEmpty("biomeBlacklist", ArrayList::new, String::new, o -> (o instanceof String));

			builder.pop();
		}
	}

	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
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
