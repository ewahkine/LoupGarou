package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGDayStartEvent extends LGEvent implements Cancellable{
	public LGDayStartEvent(LGGame game) {
		super(game);
	}
	
	@Getter @Setter private boolean cancelled;
}