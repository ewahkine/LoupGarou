package fr.valgrifer.loupgarou.classes.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO Add Identifier for add a LGChat Event

@RequiredArgsConstructor
public class LGChat {
	@Getter private final Map<LGPlayer, LGChatCallback> viewers = new HashMap<>();
	@Getter private final LGChatCallback defaultCallback;
	
	public interface LGChatCallback{
		String receive(LGPlayer sender, String message);
		default String send(LGPlayer sender, String message) {return null;}
	}

	public void sendMessage(LGPlayer sender, String message) {
		String sendMessage = getViewers().get(sender).send(sender, message);
		for(Entry<LGPlayer, LGChatCallback> entry : viewers.entrySet())
			entry.getKey().sendMessage(sendMessage != null ? sendMessage : entry.getValue().receive(sender, message));
	}

	public void join(LGPlayer player, LGChatCallback callback) {
        getViewers().put(player, callback == null ? defaultCallback : callback);
	}
	public void leave(LGPlayer player) {
		getViewers().remove(player);
	}
}
