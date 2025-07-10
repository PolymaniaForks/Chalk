package de.dafuqs.chalk.common;

import de.dafuqs.chalk.common.blocks.*;
import de.dafuqs.chalk.common.items.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.fabricmc.loader.api.*;
import net.minecraft.block.*;
import net.minecraft.block.piston.*;
import net.minecraft.client.render.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.sound.*;
import net.minecraft.util.*;

import java.util.*;

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
	
	public static Map<DyeColor, ChalkRegistry.ChalkVariant> chalkVariants = new HashMap<>();

	public static void init() {
		boolean addonLoaded = FabricLoader.getInstance().isModLoaded("chalk-colorful-addon");
		
		/*
		 * colored chalk variants are only added if the colorful addon is installed
		 * this allows chalk to use the "chalk" mod to use the chalk namespace for all functionality
		 * while still having it configurable / backwards compatible
		 */
		for (Map.Entry<DyeColor, Integer> entry : dyeColors.entrySet()) {
			DyeColor dyeColor = entry.getKey();
			int color = entry.getValue();
			
			if (dyeColor.equals(DyeColor.WHITE) || addonLoaded) {
				new ChalkRegistry.ChalkVariant(dyeColor, color);
			}
		}
		
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(fabricItemGroupEntries -> {
			for(ChalkVariant chalkVariant1 : chalkVariants.values()) {
				fabricItemGroupEntries.add(chalkVariant1.chalkItem);
				fabricItemGroupEntries.add(chalkVariant1.glowChalkItem);
			}
		});
		
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
			
			Identifier itemId = Chalk.id(colorString + "_chalk");
			Identifier blockId = Chalk.id(colorString + "_chalk_mark");
			
			Identifier glowItemId = Chalk.id(colorString + "_glow_chalk");
			Identifier glowBlockId = Chalk.id(colorString + "_glow_chalk_mark");
			
			RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);
			RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockId);
			
			RegistryKey<Item> glowItemKey = RegistryKey.of(RegistryKeys.ITEM, glowItemId);
			RegistryKey<Block> glowBlockKey = RegistryKey.of(RegistryKeys.BLOCK, glowBlockId);
			
			this.chalkItem = new ChalkItem(new Item.Settings().registryKey(itemKey).maxCount(1).maxDamage(64), dyeColor);
			this.chalkBlock = new ChalkMarkBlock(AbstractBlock.Settings.create().registryKey(blockKey).replaceable().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.DESTROY), dyeColor);
			this.glowChalkItem = new GlowChalkItem(new Item.Settings().registryKey(glowItemKey).maxCount(1).maxDamage(64), dyeColor);
			this.glowChalkBlock = new GlowChalkMarkBlock(AbstractBlock.Settings.create().registryKey(glowBlockKey).replaceable().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.DESTROY), dyeColor);
			
			Registry.register(Registries.ITEM, itemId, chalkItem);
			Registry.register(Registries.BLOCK, blockId, chalkBlock);
			Registry.register(Registries.ITEM, glowItemId, glowChalkItem);
			Registry.register(Registries.BLOCK, glowBlockId, glowChalkBlock);
			
			chalkVariants.put(dyeColor, this);
		}

		public void registerClient() {
			BlockRenderLayerMap.putBlock(this.chalkBlock, BlockRenderLayer.CUTOUT);
			BlockRenderLayerMap.putBlock(this.glowChalkBlock, BlockRenderLayer.CUTOUT);
			ColorProviderRegistry.BLOCK.register((state, world, pos, index) -> color, chalkBlock);
			ColorProviderRegistry.BLOCK.register((state, world, pos, index) -> color, glowChalkBlock);
		}
	}
}