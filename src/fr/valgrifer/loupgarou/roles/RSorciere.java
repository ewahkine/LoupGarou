package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.events.AbilityConsume;
import fr.valgrifer.loupgarou.events.MessageForcable;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import static org.bukkit.ChatColor.*;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class RSorciere extends Role{
    public static final ItemBuilder itemBack = ItemBuilder
            .make(Material.IRON_NUGGET)
            .setCustomId("ac_back")
            .setDisplayName(BOLD+"Revenir au choix des potions");

    private static final MenuPreset preset = new MenuPreset(null, null) {
        @Override
        protected void preset() {
            setSlot(0, new MenuPreset.Slot(ItemBuilder
                    .make(Material.PURPLE_DYE)
                    .setCustomId("ac_life")
                    .setDisplayName(GREEN+""+BOLD+"Potion de vie")
                    .setLore(DARK_GREEN+"Sauve la cible des "+RED+""+BOLD+"Loups"+DARK_GREEN+".")) {
                @Override
                protected ItemBuilder getItem(LGInventoryHolder holder) {
                    if(!(holder instanceof LGPrivateInventoryHolder))
                        return getDefaultItem().setType(Material.AIR);

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    if(((RSorciere) lgp.getRole()).sauver == null || lgp.getCache().getBoolean("witch_used_life"))
                        return getDefaultItem().setType(Material.AIR);

                    return getDefaultItem();
                }
            }, (holder, event) -> {
                if(!(holder instanceof LGPrivateInventoryHolder))
                    return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if(!(lgp.getRole() instanceof RSorciere))
                    return;

                RSorciere role = (RSorciere) lgp.getRole();

                role.closeInventory(lgp);
                role.saveLife(lgp);
            });

            setSlot(1, new MenuPreset.Slot(ItemBuilder
                    .make(Material.IRON_NUGGET)
                    .setCustomId("ac_skip")
                    .setDisplayName(GRAY+""+BOLD+"Ne rien faire")
                    .setLore(DARK_GRAY+"Passez votre tour")), (holder, event) -> {
                if(!(holder instanceof LGPrivateInventoryHolder))
                    return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if(!(lgp.getRole() instanceof RSorciere))
                    return;

                RSorciere role = (RSorciere) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(GOLD+"Tu n'as rien fait cette nuit.");
                lgp.hideView();
                role.callback.run();
            });

            setSlot(2, new MenuPreset.Slot(ItemBuilder
                    .make(Material.ROTTEN_FLESH)
                    .setCustomId("ac_kill")
                    .setDisplayName(RED+""+BOLD+"Potion de mort")
                    .setLore(RED+"Tue la personne de ton choix.")) {
                @Override
                protected ItemBuilder getItem(LGInventoryHolder holder) {
                    if(!(holder instanceof LGPrivateInventoryHolder))
                        return getDefaultItem().setType(Material.AIR);

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    if(lgp.getCache().getBoolean("witch_used_death"))
                        return getDefaultItem().setType(Material.AIR);

                    return getDefaultItem();
                }
            }, (holder, event) -> {
                if(!(holder instanceof LGPrivateInventoryHolder))
                    return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();
                Player player = lgp.getPlayer();

                if(!(lgp.getRole() instanceof RSorciere))
                    return;

                RSorciere role = (RSorciere) lgp.getRole();

                //On le met sur le slot 0 pour éviter un missclick sur la croix
                WrapperPlayServerHeldItemSlot hold = new WrapperPlayServerHeldItemSlot();
                hold.setSlot(0);
                hold.sendPacket(lgp.getPlayer());

                role.closeInventory(lgp);
                lgp.choose((choosen) -> {
                    if (choosen != null) {
                        lgp.stopChoosing();
                        role.kill(choosen, lgp);
                    }
                }/*, sauver*/);//On peut tuer la personne qui a été tué par les loups (bien que cela ne serve à rien)

                player.getInventory().setItem(8, itemBack.build());
                player.updateInventory();
            });

            setSlot(4, new MenuPreset.Slot(ItemBuilder
                    .make(Material.ARROW)){
                @Override
                protected ItemBuilder getItem(LGInventoryHolder holder) {
                    if(!(holder instanceof LGPrivateInventoryHolder))
                        return getDefaultItem().setType(Material.AIR);

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    if(((RSorciere) lgp.getRole()).sauver == null)
                        return getDefaultItem().setType(Material.AIR);

                    return getDefaultItem()
                            .setDisplayName(GRAY+""+BOLD+""+((RSorciere) lgp.getRole()).sauver.getName()+""+RED+" est ciblé");
                }
            });
        }
    };
	
	
	public RSorciere(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Sorcière";
	}
	public static String _getFriendlyName() {
		return "de la "+_getName();
	}
	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Tu disposes de deux potions : une "+YELLOW+""+ITALIC+""+BOLD+"potion de vie"+WHITE+" pour sauver la victime des "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+", et une "+YELLOW+""+ITALIC+""+BOLD+"potion de mort"+WHITE+" pour assassiner quelqu'un.";
	}
	public static String _getTask() {
		return "Que veux-tu faire cette nuit ?";
	}
	public static String _getBroadcastedTask() {
		return "La "+_getName()+""+BLUE+" est en train de concocter un nouvel élixir.";
	}
	@Override
	public int getTimeout() {
		return 30;
	}
	
	private LGPlayer sauver;
	private Runnable callback;
	
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		player.showView();
		this.callback = callback;
		sauver = getGame().getDeaths().get(Reason.LOUP_GAROU);
		if(sauver == null)
			sauver = getGame().getDeaths().get(Reason.DONT_DIE);
		
		openInventory(player);
	}
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.getPlayer().getInventory().setItem(8, null);
		player.stopChoosing();
		closeInventory(player);
		player.getPlayer().updateInventory();
		player.hideView();
	}

    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;

	private void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(InventoryType.BREWING, BLACK + (sauver == null ? "Personne n'a été ciblé" : BOLD+""+sauver.getName()+" "+BLACK+"est ciblé"), player);
        invHolder.setDefaultPreset(preset.clone(invHolder));

        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
	}

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        invHolder = null;
        player.getPlayer().closeInventory();
    }
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        LGPlayer lgp = LGPlayer.thePlayer(player);

        if(lgp.getRole() != this || !ItemBuilder.checkId(e.getItem(), itemBack.getCustomId()))
            return;

        e.setCancelled(true);

        lgp.stopChoosing();
        player.getInventory().setItem(8, null);
        player.updateInventory();

        openInventory(lgp);
    }
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        LGPlayer player;
        if(event.getClickedInventory() == null ||
                event.getClickedInventory().getType() != InventoryType.BREWING ||
                (player = LGPlayer.thePlayer((Player)event.getWhoClicked())).getGame() == null ||
                !player.getGame().isStarted() ||
                player.getRole() != this ||
                player.isDead() ||
                !inMenu ||
                invHolder == null)
            return;

        invHolder.onClick(event);
    }
    @EventHandler
    public void onQuitInventory(InventoryCloseEvent e) {
        LGPlayer player;
        if(e.getInventory().getType() != InventoryType.BREWING ||
                (player = LGPlayer.thePlayer((Player)e.getPlayer())).getGame() == null ||
                !player.getGame().isStarted() ||
                player.getRole() != this ||
                player.isDead() ||
                !inMenu ||
                invHolder == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(e.getInventory());
            }
        }.runTaskLater(MainLg.getInstance(), 1);
    }
	
	private void kill(LGPlayer choosen, LGPlayer player) {
        player.hideView();
		player.getPlayer().getInventory().setItem(8, null);
		player.getPlayer().updateInventory();

        LGRoleActionEvent e = new LGRoleActionEvent(getGame(), new KillAction(sauver), player);
        Bukkit.getPluginManager().callEvent(e);
        KillAction action = (KillAction) e.getAction();
        if(!action.isCancelled() || action.isForceMessage())
        {
            player.sendMessage(GOLD+"Tu as décidé d'assassiner "+GRAY+""+BOLD+""+action.getTarget().getName()+""+GOLD+".");
            player.sendActionBarMessage(GRAY+""+BOLD+""+action.getTarget().getName()+""+BLUE+" a été tué.");
        }
        else
            player.sendMessage(RED+"Votre cible est immunisée.");

        if(!action.isCancelled() || action.isForceConsume())
            player.getCache().set("witch_used_death", true);

        if(action.isCancelled())
        {
            callback.run();
            return;
        }

        LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), action.getTarget(), Reason.SORCIERE);
        Bukkit.getPluginManager().callEvent(killEvent);
        if(killEvent.isCancelled())
        {
            callback.run();
            return;
        }

		getGame().kill(killEvent.getKilled(), Reason.SORCIERE);
		callback.run();
	}
	private void saveLife(LGPlayer player) {
        player.hideView();

        LGRoleActionEvent e = new LGRoleActionEvent(getGame(), new SaveAction(sauver), player);
        Bukkit.getPluginManager().callEvent(e);
        SaveAction action = (SaveAction) e.getAction();
        if(!action.isCancelled() || action.isForceMessage())
        {
            player.sendMessage(GOLD+"Tu as décidé de sauver "+GRAY+""+BOLD+""+action.getTarget().getName()+""+GOLD+".");
            player.sendActionBarMessage(GRAY+""+BOLD+""+action.getTarget().getName()+""+BLUE+" a été sauvé.");
        }
        else
            player.sendMessage(RED+"Votre cible est immunisée.");

        if(!action.isCancelled() || action.isForceConsume())
            player.getCache().set("witch_used_life", true);

        if(action.isCancelled())
        {
            callback.run();
            return;
        }

		getGame().getDeaths().remove(Reason.LOUP_GAROU, action.getTarget());
		callback.run();
	}

    public static class KillAction implements LGRoleActionEvent.RoleAction, Cancellable, MessageForcable, AbilityConsume
    {
        public KillAction(LGPlayer target)
        {
            this.target = target;
        }

        @Getter
        @Setter
        private boolean cancelled;
        @Getter @Setter private LGPlayer target;
        @Getter @Setter private boolean forceMessage;
        @Getter @Setter private boolean forceConsume;
    }
    public static class SaveAction implements LGRoleActionEvent.RoleAction, Cancellable, MessageForcable, AbilityConsume
    {
        public SaveAction(LGPlayer target)
        {
            this.target = target;
        }

        @Getter
        @Setter
        private boolean cancelled;
        @Getter @Setter private LGPlayer target;
        @Getter @Setter private boolean forceMessage;
        @Getter @Setter private boolean forceConsume;
    }
}
