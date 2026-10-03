package org.tovasha.ych;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tovasha.ych.config.MainConfig;

public class YourCustomHud implements ClientModInitializer {
	public static final String MOD_ID = "ych";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static MainConfig CONFIG;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(MainConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(MainConfig.class).getConfig();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
