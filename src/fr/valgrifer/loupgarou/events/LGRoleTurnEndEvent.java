package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.roles.Role;
import lombok.Getter;

public class LGRoleTurnEndEvent extends LGEvent{
	public LGRoleTurnEndEvent(LGGame game, Role newRole, Role previousRole) {
		super(game);
		this.newRole = newRole;
		this.previousRole = previousRole;
	}
	
	@Getter private final Role newRole, previousRole;
}