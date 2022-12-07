package fr.valgrifer.loupgarou.events;

import java.util.List;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;

public class LGCustomItemChangeEvent extends LGEvent {
	@Getter private final LGPlayer player;
	@Getter private final List<String> constraints;
	
	public LGCustomItemChangeEvent(LGGame game, LGPlayer player, List<String> constraints) {
		super(game);
		this.player = player;
		this.constraints = constraints;
	}
}
