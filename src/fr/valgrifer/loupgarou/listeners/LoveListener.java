package fr.valgrifer.loupgarou.listeners;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.roles.RCupid;
import fr.valgrifer.loupgarou.roles.RoleWinType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
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
        WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
        List<Integer> ids = new ArrayList<>();

        for(LGPlayer lgp : e.getGame().getInGame())
            ids.add(Integer.MAX_VALUE-lgp.getPlayer().getEntityId());

        int[] intList = new int[ids.size()];

        for(int i = 0;i<ids.size();i++)
            intList[i] = ids.get(i);

        destroy.setEntityIds(intList);

        for(LGPlayer lgp : e.getGame().getInGame())
            destroy.sendPacket(lgp.getPlayer());

        if(e.getWinType() == LGWinType.COUPLE)
            e.getWinners().addAll(e.getGame().getAlive());
        else
            e.getWinners().removeAll(e.getWinners().stream()
                .filter(player -> player.getCache().has(loveKey))
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @EventHandler
    public void onEndCheck(LGEndCheckEvent e) {
        if(e.getGame().getAlive(lgPlayer -> lgPlayer.getRoleWinType() != RoleWinType.NONE).size() > 3)
            return;

        boolean cupidonAlive = false;
        int manyCoupleAlive = 0;

        for(LGPlayer lgp : e.getGame().getAlive())
            if (lgp.getRole() instanceof RCupid)
                cupidonAlive = true;
            else if(lgp.getCache().has(loveKey))
                manyCoupleAlive++;

        boolean coupleAlive = manyCoupleAlive == 2;

        if(e.getGame().getAlive().size() == ((coupleAlive ? 2 : 0) + (cupidonAlive ? 1 : 0)))
            e.setWinType(LGWinType.COUPLE);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        LGPlayer player = LGPlayer.thePlayer(e.getPlayer());
        if(e.getMessage().startsWith("!")) {
            e.setCancelled(true);
            if(player.getCache().has(loveKey))
            {
                player.sendMessage(LIGHT_PURPLE+"❤ Vous "+GOLD+"» "+WHITE+e.getMessage().substring(1));
                player.getCache().<LGPlayer>get(loveKey).sendMessage(LIGHT_PURPLE+"❤ Votre Amoureux "+GOLD+"» "+WHITE+e.getMessage().substring(1));
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onUpdatePrefix (LGUpdatePrefixEvent e) {
        if(e.getTo().getCache().get(loveKey) == e.getPlayer() || (e.getTo() == e.getPlayer() && e.getPlayer().getCache().has(loveKey)))
            e.setPrefix(LIGHT_PURPLE+"❤ "+WHITE+e.getPrefix());
    }
}
