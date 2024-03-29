package spacefactory.compatibility.rei.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import spacefactory.compatibility.rei.SpaceFactoryPlugin;

import java.util.Collections;
import java.util.List;

public class MachineArrowWidget extends Arrow {
    private final Rectangle bounds;
    private final MachineArrowWidget.Type type;
    private double animationDuration = -1.0D;
    private int euPerTick, euTotal;

    public MachineArrowWidget(Rectangle bounds, Type type) {
        this.bounds = new Rectangle(bounds);
        this.type = type;
    }

    public double getAnimationDuration() {
        return this.animationDuration;
    }

    public void setAnimationDuration(double animationDurationMS) {
        this.animationDuration = animationDurationMS;
        if (this.animationDuration <= 0.0D) {
            this.animationDuration = -1.0D;
        }
    }

    public MachineArrowWidget cost(int euPerTick, int euTotal) {
        this.euPerTick = euPerTick;
        this.euTotal = euTotal;
        return this;
    }

    public Rectangle getBounds() {
        return this.bounds;
    }

    public List<? extends Element> children() {
        return Collections.emptyList();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
        RenderSystem.setShaderTexture(0, SpaceFactoryPlugin.WIDGETS);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        if (this.getAnimationDuration() > 0.0D) {
            int width = MathHelper.ceil((double) System.currentTimeMillis() / (this.animationDuration / 24.0D) % 24.0D);
            this.drawTexture(matrices, this.getX() + width, this.getY(), width, this.type.uOffset, 24 - width, 17);
            this.drawTexture(matrices, this.getX(), this.getY(), 24, this.type.uOffset, width, 17);
        } else {
            this.drawTexture(matrices, this.getX(), this.getY(), 0, this.type.uOffset, 24, 17);
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            MinecraftClient.getInstance().currentScreen
                    .renderTooltip(matrices, ImmutableList.of(
                                    new TranslatableText("tooltip.spacefactory.energy", this.euTotal),
                                    new TranslatableText("tooltip.spacefactory.energy_per_tick", this.euPerTick).formatted(Formatting.GRAY)),
                            mouseX, mouseY);
        }
    }

    public enum Type {
        DEFAULT(16),
        PULVERIZING(16 + 17),
        COMPRESSING(16 + 17 * 2),
        EXTRACTING(16 + 17 * 3),
        ATOMIC_CONSTRUCTION(16 + 17 * 4);

        public final int uOffset;

        Type(int uOffset) {
            this.uOffset = uOffset;
        }
    }
}
