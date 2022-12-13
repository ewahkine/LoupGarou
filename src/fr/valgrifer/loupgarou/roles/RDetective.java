package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

import static org.bukkit.ChatColor.*;

public class RDetective extends Role{
	public RDetective(LGGame game) {
		super(game);
	}
	public static RoleType _getType() {
		return RoleType.VILLAGER;
	}
	public static RoleWinType _getWinType() {
		return RoleWinType.VILLAGE;
	}
	public static String _getName() {
		return GREEN+""+BOLD+"Détective";
	}
	public static String _getFriendlyName() {
		return "du "+_getName();
	}
	public static String _getShortDescription() {
		return RVillager._getShortDescription();
	}
	public static String _getDescription() {
		return _getShortDescription()+WHITE+". Chaque nuit, tu mènes l'enquête sur deux joueurs pour découvrir s'ils font partie du même camp.";
	}
	public static String _getTask() {
		return "Choisis deux joueurs à étudier.";
	}
	public static String _getBroadcastedTask() {
		return "Le "+_getName()+""+BLUE+" est sur une enquête...";
	}
	
	@Override
	public int getTimeout() {
		return 15;
	}

    LGPlayer first;
	@Override
	protected void onNightTurn(LGPlayer player, Runnable callback) {
        first = null;
		player.showView();
		
		player.choose(choosen -> {
            if(choosen == null)
                return;

            if(choosen == player) {
                player.sendMessage(RED+"Vous ne pouvez pas vous sélectionner !");
                return;
            }
            if(first != null) {
                first = player.getCache().remove("detective_first");
                if(first == choosen) {
                    player.sendMessage(RED+"Vous ne pouvez pas comparer "+GRAY+""+BOLD+""+first.getName()+""+RED+" avec lui même !");
                } else {
                    player.stopChoosing();
                    player.hideView();

                    LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new CompareAction(first, choosen, (first.getRoleType() == RoleType.NEUTRAL || choosen.getRoleType() == RoleType.NEUTRAL) ? first.getRole().getClass() != choosen.getRole().getClass() : first.getRoleType() != choosen.getRoleType()), player);
                    Bukkit.getPluginManager().callEvent(event);
                    CompareAction action = (CompareAction) event.getAction();
                    if(!action.isCancelled())
                        if(action.isDifferentCamp())
                            player.sendMessage(GRAY+""+BOLD+""+action.getTarget1().getName()+""+GOLD+" et "+GRAY+""+BOLD+""+action.getTarget2().getName()+""+GOLD+" ne sont "+RED+"pas du même camp.");
                        else
                            player.sendMessage(GRAY+""+BOLD+""+action.getTarget1().getName()+""+GOLD+" et "+GRAY+""+BOLD+""+action.getTarget2().getName()+""+GOLD+" sont "+GREEN+"du même camp.");
                    else
                        player.sendMessage(RED+"Votre information a été brouillé.");
                    callback.run();
                }
            } else {
                first = choosen;
                player.sendMessage(BLUE+"Choisis un joueur avec qui tu souhaites comparer le rôle de "+GRAY+""+BOLD+""+choosen.getName());
            }
        });
	}

	@Override
	protected void onNightTurnTimeout(LGPlayer player) {
		player.stopChoosing();
		player.hideView();
	}


    public static class CompareAction implements LGRoleActionEvent.RoleAction, Cancellable
    {
        public CompareAction(LGPlayer target1, LGPlayer target2, boolean differentCamp)
        {
            this.target1 = target1;
            this.target2 = target2;
            this.differentCamp = differentCamp;
        }

        @Getter
        @Setter
        private boolean cancelled;
        @Getter @Setter private LGPlayer target1;
        @Getter @Setter private LGPlayer target2;
        @Getter @Setter private boolean differentCamp;
    }
}
