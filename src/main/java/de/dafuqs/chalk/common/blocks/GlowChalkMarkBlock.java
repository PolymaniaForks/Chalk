package de.dafuqs.chalk.common.blocks;

import de.dafuqs.chalk.common.ChalkRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class GlowChalkMarkBlock extends ChalkMarkBlock {

	public GlowChalkMarkBlock(Properties settings, DyeColor dyeColor) {
		super(settings, dyeColor);
	}
	
	@Override
	protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return ChalkRegistry.chalkVariants.get(dyeColor).glowChalkItem.getDefaultInstance();
	}
	
}
