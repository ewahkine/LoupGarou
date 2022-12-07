package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;

public class LGVampiredEvent extends LGEvent{
	public LGVampiredEvent(LGGame game, LGPlayer player) {
		super(game);
		this.player = player;
	}
	
	@Getter @Setter private boolean immuned, protect;
	@Getter @Setter private LGPlayer player;
}