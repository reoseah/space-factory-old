package spacefactory.screen;

import spacefactory.SpaceFactory;
import spacefactory.block.entity.GeneratorBlockEntity;
import spacefactory.core.screen.ContainerScreenHandler;
import spacefactory.core.screen.property.ReadProperty;
import spacefactory.core.screen.property.WriteProperty;
import spacefactory.core.screen.slot.FuelSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;

public class GeneratorScreenHandler extends ContainerScreenHandler {
    protected int fuelLeft, fuelDuration, energy;

    protected GeneratorScreenHandler(int syncId, Inventory inventory, PlayerInventory user) {
        super(SpaceFactory.ScreenHandlerTypes.GENERATOR, syncId, inventory);

        this.addQuickTransferSlot(AbstractFurnaceBlockEntity::canUseAsFuel, new FuelSlot(this.inventory, 0, 80, 54));
        this.addPlayerSlots(user);
    }

    public GeneratorScreenHandler(int syncId, GeneratorBlockEntity be, PlayerEntity player) {
        this(syncId, be, player.getInventory());

        this.addProperty(new ReadProperty(be::getFuelLeft));
        this.addProperty(new ReadProperty(be::getFuelDuration));
    }

    public GeneratorScreenHandler(int syncId, PlayerInventory user) {
        this(syncId, new SimpleInventory(2), user);

        this.addProperty(new WriteProperty(value -> this.fuelLeft = value));
        this.addProperty(new WriteProperty(value -> this.fuelDuration = value));
    }

    @Environment(EnvType.CLIENT)
    public boolean isBurning() {
        return this.fuelLeft > 0;
    }

    @Environment(EnvType.CLIENT)
    public int getFuelDisplay() {
        int duration = this.fuelDuration == 0 ? 200 : this.fuelDuration;
        return this.fuelLeft * 13 / duration;
    }

    public int getFuelLeft() {
        return this.fuelLeft;
    }
}
