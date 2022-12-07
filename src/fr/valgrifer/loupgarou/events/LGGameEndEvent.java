package fr.valgrifer.loupgarou.events;

import java.util.List;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGGameEndEvent extends LGEvent implements Cancellable{
	@Getter @Setter private boolean cancelled;
	@Getter private final LGWinType winType;
	@Getter private final List<LGPlayer> winners;
	public LGGameEndEvent(LGGame game, LGWinType winType, List<LGPlayer> winners) {
		super(game);
		this.winType = winType;
		this.winners = winners;
	}
}