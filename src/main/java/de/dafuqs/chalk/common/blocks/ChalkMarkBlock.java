package de.dafuqs.chalk.common.blocks;

import de.dafuqs.chalk.common.ChalkRegistry;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModel;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static de.dafuqs.chalk.common.ChalkRegistry.dyeColors;

public class ChalkMarkBlock extends Block implements FactoryBlock {

    protected DyeColor dyeColor;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final IntegerProperty ORIENTATION = IntegerProperty.create("orientation", 0, 8);

    // Hitbox margin: 0 … 2 (Use 1.5D to create a look identical to previous versions)
    private static final double margin = 0D;
    // Hitbox thickness: 0.001 … 2 (use 0.5D to create a look identical to previous versions)
    private static final double thick = 0.025D;
    private static final VoxelShape DOWN_AABB = Block.box(margin, 16D - thick, margin, 16D - margin, 16D, 16D - margin);
    private static final VoxelShape UP_AABB = Block.box(margin, 0D, margin, 16D - margin, thick, 16D - margin);
    private static final VoxelShape SOUTH_AABB = Block.box(margin, margin, 0D, 16D - margin, 16D - margin, thick);
    private static final VoxelShape EAST_AABB = Block.box(0D, margin, margin, thick, 16D - margin, 16D - margin);
    private static final VoxelShape WEST_AABB = Block.box(16D - thick, margin, margin, 16D, 16D - margin, 16D - margin);
    private static final VoxelShape NORTH_AABB = Block.box(margin, margin, 16D - thick, 16D - margin, 16D - margin, 16D);

    public ChalkMarkBlock(Properties settings, DyeColor dyeColor) {
        super(settings);
        this.dyeColor = dyeColor;
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ORIENTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ORIENTATION);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return ChalkRegistry.chalkVariants.get(dyeColor).chalkItem.getDefaultInstance();
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
        RandomSource random = world.getRandom();
        if (world instanceof ServerLevel serverWorld) {
            serverWorld.playSound(null, pos, SoundEvents.WART_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, random.nextFloat() * 0.2f + 0.8f);
            serverWorld.sendParticles(new DustParticleOptions(this.dyeColor.getFireworkColor(), 0.5f), pos.getX() + (0.5 * (random.nextFloat() + 0.15)), pos.getY() + 0.3, pos.getZ() + (0.5 * (random.nextFloat() + 0.15)), 0, 0.0D, 0.0D, 0.0D, 0);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> UP_AABB;
            case NORTH -> NORTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> DOWN_AABB;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return Block.isFaceFull(world.getBlockState(pos.relative(facing.getOpposite())).getCollisionShape(world, pos.relative(facing)), facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        boolean support = neighborPos.equals(pos.relative(state.getValue(FACING).getOpposite()));
        if (support) {
            if (!this.canSurvive(state, world, pos)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return state;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, pos);
    }

    public class Model extends BlockStateModel {
        private InteractionElement[] interactionElements;

        public Model(BlockState blockState, BlockPos pos) {
            super(blockState, pos, 1);
            this.applyUpdates(blockState, pos);
        }

        @Override
        protected void applyUpdates(BlockState blockState, BlockPos pos) {
            for (var element : this.getElements()) {
                if (element instanceof ItemDisplayElement itemDisplayElement) {
                    var stack = itemDisplayElement.getItem().copy();
                    stack.set(DataComponents.MAP_COLOR, new MapItemColor(dyeColors.get(dyeColor)));
                    itemDisplayElement.setItem(stack);
                }
            }

            if (interactionElements == null) {
                interactionElements = new InteractionElement[0];
            }
            setupInterations(blockState);
        }

        protected void setupInterations(BlockState blockState) {
            var facing = blockState.getValue(FACING);

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
                inter.setSize(1F, (float) thick * facing.getAxisDirection().getStep());
                inter.setOffset(new Vec3(0, -0.5 * facing.getAxisDirection().getStep(), 0));

                this.addElement(inter);
                interactionElements = new InteractionElement[]{inter};
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
                    inter[i].setOffset(new Vec3(
                            -facing.getStepX() * 0.5 + facing.getAxis().choose(0, 0, i * thick * 2 - 0.5 + thick),
                            -0.5f,
                            -facing.getStepZ() * 0.5 + facing.getAxis().choose(i * thick * 2 - 0.5 + thick, 0, 0)
                    ));


                    this.addElement(inter[i]);
                }

                interactionElements = inter;
            }
        }

        private VirtualElement.InteractionHandler createInteraction(InteractionElement inter) {
            return new VirtualElement.InteractionHandler() {
                @Override
                public void interact(ServerPlayer player, InteractionHand hand, Vec3 pos, boolean usingSecondaryAction) {
                    new ServerboundUseItemOnPacket(hand, new BlockHitResult(Vec3.atCenterOf(blockPos()).add(inter.getOffset()).add(pos), blockState().getValue(FACING), blockPos(), false), 0).handle(player.connection);
                }

                @Override
                public void attack(ServerPlayer player) {
                    new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos(), player.getNearestViewDirection()).handle(player.connection);
                }

                //@Override
                public void pickItem(ServerPlayer player, boolean includeData) {
                    new ServerboundPickItemFromBlockPacket(blockPos(), includeData).handle(player.connection);
                }
            };
        }
    }
}