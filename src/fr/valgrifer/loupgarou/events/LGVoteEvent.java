package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGVoteCause;
import fr.valgrifer.loupgarou.classes.LGGame;
import org.bukkit.event.Cancellable;

import lombok.Getter;
import lombok.Setter;

public class LGVoteEvent extends LGEvent implements Cancellable{
	public LGVoteEvent(LGGame game, LGVoteCause cause) {
		super(game);

        this.cause = cause;
	}

	@Getter private final LGVoteCause cause;

	@Getter @Setter private boolean cancelled = false;
	@Getter @Setter private boolean continuePeopleVote = true;
	@Getter @Setter private boolean hiveViewersMessage = false;
}
