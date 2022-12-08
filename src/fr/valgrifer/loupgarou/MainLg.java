package fr.valgrifer.loupgarou;

import com.comphenix.protocol.ProtocolLibrary;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
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
import org.bukkit.event.player.PlayerQuitEvent;
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
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("addspawn")) {
                    if(!(sender instanceof Player))
                    {
                        sender.sendMessage("Erreur: Vous n'êtes pas un joueur");
                        return true;
                    }
                    Player player = (Player) sender;
                    Location loc = player.getLocation();
                    List<Object> list = (List<Object>) getConfig().getList("spawns");
                    assert list != null;
                    list.add(Arrays.asList((double) loc.getBlockX(), loc.getY(), (double) loc.getBlockZ(), (double) loc.getYaw(), (double) loc.getPitch()));
                    saveConfig();
                    loadConfig();
                    sender.sendMessage(GREEN + "La position a bien été ajoutée !");
                    return true;
                } else if (args[0].equalsIgnoreCase("end")) {
                    if (getCurrentGame() == null) {
                        sender.sendMessage(DARK_RED + "Erreur : " + RED + "Il n'y a pas de partie.");
                        return true;
                    }
                    getCurrentGame().cancelWait();
                    getCurrentGame().endGame(LGWinType.EQUAL);
                    getCurrentGame().broadcastMessage(RED + "La partie a été arrêtée de force !", true);
                    return true;
                } else if (args[0].equalsIgnoreCase("start")) {
                    if (getCurrentGame() == null) {
                        sender.sendMessage(DARK_RED + "Erreur : " + RED + "Il n'y a pas de partie.");
                        return true;
                    }
                    if (Objects.requireNonNull(MainLg.getInstance().getConfig().getList("spawns")).size() < getCurrentGame().getMaxPlayers()) {
                        sender.sendMessage(DARK_RED + "Erreur : " + RED + "Il n'y a pas assez de points de spawn !");
                        sender.sendMessage(DARK_GRAY + "" + ITALIC + "Pour les définir, merci de faire " + GRAY + "/lg addSpawn");
                        return true;
                    }
                    sender.sendMessage(GREEN + "Vous avez bien démarré une nouvelle partie !");
                    getCurrentGame().updateStart();
                    return true;
                } else if (args[0].equalsIgnoreCase("reloadconfig")) {
                    sender.sendMessage(GREEN + "Vous avez bien reload la config !");
                    sender.sendMessage(GRAY + "" + ITALIC + "Si vous avez changé les rôles, écriver " + DARK_GRAY + "" + ITALIC + "/lg joinall" + GRAY + "" + ITALIC + " !");
                    loadConfig();
                    return true;
                } else if (args[0].equalsIgnoreCase("joinall")) {
                    for (Player p : Bukkit.getOnlinePlayers())
                        Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, "joinall"));
                    for (Player p : Bukkit.getOnlinePlayers())
                        Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, "joinall"));
                    return true;
                } else if (args[0].equalsIgnoreCase("reloadPacks")) {
                    for (Player p : Bukkit.getOnlinePlayers())
                        Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, "reloadPacks"));
                    for (Player p : Bukkit.getOnlinePlayers())
                        Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, "reloadPacks"));
                    return true;
                } else if (args[0].equalsIgnoreCase("nextNight")) {
                    sender.sendMessage(GREEN + "Vous êtes passé à la prochaine nuit");
                    if (getCurrentGame() != null) {
                        getCurrentGame().broadcastMessage(DARK_GREEN + "" + BOLD + "Le passage à la prochaine nuit a été forcé !", true);
                        for (LGPlayer lgp : getCurrentGame().getInGame())
                            lgp.stopChoosing();
                        getCurrentGame().cancelWait();
                        getCurrentGame().nextNight();
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("nextDay")) {
                    sender.sendMessage(GREEN + "Vous êtes passé à la prochaine journée");
                    if (getCurrentGame() != null) {
                        getCurrentGame().broadcastMessage(DARK_GREEN + "" + BOLD + "Le passage à la prochaine journée a été forcé !", true);
                        getCurrentGame().cancelWait();
                        for (LGPlayer lgp : getCurrentGame().getInGame())
                            lgp.stopChoosing();
                        getCurrentGame().endNight();
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("roles")) {
                    if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
                        sender.sendMessage(GOLD + "Voici la liste des rôles:");
                        int index = 0;
                        for (String role : getRoles().keySet())
                            sender.sendMessage("  " + YELLOW + "- " + index++ + " - " + GOLD + "" + role + " " + YELLOW + "> " + MainLg.getInstance().getConfig().getInt("role." + role));
                        sender.sendMessage("\n" + " " + GRAY + "Écrivez " + DARK_GRAY + "" + ITALIC + "/lg roles set <role_id/role_name> <nombre>" + GRAY + " pour définir le nombre de joueurs qui devrons avoir ce rôle.");
                    } else {
                        if (args[1].equalsIgnoreCase("set") && args.length == 4) {
                            String role = null;
                            if (args[2].length() <= 2)
                                try {
                                    int i = Integer.parseInt(args[2]);
                                    Object[] array = getRoles().keySet().toArray();
                                    if (array.length <= i) {
                                        sender.sendMessage(DARK_RED + "Erreur: " + RED + "Ce rôle n'existe pas.");
                                        return true;
                                    } else
                                        role = (String) array[i];
                                } catch (Exception err) {
                                    sender.sendMessage(DARK_RED + "Erreur: " + RED + "Ceci n'est pas un nombre");
                                }
                            else
                                role = args[2];

                            if (role != null) {
                                String real_role = null;
                                for (String real : getRoles().keySet())
                                    if (real.equalsIgnoreCase(role)) {
                                        real_role = real;
                                        break;
                                    }

                                if (real_role != null) {
                                    try {
                                        MainLg.getInstance().getConfig().set("role." + real_role, Integer.valueOf(args[3]));
                                        sender.sendMessage(GOLD + "Il y aura " + YELLOW + "" + args[3] + " " + GOLD + "" + real_role);
                                        saveConfig();
                                        loadConfig();
                                        makeNewGame();
                                        sender.sendMessage(GRAY + "" + ITALIC + "Si vous avez fini de changer les rôles, écriver " + DARK_GRAY + "" + ITALIC + "/lg joinall" + GRAY + "" + ITALIC + " !");
                                    } catch (Exception err) {
                                        sender.sendMessage(DARK_RED + "Erreur: " + RED + "" + args[3] + " n'est pas un nombre");
                                    }
                                    return true;
                                }
                            }
                            sender.sendMessage(DARK_RED + "Erreur: " + RED + "Le rôle que vous avez entré est incorrect");

                        } else {
                            sender.sendMessage(DARK_RED + "Erreur: " + RED + "Commande incorrecte.");
                            sender.sendMessage(DARK_RED + "Essayez " + RED + "/lg roles set <role_id/role_name> <nombre>" + DARK_RED + " ou " + RED + "/lg roles list");
                        }
                    }
                    return true;
                }
            }
            else if(sender instanceof Player)
            {
                ((Player) sender).openInventory(ConfigManager.getMainConfigManager().getInventory());
                return true;
            }
            sender.sendMessage(DARK_RED + "Erreur: " + RED + "Commande incorrecte.");
            sender.sendMessage(DARK_RED + "Essayez /lg " + RED + "addSpawn/end/start/nextNight/nextDay/reloadConfig/roles/reloadPacks/joinAll");
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
        if (!sender.hasPermission("loupgarou.admin"))
            return new ArrayList<>(0);

        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("roles"))
                if (args.length == 2)
                    return getStartingList(args[1], "list", "set");
                else if (args.length == 3 && args[1].equalsIgnoreCase("set"))
                    return getStartingList(args[2], getRoles().keySet().toArray(new String[0]));
                else if (args.length == 4)
                    return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        } else if (args.length == 1)
            return getStartingList(args[0], "addSpawn", "end", "start", "nextNight", "nextDay", "reloadConfig", "roles", "joinAll", "reloadPacks");
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
