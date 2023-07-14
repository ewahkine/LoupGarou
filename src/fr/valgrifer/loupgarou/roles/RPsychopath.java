package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPsychopath extends Role {

    private static final String PsychopathInventoryKey = "psychopath_inventory";
    private static final String PsychopathPlayerSelectedKey = "psychopath_player_selected";

    public static final String PsychopathItemId = "psychopath";
    public static final ItemStack PsychopathItem = ItemBuilder.make(Material.BOOK)
            .setCustomId(PsychopathItemId)
            .setDisplayName(RESET + "Deviner & Tuer")
            .setLore(GRAY + "Durant le jour",
                    GRAY + "tu peux deviner le role d'un joueur",
                    GRAY + "si tu as raison il meurt sur le coup",
                    GRAY + "mais si tu as tort tu mourras.")
            .build();

    private static LGPrivateInventoryHolder makeInventory(LGPlayer player)
    {
        LGPrivateInventoryHolder inventoryHolder = new LGPrivateInventoryHolder(6, _getName(), player);

        PaginationMapPreset<LGPlayer> playerPreset = new PaginationMapPreset<LGPlayer>(inventoryHolder) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                .setLore(AQUA + BOLD + "Click " + GRAY + "pour sélectionner le joueur");
                    }
                };
            }

            @Override
            protected ItemBuilder mapList(LGPlayer player) {
                return ItemBuilder.make(Material.PLAYER_HEAD)
                        .setCustomId(player.getPlayer().getUniqueId().toString())
                        .setDisplayName(RESET + player.getName())
                        .setSkull(player.getPlayer());
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, LGPlayer player) {
                holder.getCache().set(PsychopathPlayerSelectedKey, player);
                holder.loadPreset("role");
            }

            @Override
            protected void preset() {}
        };

        PaginationMapPreset<Role> rolePreset = new PaginationMapPreset<Role>(inventoryHolder) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                .setLore(AQUA + BOLD + "Click " + GRAY + "pour sélectionner le role du joueur");
                    }
                };
            }

            @Override
            protected ItemBuilder mapList(Role role) {
                return Role.getCard(role.getClass());
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, Role role) {
                if(!(holder instanceof LGPrivateInventoryHolder))
                    return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if(!(lgp.getRole() instanceof RPsychopath))
                    return;

                RPsychopath lgr = (RPsychopath) lgp.getRole();

                LGPlayer target = holder.getCache().get(PsychopathPlayerSelectedKey);

                LGRoleActionEvent guessTargetEvent = new LGRoleActionEvent(lgp.getGame(), new PsychopathTargetAction(target, role), lgp);
                Bukkit.getPluginManager().callEvent(guessTargetEvent);

                PsychopathTargetAction action = (PsychopathTargetAction) guessTargetEvent.getAction();

                if(action.isForceConsume())
                    lgp.getPlayer().getInventory().setItem(8, null);

                if(action.isCancelled())
                {
                    lgp.getPlayer().closeInventory();
                    return;
                }


                LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(
                        lgr.getGame(),
                        action.isGoodGuess() ? action.getTarget() : lgp,
                        action.isGoodGuess() ? PSYCHOPATH_GOOD : PSYCHOPATH_BAD);
                Bukkit.getPluginManager().callEvent(killEvent);

                if(!killEvent.isCancelled())
                {
                    lgp.getGame().kill(target, killEvent.getReason(), true);

                    lgr.vote.getParticipants().remove(killEvent.getKilled());
                    lgr.vote.getVotes().remove(killEvent.getKilled());
                    killEvent.getKilled().getPlayer().closeInventory();
                }

                lgp.getPlayer().getInventory().setItem(8, null);
                lgp.getPlayer().closeInventory();
            }

            @Override
            protected void preset() {}
        };

        inventoryHolder.savePreset("player", playerPreset);
        inventoryHolder.savePreset("role", rolePreset);

        playerPreset.setObjectList(player.getGame().getAlive().stream()
                .filter(lgp -> lgp != player)
                .collect(Collectors.toCollection(ArrayList::new)));

        rolePreset.setObjectList(new ArrayList<>(playerPreset.getObjectList().stream()
                .map(LGPlayer::getRole)
                .collect(Collectors.toCollection(HashSet::new))));

        return inventoryHolder;
    }


    public static final LGPlayerKilledEvent.Reason PSYCHOPATH_GOOD = LGPlayerKilledEvent.Reason.register("PSYCHOPATH_GOOD",  GRAY+BOLD+"%s"+DARK_RED+" est mort d'une cause in..connue??");
    public static final LGPlayerKilledEvent.Reason PSYCHOPATH_BAD = LGPlayerKilledEvent.Reason.register("PSYCHOPATH_BAD",  PSYCHOPATH_GOOD.getMessage());

    public static final LGWinType PSYCHOPATH = LGWinType.register("PSYCHOPATH", GOLD+BOLD+ITALIC+"La partie a été gagnée par le "+_getName()+GOLD+BOLD+ITALIC+" !");

    public RPsychopath(LGGame game) {
        super(game);
    }
    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }
    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }
    public static String _getName() {
        return LIGHT_PURPLE+BOLD+"Psychopathe";
    }
    public static String _getFriendlyName() {
        return "le "+_getName();
    }
    public static String _getShortDescription() {
        return WHITE+"Tu gagnes "+RoleWinType.SOLO.getColoredName(BOLD);
    }
    public static String _getDescription() {
        return _getShortDescription()+WHITE+". Durant le jour, tu peux deviner le role d'un joueur, si tu as raison il meurt sur le coup, mais si tu as tort tu mourras.";
    }
    public static String _getTask() {
        return "";
    }
    public static String _getBroadcastedTask() {
        return "";
    }



    @EventHandler
    public void onDay(LGDayStartEvent event) {
        if(event.getGame() != getGame())
            return;

        getPlayers().forEach(player -> player.getPlayer().getInventory().setItem(8, PsychopathItem));
    }

    @EventHandler
    public void onNight(LGDayEndEvent event) {
        if(event.getGame() != getGame())
            return;

        getPlayers().forEach(player -> {
            player.getPlayer().getInventory().setItem(8, null);
            player.getPlayer().closeInventory();
        });
    }

    @SuppressWarnings("rawtypes")
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if(event.getHand() != EquipmentSlot.HAND)
            return;

        if(event.getAction() != Action.RIGHT_CLICK_AIR || event.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        LGPlayer player = LGPlayer.thePlayer(event.getPlayer());

        if(player.getGame() != getGame())
            return;

        if(!ItemBuilder.checkId(event.getItem(), PsychopathItemId))
            return;

        event.setCancelled(true);

        LGInventoryHolder inventoryHolder = player.getCache().get(PsychopathInventoryKey);

        if (inventoryHolder == null)
            player.getCache().set(PsychopathInventoryKey, inventoryHolder = makeInventory(player));


        ((PaginationMapPreset) inventoryHolder.getPreset("player"))
                .setObjectList(player.getGame().getAlive().stream()
                    .filter(lgp -> lgp != player)
                    .collect(Collectors.toCollection(ArrayList::new)));

        inventoryHolder.loadPreset("player");
        inventoryHolder.getCache().remove(PsychopathPlayerSelectedKey);

        player.getPlayer().openInventory(inventoryHolder.getInventory());
    }

    @EventHandler
    public void onDeathAnnouncement(LGDeathAnnouncementEvent e) {
        if(e.getGame() == getGame() && (e.getReason() == PSYCHOPATH_GOOD || e.getReason() == PSYCHOPATH_BAD || (e.getKilled().getRole() == this && getGame().getRole(RPriestess.class) != null)))
            e.setShowedRole(HiddenRole.class);
    }

    private LGVote vote = null;

    @EventHandler
    public void onVoteStart(LGVoteStartEvent e)
    {
        if(e.getGame() != getGame())
            return;

        if(e.getCause() != LGVoteCause.VILLAGE)
            return;

        vote = e.getVote();
    }

    @EventHandler
    public void onVoteEnd(LGVoteEndEvent e)
    {
        if(e.getGame() != getGame())
            return;

        vote = null;
    }


    @EventHandler
    public void onEndgameCheck(LGEndCheckEvent e) {
        if(e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO && getPlayers().size() > 0)
            e.setWinType(PSYCHOPATH);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndGame(LGGameEndEvent e) {
        if(e.getWinType() == PSYCHOPATH) {
            e.getWinners().clear();
            e.getWinners().addAll(getPlayers());
        }
    }

    public static class PsychopathTargetAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, AbilityConsume
    {
        public PsychopathTargetAction(LGPlayer target, Role role)
        {
            this.target = target;
            this.role = role;
        }

        @Getter @Setter private boolean cancelled;
        @Getter @Setter private boolean forceConsume;
        @Getter @Setter private LGPlayer target;
        @Getter @Setter private Role role;

        public boolean isGoodGuess()
        {
            return this.target.getRole() == this.role;
        }
    }
}
