package de.dafuqs.chalk.common.items;

import de.dafuqs.chalk.common.*;
import de.dafuqs.chalk.common.blocks.ChalkMarkBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

public class ChalkItem extends Item implements PolymerItem {
	protected DyeColor dyeColor;

	public ChalkItem(Settings settings, DyeColor dyeColor) {
		super(settings);
		this.dyeColor = dyeColor;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		final World world = context.getWorld();
		final BlockPos pos = context.getBlockPos();
		final BlockState clickedBlockState = world.getBlockState(pos);
		final PlayerEntity player = context.getPlayer();
		final ItemStack stack = context.getStack();
		Direction clickedFace = context.getSide();
		BlockPos markPosition = clickedBlockState.isReplaceable() ?  pos: pos.offset(clickedFace);
		if (world.isAir(markPosition) || world.getBlockState(markPosition).getBlock() instanceof ChalkMarkBlock) {
			if (clickedBlockState.getBlock() instanceof ChalkMarkBlock) { // replace mark
				clickedFace = clickedBlockState.get(ChalkMarkBlock.FACING);
				markPosition = pos;
				world.removeBlock(pos, false);
			} else if (player != null &&
					!Block.isFaceFullSquare(clickedBlockState.getCollisionShape(world, pos, ShapeContext.of(player)), clickedFace)) {
				return ActionResult.PASS;
			} else if ((!world.isAir(markPosition) && world.getBlockState(markPosition).getBlock() instanceof ChalkMarkBlock) || stack.getItem() != this) {
				return ActionResult.PASS;
			}

			if (world instanceof ServerWorld serverWorld) {
				Random random = world.getRandom();
					serverWorld.spawnParticles(new DustParticleEffect(this.dyeColor.getFireworkColor(), 0.5f), pos.getX() + (0.5 * (random.nextFloat() + 0.15)), pos.getY() + 0.3, pos.getZ() + (0.5 * (random.nextFloat() + 0.15)), 0, 0.0D, 0.0D, 0.0D, 0);
			}

			final int orientation = getClickedRegion(context.getHitPos(), clickedFace);

			BlockState blockState = getChalkMarkBlock().getDefaultState()
					.with(ChalkMarkBlock.FACING, clickedFace)
					.with(ChalkMarkBlock.ORIENTATION, orientation);

			if (world.setBlockState(markPosition, blockState, 1 | 2)) {
				if (player != null &&
						!player.isCreative()) {
					if (stack.getDamage() >= stack.getMaxDamage()) {
						world.playSound(null, markPosition, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.BLOCKS, 0.5f, 1f);
					}
					stack.damage(1, player, LivingEntity.getSlotForHand(context.getHand()));
				}
				world.playSound(null, markPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 0.6f, world.random.nextFloat() * 0.2f + 0.8f);
				return ActionResult.SUCCESS_SERVER;
			}
		}
		return ActionResult.FAIL;
	}

	public Block getChalkMarkBlock() {
		return ChalkRegistry.chalkVariants.get(this.dyeColor).chalkBlock;
	}

	/**
	 * Calculate the fractional part of v
	 * @return Fractional part of v (always non-negative and less than 1)
	 */
	private static double frac(double v) {
		return v - Math.floor(v);
	}

	/**
	 * Calculates which region of the block was clicked
	 * @param rx [0, 1, 2] = [left, center, right]
	 * @param ry [0, 1, 2] = [top, center, bottom]
	 * @return region number (top-left = 0 … bottom-right = 8)
	 */
	private static int blockreg(int rx, int ry) {
		return 3 * rx + ry;
	}

	private int getClickedRegion(@NotNull Vec3d clickLocation, @NotNull Direction face) {
		final double dx = frac(clickLocation.x);
		final double dy = frac(clickLocation.y);
		final double dz = frac(clickLocation.z);

		return switch (face) {
			case NORTH, SOUTH -> blockreg(Math.min(2, (int) (3 * (1 - dy))), Math.min(2, (int) (3 * dx)));
			case WEST, EAST -> blockreg(Math.min(2, (int) (3 * (1 - dy))), Math.min(2, (int) (3 * dz)));
			default -> blockreg(Math.min(2, (int) (3 * dz)), Math.min(2, (int) (3 * dx)));
		};
	}

	@Override
	public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
		return Items.TRIAL_KEY;
	}
}