package org.stepan1411.pvp_bot.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WarningScreen extends Screen {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PVP_BOT_WARNING");
    private static final Identifier WARNING_TEXTURE = Identifier.of("pvp_bot", "textures/gui/warning.png");
    
    private final Screen parent;
    private final Path warningFile;
    private CheckboxWidget dontShowAgain;
    private boolean textureLogged = false;
    
    public WarningScreen(Screen parent) {
        super(Text.literal("WARNING: This mod is unstable in singleplayer!"));
        this.parent = parent;
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        this.warningFile = configDir.resolve("no_singleplayer_warning.txt");
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (!textureLogged) {
            LOGGER.info("Initializing warning screen");
            LOGGER.info("Texture identifier: {}", WARNING_TEXTURE);
            textureLogged = true;
        }
        
        int centerX = this.width / 2;
        

        int bottomY = this.height - 60;
        

        this.dontShowAgain = CheckboxWidget.builder(Text.literal("Don't show again"), this.textRenderer)
                .pos(centerX - 60, bottomY)
                .build();
        this.addDrawableChild(dontShowAgain);
        

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("OK"),
                button -> {
                    if (dontShowAgain.isChecked()) {
                        try {
                            Files.createDirectories(warningFile.getParent());
                            Files.writeString(warningFile, "User disabled singleplayer warning");
                            LOGGER.info("Warning disabled by user");
                        } catch (IOException ex) {
                            LOGGER.error("Failed to save warning preference", ex);
                        }
                    }
                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                }
        ).dimensions(centerX - 40, bottomY + 30, 80, 20).build());
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        context.fill(0, 0, this.width, this.height, 0xFF000000);
        

        int displayHeight = this.height;
        int displayWidth = displayHeight;
        int imageX = (this.width - displayWidth) / 2;
        int imageY = 0;
        
        try {


            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                WARNING_TEXTURE,
                imageX, imageY,
                0, 0,
                displayWidth, displayHeight,
                1024, 1024,
                1024, 1024
            );
        } catch (Exception e) {
            if (!textureLogged) {
                LOGGER.error("Failed to render warning texture", e);
                textureLogged = true;
            }

            context.fill(imageX, imageY, imageX + displayWidth, imageY + displayHeight, 0xFFFF0000);
            

            context.drawCenteredTextWithShadow(
                this.textRenderer,
                "Failed to load warning image",
                this.width / 2,
                this.height / 2,
                0xFFFFFF
            );
        }
        

        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
