package spacefactory.features.generator;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import spacefactory.SpaceFactory;
import spacefactory.api.EU;
import spacefactory.api.EnergyTier;
import spacefactory.core.block.entity.ElectricInventoryBlockEntity;

public class GeneratorBlockEntity extends ElectricInventoryBlockEntity implements SidedInventory, EU.Sender {
	private static final int[] TOP_SLOTS = {1};
	private static final int[] BOTTOM_SLOTS = {0};
	private static final int[] SIDE_SLOTS = {0};

	protected int fuelLeft, fuelDuration;

	public GeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(SpaceFactory.BlockEntityTypes.GENERATOR, pos, state);
	}

	@Override
	protected int getInventorySize() {
		return 2;
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new GeneratorScreenHandler(syncId, this, player);
	}

	@Override
	protected EnergyTier getEnergyTier() {
		return EnergyTier.LOW;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.fuelLeft = nbt.getShort("FuelLeft");
		this.fuelDuration = nbt.getShort("FuelDuration");
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putShort("FuelLeft", (short) this.fuelLeft);
		nbt.putShort("FuelDuration", (short) this.fuelDuration);
	}

	public int getFuelLeft() {
		return this.fuelLeft;
	}

	public int getFuelDuration() {
		return this.fuelDuration;
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return switch (side) {
			case DOWN -> BOTTOM_SLOTS;
			case UP -> TOP_SLOTS;
			default -> SIDE_SLOTS;
		};
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return slot != 1 || EU.isElectricItem(stack);
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return switch (slot) {
			case 0 -> AbstractFurnaceBlockEntity.canUseAsFuel(stack);
			case 1 -> EU.isElectricItem(stack);
			default -> true;
		};
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot != 0 || !AbstractFurnaceBlockEntity.canUseAsFuel(stack);
	}

	public static void tick(World world, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
		ElectricInventoryBlockEntity.tick(world, pos, state, be);

		boolean wasBurning = be.fuelLeft > 0;
		boolean markDirty = false;

		if (be.energy > 0) {
			int energy = be.getEnergyTier().transferRate;
			// TODO
//			energy -= EnergyStorageUtil.move(be.energy, be.getItemApi(1, EnergyStorage.ITEM), Integer.MAX_VALUE, null);
			for (Direction side : Direction.values()) {
				if (!be.canSend(side)) {
					continue;
				}
				energy -= EU.send(be.energy, world, pos.offset(side), side.getOpposite());
				if (energy == 0) {
					break;
				}
			}
		}

		if (be.fuelLeft <= 0 && be.energy < SpaceFactory.Constants.GENERATOR_OUTPUT) {
			// TODO only consume fuel if there's a consumer...
			ItemStack fuel = be.inventory.get(0);
			int value = AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuel.getItem(), 0) / SpaceFactory.Constants.GENERATOR_CONSUMPTION;
			if (value > 0) {
				fuel.decrement(1);
				be.fuelDuration = be.fuelLeft = value;
				markDirty = true;
			} else {
				be.fuelDuration = 0;
				markDirty = true;
			}
		}
		if (be.fuelLeft > 0) {
			be.fuelLeft--;
			if (be.energy < SpaceFactory.Constants.GENERATOR_OUTPUT) {
				be.energy = Math.min(be.energy + SpaceFactory.Constants.GENERATOR_OUTPUT, SpaceFactory.Constants.GENERATOR_OUTPUT);
			}
			markDirty = true;
		}
		boolean isBurning = be.fuelLeft > 0;

		if (isBurning != wasBurning) {
			world.setBlockState(pos, be.getCachedState().with(Properties.LIT, isBurning));
			markDirty = true;
		}
		if (markDirty) {
			be.markDirty();
		}
	}

}