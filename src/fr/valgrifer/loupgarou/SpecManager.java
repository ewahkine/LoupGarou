package fr.valgrifer.loupgarou;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

import static org.bukkit.ChatColor.*;

public class SpecManager extends LGInventoryHolder
{
    private static SpecManager mainSpecManager = null;
    public static SpecManager getMainSpecManager()
    {
        return mainSpecManager == null ? (mainSpecManager = new SpecManager()) : mainSpecManager;
    }

    public SpecManager() {
        super(6, GOLD + "Spec");

        setDefaultPreset(new SpecPreset(this));
    }

    private static class SpecPreset extends MenuPreset
    {
        public SpecPreset(LGInventoryHolder holder) {
            super(holder);
        }

        Function<LGPlayer, ItemStack> lobby;
        Function<LGPlayer, ItemStack> inGame;

        @Override
        protected void preset()
        {
            lobby = lgp -> ItemBuilder
                    .make(Material.PLAYER_HEAD)
                    .setSkull(lgp.getPlayer())
                    .setDisplayName(WHITE + lgp.getName())
                    .build();

            inGame = lgp -> ItemBuilder
                    .make(Material.PLAYER_HEAD)
                    .setSkull(lgp.getPlayer())
                    .setDisplayName((lgp.isDead() ? GRAY + "" + STRIKETHROUGH : WHITE + "" + BOLD) + lgp.getName())
                    .setLore(GRAY + (lgp.isDead() ? "étais" : "est"), lgp.getRevealRole())
                    .build();
        }

        @Override
        public void apply()
        {
            LGGame game = MainLg.getInstance().getCurrentGame();
            getHolder().getInventory().setContents(
                    game.getInGame()
                            .parallelStream()
                            .map(game.isStarted() ? inGame : lobby)
                            .toArray(ItemStack[]::new));
        }

        @Override
        public boolean autoUpdate() {
            return true;
        }
    }
}
