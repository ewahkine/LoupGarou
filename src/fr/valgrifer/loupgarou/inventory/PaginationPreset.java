package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bukkit.ChatColor.*;

public abstract class PaginationPreset extends MenuPreset
{
    private List<Slot> registeredSlots;
    public int getSize()
    {
        return registeredSlots.size();
    }

    @Getter
    private final int maxPerPage;
    @Getter
    private final int maxPage;

    public PaginationPreset(LGInventoryHolder holder)
    {
        super(holder);

        if(holder.getInventory().getType() != InventoryType.CHEST)
            throw new RuntimeException("PaginationPreset need `InventoryType.CHEST`");
        if(holder.getMaxSlot() < 18)
            throw new RuntimeException("PaginationPreset need inventory with minimum 18 slots");

        registeredSlots = Collections.unmodifiableList(registeredSlots);

        maxPerPage = (holder.getMaxLine()-2)*9;
        maxPage = (int) Math.ceil((double) registeredSlots.size() / ((holder.getMaxLine()-2)*9));

        setSlot(3, holder.getMaxLine()-1,
                new Slot(ItemBuilder.make(Material.ARROW)
                        .setCustomId("ac_page_previous"))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder holder) {
                        int index = getPageIndex();
                        if(index == 0)
                            return ItemBuilder.make(Material.AIR);
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Go to Page " + GOLD + index);
                    }
                },
                (h, event) -> setPageIndex(getPageIndex() - 1));

        setSlot(4, holder.getMaxLine()-1, makeInfoButtonIcon());

        setSlot(5, holder.getMaxLine()-1,
                new Slot(ItemBuilder.make(Material.ARROW)
                        .setCustomId("ac_page_next"))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        int index = getPageIndex();
                        if(index == getMaxPage()-1)
                            return ItemBuilder.make(Material.AIR);
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Go to Page " + GOLD + (index + 2));
                    }
                },
                (h, event) -> setPageIndex(getPageIndex() + 1));
    }

    public void setPageIndex(int index)
    {
        getHolder().getCache().set("pageIndex", VariousUtils.MinMax(index, 0, getMaxPage()-1));
        apply();
    }
    public int getPageIndex()
    {
        return VariousUtils.MinMax(getHolder().getCache().get("pageIndex", 0), 0, getMaxPage()-1);
    }

    protected abstract Slot makeInfoButtonIcon();

    protected void registerItem(Slot slot)
    {
        registerItem(slot, null);
    }
    protected void registerItem(Slot item, ClickAction action)
    {
        if(this.registeredSlots == null)
            this.registeredSlots = new ArrayList<>();

        this.registeredSlots.add(item);

        String customId = item.getDefaultItem().getCustomId();
        if(customId != null && action != null)
            putAction(customId, action);
    }


    public void apply()
    {
        if(getHolder() == null)
            return;

        int pageIndex = getPageIndex();

        int i = 0,
                offset = getMaxPerPage() * pageIndex,
                to = Math.min(getMaxPerPage(), registeredSlots.size() - offset);
        for(; i < to; i++)
            content[i] = registeredSlots.get(i + offset);
        for(; i < getMaxPerPage(); i++)
            content[i] = null;

        getHolder().getInventory().setContents(
                Arrays.stream(content)
                        .map(slot -> {
                            try {
                                if(slot == null)
                                    return null;
                                ItemBuilder item;
                                if((item = slot.getItem(getHolder())) == null)
                                    return null;
                                return item.build();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .toArray(ItemStack[]::new));
    }
}
