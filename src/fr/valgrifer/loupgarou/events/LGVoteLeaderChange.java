package fr.valgrifer.loupgarou.events;

import java.util.List;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVote;
import lombok.Getter;

public class LGVoteLeaderChange extends LGEvent{

	public LGVoteLeaderChange(LGGame game, LGVote vote, List<LGPlayer> latest, List<LGPlayer> now) {
		super(game);
		this.latest = latest;
		this.now = now;
		this.vote = vote;
	}
	
	@Getter List<LGPlayer> latest, now;
	@Getter LGVote vote;

}
