package fr.valgrifer.loupgarou;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.inventory.*;
import fr.valgrifer.loupgarou.roles.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

import static org.bukkit.ChatColor.*;

@SuppressWarnings({"unused"})
public class ConfigManager extends LGInventoryHolder
{
    private static ConfigManager mainConfigManager = null;
    public static ConfigManager getMainConfigManager()
    {
        return mainConfigManager == null ? (mainConfigManager = new ConfigManager()) : mainConfigManager;
    }

    private final List<String> roles;
    @SuppressWarnings("SpellCheckingInspection")
    public ConfigManager()
    {
        super(6, BLACK + "Lg Config");

        List<String> tempsRoles = new ArrayList<>(MainLg.getInstance().getRoles().keySet());
        tempsRoles.sort(Comparator.comparing(role -> Role.getName(MainLg.getInstance().getRoles().get(role))));
        roles = Collections.unmodifiableList(tempsRoles);

        setDefaultPreset(new MenuPreset(this) {
            @Override
            protected void preset()
            {
                setSlot(4, 1, new Slot(ItemBuilder.make(Material.CHEST)
                                .setCustomId("ac_compo")
                                .setDisplayName(GOLD + "Composition")) {
                            @Override
                            protected ItemBuilder getItem(LGInventoryHolder holder) {
                                return getDefaultItem().setLore(roles.parallelStream()
                                        .filter(role ->
                                                MainLg.getInstance().getConfig().getInt("role." + role, 0) > 0)
                                        .map(role -> {
                                            int count = MainLg.getInstance().getConfig().getInt("role." + role, 0);
                                            return (count > 0 ? GREEN : RED) +""+ count + " " + Role.getScoreBoardName(MainLg.getInstance().getRoles().get(role));
                                        })
                                        .sorted()
                                        .toArray(String[]::new));
                            }
                        },
                        (holder, event) -> loadPreset("editCompo"));

                setSlot(4, 2,
                        new Slot(ItemBuilder.make(Material.BIRCH_DOOR)
                                .setCustomId("ac_joinall")
                                .setDisplayName(GREEN + "/lg joinall")
                                .setLore(GRAY + "A faire après chaque édition de composition")),
                        (holder, event) -> {
                            for (Player p : Bukkit.getOnlinePlayers())
                                Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, "joinall"));
                            for (Player p : Bukkit.getOnlinePlayers())
                                Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, "joinall"));
                        });

                setSlot(1, 2,
                        new Slot(ItemBuilder.make(Material.CLOCK)
                                .setCustomId("ac_skiptoday")
                                .setDisplayName(GREEN + "Skip au prochain jour")),
                        (holder, event) -> {
                            event.getWhoClicked().sendMessage(GREEN + "Vous êtes passé à la prochaine journée");
                            if (MainLg.getInstance().getCurrentGame() != null) {
                                MainLg.getInstance().getCurrentGame().broadcastMessage(DARK_GREEN + "" + BOLD + "Le passage à la prochaine journée a été forcé !", true);
                                MainLg.getInstance().getCurrentGame().cancelWait();
                                for (LGPlayer lgp : MainLg.getInstance().getCurrentGame().getInGame())
                                    lgp.stopChoosing();
                                MainLg.getInstance().getCurrentGame().endNight();
                            }
                        });

                setSlot(1, 3,
                        new Slot(ItemBuilder.make(Material.CLOCK)
                                .setCustomId("ac_skiptonight")
                                .setDisplayName(GREEN + "Skip à la prochaine nuit")),
                        (holder, event) -> {
                            event.getWhoClicked().sendMessage(GREEN + "Vous êtes passé à la prochaine nuit");
                            if (MainLg.getInstance().getCurrentGame() != null) {
                                MainLg.getInstance().getCurrentGame().broadcastMessage(DARK_GREEN + "" + BOLD + "Le passage à la prochaine nuit a été forcé !", true);
                                for (LGPlayer lgp : MainLg.getInstance().getCurrentGame().getInGame())
                                    lgp.stopChoosing();
                                MainLg.getInstance().getCurrentGame().cancelWait();
                                MainLg.getInstance().getCurrentGame().nextNight();
                            }
                        });

                setSlot(7, 2,
                        new Slot(ItemBuilder.make(Material.SUGAR)
                                .setCustomId("ac_reloadresourcepack")
                                .setDisplayName(DARK_GREEN + "Recharge le resource pack")
                                .setLore(GRAY + "pour tout le monde")),
                        (holder, event) -> {
                            for (Player p : Bukkit.getOnlinePlayers())
                                Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, "reloadPacks"));
                            for (Player p : Bukkit.getOnlinePlayers())
                                Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, "reloadPacks"));
                        });

                setSlot(7, 3,
                        new Slot(ItemBuilder.make(Material.PAPER)
                                .setCustomId("ac_reloadconfig")
                                .setDisplayName(DARK_GREEN + "Recharge les config")
                                .setLore(GRAY + "A faire si la compo est modifier par le fichier", RESET + "" + DARK_GRAY + "Autant dire jamais")),
                        (holder, event) -> {
                            event.getWhoClicked().sendMessage(GREEN + "Vous avez bien reload la config !");
                            event.getWhoClicked().sendMessage(GRAY + "" + ITALIC + "Si vous avez changé les rôles, écriver " + DARK_GRAY + "" + ITALIC + "/lg joinall" + GRAY + "" + ITALIC + " !");
                            MainLg.getInstance().loadMaxPlayers();
                        });


                setSlot(4, 4,
                        new Slot(ItemBuilder.make(Material.LIME_STAINED_GLASS)
                                .setCustomId("ac_gamestartstop")
                                .setDisplayName(DARK_GREEN + "Démarrer la Game"))
                        {
                            @Override
                            protected ItemBuilder getItem(LGInventoryHolder holder) {
                                ItemBuilder builder = getDefaultItem();
                                if(MainLg.getInstance().getCurrentGame().isStarted())
                                {
                                    builder.setType(Material.REDSTONE_BLOCK)
                                            .setDisplayName(DARK_RED + "Arrêter la Game");
                                }
                                return builder;
                            }
                        },
                        (holder, event) -> {
                            if(MainLg.getInstance().getCurrentGame().isStarted())
                            {
                                LGGame game = MainLg.getInstance().getCurrentGame();
                                game.cancelWait();
                                game.endGame(LGWinType.EQUAL);
                                game.broadcastMessage("§cLa partie a été arrêtée de force !", true);
                            }
                            else
                            {
                                event.getWhoClicked().sendMessage("§aVous avez démarré une nouvelle partie !");
                                MainLg.getInstance().getCurrentGame().updateStart();
                                try
                                {
                                    getInventory().getViewers().forEach(HumanEntity::closeInventory);
                                }
                                catch (Exception ignored) {}
                            }
                        });
            }

            @Override
            public boolean autoUpdate() {
                return true;
            }
        });
        savePreset("editCompo", new PaginationPreset(this) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        int roleAmount = roles.stream()
                                .reduce(0, (total, role) -> total + MainLg.getInstance().getConfig().getInt("role." + role, 0), Integer::sum);
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                .setLore(AQUA + "" + BOLD + "Click Gauche " + RESET + ":" + GRAY + " Ajoute le rôle",
                                        AQUA + "" + BOLD + "Click Droit   " + RESET + ":" + GRAY + " Retire le rôle",
                                        " ",
                                        RESET +""+ GRAY + "Il y a " + GOLD + roleAmount + GRAY + " rôle" + (roleAmount > 1 ? "s" : ""));
                    }
                };
            }

            @Override
            protected void preset() {
                setSlot(0, getMaxLine()-1, new Slot(baseBackButton()
                        .setDisplayName(RED + "Retour")
                        .setLore(WHITE + "Si les rôles on étais changé ou simaintient " + GRAY + "Shift",
                                WHITE + "pour faire " + GRAY + "" + UNDERLINE + "joinall" + WHITE + " en même temps que revenir à la page d'accueil")),
                        (holder, event) -> {
                            getCache().remove("pageIndex");
                            loadPreset("default");
                            if(event.isShiftClick() || getCache().get("roleChanged", false))
                            {
                                getCache().remove("roleChanged");
                                for (Player p : Bukkit.getOnlinePlayers())
                                    Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, "joinall"));
                                for (Player p : Bukkit.getOnlinePlayers())
                                    Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, "joinall"));
                            }
                        });

                roles.forEach(role -> {
                    Class<? extends Role> clazz = MainLg.getInstance().getRoles().get(role);

                    List<String> description = new ArrayList<>();
                    String[] words = Role.getDescription(clazz).split(" ");
                    StringBuilder line = new StringBuilder();
                    int wordCounter = 0;
                    for(String word : words)
                    {
                        if(wordCounter > 0)
                            line.append(" ");
                        line.append(word);
                        wordCounter++;

                        if(wordCounter >= 6 || line.toString().endsWith(".")){
                            description.add(line.toString());
                            line = new StringBuilder(WHITE.toString());
                            wordCounter = 0;
                        }
                    }

                    registerItem(new Slot(new ItemBuilder()
                                         .setCustomId("ac_role_" + role)
                                         .setLore(description.toArray(new String[0])))
                                 {
                                     @Override
                                     protected ItemBuilder getItem(LGInventoryHolder holder) {
                                         int count = MainLg.getInstance().getConfig().getInt("role." + role, 0);
                                         return getDefaultItem()
                                                 .setType(count > 0 ? Material.LIME_STAINED_GLASS : Material.REDSTONE_BLOCK)
                                                 .setDisplayName((count > 0 ? GREEN : RED) +""+ count + " " + Role.getScoreBoardName(clazz))
                                                 .setAmount(count);
                                     }
                                 },
                            (holder, event) -> {
                                int newCount = MainLg.getInstance().getConfig().getInt("role." + role, 0);

                                if(event.isLeftClick()) newCount++;
                                if(event.isRightClick()) newCount--;

                                newCount = Math.max(newCount, 0);

                                getCache().set("roleChanged", true);
                                MainLg.getInstance().getConfig().set("role." + role, newCount);
                                MainLg.getInstance().saveConfig();
                                MainLg.getInstance().loadMaxPlayers();
                                MainLg.makeNewGame();
                                apply();
                            });
                });
            }
        });
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if(!event.getWhoClicked().hasPermission("loupgarou.admin"))
        {
            event.getWhoClicked().sendMessage("§4Erreur: Vous n'avez pas la permission...");
            return;
        }
        super.onClick(event);
    }
}
