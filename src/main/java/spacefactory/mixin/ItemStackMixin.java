package spacefactory.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spacefactory.SpaceFactory;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	@Final
	private Item item;

	@Inject(at = @At("RETURN"), method = "getEnchantments")
	public void getEnchantments(CallbackInfoReturnable<NbtList> ci) {
		if (this.item == SpaceFactory.Items.FLAK_VEST) {
			boolean hasEnchantment = false;
			for (int i = 0; i < ci.getReturnValue().size(); ++i) {
				NbtCompound nbt = ci.getReturnValue().getCompound(i);
				Enchantment enchantment = Registry.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(nbt)).orElse(null);
				if (enchantment instanceof ProtectionEnchantment) {
					hasEnchantment = true;
					break;
				}
			}
			if (!hasEnchantment) {
				NbtCompound nbt = EnchantmentHelper.createNbt(Registry.ENCHANTMENT.getId(Enchantments.BLAST_PROTECTION), 4);
				ci.getReturnValue().add(nbt);
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "addEnchantment", cancellable = true)
	public void addEnchantment(Enchantment enchantment, int level, CallbackInfo ci) {
		if (this.item == SpaceFactory.Items.FLAK_VEST && enchantment instanceof ProtectionEnchantment) {
			ci.cancel();
		}
	}
}