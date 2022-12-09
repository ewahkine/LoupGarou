package fr.valgrifer.loupgarou.roles;

import java.util.Comparator;

import static org.bukkit.ChatColor.*;

import fr.valgrifer.loupgarou.classes.LGCustomItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;

public class REnfantSauvageLG extends Role{
	public REnfantSauvageLG(LGGame game) {
		super(game);
	}
	public static String _getName() {
        return RED+""+BOLD+"Enfant-Sauvage";
	}

	public static String _getFriendlyName() {
		return "de l'"+_getName();
	}

    public static String _getScoreBoardName()
    {
        return REnfantSauvage._getScoreBoardName();
    }

	public static String _getShortDescription() {
        return REnfantSauvage._getShortDescription();
    }

	public static String _getDescription() {
        return REnfantSauvage._getDescription();
    }

	public static String _getTask() {
        return REnfantSauvage._getTask();
    }

	public static String _getBroadcastedTask() {
        return REnfantSauvage._getBroadcastedTask();
    }
	public static RoleType _getType() {
		return RoleType.LOUP_GAROU;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.LOUP_GAROU;
	}

	@Override
	public int getTimeout() {
		return -1;
	}
	
	@Override
	public void join(LGPlayer player, boolean sendMessage) {
		super.join(player, sendMessage);
		player.setRole(this);
        RLoupGarou lgs = RLoupGarou.forceJoin(player);
        LGCustomItems.updateItem(player);
        if(lgs != null)
            for(LGPlayer lgp : lgs.getPlayers())
                if(lgp != player)
                    lgp.sendMessage(GRAY+""+BOLD+""+player.getName()+""+GOLD+" a rejoint les "+RED+""+BOLD+"Loups-Garous"+GOLD+".");
	}
}