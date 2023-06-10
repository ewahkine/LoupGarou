package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVote;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import lombok.Getter;
import lombok.Setter;

public class LGVoteEndEvent extends LGEvent {
	public LGVoteEndEvent(LGGame game, LGVote vote, LGVoteCause cause) {
		super(game);

        this.cause = cause;
        this.vote = vote;
	}

	@Getter private final LGVoteCause cause;
	@Getter private final LGVote vote;
}
