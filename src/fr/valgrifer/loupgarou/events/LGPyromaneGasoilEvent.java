package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGPyromaneGasoilEvent extends LGEvent implements Cancellable{
	public LGPyromaneGasoilEvent(LGGame game, LGPlayer player) {
		super(game);
		this.player = player;
	}
	
	@Getter @Setter private boolean cancelled;
	@Getter @Setter private LGPlayer player;
}