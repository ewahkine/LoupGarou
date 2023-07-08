package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.events.LGDeathAnnouncementEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.concurrent.ThreadLocalRandom;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPriestess extends Role{
	public RPriestess(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+BOLD+"Prêtresse";
	}
	public static String _getFriendlyName() {
		return "des "+_getName();
	}
	public static String _getShortDescription() {
		return WHITE+"Tu gagnes avec le "+RoleWinType.VILLAGE.getColoredName(BOLD);
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Ton Seul pouvoir est de caché le role des mort constamment, le "+RoleWinType.VILLAGE.getColoredName(BOLD)+WHITE+" auront 70% de chance de voir le role du mort t'en dit que les "+RoleWinType.LOUP_GAROU.getColoredName(BOLD)+WHITE+" auront que 30% et à ta mort les pourcentages s'inverse, les autres camps ont 100% dans tous les cas.";
	}
	public static String _getTask() {
		return "";
	}
	public static String _getBroadcastedTask() {
		return "";
	}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathAnnouncement(LGDeathAnnouncementEvent e)
    {
        if(e.getGame() != getGame())
            return;

        if(e.getPlayer().isDead())
            return;

        if(e.getPlayer().getRole().getType() == RoleType.NEUTRAL)
            return;

        boolean priestessAlive = !getGame().getAlive(lgPlayer -> lgPlayer.getRole() instanceof RPriestess).isEmpty();

        int random = 30;

        if(priestessAlive && e.getPlayer().getRole().getType() == RoleType.VILLAGER)
            random = 70;
        else if(!priestessAlive && e.getPlayer().getRole().getType() == RoleType.LOUP_GAROU)
            random = 70;

        if(ThreadLocalRandom.current().nextInt(0, 100) > random)
            e.setShowedRole(HiddenRole.class);
    }

    public static class HiddenRole extends Role {

        private HiddenRole(LGGame game) throws Exception
        {
            super(game);
            throw new Exception("This class can not be created");
        }

        public static RoleType _getType() {
            return RoleType.NEUTRAL;
        }
        public static RoleWinType _getWinType() {
            return RoleWinType.NONE;
        }
        public static String _getName() {
            return GRAY+MAGIC+BOLD+"Hidden";
        }
        public static String _getFriendlyName() {
            return _getName();
        }
        public static String _getShortDescription() {
            return "";
        }
        public static String _getDescription() {
            return "";
        }
        public static String _getTask() {
            return "";
        }
        public static String _getBroadcastedTask() {
            return "";
        }
    }
}
