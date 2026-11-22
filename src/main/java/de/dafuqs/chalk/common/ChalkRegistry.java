package de.dafuqs.chalk.common;

import de.dafuqs.chalk.common.blocks.*;
import de.dafuqs.chalk.common.items.*;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.loader.api.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import java.util.*;

import static de.dafuqs.chalk.common.Chalk.id;

public class ChalkRegistry {
	
	// We use this map instead of the DyeColor enum
	// in case a mod extends the DyeColor enum and stuff inevitably breaks
	public static Map<DyeColor, Integer> dyeColors = new TreeMap<>() {{
		put(DyeColor.WHITE, 0xFFFFFF);
		put(DyeColor.ORANGE, 0xe16201);
		put(DyeColor.MAGENTA, 0xaa32a0);
		put(DyeColor.LIGHT_BLUE, 0x258ac8);
		put(DyeColor.YELLOW, 0xf0ff15);
		put(DyeColor.LIME, 0x5faa19);
		put(DyeColor.PINK, 0xd6658f);
		put(DyeColor.GRAY, 0x292929);
		put(DyeColor.LIGHT_GRAY, 0x8b8b8b);
		put(DyeColor.CYAN, 0x157687);
		put(DyeColor.PURPLE, 0x641f9c);
		put(DyeColor.BLUE, 0x2c2e8e);
		put(DyeColor.BROWN, 0x613c20);
		put(DyeColor.GREEN, 0x495b24);
		put(DyeColor.RED, 0x8f2121);
		put(DyeColor.BLACK, 0x171717);
	}};

	public static final List<DyeColor> COLORS_CREATIVE = List.of(DyeColor.WHITE,
			DyeColor.LIGHT_GRAY,
			DyeColor.GRAY,
			DyeColor.BLACK,
			DyeColor.BROWN,
			DyeColor.RED,
			DyeColor.ORANGE,
			DyeColor.YELLOW,
			DyeColor.LIME,
			DyeColor.GREEN,
			DyeColor.CYAN,
			DyeColor.LIGHT_BLUE,
			DyeColor.BLUE,
			DyeColor.PURPLE,
			DyeColor.MAGENTA,
			DyeColor.PINK);

	public static Map<DyeColor, ChalkRegistry.ChalkVariant> chalkVariants = new HashMap<>();

	public static void init() {
		/*
		 * colored chalk variants are only added if the colorful addon is installed
		 * this allows chalk to use the "chalk" mod to use the chalk namespace for all functionality
		 * while still having it configurable / backwards compatible
		 */
		for (Map.Entry<DyeColor, Integer> entry : dyeColors.entrySet()) {
			DyeColor dyeColor = entry.getKey();
			int color = entry.getValue();
			
			new ChalkRegistry.ChalkVariant(dyeColor, color);
		}

		PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(id("chalk"), PolymerCreativeModeTabUtils.builder()
				.icon(() -> chalkVariants.get(DyeColor.WHITE).chalkItem.getDefaultInstance())
						.title(Component.translatable("item_group.chalk"))
						.displayItems(((displayContext, entries) -> {
							for (var color : COLORS_CREATIVE) {
								entries.accept(chalkVariants.get(color).chalkItem);
								entries.accept(chalkVariants.get(color).glowChalkItem);
							}
						}))
				.build());


	}
	
	public static class ChalkVariant {
		public Item chalkItem;
		public Block chalkBlock;
		public Item glowChalkItem;
		public Block glowChalkBlock;
		String colorString;
		int color;

		public ChalkVariant(DyeColor dyeColor, int color) {
			this.color = color;
			this.colorString = dyeColor.toString();
			
			Identifier itemId = id(colorString + "_chalk");
			Identifier blockId = id(colorString + "_chalk_mark");
			
			Identifier glowItemId = id(colorString + "_glow_chalk");
			Identifier glowBlockId = id(colorString + "_glow_chalk_mark");
			
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);
			ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockId);
			
			ResourceKey<Item> glowItemKey = ResourceKey.create(Registries.ITEM, glowItemId);
			ResourceKey<Block> glowBlockKey = ResourceKey.create(Registries.BLOCK, glowBlockId);
			
			this.chalkItem = new ChalkItem(new Item.Properties().setId(itemKey).stacksTo(1).durability(64), dyeColor);
			this.chalkBlock = new ChalkMarkBlock(BlockBehaviour.Properties.of().setId(blockKey).replaceable().noCollision().noOcclusion().sound(SoundType.GRAVEL).pushReaction(PushReaction.POPPED), dyeColor);
			this.glowChalkItem = new GlowChalkItem(new Item.Properties().setId(glowItemKey).stacksTo(1).durability(64), dyeColor);
			this.glowChalkBlock = new GlowChalkMarkBlock(BlockBehaviour.Properties.of().setId(glowBlockKey).replaceable().noCollision().noOcclusion().sound(SoundType.GRAVEL).pushReaction(PushReaction.POPPED), dyeColor);
			
			Registry.register(BuiltInRegistries.ITEM, itemId, chalkItem);
			Registry.register(BuiltInRegistries.BLOCK, blockId, chalkBlock);
			Registry.register(BuiltInRegistries.ITEM, glowItemId, glowChalkItem);
			Registry.register(BuiltInRegistries.BLOCK, glowBlockId, glowChalkBlock);
			
			chalkVariants.put(dyeColor, this);

			BlockStateModelManager.addBlock(blockId, this.chalkBlock);
			BlockStateModelManager.addBlock(glowBlockId, this.glowChalkBlock);
		}
	}
}