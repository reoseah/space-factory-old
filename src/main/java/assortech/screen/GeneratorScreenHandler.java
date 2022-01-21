package assortech.screen;

import assortech.Assortech;
import assortech.block.entity.GeneratorBlockEntity;
import assortech.screen.property.ReadProperty;
import assortech.screen.property.WriteProperty;
import assortech.screen.slot.GenericFuelSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;

public class GeneratorScreenHandler extends AtScreenHandler {
    protected int fuelLeft, fuelDuration, energy;

    protected GeneratorScreenHandler(int syncId, Inventory inventory, PlayerInventory user) {
        super(Assortech.AtScreenHandlerTypes.GENERATOR, syncId, inventory);

        this.addQuickTransferSlot(AbstractFurnaceBlockEntity::canUseAsFuel, new GenericFuelSlot(this.inventory, 0, 80, 54));
        this.addPlayerSlots(user);
    }

    public GeneratorScreenHandler(int syncId, GeneratorBlockEntity be, PlayerEntity player) {
        this(syncId, be, player.getInventory());

        this.addProperty(new ReadProperty(be::getFuelLeft));
        this.addProperty(new ReadProperty(be::getFuelDuration));
        this.addProperty(new ReadProperty(be::getEnergy));
    }

    @Environment(EnvType.CLIENT)
    public GeneratorScreenHandler(int syncId, PlayerInventory user) {
        this(syncId, new SimpleInventory(1), user);

        this.addProperty(new WriteProperty(value -> this.fuelLeft = value));
        this.addProperty(new WriteProperty(value -> this.fuelDuration = value));
        this.addProperty(new WriteProperty(value -> this.energy = value));
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

    @Environment(EnvType.CLIENT)
    public int getEnergyDisplay() {
        return this.energy * 20 / GeneratorBlockEntity.CAPACITY;
    }

    @Environment(EnvType.CLIENT)
    public int getEnergy() {
        return this.energy;
    }
}
