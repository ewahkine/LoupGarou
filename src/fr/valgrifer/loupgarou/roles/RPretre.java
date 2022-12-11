package fr.valgrifer.loupgarou.roles;

import java.util.*;

import com.comphenix.packetwrapper.WrapperPlayServerEntityEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import static org.bukkit.ChatColor.*;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGCustomItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPreDayStartEvent;

public class RPretre extends Role{
    private static final ItemBuilder itemNoAction = ItemBuilder.make(Material.IRON_NUGGET)
            .setCustomId("ac_no")
            .setDisplayName(GRAY+""+BOLD+"Ne rien faire")
            .setLore(DARK_GRAY+"Passez votre tour");
    private static final Map<LGPlayer, RPretre> playerToRole = new HashMap<>();
    private static final LGInventoryHolder invHolder;
	static {
        invHolder = new LGInventoryHolder(1, BLACK+"Veux-tu réssusciter quelqu'un ?");
        invHolder.setDefaultPreset(new MenuPreset(invHolder) {
            @Override
            protected void preset() {
                setSlot(3, new Slot(itemNoAction),
                        ((holder, event) -> {
                            LGPlayer lgp = LGPlayer.thePlayer((Player)event.getWhoClicked());
                            RPretre role = playerToRole.get(lgp);

                            if(role == null)
                                return;

                            role.closeInventory(lgp);
                            lgp.sendMessage(GOLD+"Tu n'as rien fait cette nuit.");
                            role.hidePlayers(lgp);
                            lgp.hideView();
                            role.callback.run();
                        }));
                setSlot(5, new Slot(ItemBuilder.make(Material.ROTTEN_FLESH)
                                .setCustomId("ac_no")
                                .setDisplayName(DARK_GREEN+""+BOLD+"Ressuciter")
                                .setLore(DARK_GRAY+"Tu peux ressusciter un "+RoleWinType.VILLAGE.getColoredName(BOLD),
                                        DARK_GRAY+"mort précédemment pendant la partie.")),
                        ((holder, event) -> {
                            Player player = (Player)event.getWhoClicked();
                            LGPlayer lgp = LGPlayer.thePlayer(player);
                            RPretre role = playerToRole.get(lgp);

                            if(role == null)
                                return;

                            role.closeInventory(lgp);
                            player.getInventory().setItem(8, itemNoAction.build());
                            player.updateInventory();
                            //Pour éviter les missclick
                            WrapperPlayServerHeldItemSlot held = new WrapperPlayServerHeldItemSlot();
                            held.setSlot(0);
                            held.sendPacket(player);
                            lgp.sendMessage(GOLD+"Choisissez qui réssusciter.");
                            lgp.canSelectDead = true;
                            lgp.choose(choosen -> {
                                if(choosen == null)
                                    return;

                                if(!choosen.isDead())
                                {
                                    lgp.sendMessage(GRAY + "" + BOLD + choosen.getName() + "" + RED + " n'est pas mort.");
                                    return;
                                }
                                else if(lgp.getRoleType() == RoleType.LOUP_GAROU && choosen.getRoleType() == RoleType.NEUTRAL)
                                {
                                    lgp.sendMessage(GRAY + "" + BOLD + choosen.getName() + "" + RED + " ne faisait ni partie du " + GREEN + "" + BOLD + "Village" + GOLD + " ni des " + RED + "" + BOLD + "Loups" + GOLD + ".");
                                    return;
                                }
                                else if(lgp.getRoleType() != RoleType.LOUP_GAROU && choosen.getRoleType() != RoleType.VILLAGER)
                                {
                                    lgp.sendMessage(GRAY + "" + BOLD + choosen.getName() + "" + RED + " ne faisait pas partie du " + GREEN + "" + BOLD + "Village" + GOLD + ".");
                                    return;
                                }

                                player.getInventory().setItem(8, null);
                                player.updateInventory();
                                lgp.stopChoosing();
                                lgp.canSelectDead = false;
                                lgp.sendMessage(GOLD+"Tu as ramené "+GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" à la vie.");
                                lgp.sendActionBarMessage(GRAY+""+BOLD+""+choosen.getName()+""+GOLD+" sera réssuscité");


                                role.ressucited.add(choosen);
                                role.getPlayers().remove(lgp);//Pour éviter qu'il puisse sauver plusieurs personnes.
                                choosen.sendMessage(GOLD+"Tu vas être réssuscité en tant que "+GREEN+""+BOLD+"Villageois"+GOLD+".");
                                role.hidePlayers(lgp);
                                lgp.hideView();
                                role.callback.run();
                            }, lgp);
                        }));
            }
        });
	}

	public RPretre(LGGame game) {
		super(game);
	}

	public static String _getName() {
		return GREEN+""+BOLD+"Prêtre";
	}

	public static String _getFriendlyName() {
		return "du "+_getName();
	}

	public static String _getShortDescription() {
		return RVillageois._getShortDescription();
	}

	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Une fois dans la partie, tu peux ressusciter parmi les morts un membre du "+RoleWinType.VILLAGE.getColoredName(BOLD)+WHITE+", qui reviendra à la vie sans ses pouvoirs.";
	}

	public static String _getTask() {
		return "Veux-tu ressusciter un allié défunt ?";
	}

	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" récite ses ouvrages...";
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}

	@Override
	public int getTimeout() {
		return 30;
	}
	@Override
	public boolean hasPlayersLeft() {
		for(LGPlayer pretre : getPlayers())
			for(LGPlayer lgp : getGame().getInGame())
				if(lgp.isDead() && (lgp.getRoleType() == RoleType.VILLAGER || lgp.getRoleType() == pretre.getRoleType()))
					return super.hasPlayersLeft();
		return false;
	}
	
	Runnable callback;

    public void openInventory(LGPlayer player) {
        inMenu.put(player, true);
        playerToRole.put(player, this);
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }
    private boolean listenerRegistered = false;
    private final WrappedDataWatcherObject invisible = new WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
    private final PotionEffect invisibleEffect = new PotionEffect(PotionEffectType.INVISIBILITY, 0, 0, false, false, false);
    private final PacketListener metadataListener = new PacketAdapter(MainLg.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
        @Override
        public void onPacketSending(PacketEvent e) {
            WrapperPlayServerEntityMetadata event = new WrapperPlayServerEntityMetadata(e.getPacket());
            LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
            if(player.getGame() != getGame())
                return;

            for(LGPlayer lgp : getGame().getInGame())
                if (lgp.getPlayer().getEntityId() == event.getEntityID() && (!lgp.isDead() || (lgp.getRoleType() != RoleType.VILLAGER && lgp.getRoleType() != player.getRoleType())))
                    return;

            event.setMetadata(Collections.singletonList(new WrappedWatchableObject(invisible, (byte) 0)));
        }
    };
    private final PacketListener effectListener = new PacketAdapter(MainLg.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EFFECT) {
        @Override
        public void onPacketSending(PacketEvent e) {
            WrapperPlayServerEntityEffect event = new WrapperPlayServerEntityEffect(e.getPacket());
            LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
            if(player.getGame() != getGame())
                return;

            for(LGPlayer lgp : getGame().getInGame())
                if (lgp.getPlayer().getEntityId() == event.getEntityID() && (!lgp.isDead() || (lgp.getRoleType() != RoleType.VILLAGER && lgp.getRoleType() != player.getRoleType())))
                    return;

            event.setPotionEffect(invisibleEffect);
        }
    };
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
        if(!listenerRegistered)
        {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(metadataListener);
            protocolManager.addPacketListener(effectListener);

            listenerRegistered = true;
        }

		player.showView();
		for(LGPlayer lgp : getGame().getInGame())
        {
            if (!lgp.isDead() || (lgp.getRoleType() != RoleType.VILLAGER && lgp.getRoleType() != player.getRoleType()))
            {
                player.getPlayer().hidePlayer(MainLg.getInstance(), lgp.getPlayer());
                continue;
            }

            if (lgp.getPlayer() != null)
                continue;

            player.getPlayer().showPlayer(MainLg.getInstance(), lgp.getPlayer());
            WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
            meta.setEntityID(lgp.getPlayer().getEntityId());
            meta.setMetadata(Collections.singletonList(new WrappedWatchableObject(invisible, (byte) 0)));
            meta.sendPacket(player.getPlayer());
            WrapperPlayServerEntityEffect effect = new WrapperPlayServerEntityEffect();
            effect.setEntityID(lgp.getPlayer().getEntityId());
            effect.setPotionEffect(invisibleEffect);
            effect.sendPacket(player.getPlayer());
        }
		this.callback = callback;
		openInventory(player);
	}
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
        if(listenerRegistered)
        {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.removePacketListener(metadataListener);
            protocolManager.removePacketListener(effectListener);

            listenerRegistered = false;
        }

		player.getPlayer().getInventory().setItem(8, null);
		player.stopChoosing();
		closeInventory(player);
		player.canSelectDead = false;
		player.getPlayer().updateInventory();
		hidePlayers(player);
		player.sendMessage(GOLD+"Tu n'as rien fait cette nuit.");
	}

	private void hidePlayers(LGPlayer player) {
		if(player.getPlayer() != null) {
			for(LGPlayer lgp : getGame().getInGame())
				if(lgp.getPlayer() != null && lgp != player)
					player.getPlayer().hidePlayer(MainLg.getInstance(), lgp.getPlayer());
		}
	}

    private final Map<LGPlayer, Boolean> inMenu = new HashMap<>();
	List<LGPlayer> ressucited = new ArrayList<>();

    private void closeInventory(LGPlayer player) {
        inMenu.remove(player);
        playerToRole.remove(player);
        player.getPlayer().closeInventory();
    }
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		LGPlayer lgp = LGPlayer.thePlayer(player);

        if(lgp.getRole() != this || !ItemBuilder.checkId(e.getItem(), itemNoAction.getCustomId()))
            return;

        e.setCancelled(true);
        player.getInventory().setItem(8, null);
        player.updateInventory();
        lgp.stopChoosing();
        lgp.sendMessage(GOLD+"Tu n'as rien fait cette nuit.");
        lgp.canSelectDead = false;
        hidePlayers(lgp);
        callback.run();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDayStart(LGPreDayStartEvent e) {
		if(e.getGame() != getGame())
            return;

        if(ressucited.size() == 0)
            return;

        for(LGPlayer lgp : ressucited) {
            if(lgp.getPlayer() == null || !lgp.isDead())
                continue;
            lgp.setDead(false);
            lgp.getCache().reset();
            RVillageois villagers = getGame().getRole(RVillageois.class);
            if(villagers == null)
                getGame().getRoles().add(villagers = new RVillageois(getGame()));
            villagers.join(lgp, false);//Le joueur réssuscité rejoint les villageois.
            lgp.setRole(villagers);
            lgp.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            lgp.getPlayer().getInventory().setHelmet(null);
            lgp.getPlayer().updateInventory();
            LGCustomItems.updateItem(lgp);

            lgp.joinChat(getGame().getDayChat());//Pour qu'il ne parle plus dans le chat des morts (et ne le voit plus) et qu'il parle dans le chat des vivants
            VariousUtils.setWarning(lgp.getPlayer(), true);

            getGame().updateRoleScoreboard();

            getGame().broadcastMessage(GRAY+""+BOLD+lgp.getName()+GOLD+" a été ressuscité cette nuit.", true);

            for(LGPlayer player : getGame().getInGame())
                if(player.getPlayer() != null && player != lgp)
                    player.getPlayer().showPlayer(MainLg.getInstance(), lgp.getPlayer());
        }
        ressucited.clear();
	}
    @EventHandler
    public void onQuitInventory(InventoryCloseEvent e) {
        LGPlayer player;
        if(!(e.getInventory().getHolder() instanceof LGInventoryHolder) ||
                !e.getInventory().getHolder().equals(invHolder) ||
                (player = LGPlayer.thePlayer((Player)e.getPlayer())).getRole() != this ||
                !inMenu.getOrDefault(player, false))
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(e.getInventory());
            }
        }.runTaskLater(MainLg.getInstance(), 1);
    }
	
}
