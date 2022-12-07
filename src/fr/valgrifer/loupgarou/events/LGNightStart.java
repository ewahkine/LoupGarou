package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGNightStart extends LGEvent implements Cancellable{

	public LGNightStart(LGGame game) {
		super(game);
	}

	@Getter @Setter boolean cancelled;

}
