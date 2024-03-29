package spacefactory.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.slot.Slot;
import spacefactory.SpaceFactory;
import spacefactory.api.EU;
import spacefactory.core.screen.ContainerScreenHandler;
import spacefactory.core.screen.property.ReadProperty;
import spacefactory.core.screen.property.WriteProperty;
import spacefactory.block.entity.SolarPanelBlockEntity;

public class SolarPanelScreenHandler extends ContainerScreenHandler {
    protected boolean generating, skyView;

    protected SolarPanelScreenHandler(int syncId, Inventory inventory, PlayerInventory user) {
        super(SpaceFactory.ScreenHandlerTypes.SOLAR_PANEL, syncId, inventory);

        this.addQuickTransferSlot(EU::isElectricItem, new Slot(inventory, 0, 80, 27));
        this.addPlayerSlots(user);
    }

    public SolarPanelScreenHandler(int syncId, SolarPanelBlockEntity be, PlayerEntity player) {
        this(syncId, be, player.getInventory());

        this.addProperty(new ReadProperty(() -> be.isGenerating() ? 1 : 0));
        this.addProperty(new ReadProperty(() -> be.hasSkyView() ? 1 : 0));
    }

    public SolarPanelScreenHandler(int syncId, PlayerInventory user) {
        this(syncId, new SimpleInventory(1), user);

        this.addProperty(new WriteProperty(value -> this.generating = value == 1));
        this.addProperty(new WriteProperty(value -> this.skyView = value == 1));
    }

    @Environment(EnvType.CLIENT)
    public boolean isGenerating() {
        return this.generating;
    }


    @Environment(EnvType.CLIENT)
    public boolean hasSkyView() {
        return this.skyView;
    }
}
