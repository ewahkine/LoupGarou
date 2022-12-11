package fr.valgrifer.loupgarou;

import com.comphenix.protocol.ProtocolLibrary;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.listeners.*;
import fr.valgrifer.loupgarou.roles.*;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

import static org.bukkit.ChatColor.*;

public class MainLg extends JavaPlugin {
    private static MainLg instance;
    @Getter
    private final Map<String, Constructor<? extends Role>> roles = new HashMap<>();
    @Getter
    private static int maxPlayers = 0;

    @Getter
    @Setter
    private LGGame currentGame;//Because for now, only one game will be playable on one server (flemme)

    public static void makeNewGame() {
        LGGame game = new LGGame(MainLg.getMaxPlayers());
        MainLg.getInstance().setCurrentGame(game);
    }

    public static MainLg getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        loadRoles();
        if (!new File(getDataFolder(), "config.yml").exists()) {//Créer la config
            saveDefaultConfig();
            FileConfiguration config = getConfig();
            config.set("spawns", new ArrayList<List<Double>>());
            for (String role : roles.keySet())//Nombre de participants pour chaque rôle
                config.set("role." + role, 0);
            saveConfig();
        }
        loadConfig();

        getLogger().info("ResourcePack Url Used: " + VariousUtils.resourcePackAdress());

        makeNewGame();

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new CancelListener(), this);
        pm.registerEvents(new VoteListener(), this);
        pm.registerEvents(new ChatListener(), this);
        pm.registerEvents(new LoupGarouListener(), this);

        for (Player player : Bukkit.getOnlinePlayers())
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, "is connected"));

        if (pm.getPlugin("ProtocolLib") != null)
            ProtocolLibHook.hook(this);
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lg")) {
            if (!sender.hasPermission("loupgarou.admin")) {
                sender.sendMessage(DARK_RED + "Erreur: Vous n'avez pas la permission...");
                return true;
            }
            if(!(sender instanceof Player))
            {
                sender.sendMessage("Erreur: Vous n'êtes pas un joueur");
                return true;
            }
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("addspawn")) {
                    Player player = (Player) sender;
                    Location loc = player.getLocation();
                    List<Object> list = (List<Object>) getConfig().getList("spawns");
                    assert list != null;
                    list.add(Arrays.asList((double) loc.getBlockX(), loc.getY(), (double) loc.getBlockZ(), (double) loc.getYaw(), (double) loc.getPitch()));
                    saveConfig();
                    loadConfig();
                    sender.sendMessage(GREEN + "La position a bien été ajoutée !");
                    return true;
                }
            }
            ((Player) sender).openInventory(ConfigManager.getMainConfigManager().getInventory());
            return true;
        }
        if (command.getName().equalsIgnoreCase("spec")) {
            if(!(sender instanceof Player))
            {
                sender.sendMessage(DARK_RED + "Erreur: " + RED + "Vous n'êtes pas un joueur");
                return true;
            }
            LGPlayer lgp = LGPlayer.thePlayer((Player) sender);

            boolean canOpen = lgp.getGame() == null || !lgp.getGame().isStarted() || (lgp.getGame().isStarted() && lgp.isDead());

            for (Role rl : lgp.getGame().getRoles())
                if(blacklistRoleSpec.contains(rl.getClass()))
                {
                    canOpen = false;
                    break;
                }

            if(canOpen)
                ((Player) sender).openInventory(SpecManager.getMainSpecManager().getInventory());
            else
                lgp.sendMessage(DARK_RED + "Erreur: " + RED + "La Commande vous à étais bloqué");
            return true;
        }
        return false;
    }
    private final List<Class<? extends Role>> blacklistRoleSpec = new ArrayList<>();

    @SuppressWarnings("NullableProblems")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission("loupgarou.admin") && args.length == 1)
            return getStartingList(args[0], "addSpawn");
        return new ArrayList<>(0);
    }

    private List<String> getStartingList(String startsWith, String... list) {
        startsWith = startsWith.toLowerCase();
        List<String> returnlist = new ArrayList<>();
        if (startsWith.length() == 0)
            return Arrays.asList(list);
        for (String s : list)
            if (s.toLowerCase().startsWith(startsWith))
                returnlist.add(s);
        return returnlist;
    }

    public void loadConfig() {
        int players = 0;
        for (String role : roles.keySet())
            players += getConfig().getInt("role." + role);
        maxPlayers = players;
    }

    public void addRole(Class<? extends Role> clazz) {
        try {
            this.roles.put(clazz.getSimpleName().substring(1), clazz.getConstructor(LGGame.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void addBlackListSpecRole(Class<? extends Role> clazz) {
        if(!this.blacklistRoleSpec.contains(clazz))
            this.blacklistRoleSpec.add(clazz);
    }

    private void loadRoles() {
        try {
            addRole(RLoupGarou.class);
            addRole(RLoupGarouNoir.class);
            addRole(RGarde.class);
            addRole(RSorciere.class);
            addRole(RVoyante.class);
            addRole(RChasseur.class);
            addRole(RVillageois.class);
            addRole(RMedium.class);
            addRole(RDictateur.class);
            addRole(RCupidon.class);
            addRole(RPetiteFille.class);
            addRole(RChaperonRouge.class);
            addRole(RLoupGarouBlanc.class);
            addRole(RBouffon.class);
            addRole(RAnge.class);
            addRole(RSurvivant.class);
            addRole(RAssassin.class);
            addRole(RGrandMechantLoup.class);
            addRole(RCorbeau.class);
            addRole(RDetective.class);
            addRole(RChienLoup.class);
            addRole(RPirate.class);
            addRole(RPyromane.class);
//            addRole(RPretre.class);
            addRole(RFaucheur.class);
            addRole(REnfantSauvage.class);
            addRole(RMontreurDOurs.class);
            addRole(RVampire.class);
            addRole(RChasseurDeVampire.class);

            addBlackListSpecRole(RMedium.class);
            addBlackListSpecRole(RPretre.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
