package fr.valgrifer.loupgarou.roles;

import java.util.Arrays;

import fr.valgrifer.loupgarou.classes.ResourcePack;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
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
		return LIGHT_PURPLE+BOLD+"Cupidon";
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
		return _getName()+BLUE+" choisit deux âmes à unir.";
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
                    player.sendMessage(GRAY+BOLD+choosen.getName()+BLUE+" est désélectionné pour être amoureux .");
                }
                else
                {
                    //	sendHead(player, choosen);
                    int entityId = Integer.MAX_VALUE-first.getPlayer().getEntityId();
                    WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                    destroy.setEntityIds(new int[] {entityId});
                    destroy.sendPacket(player.getPlayer());

                    setInLove(first, choosen);
                    player.sendMessage(GRAY+BOLD+first.getName()+BLUE+" et "+GRAY+BOLD+choosen.getName()+BLUE+" sont désormais follement amoureux.");
                    player.stopChoosing();
                    player.hideView();
                    callback.run();
                }
            }
            else
            {
                sendHead(player, choosen);
                player.getCache().set("cupidon_first", choosen);
                player.sendMessage(GRAY+BOLD+choosen.getName()+BLUE+" est sélectionné pour être amoureux .");
            }
        }, player);
	}
	protected void setInLove(LGPlayer player1, LGPlayer player2)
    {
		player1.getCache().set("inlove", player2);
		player1.addEndGameReaveal(LIGHT_PURPLE+"\u2764");
        player1.sendMessage(BLUE+"Tu tombes amoureux de "+GRAY+BOLD+player2.getName()+BLUE+", il est "+player2.getRole().getName());
        player1.sendMessage(BLUE+ITALIC+"Tu peux lui parler en mettant un "+YELLOW+"!"+BLUE+" devant ton message.");
		
		player2.getCache().set("inlove", player1);
        player2.addEndGameReaveal(LIGHT_PURPLE+"\u2764");
        player2.sendMessage(BLUE+"Tu tombes amoureux de "+GRAY+BOLD+player1.getName()+BLUE+", il est "+player1.getRole().getName());
        player2.sendMessage(BLUE+ITALIC+"Tu peux lui parler en mettant un "+YELLOW+"!"+BLUE+" devant ton message.");

        if(forceCoupleWin)
        {
            player1.setRoleWinType(RoleWinType.COUPLE);
            player1.sendMessage(BLUE+ITALIC+"Vous devez gagné forcement avec votre couple et votre cupidon");
            player2.setRoleWinType(RoleWinType.COUPLE);
            player2.sendMessage(BLUE+ITALIC+"Vous devez gagné forcement avec votre couple et votre cupidon");
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
}
