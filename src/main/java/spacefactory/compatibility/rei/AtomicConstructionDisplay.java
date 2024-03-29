package spacefactory.compatibility.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import spacefactory.recipe.AtomicConstructionRecipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class AtomicConstructionDisplay extends BasicDisplay {
    private final int duration;

    public AtomicConstructionDisplay(AtomicConstructionRecipe recipe) {
        super(EntryIngredients.ofIngredients(Arrays.asList(recipe.input1, recipe.input2)),
                Collections.singletonList(EntryIngredients.of(recipe.getOutput())),
                Optional.of(recipe.getId()));
        this.duration = recipe.getDuration();
    }

    public int getDuration() {
        return this.duration;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SpaceFactoryPlugin.ATOMIC_CONSTRUCTION;
    }
}
