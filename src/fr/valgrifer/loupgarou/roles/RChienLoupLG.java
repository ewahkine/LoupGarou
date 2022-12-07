package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGCustomItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import static org.bukkit.ChatColor.*;

@SuppressWarnings("unused")
public class RChienLoupLG extends Role{
	public RChienLoupLG(LGGame game) {
		super(game);
	}
	public static String _getName() {
        return RED+""+BOLD+"Chien-Loup";
	}

	public static String _getFriendlyName() {
		return "du "+_getName();
	}

    public static String _getScoreBoardName()
    {
        return RChienLoup._getScoreBoardName();
    }

	public static String _getShortDescription() {
		return RChienLoup._getShortDescription();
	}

	public static String _getDescription() {
		return RChienLoup._getDescription();
    }

	public static String _getTask() {
		return RChienLoup._getTask();
	}

	public static String _getBroadcastedTask() {
		return RChienLoup._getBroadcastedTask();
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
