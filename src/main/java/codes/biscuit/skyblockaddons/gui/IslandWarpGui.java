package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.SkyblockDate;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonToggleNew;
import codes.biscuit.skyblockaddons.gui.buttons.IslandButton;
import codes.biscuit.skyblockaddons.gui.buttons.IslandMarkerButton;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;

import static codes.biscuit.skyblockaddons.core.Translations.*;

public class IslandWarpGui extends GuiScreen {

    @Getter @Setter private static Marker doubleWarpMarker;

    private static int TOTAL_WIDTH;
    private static int TOTAL_HEIGHT;

    public static float SHIFT_LEFT;
    public static float SHIFT_TOP;

    private Marker selectedMarker;

    public static float ISLAND_SCALE;

    public IslandWarpGui() {
        super();
    }

    @Override
    public void initGui() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        for (Island island : Island.values()) {
            if (island == Island.JERRYS_WORKSHOP
                    && main.getUtils().getCurrentDate().getMonth() != SkyblockDate.SkyblockMonth.LATE_WINTER) {
                continue;
            }
            this.buttonList.add(new IslandButton(island));
        }

        int screenWidth = mc.displayWidth;
        int screenHeight = mc.displayHeight;

        ISLAND_SCALE = 0.7F/1080*screenHeight;

        float scale = ISLAND_SCALE;
        float totalWidth = TOTAL_WIDTH*scale;
        float totalHeight = TOTAL_HEIGHT*scale;
        SHIFT_LEFT = (screenWidth/2F-totalWidth/2F)/scale;
        SHIFT_TOP = (screenHeight/2F-totalHeight/2F)/scale;

        int x = Math.round(screenWidth/ISLAND_SCALE-SHIFT_LEFT-475);
        int y = Math.round(screenHeight/ISLAND_SCALE-SHIFT_TOP);

        this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60 * 2, 50,
                () -> main.getConfigValues().isEnabled(Feature.FANCY_WARP_MENU),
                () -> Feature.FANCY_WARP_MENU.setEnabled(!main.getConfigValues().isEnabled(Feature.FANCY_WARP_MENU))));
        this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60, 50,
                () -> main.getConfigValues().isEnabled(Feature.DOUBLE_WARP),
                () -> Feature.DOUBLE_WARP.setEnabled(!main.getConfigValues().isEnabled(Feature.DOUBLE_WARP))));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        int guiScale = sr.getScaleFactor();

        int startColor = new Color(0,0, 0, Math.round(255/3F)).getRGB();
        int endColor = new Color(0,0, 0, Math.round(255/2F)).getRGB();
        drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), startColor, endColor);

        drawCenteredString(mc.fontRendererObj, getMessage("warpMenu.click"), sr.getScaledWidth()/2, 10, 0xFFFFFFFF);
        drawCenteredString(mc.fontRendererObj, getMessage("warpMenu.mustUnlock"), sr.getScaledWidth()/2, 20, 0xFFFFFFFF);

        GlStateManager.pushMatrix();
        ISLAND_SCALE = 0.7F/1080*mc.displayHeight;
        float scale = ISLAND_SCALE;
        GlStateManager.scale(1F/guiScale, 1F/guiScale, 1);
        GlStateManager.scale(scale, scale, 1);

        float totalWidth = TOTAL_WIDTH*scale;
        float totalHeight = TOTAL_HEIGHT*scale;

        SHIFT_LEFT = (mc.displayWidth/2F-totalWidth/2F)/scale;
        SHIFT_TOP = (mc.displayHeight/2F-totalHeight/2F)/scale;
        GlStateManager.translate(SHIFT_LEFT, SHIFT_TOP, 0);

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        IslandButton lastHoveredButton = null;

        for (GuiButton button : buttonList) {
            if (button instanceof IslandButton) {
                IslandButton islandButton = (IslandButton)button;

                // Call this just so it calculates the hover, don't actually draw.
                islandButton.drawButton(mc, mouseX, mouseY, false);

                if (islandButton.isHovering()) {
                    if (lastHoveredButton != null) {
                        lastHoveredButton.setDisableHover(true);
                    }
                    lastHoveredButton = islandButton;
                }
            }
        }

        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY);
        }


        int x = Math.round(mc.displayWidth/ISLAND_SCALE-SHIFT_LEFT-500);
        int y = Math.round(mc.displayHeight/ISLAND_SCALE-SHIFT_TOP);
        GlStateManager.pushMatrix();
        float textScale = 3F;
        GlStateManager.scale(textScale, textScale, 1);
        mc.fontRendererObj.drawStringWithShadow(
                Feature.FANCY_WARP_MENU.getMessage()
                , x / textScale + 50, (y - 30 - 60 * 2) / textScale + 5, 0xFFFFFFFF
        );
        mc.fontRendererObj.drawStringWithShadow(
                Feature.DOUBLE_WARP.getMessage()
                , x / textScale + 50, (y - 30 - 60) / textScale + 5, 0xFFFFFFFF
        );
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        detectClosestMarker(mouseX, mouseY);
    }

    public static float IMAGE_SCALED_DOWN_FACTOR = 0.75F;

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && selectedMarker != null) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            mc.displayGuiScreen(null);

            if (main.getConfigValues().isEnabled(Feature.DOUBLE_WARP)) {
                doubleWarpMarker = selectedMarker;

                // Remove the marker if it didn't trigger for some reason...
                main.getNewScheduler().scheduleDelayedTask(new SkyblockRunnable() {
                    @Override
                    public void run() {
                        if (doubleWarpMarker != null) {
                            doubleWarpMarker = null;
                        }
                    }
                }, 20);
            }
            if (selectedMarker != null) {
                mc.thePlayer.sendChatMessage("/warp " + selectedMarker.getWarpName());
            }

        }

        Pair<Integer, Integer> scaledMouseLocations = getScaledMouseLocation(mouseX, mouseY);
        super.mouseClicked(scaledMouseLocations.getKey(), scaledMouseLocations.getValue(), mouseButton);
    }


    public void detectClosestMarker(int mouseX, int mouseY) {
        Pair<Integer, Integer> scaledMouseLocations = getScaledMouseLocation(mouseX, mouseY);

        IslandWarpGui.Marker hoveredMarker = null;
        double markerDistance = IslandMarkerButton.MAX_SELECT_RADIUS + 1;

        for (GuiButton button : this.buttonList) {
            if (button instanceof IslandButton) {
                IslandButton islandButton = (IslandButton) button;

                for (IslandMarkerButton marker : islandButton.getMarkerButtons()) {
                    double distance = marker.getDistance(
                            scaledMouseLocations.getKey(), // x
                            scaledMouseLocations.getValue() // y
                    );

                    if (distance != -1 && distance < markerDistance) {
                        hoveredMarker = marker.getMarker();
                        markerDistance = distance;
                    }
                }
            }
        }

        selectedMarker = hoveredMarker;

        //if (hoveredMarker != null) System.out.println(hoveredMarker.getLabel()+" "+markerDistance);
    }

    /**
     * Returns a scaled X,Y pair after using {@link IslandWarpGui#ISLAND_SCALE} for scaling
     * <br> See Also: {@link IslandWarpGui#SHIFT_LEFT} {@link IslandWarpGui#SHIFT_TOP}
     * @param mouseX current mouseX
     * @param mouseY current mouseY
     * @return scaled X, Y {@link codes.biscuit.skyblockaddons.utils.objects.Pair}
     */
    @SuppressWarnings("lossy-conversions")
    public static Pair<Integer, Integer> getScaledMouseLocation(int mouseX, int mouseY) {
        int minecraftScale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        float islandGuiScale = IslandWarpGui.ISLAND_SCALE;

        mouseX *= minecraftScale;
        mouseY *= minecraftScale;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        return new Pair<>(mouseX, mouseY);
    }

    @Getter
    public enum Island {
        THE_END("The End", 240, 30),
        CRIMSON_ISLE("Crimson Isle", 835, 25),
        THE_PARK("The Park", 80, 440),
        SPIDERS_DEN("Spider's Den", 500, 470),
        DEEP_CAVERNS("Deep Caverns", 1400, 250),
        GOLD_MINE("Gold Mine", 1130, 525),
        MUSHROOM_DESERT("Mushroom Desert", 1470, 525),
        THE_BARN("The Barn", 1125, 850),
        HUB("Hub", 300, 820),
        PRIVATE_ISLAND("Private Island", 275, 1172),
		GARDEN("Garden", 50, 1050),
        DUNGEON_HUB("Dungeon Hub", 1500, 1100),
        JERRYS_WORKSHOP("Jerry's Workshop", 1280, 1150)
        ;

        private final String label;
        private final int x;
        private final int y;
        private int w;
        private int h;

        private final ResourceLocation resourceLocation;
        private BufferedImage bufferedImage;

        @SuppressWarnings("lossy-conversions")
        Island(String label, int x, int y) {
            this.label = label;
            this.x = x;
            this.y = y;

            this.resourceLocation = new ResourceLocation(
                    "skyblockaddons"
                    , "islands/" + this.name().toLowerCase(Locale.US).replace("_", "") + ".png"
            );
            try {
                bufferedImage = TextureUtil.readBufferedImage(
                        Minecraft.getMinecraft().getResourceManager().getResource(this.resourceLocation).getInputStream()
                );
                this.w = bufferedImage.getWidth();
                this.h = bufferedImage.getHeight();

                if (label.equals("The End")) {
                    IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR = this.w/573F; // The original end HD texture is 573 pixels wide.

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            this.w /= IMAGE_SCALED_DOWN_FACTOR;
            this.h /= IMAGE_SCALED_DOWN_FACTOR;

            if (this.y + this.h > TOTAL_HEIGHT) {
                TOTAL_HEIGHT = this.y + this.h;
            }
            if (this.x + this.w > TOTAL_WIDTH) {
                TOTAL_WIDTH = this.x + this.w;
            }
        }
    }

    //TODO: Maybe change these to load from a file at some point
    @Getter
    public enum Marker {
        PRIVATE_ISLAND("home", getMessage("warpMenu.home"), Island.PRIVATE_ISLAND, true, 72, 90),
		
		GARDEN("garden", getMessage("warpMenu.spawn"), Island.GARDEN, true, 160, 70),

        JERRYS_WORKSHOP("workshop", getMessage("warpMenu.spawn"), Island.JERRYS_WORKSHOP, 35, 90),

        HUB("hub", getMessage("warpMenu.spawn"), Island.HUB, true, 600, 200),
        ELIZABETH("elizabeth", "Elizabeth", Island.HUB, 660, 150),
        CASTLE("castle", "Castle", Island.HUB, 130, 80),
        DARK_AUCTION("da", "Sirius Shack", Island.HUB, 385, 415),
        CRYPT("crypt", "Crypts", Island.HUB, 550, 100),
        WIZARD_TOWER("wizard", "Wizard Tower", Island.HUB, 475, 260),
        DUNGEON_HUB("dungeon_hub", "Dungeon Hub", Island.HUB, false, 400, 175),
        MUSEUM("museum", "Museum", Island.HUB, true, 310, 200),

        SPIDERS_DEN("spider", getMessage("warpMenu.spawn"), Island.SPIDERS_DEN, true, 345, 240),
        SPIDERS_DEN_NEST("nest", "Top of Nest", Island.SPIDERS_DEN, 450, 30),
        ARACHNES_SANCTUARY("arachne", "Arachne's Sanctuary", Island.SPIDERS_DEN, 240,135),

        THE_PARK("park", getMessage("warpMenu.spawn"), Island.THE_PARK, true, 263, 308),
        HOWLING_CAVE("howl", "Howling Cave", Island.THE_PARK, 254, 202),
        THE_PARK_JUNGLE("jungle", "Jungle", Island.THE_PARK, 194, 82),

        THE_END("end", getMessage("warpMenu.spawn"), Island.THE_END, true, 440, 291),
        DRAGONS_NEST("drag", "Dragon's Nest", Island.THE_END, 260, 248),
        VOID_SEPULTURE("void", "Void Sepulture", Island.THE_END, true, 370, 227),

        CRIMSON_ISLE("nether", getMessage("warpMenu.spawn"), Island.CRIMSON_ISLE, true, 70, 280),
        FORGOTTEN_SKULL("kuudra", "Forgotten Skull", Island.CRIMSON_ISLE, true, 460, 90),
        THE_WASTELAND("wasteland", "The Wasteland", Island.CRIMSON_ISLE, true, 330, 160),
        DRAGONTAIL("dragontail", "Dragontail", Island.CRIMSON_ISLE, true, 140, 150),
        SCARLETON("scarleton", "Scarleton", Island.CRIMSON_ISLE, true, 400, 220),
        SMOLDERING_TOMB("smold", "Smoldering Tomb", Island.CRIMSON_ISLE, true, 350, 70),

        THE_BARN("barn", getMessage("warpMenu.spawn"), Island.THE_BARN, true, 140, 150),
        MUSHROOM_DESERT("desert", getMessage("warpMenu.spawn"), Island.MUSHROOM_DESERT, true, 210, 295),
		TRAPPER("trapper", "Trapper's Hut", Island.MUSHROOM_DESERT, true, 300, 200),

        GOLD_MINE("gold", getMessage("warpMenu.spawn"), Island.GOLD_MINE, true, 86, 259),

        DEEP_CAVERNS("deep", getMessage("warpMenu.spawn"), Island.DEEP_CAVERNS, true, 97, 213),
        DWARVEN_MINES("mines", "Dwarven Mines", Island.DEEP_CAVERNS, false, 280, 205),
        DWARVEN_FORGE("forge", "Forge", Island.DEEP_CAVERNS, true, 280, 280),
        DWARVEN_BASE_CAMP("base", "Dwarven Base Camp", Island.DEEP_CAVERNS, true, 240, 330),
        CRYSTAL_HOLLOWS("crystals", "Crystal Hollows", Island.DEEP_CAVERNS, true, 190, 360),
        CRYSTAL_NUCLEUS("nucleus", "Crystal Nucleus", Island.DEEP_CAVERNS, true, 140, 390),

        DUNGEON_HUB_ISLAND("dungeon_hub", getMessage("warpMenu.spawn"), Island.DUNGEON_HUB, false, 35, 80),
        ;

        private final String warpName;
        private final String label;
        private final Island island;
        private final boolean advanced;
        private final int x;
        private final int y;

        Marker(String warpName, String label, Island island, int x, int y) {
            this(warpName, label, island, false, x, y);
        }

        Marker(String warpName, String label, Island island, boolean advanced, int x, int y) {
            this.warpName = warpName;
            this.label = label;
            this.island = island;
            this.x = x;
            this.y = y;
            this.advanced = advanced;
        }
    }
}
