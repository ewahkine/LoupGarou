package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.events.LGNightEndEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import static org.bukkit.ChatColor.*;

import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGCustomItems;
import fr.valgrifer.loupgarou.classes.LGCustomItems.LGCustomItemsConstraints;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGCustomItemChangeEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class RLoupGarouNoir extends Role{
    private static final MenuPreset preset = new MenuPreset(1) {
        @Override
        protected void preset() {
            setSlot(3, new Slot(ItemBuilder
                    .make(Material.IRON_NUGGET)
                    .setCustomId("ac_skip")
                    .setDisplayName(GRAY+""+BOLD+"Ne rien faire")
                    .setLore(DARK_GRAY+"Passez votre tour")), (holder, event) -> {
                if(!(holder instanceof LGPrivateInventoryHolder))
                    return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if(!(lgp.getRole() instanceof RLoupGarouNoir))
                    return;

                RLoupGarouNoir role = (RLoupGarouNoir) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(GOLD+"Tu n'as rien fait cette nuit.");
                lgp.hideView();
                role.callback.run();
            });

            setSlot(5, new Slot(ItemBuilder
                    .make(Material.ROTTEN_FLESH)
                    .setCustomId("ac_infect")
                    .setDisplayName(RED+""+BOLD+"Infecter")
                    .setLore(DARK_GRAY+"Tu peux infecter la cible du vote.",
                            DARK_GRAY+"Le joueur tiendra avec les Loups.")), (holder, event) -> {
                if(!(holder instanceof LGPrivateInventoryHolder))
                    return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if(!(lgp.getRole() instanceof RLoupGarouNoir))
                    return;

                RLoupGarouNoir role = (RLoupGarouNoir) lgp.getRole();

                role.closeInventory(lgp);

                lgp.getCache().set("has_infected", true);
                role.toInfect.getCache().set("infected", true);
                role.toInfect.addEndGameReaveal(RED+"Infecté");
                role.toInfect.setRoleType(RoleType.LOUP_GAROU);
                role.toInfect.setRoleWinType(RoleWinType.LOUP_GAROU);
                role.getPlayers().remove(lgp);
                role.toInfect.getCache().set("just_infected", true);
                lgp.sendActionBarMessage(BLUE+""+BOLD+"Vous infectez "+BLUE+""+role.toInfect.getName());
                lgp.sendMessage(GOLD+"Tu as infecté "+GRAY+""+BOLD+""+role.toInfect.getName()+""+GOLD+".");
                lgp.stopChoosing();
                role.getGame().getDeaths().remove(Reason.LOUP_GAROU, role.toInfect);
                lgp.hideView();
                role.callback.run();
            });
        }
    };

	public RLoupGarouNoir(LGGame game) {
		super(game);
	}

	public static String _getName() {
		return RED+""+BOLD+"Loup Noir";
	}

	public static String _getFriendlyName() {
		return "du "+_getName();
	}

	public static String _getShortDescription() {
		return RLoupGarou._getShortDescription();
	}

	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à éliminer... Une fois dans la partie, tu peux transformer la personne désignée en "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+". L'infecté conserve ses pouvoirs mais gagne désormais avec les "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+".";
	}

	public static String _getTask() {
		return "Veux-tu infecter la cible du vote ?";
	}

	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" décide s'il veut infecter sa cible.";
	}
	public static RoleType _getType() {
		return RoleType.LOUP_GAROU;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.LOUP_GAROU;
	}

	@Override
	public int getTimeout() {
		return 15;
	}
	
	@Override
	public boolean hasPlayersLeft() {
		return super.hasPlayersLeft() && getGame().getDeaths().containsKey(Reason.LOUP_GAROU);
	}

    Runnable callback;
	LGPlayer toInfect;
    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;
	
	public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK+"Infecter " + toInfect.getName() + " ?", player);
        invHolder.setDefaultPreset(preset.clone(invHolder));
		player.getPlayer().closeInventory();
		player.getPlayer().openInventory(invHolder.getInventory());
	}
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		toInfect = getGame().getDeaths().get(Reason.LOUP_GAROU);
        if(toInfect == null)
        {
            callback.run();
            return;
        }
		player.showView();
		this.callback = callback;
		openInventory(player);
	}
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.getPlayer().getInventory().setItem(8, null);
		player.stopChoosing();
		closeInventory(player);
		player.getPlayer().updateInventory();
		player.hideView();
		//player.sendTitle(RED+"Vous n'infectez personne", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
		player.sendMessage(GOLD+"Tu n'as rien fait cette nuit.");
	}
	
	private void closeInventory(LGPlayer player) {
        inMenu = false;
        invHolder = null;
        player.getPlayer().closeInventory();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDayStart(LGNightEndEvent e) {
		if(e.getGame() == getGame())
			for(LGPlayer player : getGame().getAlive()) {
				if(player.getCache().getBoolean("just_infected")) {
					player.getCache().remove("just_infected");
					player.sendMessage(GOLD+"Tu as été infecté pendant la nuit.");
					player.sendMessage(GOLD+""+ITALIC+"Tu gagnes désormais avec les "+RED+""+BOLD+""+ITALIC+"Loups-Garous"+GOLD+""+ITALIC+".");
                    if(!player.isDead()) {//Si il n'a pas été tué je ne sais comment
                        RLoupGarou.forceJoin(player);
                        player.getPlayer().getInventory().setItemInOffHand(new ItemStack(LGCustomItems.getItem(player)));
                    }
					
					for(LGPlayer lgp : getGame().getInGame()) {
						if(lgp.getRoleType() == RoleType.LOUP_GAROU)
							lgp.sendMessage(GRAY+""+BOLD+""+player.getName()+""+GOLD+" s'est fait infecter pendant la nuit.");
						else
							lgp.sendMessage(GOLD+"Un joueur a été "+RED+""+BOLD+"infecté"+GOLD+" pendant la nuit.");
					}
					
					if(getGame().checkEndGame())
						e.setCancelled(true);
				}
			}
	}
	
	@Override
	public void join(LGPlayer player, boolean sendMessage) {
		super.join(player, sendMessage);
        RLoupGarou.forceJoin(player);
	}

    @EventHandler
    public void onQuitInventory(InventoryCloseEvent e) {
        if(!(e.getInventory().getHolder() instanceof LGInventoryHolder) ||
                !e.getInventory().getHolder().equals(invHolder) ||
                LGPlayer.thePlayer((Player)e.getPlayer()).getRole() != this ||
                !inMenu)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(e.getInventory());
            }
        }.runTaskLater(MainLg.getInstance(), 1);
    }
	
	@EventHandler
	public void onCustomItemChange(LGCustomItemChangeEvent e) {
		if(e.getGame() == getGame())
			if(e.getPlayer().getCache().getBoolean("infected"))
				e.getConstraints().add(LGCustomItemsConstraints.INFECTED.getName());
	}
	
}