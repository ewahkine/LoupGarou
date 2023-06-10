package fr.valgrifer.loupgarou.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class ItemBuilder {
    private static final NamespacedKey CUSTOMIDTAG = new NamespacedKey("lg", "customidlg");

    private String customId = null;
    private Material mat = Material.AIR;
    private String displayName = null;
    private List<String> lore = new ArrayList<>();
    private int amount = 1;
    private int customModelData = 0;
    private OfflinePlayer skull;

    public ItemBuilder(Material mat)
    {
        this.setType(mat);
    }

    public ItemBuilder()
    {}

    public static ItemBuilder make(Material mat)
    {
        return new ItemBuilder(mat);
    }
    public static boolean checkId(ItemStack item, String customId)
    {
        PersistentDataContainer tag;
        if(item == null || item.getType() == Material.AIR || (tag = item.getItemMeta().getPersistentDataContainer()) == null || tag.isEmpty())
            return false;
        return tag.getOrDefault(CUSTOMIDTAG, PersistentDataType.STRING, "").equalsIgnoreCase(customId);
    }
    public static String getCustomId(ItemStack item)
    {
        PersistentDataContainer tag;
        if(item == null || item.getType() == Material.AIR || (tag = item.getItemMeta().getPersistentDataContainer()) == null || tag.isEmpty())
            return null;
        return tag.getOrDefault(CUSTOMIDTAG, PersistentDataType.STRING, null);
    }

    public ItemBuilder clone()
    {
        return new ItemBuilder()
                .setCustomId(customId)
                .setType(mat)
                .setDisplayName(displayName)
                .setLore(new ArrayList<>(lore))
                .setAmount(amount)
                .setCustomModelData(customModelData)
                .setSkull(skull);
    }

    public ItemStack build()
    {
        ItemStack item = new ItemStack(mat);
        item.setAmount(amount);

        if(displayName != null || lore.size() > 0 || customModelData > 0 || customId != null)
        {
            ItemMeta meta = item.getItemMeta();
            if(meta != null)
            {
                if(displayName != null)
                    meta.setDisplayName(displayName);
                meta.setLore(lore);
                if(skull != null && mat == Material.PLAYER_HEAD && meta instanceof SkullMeta)
                    ((SkullMeta) meta).setOwningPlayer(skull);
                if(customModelData > 0)
                    meta.setCustomModelData(customModelData);
                if(customId != null)
                    meta.getPersistentDataContainer().set(CUSTOMIDTAG, PersistentDataType.STRING, customId);
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    public ItemBuilder setType(Material mat)
    {
        this.mat = mat != null ? mat : Material.AIR;
        return this;
    }
    public Material getType()
    {
        return mat;
    }

    public ItemBuilder setAmount(int amount)
    {
        this.amount = Math.min(Math.max(amount, 1), 64);
        return this;
    }
    public int getAmount()
    {
        return amount;
    }

    public ItemBuilder setDisplayName(String displayName)
    {
        this.displayName = displayName;
        return this;
    }
    public String getDisplayName()
    {
        return displayName;
    }

    public ItemBuilder setCustomId(String customId)
    {
        this.customId = customId != null ? customId.toLowerCase() : null;
        return this;
    }
    public String getCustomId()
    {
        return customId;
    }

    public ItemBuilder setCustomModelData(int customModelData)
    {
        this.customModelData = Math.max(customModelData, 0);
        return this;
    }
    public int getCustomModelData()
    {
        return customModelData;
    }

    public ItemBuilder setLore(String... lines)
    {
        lore = new ArrayList<>(Arrays.asList(lines));
        return this;
    }
    public ItemBuilder setLore(List<String> lines)
    {
        lore = lines;
        return this;
    }
    public List<String> getLore()
    {
        return lore;
    }
    public ItemBuilder addLore(String... lines)
    {
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder setSkull(OfflinePlayer skull)
    {
        this.skull = skull;
        return this;
    }
    public OfflinePlayer getSkull()
    {
        return skull;
    }
}
