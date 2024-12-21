package de.dafuqs.chalk.common;

import de.dafuqs.chalk.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

public class Chalk implements ModInitializer {
	
	public static final String MOD_ID = "chalk";
	
	public static ChalkConfig CONFIG;
	
	@Override
	public void onInitialize() {
		ChalkRegistry.init();
		
		AutoConfig.register(ChalkConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(ChalkConfig.class).getConfig();
	}
	
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}
	
}