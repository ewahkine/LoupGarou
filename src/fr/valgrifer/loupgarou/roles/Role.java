package fr.valgrifer.loupgarou.roles;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGCustomItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
public abstract class Role implements Listener{
	@Getter @Setter private int waitedPlayers;
	@Getter private final ArrayList<LGPlayer> players = new ArrayList<>();
	@Getter private final LGGame game;
	
	public Role(LGGame game) {
		this.game = game;
		Bukkit.getPluginManager().registerEvents(this, MainLg.getInstance());
		FileConfiguration config = MainLg.getInstance().getConfig();
		String roleConfigName = "role."+getClass().getSimpleName().substring(1);
		if(config.contains(roleConfigName))
			waitedPlayers = config.getInt(roleConfigName);
	}

    protected static Object getStatic(Class<? extends Role> clazz, String fn)
    {
        return getStatic(clazz, fn, true);
    }
    @SneakyThrows
    protected static Object getStatic(Class<? extends Role> clazz, String fn, boolean isCatch)
    {
        try
        {
            return clazz.getDeclaredMethod(fn).invoke(null);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            if(isCatch)
                return fn + "() is not defined (in static) in " + clazz.getSimpleName();
            else
                throw e;
        }
    }

    @SneakyThrows
    public static <T extends Role> T makeNew(Class<T> r, LGGame game)
    {
        return r.getConstructor(LGGame.class).newInstance(game);
    }
	

    public String getName()
    {
        return getName(this.getClass());
    }
    public static String getName(Class<? extends Role> clazz)
    {
        return (String) getStatic(clazz, "_getName");
    }
    public String getPublicName(LGPlayer lgp)
    {
        return getScoreBoardName();
    }
    public String getFriendlyName()
    {
        return getFriendlyName(this.getClass());
    }
    public static String getFriendlyName(Class<? extends Role> clazz)
    {
        return (String) getStatic(clazz, "_getFriendlyName");
    }
    public String getScoreBoardName()
    {
        return getScoreBoardName(this.getClass());
    }
    public static String getScoreBoardName(Class<? extends Role> clazz)
    {
        try
        {
            return (String) getStatic(clazz, "_getScoreBoardName", false);
        }
        catch (Exception ignored)
        {
            return getName(clazz);
        }
    }
    public String getShortDescription()
    {
        return getShortDescription(this.getClass());
    }
    public static String getShortDescription(Class<? extends Role> clazz)
    {
        return (String) getStatic(clazz, "_getShortDescription");
    }
    public String getDescription()
    {
        return getDescription(this.getClass());
    }
    public static String getDescription(Class<? extends Role> clazz)
    {
        return (String) getStatic(clazz, "_getDescription");
    }
    public String getTask()
    {
        return getTask(this.getClass());
    }
    public static String getTask(Class<? extends Role> clazz)
    {
        return (String) getStatic(clazz, "_getTask");
    }
    public String getBroadcastedTask()
    {
        return getBroadcastedTask(this.getClass());
    }
    public static String getBroadcastedTask(Class<? extends Role> clazz)
    {
        return (String) getStatic(clazz, "_getBroadcastedTask");
    }
    public RoleType getType()
    {
        return getType(this.getClass());
    }
    public static RoleType getType(Class<? extends Role> clazz)
    {
        try {
            return (RoleType) getStatic(clazz, "_getType", false);
        }
        catch (Exception ignored) {}
        return RoleType.NEUTRAL;
    }
    public RoleWinType getWinType()
    {
        return getWinType(this.getClass());
    }
    public static RoleWinType getWinType(Class<? extends Role> clazz)
    {
        try {
            return (RoleWinType) getStatic(clazz, "_getWinType", false);
        }
        catch (Exception ignored) {}
        return RoleWinType.NONE;
    }
	/**
	 * @return Timeout in second for this role
	 */
	public int getTimeout()
    { return -1; }
	
	public void onNightTurn(Runnable callback) {
		 ArrayList<LGPlayer> players = (ArrayList<LGPlayer>) getPlayers().clone();
		 new Runnable() {
			
			@Override
			public void run() {
				getGame().cancelWait();
				if(players.size() == 0) {
					onTurnFinish(callback);
					return;
				}
				LGPlayer player = players.remove(0);
				if(player.isRoleActive()) {
					getGame().wait(getTimeout(), ()->{
						try {
							Role.this.onNightTurnTimeout(player);
						}catch(Exception err) {
							MainLg.getInstance().getLogger().warning("Error when timeout role");
							err.printStackTrace();
						}
						this.run();
					}, (currentPlayer, secondsLeft)-> currentPlayer == player ? BLUE+""+ BOLD+"C'est à ton tour !" : GOLD+"C'est au tour "+getFriendlyName()+" "+GOLD+"("+YELLOW+""+secondsLeft+" s"+GOLD+")");
					player.sendMessage(GOLD+""+getTask());
				//	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+""+getTask(), 100);
					onNightTurn(player, this);
				} else {
					getGame().wait(getTimeout(), ()->{}, (currentPlayer, secondsLeft)-> currentPlayer == player ? RED+""+BOLD+"Tu ne peux pas jouer" : GOLD+"C'est au tour "+getFriendlyName()+" "+GOLD+"("+YELLOW+""+secondsLeft+" s"+GOLD+")");
					Runnable run = this;
					new BukkitRunnable() {
						
						@Override
						public void run() {
							run.run();
						}
					}.runTaskLater(MainLg.getInstance(), 20L *(ThreadLocalRandom.current().nextInt(getTimeout()/3*2-4)+4));
				}
			}
		}.run();
	}
	public void join(LGPlayer player, boolean sendMessage) {
        getGame().logMessage(player.getName() + " est " + getName());
		players.add(player);
		if(player.getRole() == null)
			player.setRole(this);
		waitedPlayers--;
		if(sendMessage) {
			player.sendTitle(GOLD+"Tu es "+getName(), YELLOW+""+getShortDescription(), 200);
			player.sendMessage(GOLD+"Tu es "+getName()+""+GOLD+".");
			player.sendMessage(GOLD+"Description : "+getDescription());
		}
	}
	public void join(LGPlayer player) {
		join(player, !getGame().isStarted());
		LGCustomItems.updateItem(player);
	}
	public boolean hasPlayersLeft() {
		return getPlayers().size() > 0;
	}
	protected void onNightTurnTimeout(LGPlayer player) {}
	protected void onNightTurn(LGPlayer player, Runnable callback) {}
	protected void onTurnFinish(Runnable callback) {
		callback.run();
	}
	public int getTurnOrder() {
		try {
			return RoleSort.indexOfRoleSort(getClass().getSimpleName().substring(1));
		}catch(Throwable e) {
			return -1;
		}
	}//En combientième ce rôle doit être appellé
}
