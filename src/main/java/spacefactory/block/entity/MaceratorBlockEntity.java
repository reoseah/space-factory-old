package spacefactory.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import spacefactory.SpaceFactory;
import spacefactory.recipe.MaceratorRecipe;
import spacefactory.screen.MaceratorScreenHandler;

public class MaceratorBlockEntity extends CraftingMachineBlockEntity<MaceratorRecipe> {
    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(SpaceFactory.BlockEntityTypes.MACERATOR, pos, state);
    }

    @Override
    protected RecipeType<MaceratorRecipe> getRecipeType() {
        return SpaceFactory.RecipeTypes.MACERATING;
    }

    @Override
    protected int getEnergyPerTick() {
        return SpaceFactory.config.maceratorConsumption;
    }

    @Override
    protected int getRecipeDuration(MaceratorRecipe recipe) {
        return recipe.getDuration();
    }

    @Override
    protected boolean canAcceptRecipeOutput(@Nullable MaceratorRecipe recipe) {
        if (recipe == null || this.inventory.get(0).isEmpty()) {
            return false;
        }
        return this.canAccept(2, recipe.getOutput());
    }

    @Override
    protected void craftRecipe(@Nullable MaceratorRecipe recipe) {
        if (this.canAcceptRecipeOutput(recipe)) {
            assert recipe != null;

            this.inventory.get(0).decrement(recipe.getIngredientCount());

            ItemStack slot = this.inventory.get(2);
            ItemStack output = recipe.getOutput();
            if (slot.isEmpty()) {
                this.inventory.set(2, output.copy());
            } else if (slot.getItem() == output.getItem()) {
                slot.increment(output.getCount());
            }
        }
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new MaceratorScreenHandler(syncId, this, player);
    }
}
