package spacefactory.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import spacefactory.SpaceFactory;
import spacefactory.core.block.MachineBlock;
import spacefactory.block.entity.EngineeringAIBlockEntity;

import java.util.List;

public class EngineeringAIBlock extends MachineBlock {
    public static final Text TOOLTIP = new TranslatableText("tooltip.spacefactory.energy_per_tick", SpaceFactory.config.fabricatorAIConsumption).formatted(Formatting.GRAY);

    public EngineeringAIBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EngineeringAIBlockEntity(pos, state);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(TOOLTIP);
    }
}
