package de.dafuqs.chalk.common;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.DyeTintSource;
import net.fabricmc.api.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.List;

public class Chalk implements ModInitializer {
	
	public static final String MOD_ID = "chalk";

	@Override
	public void onInitialize() {
		ResourcePackExtras.forDefault().addBridgedModelsFolder(id("block"), (id, b) -> {
			return new ItemAsset(new BasicItemModel(id, List.of(new DyeTintSource(-1))));
		});
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		ChalkRegistry.init();
	}
	
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}
	
}