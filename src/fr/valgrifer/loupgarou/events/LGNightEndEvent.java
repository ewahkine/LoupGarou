package fr.valgrifer.loupgarou.events;

import org.bukkit.event.Cancellable;

import fr.valgrifer.loupgarou.classes.LGGame;
import lombok.Getter;
import lombok.Setter;

public class LGNightEndEvent extends LGEvent implements Cancellable{
	public LGNightEndEvent(LGGame game) {
		super(game);
	}
	
	@Getter @Setter private boolean cancelled;
}