package spacefactory.compatibility.rei;

import spacefactory.recipe.MaceratorRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class PulverizingDisplay extends SimpleMachineDisplay {
    public PulverizingDisplay(MaceratorRecipe recipe) {
        super(recipe);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SpaceFactoryPlugin.macerator;
    }
}
