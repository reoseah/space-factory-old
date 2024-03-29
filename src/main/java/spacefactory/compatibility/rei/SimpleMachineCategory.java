package spacefactory.compatibility.rei;

import com.google.common.collect.ImmutableList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.SimpleDisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import spacefactory.compatibility.rei.widgets.EnergyCostWidget;
import spacefactory.compatibility.rei.widgets.MachineArrowWidget;

import java.util.Collections;
import java.util.List;

public class SimpleMachineCategory implements DisplayCategory<SimpleMachineDisplay> {
	private final CategoryIdentifier<? extends SimpleMachineDisplay> id;
	private final EntryStack<?> logo;
	private final String name;

	public SimpleMachineCategory(CategoryIdentifier<? extends SimpleMachineDisplay> id, EntryStack<?> logo, String name) {
		this.id = id;
		this.logo = logo;
		this.name = name;
	}

	@Override
	public Renderer getIcon() {
		return this.logo;
	}

	@Override
	public Text getTitle() {
		return new TranslatableText(this.name);
	}

	@Override
	public CategoryIdentifier<? extends SimpleMachineDisplay> getCategoryIdentifier() {
		return this.id;
	}

	public int getDisplayHeight() {
		return 49;
	}

	public DisplayRenderer getDisplayRenderer(SimpleMachineDisplay display) {
		return SimpleDisplayRenderer.from(Collections.singletonList(display.getInputEntries().get(0)), display.getOutputEntries());
	}

	public List<Widget> setupDisplay(SimpleMachineDisplay display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);

		Widget base = Widgets.createRecipeBase(bounds);
		Widget resultBackground = Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 9));
		Widget energy = new EnergyCostWidget(new Rectangle(startPoint.x + 1, startPoint.y + 20, 14, 14)).animationDurationMS(10000.0D);
		Widget arrow = new MachineArrowWidget(new Rectangle(startPoint.x + 24, startPoint.y + 8, 24, 17), this.getArrowType()).cost(this.getEuPerTick(), this.getEuTotal(display)).animationDurationTicks(display.getDuration());
		Widget output = Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 9)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput();
		Widget input = Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 1)).entries(display.getInputEntries().get(0)).markInput();

		return ImmutableList.of(base, resultBackground, energy, arrow, output, input);
	}

	private MachineArrowWidget.Type getArrowType() {
		if (this.id == SpaceFactoryPlugin.MACERATING) {
			return MachineArrowWidget.Type.PULVERIZING;
		}
		if (this.id == SpaceFactoryPlugin.COMPRESSING) {
			return MachineArrowWidget.Type.COMPRESSING;
		}
		if (this.id == SpaceFactoryPlugin.EXTRACTING) {
			return MachineArrowWidget.Type.EXTRACTING;
		}
		return MachineArrowWidget.Type.DEFAULT;
	}

	private int getEuPerTick() {
		return this.id == SpaceFactoryPlugin.ELECTRIC_SMELTING ? 3 : 2;
	}

	private int getEuTotal(SimpleMachineDisplay display) {
		return this.getEuPerTick() * display.getDuration();
	}
}