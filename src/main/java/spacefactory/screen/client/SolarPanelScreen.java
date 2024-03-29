package spacefactory.screen.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import spacefactory.screen.SolarPanelScreenHandler;

@Environment(EnvType.CLIENT)
public class SolarPanelScreen extends HandledScreen<SolarPanelScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("spacefactory:textures/gui/solar_panel.png");

    public SolarPanelScreen(SolarPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int leftX = (this.width - this.backgroundWidth) / 2;
        int topY = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, leftX, topY, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (this.handler.isGenerating()) {
            this.drawTexture(matrices, leftX + 81, topY + 46, 176, 0, 14, 14);
        } else if (this.handler.hasSkyView()) {
            int phase = MinecraftClient.getInstance().world.getMoonPhase();
            this.drawTexture(matrices, leftX + 84, topY + 49, 176, 24 + 8 * phase, 8, 8);
        }
    }
}
