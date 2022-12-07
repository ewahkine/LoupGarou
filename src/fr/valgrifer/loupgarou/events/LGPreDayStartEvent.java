package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGPreDayStartEvent extends LGEvent implements Cancellable{
	public LGPreDayStartEvent(LGGame game) {
		super(game);
	}
	
	@Getter @Setter private boolean cancelled;
}