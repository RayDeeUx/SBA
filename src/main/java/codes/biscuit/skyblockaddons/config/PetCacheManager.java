package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.PetManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

@Setter @Getter
public class PetCacheManager {
    private static final Logger logger = SkyblockAddons.getLogger();

    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private final File petCacheFile;

    private PetCache petCache = new PetCache();

    public static class PetCache {
        private PetManager.Pet currentPet = null;

        /**
         * key = index + 45 * (pageNum - 1), value = {@link PetManager.Pet}
         * @see codes.biscuit.skyblockaddons.utils.pojo.PetInfo
         */
        @Getter private final HashMap<Integer, PetManager.Pet> petMap = new HashMap<>();
    }

    public PetCacheManager(File configDir) {
        this.petCacheFile = new File(configDir.getAbsolutePath() + "/skyblockaddons_petCache.json");
    }

    /**
     * Loads the persistent values from {@code config/skyblockaddons_petCache.json} in the user's Minecraft folder.
     */
    public void loadValues() {
        if (petCacheFile.exists()) {

            try (InputStreamReader reader = new InputStreamReader(
                    Files.newInputStream(petCacheFile.toPath()), StandardCharsets.UTF_8)
            ) {
                petCache = SkyblockAddons.getGson().fromJson(reader, PetCacheManager.PetCache.class);

            } catch (Exception ex) {
                logger.error("Error while loading pet cache!", ex);
            }

        } else {
            saveValues();
        }
    }

    /**
     * Saves the pet cache to {@code config/skyblockaddons_petCache.json} in the user's Minecraft folder.
     */
    public void saveValues() {
        // TODO: Better error handling that tries again/tells the player if it fails
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            boolean isDevMode = Feature.DEVELOPER_MODE.isEnabled();
            if (isDevMode) logger.info("Saving pet cache...");

            try {
                //noinspection ResultOfMethodCallIgnored
                petCacheFile.createNewFile();

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        Files.newOutputStream(petCacheFile.toPath()), StandardCharsets.UTF_8)
                ) {
                    SkyblockAddons.getGson().toJson(petCache, writer);
                }
            } catch (Exception ex) {
                logger.error("Error while saving pet cache!", ex);
                if (Minecraft.getMinecraft().thePlayer != null) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage(
                            "Error saving pet cache! Check log for more detail."
                    );
                }
            }

            if (isDevMode) logger.info("Pet cache saved!");

            SAVE_LOCK.unlock();
        });
    }

    public PetManager.Pet getCurrentPet() {
        return petCache.currentPet;
    }

    public void setCurrentPet(PetManager.Pet pet) {
        petCache.currentPet = pet;
        saveValues();
    }

    public PetManager.Pet getPet(int index) {
        return petCache.petMap.get(index);
    }

    public void putPet(int index, PetManager.Pet pet) {
        petCache.petMap.put(index, pet);
    }

}
