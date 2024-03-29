package spacefactory.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import spacefactory.SpaceFactory;
import spacefactory.api.EU;
import spacefactory.recipe.AtomicConstructionRecipe;
import spacefactory.screen.AtomicConstructorScreenHandler;

public class AtomicConstructorBlockEntity extends CraftingMachineBlockEntity<AtomicConstructionRecipe> {
    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_OUTPUT = 3;

    private static final int[] TOP_SLOTS = {SLOT_INPUT_1, SLOT_INPUT_2};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT, SLOT_BATTERY};
    private static final int[] SIDE_SLOTS = {SLOT_BATTERY};

    public AtomicConstructorBlockEntity(BlockPos pos, BlockState state) {
        super(SpaceFactory.BlockEntityTypes.ATOMIC_CONSTRUCTOR, pos, state);
    }

    @Override
    protected int getInventorySize() {
        return 4;
    }

    @Override
    protected RecipeType<AtomicConstructionRecipe> getRecipeType() {
        return SpaceFactory.RecipeTypes.ATOMIC_CONSTRUCTION;
    }

    @Override
    protected int getEnergyPerTick() {
        return SpaceFactory.config.atomicReconstructorConsumption;
    }

    @Override
    protected int getRecipeDuration(AtomicConstructionRecipe recipe) {
        return recipe.getDuration();
    }

    protected boolean canAcceptRecipeOutput(@Nullable AtomicConstructionRecipe recipe) {
        if (recipe == null
                || this.inventory.get(SLOT_INPUT_1).isEmpty()
                && this.inventory.get(SLOT_INPUT_2).isEmpty()) {
            return false;
        }
        return this.canAccept(SLOT_OUTPUT, recipe.getOutput());
    }

    protected void craftRecipe(@Nullable AtomicConstructionRecipe recipe) {
        if (this.canAcceptRecipeOutput(recipe)) {
            assert recipe != null;

            ItemStack input1 = this.inventory.get(SLOT_INPUT_1);
            ItemStack input2 = this.inventory.get(SLOT_INPUT_2);

            ItemStack slot = this.inventory.get(SLOT_OUTPUT);
            ItemStack output = recipe.getOutput();

            if (recipe.input1.test(input1) && recipe.input2.test(input2)) {
                this.consume(SLOT_INPUT_1, recipe.input1);
                this.consume(SLOT_INPUT_2, recipe.input2);
            } else {
                this.consume(SLOT_INPUT_1, recipe.input2);
                this.consume(SLOT_INPUT_2, recipe.input1);
            }

            if (slot.isEmpty()) {
                this.inventory.set(SLOT_OUTPUT, output.copy());
            } else if (slot.getItem() == output.getItem()) {
                slot.increment(output.getCount());
            }
        }
    }


    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AtomicConstructorScreenHandler(syncId, this, player);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack previous = this.inventory.get(slot);
        boolean needsRecipeUpdate = stack.isEmpty() || !stack.isItemEqualIgnoreDamage(previous) || !ItemStack.areNbtEqual(stack, previous);

        this.inventory.set(slot, stack);

        if (needsRecipeUpdate && (slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2)) {
            this.resetCachedRecipe();
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack offer) {
        return switch (slot) {
            case SLOT_OUTPUT -> false;
            case SLOT_BATTERY -> EU.isElectricItem(offer);
            default -> true;
        };
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case UP -> TOP_SLOTS;
            case DOWN -> BOTTOM_SLOTS;
            default -> SIDE_SLOTS;
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return this.isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        if (side == Direction.DOWN && slot == SLOT_BATTERY) {
            return !EU.isElectricItem(stack);
        }
        return true;
    }
}
