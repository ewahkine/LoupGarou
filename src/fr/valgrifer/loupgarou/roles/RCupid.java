package fr.valgrifer.loupgarou.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.events.LGUpdatePrefixEvent;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerEntityLook;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.LGEndCheckEvent;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGPlayerGotKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;

public class RCupid extends Role{
	public RCupid(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.NEUTRAL;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.COUPLE;
	}
	public static String _getName() {
		return LIGHT_PURPLE+""+BOLD+"Cupidon";
	}
	public static String _getFriendlyName() {
		return "de "+_getName();
	}
	public static String _getShortDescription() {
		return WHITE+"Tu gagnes avec le "+RoleWinType.COUPLE.getColoredName(BOLD);
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Dès le début de la partie, tu dois former un couple de deux joueurs. Leur objectif sera de survivre ensemble, car si l'un d'eux meurt, l'autre se suicidera.";
	}
	public static String _getTask() {
		return "Choisis deux joueurs à mettre en couple.";
	}
	public static String _getBroadcastedTask() {
		return _getName()+""+BLUE+" choisit deux âmes à unir.";
	}
	
	@Override
	public int getTimeout() {
		return 15;
	}
	@Override
	public boolean hasPlayersLeft() {
		return getGame().getNight() == 1;
	}

    private boolean forceCoupleWin = false;
	
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
		player.showView();
		
		player.choose(choosen -> {
            if(choosen == null)
                return;

            if(player.getCache().has("cupidon_first")) {
                LGPlayer first = player.getCache().remove("cupidon_first");
                if(first == choosen)
                {
                    int entityId = Integer.MAX_VALUE-choosen.getPlayer().getEntityId();
                    WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                    destroy.setEntityIds(new int[] {entityId});
                    destroy.sendPacket(player.getPlayer());
                    player.sendMessage(GRAY+""+BOLD+""+choosen.getName()+""+BLUE+" est désélectionné pour être amoureux .");
                }
                else
                {
                    //	sendHead(player, choosen);
                    int entityId = Integer.MAX_VALUE-first.getPlayer().getEntityId();
                    WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                    destroy.setEntityIds(new int[] {entityId});
                    destroy.sendPacket(player.getPlayer());

                    setInLove(first, choosen);
                    player.sendMessage(GRAY+""+BOLD+""+first.getName()+""+BLUE+" et "+GRAY+""+BOLD+""+choosen.getName()+""+BLUE+" sont désormais follement amoureux.");
                    player.stopChoosing();
                    player.hideView();
                    callback.run();
                }
            }
            else
            {
                sendHead(player, choosen);
                player.getCache().set("cupidon_first", choosen);
                player.sendMessage(GRAY+""+BOLD+""+choosen.getName()+""+BLUE+" est sélectionné pour être amoureux .");
            }
        }, player);
	}
	protected void setInLove(LGPlayer player1, LGPlayer player2)
    {
		player1.getCache().set("inlove", player2);
		player1.addEndGameReaveal(LIGHT_PURPLE+"\u2764");
        player1.sendMessage(BLUE+"Tu tombes amoureux de "+GRAY+""+BOLD+""+player2.getName()+""+BLUE+", il est "+player2.getRole().getName());
        player1.sendMessage(BLUE+""+ITALIC+"Tu peux lui parler en mettant un "+YELLOW+"!"+BLUE+" devant ton message.");
		
		player2.getCache().set("inlove", player1);
        player2.addEndGameReaveal(LIGHT_PURPLE+"\u2764");
        player2.sendMessage(BLUE+"Tu tombes amoureux de "+GRAY+""+BOLD+""+player1.getName()+""+BLUE+", il est "+player1.getRole().getName());
        player2.sendMessage(BLUE+""+ITALIC+"Tu peux lui parler en mettant un "+YELLOW+"!"+BLUE+" devant ton message.");

        if(forceCoupleWin)
        {
            player1.setRoleWinType(RoleWinType.COUPLE);
            player1.sendMessage(BLUE+""+ITALIC+"Vous devez gagné forcement avec votre couple et votre cupidon");
            player2.setRoleWinType(RoleWinType.COUPLE);
            player2.sendMessage(BLUE+""+ITALIC+"Vous devez gagné forcement avec votre couple et votre cupidon");
        }

		
		//On peut créer des cheats grâce à ça (qui permettent de savoir qui est en couple)
		player1.updatePrefix();
		player2.updatePrefix();
	}

	private final WrappedDataWatcherObject invisible = new WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)),
							 noGravity = new WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));
	protected void sendHead(LGPlayer to, LGPlayer ofWho) {
		int entityId = Integer.MAX_VALUE-ofWho.getPlayer().getEntityId();
		WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
		spawn.setEntityID(entityId);
		spawn.setType(EntityType.DROPPED_ITEM);
		//spawn.setMetadata(new WrappedDataWatcher(Arrays.asList(new WrappedWatchableObject(invisible, (byte)0x20), new WrappedWatchableObject(noGravity, true))));
		Location loc = ofWho.getPlayer().getLocation();
		spawn.setX(loc.getX());
		spawn.setY(loc.getY()+1.9);
		spawn.setZ(loc.getZ());
		spawn.setHeadPitch(0);
		Location toLoc = to.getPlayer().getLocation();
		double diffX = loc.getX()-toLoc.getX(),
			   diffZ = loc.getZ()-toLoc.getZ();
		float yaw = 180-((float) Math.toDegrees(Math.atan2(diffX, diffZ)));
		
		spawn.setYaw(yaw);
		spawn.sendPacket(to.getPlayer());
		
		WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
		look.setEntityID(entityId);
		look.setPitch(0);
		look.setYaw(yaw);
		look.sendPacket(to.getPlayer());
		
		WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
		meta.setEntityID(entityId);
		meta.setMetadata(Arrays.asList(new WrappedWatchableObject(invisible, (byte)0x20), new WrappedWatchableObject(noGravity, true)));
		meta.sendPacket(to.getPlayer());
		
		
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment();
				equip.setEntityID(entityId);
				equip.setItem(ItemSlot.HEAD, ResourcePack.getItem("ui_heart").build());
				equip.sendPacket(to.getPlayer());
			}
		}.runTaskLater(MainLg.getInstance(), 2);
	}
	
	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.getCache().remove("cupidon_first");
		player.stopChoosing();
		player.hideView();
		player.sendTitle(RED+"Vous n'avez mis personne en couple", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
		player.sendMessage(BLUE+"Tu n'as pas créé de couple.");
	}
	
	
	@EventHandler
	public void onPlayerKill(LGPlayerGotKilledEvent e) {
		if(e.getGame() == getGame() && e.getKilled().getCache().has("inlove") && !e.getKilled().getCache().<LGPlayer>get("inlove").isDead()) {
			LGPlayer killed = e.getKilled().getCache().get("inlove");
			LGPlayerKilledEvent event = new LGPlayerKilledEvent(getGame(), killed, Reason.LOVE);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled())
				getGame().kill(event.getKilled(), event.getReason(), false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameEnd(LGGameEndEvent e) {
		if(e.getGame() != getGame())
            return;

        WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
        List<Integer> ids = new ArrayList<>();

        for(LGPlayer lgp : getGame().getInGame())
            ids.add(Integer.MAX_VALUE-lgp.getPlayer().getEntityId());

        int[] intList = new int[ids.size()];

        for(int i = 0;i<ids.size();i++)
            intList[i] = ids.get(i);

        destroy.setEntityIds(intList);

        for(LGPlayer lgp : getGame().getInGame())
            destroy.sendPacket(lgp.getPlayer());

        if(e.getWinType() == LGWinType.COUPLE)
            e.getWinners().addAll(getGame().getAlive());
	}
	
	@EventHandler
	public void onEndCheck(LGEndCheckEvent e) {
		if(e.getGame() != getGame())
            return;

        if(getGame().getAlive().size() > 3)
            return;

        boolean cupidonAlive = false;
        int manyCoupleAlive = 0;

        for(LGPlayer lgp : getGame().getAlive())
            if (lgp.getRole() instanceof RCupid)
                cupidonAlive = true;
            else if(lgp.getCache().has("inlove"))
                manyCoupleAlive++;

        boolean coupleAlive = manyCoupleAlive == 2;

        if(getGame().getAlive().size() == ((coupleAlive ? 2 : 0) + (cupidonAlive ? 1 : 0)))
            e.setWinType(LGWinType.COUPLE);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
		if(player.getGame() == getGame()) {
			if(e.getMessage().startsWith("!")) {
                e.setCancelled(true);
				if(player.getCache().has("inlove"))
                {
					player.sendMessage(LIGHT_PURPLE+"\u2764 Vous "+GOLD+"» "+WHITE+""+e.getMessage().substring(1));
					player.getCache().<LGPlayer>get("inlove").sendMessage(LIGHT_PURPLE+"\u2764 Votre Amoureux "+GOLD+"» "+WHITE+""+e.getMessage().substring(1));
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onUpdatePrefix (LGUpdatePrefixEvent e) {
		if(e.getGame() == getGame())
			if(e.getTo().getCache().get("inlove") == e.getPlayer() || ((e.getTo() == e.getPlayer() || e.getTo().getRole() == this) && e.getPlayer().getCache().has("inlove")))
				e.setPrefix(LIGHT_PURPLE+"\u2764 "+WHITE+""+e.getPrefix());
	}
}
