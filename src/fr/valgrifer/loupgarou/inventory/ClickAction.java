package fr.valgrifer.loupgarou.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickAction {
    void run(LGInventoryHolder holder, InventoryClickEvent event);
}
