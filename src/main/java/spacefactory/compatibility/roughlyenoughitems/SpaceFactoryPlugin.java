package spacefactory.compatibility.roughlyenoughitems;

import com.google.common.collect.ImmutableList;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import spacefactory.SpaceFactory;
import spacefactory.recipe.*;
import spacefactory.screen.client.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpaceFactoryPlugin implements REIClientPlugin {
    public static final Identifier WIDGETS = SpaceFactory.id("textures/gui/compatibility/rei_widgets.png");

    public static final CategoryIdentifier<PulverizingDisplay> PULVERIZER = CategoryIdentifier.of("spacefactory:pulverizing");
    public static final CategoryIdentifier<CompressingDisplay> COMPRESSING = CategoryIdentifier.of("spacefactory:compressing");
    public static final CategoryIdentifier<ExtractingDisplay> EXTRACTING = CategoryIdentifier.of("spacefactory:extracting");
    public static final CategoryIdentifier<MolecularAssemblyDisplay> MOLECULAR_ASSEMBLY = CategoryIdentifier.of("spacefactory:molecular_assembly");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SimpleMachineCategory(PULVERIZER, EntryStacks.of(SpaceFactory.SFBlocks.PULVERIZER), "category.spacefactory.pulverizing"));
        registry.add(new SimpleMachineCategory(COMPRESSING, EntryStacks.of(SpaceFactory.SFBlocks.COMPRESSOR), "category.spacefactory.compressing"));
        registry.add(new SimpleMachineCategory(EXTRACTING, EntryStacks.of(SpaceFactory.SFBlocks.EXTRACTOR), "category.spacefactory.extracting"));
        registry.add(new MolecularAssemblyCategory(MOLECULAR_ASSEMBLY, EntryStacks.of(SpaceFactory.SFBlocks.MOLECULAR_ASSEMBLER), "category.spacefactory.molecular_assembly"));

        registry.addWorkstations(BuiltinPlugin.SMELTING, EntryStacks.of(SpaceFactory.SFBlocks.ELECTRIC_FURNACE));
        registry.addWorkstations(PULVERIZER, EntryStacks.of(SpaceFactory.SFBlocks.PULVERIZER));
        registry.addWorkstations(COMPRESSING, EntryStacks.of(SpaceFactory.SFBlocks.COMPRESSOR));
        registry.addWorkstations(EXTRACTING, EntryStacks.of(SpaceFactory.SFBlocks.EXTRACTOR));
        registry.addWorkstations(MOLECULAR_ASSEMBLY, EntryStacks.of(SpaceFactory.SFBlocks.MOLECULAR_ASSEMBLER));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(PulverizerRecipe.class, SpaceFactory.SFRecipeTypes.PULVERIZING, PulverizingDisplay::new);
        registry.registerRecipeFiller(CompressorRecipe.class, SpaceFactory.SFRecipeTypes.COMPRESSING, CompressingDisplay::new);
        registry.registerRecipeFiller(ExtractorRecipe.class, SpaceFactory.SFRecipeTypes.EXTRACTING, ExtractingDisplay::new);
        registry.registerRecipeFiller(MolecularAssemblerRecipe.class, SpaceFactory.SFRecipeTypes.MOLECULAR_ASSEMBLY, MolecularAssemblyDisplay::new);

        registry.registerGlobalDisplayGenerator(new DynamicDisplayGenerator<DefaultInformationDisplay>() {
            @Override
            public Optional<List<DefaultInformationDisplay>> getUsageFor(EntryStack<?> entry) {
                return Optional.ofNullable(entry) //
                        .filter(e -> e.getIdentifier() != null && e.getIdentifier().getNamespace().equals(SpaceFactory.Constants.MOD_ID)) //
                        .map(e -> e.getValue() instanceof ItemStack stack ? stack.getItem().getTranslationKey() + ".usage" : null) //
                        .filter(I18n::hasTranslation) //
                        .map(key -> {
                            DefaultInformationDisplay display = DefaultInformationDisplay.createFromEntry(entry, entry.asFormatStrippedText());
                            if (entry.getValue() instanceof ItemStack stack)
                                if (stack.getItem() == SpaceFactory.SFBlocks.GENERATOR.asItem()) {
                                    display.line(translateWithNewLines("block.spacefactory.generator.usage", SpaceFactory.Constants.GENERATOR_OUTPUT, SpaceFactory.Constants.GENERATOR_CONSUMPTION * 100));
                                } else if (stack.getItem() == SpaceFactory.SFBlocks.SOLAR_PANEL.asItem()) {
                                    display.line(translateWithNewLines("block.spacefactory.solar_panel.usage", SpaceFactory.Constants.SOLAR_PANEL_OUTPUT));
                                } else if (stack.getItem() == SpaceFactory.SFBlocks.DRAGON_EGG_SIPHON.asItem()) {
                                    display.line(translateWithNewLines("block.spacefactory.dragon_egg_siphon.usage", SpaceFactory.Constants.DRAGON_EGG_SYPHON_OUTPUT));
                                } else if (stack.getItem() == SpaceFactory.SFItems.VANOVOLTAIC_CELL) {
                                    display.line(translateWithNewLines("item.spacefactory.vanovoltaic_cell.usage", SpaceFactory.Constants.VANOVOLTAIC_CELL_GENERATION));
                                } else {
                                    display.line(translateWithNewLines(key));
                                }
                            return display;
                        })
                        .map(ImmutableList::of);
            }

            @Override
            public Optional<List<DefaultInformationDisplay>> getRecipeFor(EntryStack<?> entry) {
                return Optional.ofNullable(entry.getIdentifier()) //
                        .filter(id -> id.getNamespace().equals(SpaceFactory.Constants.MOD_ID)) //
                        .map(id -> Util.createTranslationKey("item",
                                new Identifier(id.getNamespace(), id.getPath() + ".recipe"))) //
                        .filter(I18n::hasTranslation) //
                        .map(info -> DefaultInformationDisplay.createFromEntry(entry, entry.asFormatStrippedText())
                                .line(translateWithNewLines(info)))
                        .map(ImmutableList::of);
            }
        });
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(new Rectangle(78, 31, 28, 23), ElectricFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
        registry.registerContainerClickArea(new Rectangle(78, 31, 28, 23), PulverizerScreen.class, PULVERIZER);
        registry.registerContainerClickArea(new Rectangle(78, 31, 28, 23), CompressorScreen.class, COMPRESSING);
        registry.registerContainerClickArea(new Rectangle(78, 31, 28, 23), ExtractorScreen.class, EXTRACTING);
        registry.registerContainerClickArea(new Rectangle(78, 31, 28, 23), MolecularAssemblerScreen.class, MOLECULAR_ASSEMBLY);
    }

    public static List<EntryIngredient> toIngredientEntries(Ingredient ingredient, int count) {
        return ImmutableList.of(EntryIngredient.of(
                Arrays.stream(ingredient.getMatchingStacks())
                        .peek(stack -> stack.setCount(count))
                        .map(EntryStacks::of)
                        .toList()
        ));
    }

    public static List<EntryIngredient> toIngredientEntries(IngredientCount... inputs) {
        return Arrays.stream(inputs)
                .map(input -> EntryIngredient.of(
                        Arrays.stream(input.ingredient.getMatchingStacks())
                                .peek(stack -> stack.setCount(input.count))
                                .map(EntryStacks::of)
                                .toList()
                ))
                .toList();
    }

    private static Text translateWithNewLines(String translationKey, Object... args) {
        return new LiteralText(I18n.translate(translationKey, args).replace("\\n", "\n"));
    }
}
