package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LGEvent extends Event{
	@Getter final LGGame game;
	
    private static final HandlerList handlers = new HandlerList();
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
