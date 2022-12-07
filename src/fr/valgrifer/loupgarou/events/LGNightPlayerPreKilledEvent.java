package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGNightPlayerPreKilledEvent extends LGEvent implements Cancellable{
	public LGNightPlayerPreKilledEvent(LGGame game, LGPlayer killed, LGPlayerKilledEvent.Reason reason) {
		super(game);
		this.killed = killed;
		this.reason = reason;
	}

	@Getter @Setter boolean cancelled;
    
    @Getter private final LGPlayer killed;
    @Getter @Setter private LGPlayerKilledEvent.Reason reason;
	
}
