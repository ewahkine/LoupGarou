package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class LGRoleActionEvent extends LGEvent {
	public LGRoleActionEvent(LGGame game, RoleAction action, LGPlayer ...players) {
        this(game, action, Arrays.asList(players));
    }
	public LGRoleActionEvent(LGGame game, RoleAction action, List<LGPlayer> players) {
		super(game);
        if(action == null)
            throw new NullPointerException("'action' can't not be null");
        this.action = action;
		this.players = players;
	}

	@Getter private final RoleAction action;
	@Getter private final List<LGPlayer> players;

    public <A extends RoleAction> boolean isAction(Class<A> aClass)
    {
        return action.getClass().equals(aClass);
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
    public interface RoleAction
    { }
}