package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.roles.RoleWinType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LoveListener implements Listener {

    public static final String loveKey = "inlove";

    public static boolean forceCoupleWin = true;

    @EventHandler
    public void onPlayerKill(LGPlayerGotKilledEvent e) {
        if(e.getKilled().getCache().has(loveKey) && !e.getKilled().getCache().<LGPlayer>get(loveKey).isDead()) {
            LGPlayer killed = e.getKilled().getCache().get(loveKey);
            LGPlayerKilledEvent event = new LGPlayerKilledEvent(e.getGame(), killed, LGPlayerKilledEvent.Reason.LOVE);
            Bukkit.getPluginManager().callEvent(event);
            if(!event.isCancelled())
                e.getGame().kill(event.getKilled(), event.getReason(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameEnd(LGGameEndEvent e) {
        if(e.getWinType() == LGWinType.COUPLE)
            e.getWinners().addAll(e.getGame().getInGame().stream()
                    .filter(lgp -> lgp.getRoleWinType() == RoleWinType.COUPLE)
                    .collect(Collectors.toCollection(ArrayList::new)));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
        if(!e.getMessage().startsWith("!"))
            return;

        e.setCancelled(true);

        if(!player.getCache().has(loveKey))
            return;

        player.sendMessage(LIGHT_PURPLE+"❤ Vous "+GOLD+"» "+WHITE+e.getMessage().substring(1));
        player.getCache().<LGPlayer>get(loveKey).sendMessage(LIGHT_PURPLE+"❤ Votre Amoureux "+GOLD+"» "+WHITE+e.getMessage().substring(1));
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onUpdatePrefix (LGUpdatePrefixEvent e) {
        if(e.getTo().getCache().get(loveKey) == e.getPlayer() || (e.getTo() == e.getPlayer() && e.getPlayer().getCache().has(loveKey)))
            e.setPrefix(LIGHT_PURPLE+"❤ "+WHITE+e.getPrefix());
    }
}
