package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGPreNightEndEvent extends LGEvent implements Cancellable{
	public LGPreNightEndEvent(LGGame game) {
		super(game);
	}
	
	@Getter @Setter private boolean cancelled;
}