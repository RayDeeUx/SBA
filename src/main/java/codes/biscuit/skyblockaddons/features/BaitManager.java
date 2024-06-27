package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of bait in the Player's Inventory.
 *
 * @author Charzard4261
 */
public class BaitManager {

    /**
     * The BaitListManager instance.
     */
    @Getter private static final BaitManager instance = new BaitManager();

    public static final Map<BaitType, Integer> DUMMY_BAITS = new HashMap<>();

    static {
        DUMMY_BAITS.put(BaitType.CARROT, 1);
        DUMMY_BAITS.put(BaitType.MINNOW, 2);
        DUMMY_BAITS.put(BaitType.WHALE, 3);
    }

    /**
     * A map of all baits in the inventory and their count
     */
    @Getter private final Map<BaitType, Integer> baitsInInventory = new HashMap<>();

    /**
     * Re-count all baits in the inventory
     */
    public void refreshBaits() {
        baitsInInventory.clear();

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        for (ItemStack itemStack : player.inventory.mainInventory) {
            BaitType bait;

            // Glowy Chum and Hot bait doesn't contain skyblock id
            String skyblockID = ItemUtils.getSkyblockItemID(itemStack);
            if (skyblockID != null)
                bait = BaitType.getByItemID(skyblockID);
            else {
                String skullOwnerID = ItemUtils.getSkullOwnerID(itemStack);
                if (skullOwnerID != null)
                    bait = BaitType.getBySkullID(skullOwnerID);
                else
                    continue;
            }

            if (bait == null)
                continue;

            baitsInInventory.put(bait, baitsInInventory.getOrDefault(bait, 0) + itemStack.stackSize);
        }

    }

    @Getter
    public enum BaitType {
        MINNOW("§fMinnow Bait", "MINNOW_BAIT", "7b2c475f-ac70-34ce-af36-be5009df1585", "5f91a14867ef9867872dcdc8c54e3d4cfb5e525dccfb7f99a7314a45faeb301e"),
        FISH("§fFish Bait", "FISH_BAIT", "1b379ea9-7eb2-3e40-bf48-559eaf4da1f6", "f9bb18e1cfe6edf03535e5fdc6482e09a7fdf95125c8611269b9de9d54715b9d"),
        LIGHT("§fLight Bait", "LIGHT_BAIT", "0f759208-92ff-3686-b38b-d4f821a9247e", "336a9add25645bfcc377c25ef0c2e9901d19493c3e981ebc6ba7a1a1b6466ce4"),
        DARK("§fDark Bait", "DARK_BAIT", "df2c3cc0-6792-3af3-b127-e0d934b485b7", "c33c516f3f380916d41a9355a319f85943aacc8a9c1b4a138017484b61114f68"),
        SPIKED("§fSpiked Bait", "SPIKED_BAIT", "a82c3c93-de2a-338c-a766-7c2985f6648c", "a12bc5ef26f367bdbd7116e80fde4af1823c0e9360afab02a9d3a4cb6d5fcd82"),
        SPOOKY("§fSpooky Bait", "SPOOKY_BAIT", "74b7ab7c-3268-3a70-8937-026e1416dd7f", "51074ba79616c7d8cf8e33849039f67410a2f7c9ce793d447e21f5aa24d50108"),
        CARROT("§fCarrot Bait", "CARROT_BAIT", "0842969c-2692-3bd7-8483-2b7b2c2b7f63", "4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b"),
        BLESSED("§aBlessed Bait", "BLESSED_BAIT", "54b94885-521b-3700-a138-31330f0aba0d", "b060a46497cee7611346c9dd8eb8f5de4483de259147211353848356de15d19"),
        WHALE("§9Whale Bait", "WHALE_BAIT", "d88b7890-b977-3de5-acc6-5e14b17cc1f3", "33aa71676e81fb53a040dfdca3e5b47d53e6efd665e69fb439778e8c4efb1cc"),
        ICE("§aIce Bait", "ICE_BAIT", "b2b19dcd-dc67-31df-a790-e6cf07ae12ac", "11136616d8c4a87a54ce78a97b551610c2b2c8f6d410bc38b858f974b113b208"),
        SHARK("§aShark Bait", "SHARK_BAIT", "9a6c7271-a12d-3941-914c-7c456a086c5a", "edff904124efe486b3a54261dbb8072b0a4e11615ad8d7394d814e0e8c8ef9eb"),
        CORRUPTED("§fCorrupted Bait", "CORRUPTED_BAIT", "e4785c0e-3c90-3af3-9ac8-c8c49653af4f", "4bbcddd45cd347865bceab3e3dc5d382723463963f85ecce81cdd61b53db14e4"),
        GLOWY_CHUM("§aGlowy Chum Bait", "GLOWY_CHUM_BAIT", "aed1035d-0ce1-35e6-b532-8c26a596f510", "dfdc1eed684dd805eae96d132e3da53d64267d7361388d5e2c67f5969871e71d"),
        HOT("§aHot Bait", "HOT_BAIT", "e1eada20-e1b5-39ee-8dcb-b527054e33e3", "213c6899d97109c6cacbbcdd01e8900abaf46432f197595baa15ad137d5fb9ba"),
        OBFUSCATED_FISH_1("§f§kObfuscated 1", "OBFUSCATED_FISH_1", "6044c06f-fb6e-3de8-9e7f-485b0edfdf3a", "e1f4d91e1bf8d3c4258fe0f28ec2fa40670e25ba06ac4b5cb1abf52a83731a9c"),
        OBFUSCATED_FISH_2("§a§kObfuscated 2", "OBFUSCATED_FISH_2", "53807fee-2e39-3230-bdd1-3efcf0be73a9", "4631953a0351988029b90e838181e4e563d782e470ea33b8c612756f730625c2"),
        //OBFUSCATED_FISH_3("§9§kObfuscated 3", "OBFUSCATED_FISH_3", "c3701fb3-bd25-3772-b684-8572068672ff", "3c800c71b925587213382eeaaa426ed63112503e278ff9f5b3d39dbdb95d31d0")
        FROZEN_BAIT("§9Frozen Bait", "FROZEN_BAIT", "ccd28435-4b77-3627-81fa-b8db12f3f9cb", "c18c2990ab5571654e51381769288c0ee8e316f88baeeb95966ee296d6b9280c"),
        WORM_BAIT("§aWorm Bait", "WORM_BAIT", "730a6086-ad87-38fa-8fa4-0b76a060f4fc", "df03ad96092f3f789902436709cdf69de6b727c121b3c2daef9ffa1ccaed186c")
        ;

        private final String itemID;
        private final String skullID;
        private final ItemStack itemStack;

        BaitType(String name, String itemID, String skullID, String textureURL) {
            this.itemID = itemID;
            this.skullID = skullID;
            this.itemStack = ItemUtils.createSkullItemStack(name, itemID, skullID, textureURL);
        }

        /**
         * Check to see if the given name matches a bait's skyblock item ID
         * @return The matching BaitType or null
         */
        public static BaitType getByItemID(String itemID) {
            for (BaitType bait : values()) {
                if (itemID.startsWith(bait.itemID)) {
                    return bait;
                }
            }
            return null;
        }

        /**
         * Check to see if the given id matches a bait's skull owner ID
         * @return The matching BaitType or null
         */
        public static BaitType getBySkullID(String skullID) {
            for (BaitType bait : values()) {
                if (skullID.equals(bait.skullID)) {
                    return bait;
                }
            }
            return null;
        }
    }
}