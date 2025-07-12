package de.dafuqs.chalk.common.blocks;

import de.dafuqs.chalk.common.*;
import de.dafuqs.chalk.common.poly.BlockStateModel;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PickItemFromBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PickItemFromEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.*;
import net.minecraft.state.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;
import net.minecraft.world.tick.*;
import org.jetbrains.annotations.*;
import xyz.nucleoid.packettweaker.PacketContext;

public class ChalkMarkBlock extends Block implements FactoryBlock {

	protected DyeColor dyeColor;

	public static final EnumProperty<Direction> FACING = Properties.FACING;
	public static final IntProperty ORIENTATION = IntProperty.of("orientation", 0, 8);

	// Hitbox margin: 0 … 2 (Use 1.5D to create a look identical to previous versions)
	private static final double margin = 0D;
	// Hitbox thickness: 0.001 … 2 (use 0.5D to create a look identical to previous versions)
	private static final double thick = 0.025D;
	private static final VoxelShape DOWN_AABB = Block.createCuboidShape(margin, 16D - thick, margin, 16D - margin, 16D, 16D - margin);
	private static final VoxelShape UP_AABB = Block.createCuboidShape(margin, 0D, margin, 16D - margin, thick, 16D - margin);
	private static final VoxelShape SOUTH_AABB = Block.createCuboidShape(margin, margin, 0D, 16D - margin, 16D - margin, thick);
	private static final VoxelShape EAST_AABB = Block.createCuboidShape(0D, margin, margin, thick, 16D - margin, 16D - margin);
	private static final VoxelShape WEST_AABB = Block.createCuboidShape(16D - thick, margin, margin, 16D, 16D - margin, 16D - margin);
	private static final VoxelShape NORTH_AABB = Block.createCuboidShape(margin, margin, 16D - thick, 16D - margin, 16D - margin, 16D);

	public ChalkMarkBlock(Settings settings, DyeColor dyeColor) {
		super(settings);
		this.dyeColor = dyeColor;
		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(ORIENTATION, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, ORIENTATION);
		super.appendProperties(builder);
	}

	@Override
	public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
		super.afterBreak(world, player, pos, state, blockEntity, stack);
	}
	
	@Override
	protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return ChalkRegistry.chalkVariants.get(dyeColor).chalkItem.getDefaultStack();
	}

	@Override
	protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
		Random random = world.getRandom();
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.playSound(null, pos, SoundEvents.BLOCK_WART_BLOCK_HIT, SoundCategory.BLOCKS, 0.5f, random.nextFloat() * 0.2f + 0.8f);
			serverWorld.spawnParticles(new DustParticleEffect(this.dyeColor.getFireworkColor(), 0.5f), pos.getX() + (0.5 * (random.nextFloat() + 0.15)), pos.getY() + 0.3, pos.getZ() + (0.5 * (random.nextFloat() + 0.15)), 0, 0.0D, 0.0D, 0.0D, 0);
		}
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING)) {
			case UP -> UP_AABB;
			case NORTH -> NORTH_AABB;
			case WEST -> WEST_AABB;
			case EAST -> EAST_AABB;
			case SOUTH -> SOUTH_AABB;
			default -> DOWN_AABB;
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		return true;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		Direction facing = state.get(FACING);
		return Block.isFaceFullSquare(world.getBlockState(pos.offset(facing.getOpposite())).getCollisionShape(world, pos.offset(facing)), facing);
	}
	
	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		boolean support = neighborPos.equals(pos.offset(state.get(FACING).getOpposite()));
		if (support) {
			if (!this.canPlaceAt(state, world, pos)) {
				return Blocks.AIR.getDefaultState();
			}
		}
		return state;
	}

	@Override
	public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
		return new Model(initialBlockState);
	}

	public static class Model extends BlockStateModel {
		private InteractionElement[] interactionElements;

		public Model(BlockState blockState) {
            super(blockState);
		}

		@Override
		protected void applyUpdates(BlockState blockState) {
			if (interactionElements == null) {
				interactionElements = new InteractionElement[0];
			}
			setupInterations(blockState);
		}

		protected void setupInterations(BlockState blockState) {
			var facing = blockState.get(FACING);

			if (facing.getAxis() == Direction.Axis.Y) {
				InteractionElement inter;
				if (interactionElements.length > 0) {
					inter = interactionElements[0];
					for (var i = 1; i < interactionElements.length; i++) {
						this.removeElement(interactionElements[i]);
					}
				} else {
					inter = new InteractionElement();
					inter.setInteractionHandler(createInteraction(inter));
					inter.setInvisible(true);
				}
				inter.setSize(1F, (float) thick * facing.getDirection().offset());
				inter.setOffset(new Vec3d(0, -0.5 * facing.getDirection().offset(), 0));

				this.addElement(inter);
				interactionElements = new InteractionElement[] { inter };
			} else {
				InteractionElement[] inter = new InteractionElement[(int) (1 / thick / 2)];
				int i = 0;
				for (; i < interactionElements.length; i++) {
					inter[i] = interactionElements[i];
				}
				for (; i < inter.length; i++) {
					inter[i] = new InteractionElement();
					inter[i].setInteractionHandler(createInteraction(inter[i]));
					//inter[i].setInvisible(true);
				}
				i = 0;
				for (; i < inter.length; i++) {
					inter[i].setSize((float) thick * 2, 1F);
					inter[i].setOffset(new Vec3d(
							-facing.getOffsetX() * 0.5 + facing.getAxis().choose(0, 0, i * thick * 2 - 0.5 + thick),
							-0.5f,
							-facing.getOffsetZ() * 0.5 + facing.getAxis().choose(i * thick * 2 - 0.5 + thick, 0, 0)
					));


					this.addElement(inter[i]);
				}

				interactionElements = inter;
			}
		}

		private VirtualElement.InteractionHandler createInteraction(InteractionElement inter) {
			return new VirtualElement.InteractionHandler() {
				@Override
				public void interact(ServerPlayerEntity player, Hand hand) {}

				@Override
				public void interactAt(ServerPlayerEntity player, Hand hand, Vec3d pos) {
					new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(Vec3d.ofCenter(blockPos()).add(inter.getOffset()).add(pos), blockState().get(FACING), blockPos(), false), 0).apply(player.networkHandler);
				}

				@Override
				public void attack(ServerPlayerEntity player) {
					new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos(), player.getFacing()).apply(player.networkHandler);
				}

				//@Override
				public void pickItem(ServerPlayerEntity player, boolean includeData) {
					new PickItemFromBlockC2SPacket(blockPos(), includeData).apply(player.networkHandler);
				}
			};
		}
	}
}