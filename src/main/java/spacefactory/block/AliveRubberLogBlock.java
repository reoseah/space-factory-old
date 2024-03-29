package spacefactory.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import spacefactory.SpaceFactory;

import java.util.Locale;
import java.util.Random;

public class AliveRubberLogBlock extends Block {
    public static final EnumProperty<State> STATE = EnumProperty.of("state", State.class);

    public AliveRubberLogBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(STATE, State.NATURAL));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(state.get(STATE) == State.NATURAL ? SpaceFactory.Blocks.RUBBER_LOG : SpaceFactory.Blocks.STRIPPED_RUBBER_LOG);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(STATE) == State.TAPPED) {
            boolean hasLeaves = false, hasSoil = false;
            for (int y = 1; y < 8; y++) {
                BlockState s = world.getBlockState(pos.up(y));
                if (s.isOf(SpaceFactory.Blocks.RUBBER_LEAVES)) {
                    hasLeaves = true;
                    break;
                } else if (!s.isOf(this) || s.isOf(this) && s.get(STATE) != State.NATURAL) {
                    break;
                }
            }
            for (int y = 1; y < 8; y++) {
                BlockState s = world.getBlockState(pos.down(y));
                if (((RubberSaplingBlock) SpaceFactory.Blocks.RUBBER_SAPLING).canPlantOnTop(s, world, pos.down(y))) {
                    hasSoil = true;
                    break;
                } else if (!s.isOf(this) || s.isOf(this) && s.get(STATE) != State.NATURAL) {
                    break;
                }
            }
            if (!hasLeaves || !hasSoil) {
                world.setBlockState(pos, SpaceFactory.Blocks.STRIPPED_RUBBER_LOG.getDefaultState());
                return;
            }
            if (random.nextInt(8) == 0) {
                world.setBlockState(pos, state.with(STATE, State.READY));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (hitResult.getSide().getAxis().isHorizontal() && world.canPlayerModifyAt(player, pos)) {
            if (state.get(STATE) == State.READY) {
                if (!world.isClient()) {
                    ItemStack stack = new ItemStack(SpaceFactory.Items.RAW_RUBBER, 1 + world.getRandom().nextInt(3));
                    Direction side = hitResult.getSide();

                    double x = pos.getX() + 0.5 + 0.70 * side.getOffsetX();
                    double y = pos.getY() + 0.25 + 0.70 * side.getOffsetY();
                    double z = pos.getZ() + 0.5 + 0.70 * side.getOffsetZ();


                    double vx = 0.05 * side.getOffsetX();
                    double vy = 0.05 * side.getOffsetY();
                    double vz = 0.05 * side.getOffsetZ();

                    ItemEntity entity = new ItemEntity(world, x, y, z, stack, vx, vy, vz);
                    world.spawnEntity(entity);
                }
                world.setBlockState(pos, state.with(STATE, State.TAPPED));
                return ActionResult.SUCCESS;
            } else if (state.get(STATE) == State.NATURAL && player.getStackInHand(hand).getItem() instanceof AxeItem) {
                world.setBlockState(pos, state.with(STATE, State.TAPPED));
                player.getStackInHand(hand).damage(1, player, p -> p.sendToolBreakStatus(hand));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    public enum State implements StringIdentifiable {
        NATURAL,
        TAPPED,
        READY;

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
