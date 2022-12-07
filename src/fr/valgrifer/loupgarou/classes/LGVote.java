package fr.valgrifer.loupgarou.classes;

import java.util.*;
import java.util.Map.Entry;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.utils.NMSUtils;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.valgrifer.loupgarou.classes.LGPlayer.LGChooseCallback;
import fr.valgrifer.loupgarou.events.LGVoteLeaderChange;
import lombok.Getter;

public class LGVote {
	@Getter LGPlayer choosen;
	private int timeout;
    private final int initialTimeout, littleTimeout;
	private Runnable callback;
	private final LGGame game;
	@Getter private List<LGPlayer> participants, viewers;
	private final LGGame.TextGenerator generator;
	@Getter private final Map<LGPlayer, List<LGPlayer>> votes = new HashMap<>();
	private int votesSize = 0;
	private LGPlayer mayor;
	private List<LGPlayer> latestTop = new ArrayList<>(), blacklisted = new ArrayList<>();
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final boolean hiveViewersMessage, randomIfEqual;
	@Getter private boolean mayorVote;
    private boolean ended;
	public LGVote(int timeout, int littleTimeout, LGGame game, boolean hiveViewersMessage, boolean randomIfEqual, LGGame.TextGenerator generator) {
		this.littleTimeout = littleTimeout;
		this.initialTimeout = timeout;
		this.timeout = timeout;
		this.game = game;
		this.generator = generator;
		this.hiveViewersMessage = hiveViewersMessage;
		this.randomIfEqual = randomIfEqual;
	}
	public void start(List<LGPlayer> participants, List<LGPlayer> viewers, Runnable callback) {
		this.callback = callback;
		this.participants = participants;
		this.viewers = viewers;
		game.wait(timeout, this::end, generator);
		for(LGPlayer player : participants)
			player.choose(getChooseCallback(player));
	}
	public void start(List<LGPlayer> participants, List<LGPlayer> viewers, Runnable callback, ArrayList<LGPlayer> blacklisted) {
		this.callback = callback;
		this.participants = participants;
		this.viewers = viewers;
		game.wait(timeout, this::end, generator);
		for(LGPlayer player : participants)
			player.choose(getChooseCallback(player));
		this.blacklisted = blacklisted;
	}
	public void start(List<LGPlayer> participants, List<LGPlayer> viewers, Runnable callback, LGPlayer mayor) {
		this.callback = callback;
		this.participants = participants;
		this.viewers = viewers;
		this.mayor = mayor;
		game.wait(timeout, this::end, generator);
		for(LGPlayer player : participants)
			player.choose(getChooseCallback(player));
	}
	private void end() {
		ended = true;
		for(LGPlayer lgp : viewers)
			showVoting(lgp, null);
		for(LGPlayer lgp : votes.keySet())
			updateVotes(lgp, true);
		int max = 0;
		boolean equal = false;
		for(Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
			if(entry.getValue().size() > max) {
				equal = false;
				max = entry.getValue().size();
				choosen = entry.getKey();
			}else if(entry.getValue().size() == max)
				equal = true;
		for(LGPlayer player : participants) {
			player.getCache().remove("vote");
			player.stopChoosing();
		}
		if(equal)
			choosen = null;
		if(equal && mayor == null && randomIfEqual) {
			List<LGPlayer> choosable = new ArrayList<>();
			for(Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
				if(entry.getValue().size() == max)
					choosable.add(entry.getKey());
			choosen = choosable.get(game.getRandom().nextInt(choosable.size()));
		}
		
		if(equal && mayor != null && max != 0) {
			for(LGPlayer player : viewers)
				player.sendMessage(BLUE+"Égalité, le "+DARK_PURPLE+""+BOLD+"Capitaine"+BLUE+" va départager les votes.");
			mayor.sendMessage(GOLD+"Tu dois choisir qui va mourir.");

			List<LGPlayer> choosable = new ArrayList<>();
			for(Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
				if(entry.getValue().size() == max)
					choosable.add(entry.getKey());
			
			for(int i = 0;i<choosable.size();i++) {
				LGPlayer lgp = choosable.get(i);
				showArrow(mayor, lgp, -mayor.getPlayer().getEntityId()-i);
			}
			
			StringJoiner sj = new StringJoiner(", ");
			for(int i = 0;i<choosable.size()-1;i++)
				sj.add(choosable.get(0).getName());
			//mayor.sendTitle(GOLD+"C'est à vous de délibérer", "Faut-il tuer "+sj+" ou "+choosable.get(choosable.size()-1).getName()+" ?", 100);
			List<LGPlayer> blackListed = new ArrayList<>();
			for(LGPlayer player : participants)
				if(!choosable.contains(player))
					blackListed.add(player);
				else {
					VariousUtils.setWarning(player.getPlayer(), true);
					//player.sendMessage(DARK_RED+""+BOLD+"Vous êtes un des principaux suspects ! Défendez vous !");
					//player.sendTitle(DARK_RED+""+BOLD+"Défendez vous !", RED+"Vous êtes l'un des principaux suspects", 100);
				}
			mayorVote = true;
			game.wait(30, ()->{
				for(LGPlayer player : participants)
					if(choosable.contains(player))
						VariousUtils.setWarning(player.getPlayer(), false);

				for(int i = 0;i<choosable.size();i++) {
					showArrow(mayor, null, -mayor.getPlayer().getEntityId()-i);
				}
				//Choix au hasard d'un joueur si personne n'a été désigné
				choosen = choosable.get(game.getRandom().nextInt(choosable.size()));
				callback.run();
			}, (player, secondsLeft)->{
				timeout = secondsLeft;
				return mayor == player ? GOLD+"Il te reste "+YELLOW+""+secondsLeft+" seconde"+(secondsLeft > 1 ? "s" : "")+""+GOLD+" pour délibérer" : GOLD+"Le "+DARK_PURPLE+""+BOLD+"Capitaine"+GOLD+" délibère ("+YELLOW+""+secondsLeft+" s"+GOLD+")";
			});
			mayor.choose(choosen -> {
                if(choosen != null) {
                    if(blackListed.contains(choosen))
                        mayor.sendMessage(DARK_RED+""+ITALIC+"Ce joueur n'est pas concerné par le choix.");
                    else {
                        for(LGPlayer player : participants)
                            if(choosable.contains(player))
                                VariousUtils.setWarning(player.getPlayer(), false);

                        for(int i = 0;i<choosable.size();i++) {
                            showArrow(mayor, null, -mayor.getPlayer().getEntityId()-i);
                        }
                        game.cancelWait();
                        LGVote.this.choosen = choosen;
                        callback.run();
                    }
                }
            });
		} else {
			game.cancelWait();
			callback.run();
		}
		
	}
	public LGChooseCallback getChooseCallback(LGPlayer who) {
		return choosen -> {
            if(choosen != null)
                vote(who, choosen);
        };
	}
	public void vote(LGPlayer voter, LGPlayer voted) {
		if(blacklisted.contains(voted)) {
			voter.sendMessage(RED+"Vous ne pouvez pas votre pour "+GRAY+""+BOLD+""+voted.getName()+""+RED+".");
			return;
		}
		if(voted == voter.getCache().get("vote"))
			voted = null;
		
		if(voted != null && voter.getPlayer() != null)
			votesSize++;
		if(voter.getCache().has("vote"))
			votesSize--;
		
		if(votesSize == participants.size() && game.getWaitTicks() > littleTimeout*20) {
			votesSize = 999;
			game.wait(littleTimeout, initialTimeout, this::end, generator);
		}
        boolean changeVote = false;
		if(voter.getCache().has("vote")) {//On enlève l'ancien vote
			LGPlayer devoted = voter.getCache().get("vote");
			if(votes.containsKey(devoted)) {
				List<LGPlayer> voters = votes.get(devoted);
				if(voters != null) {
					voters.remove(voter);
					if(voters.size() == 0)
						votes.remove(devoted);
				}
			}
			voter.getCache().remove("vote");
			updateVotes(devoted);
			changeVote = true;
		}
		
		if(voted != null) {//Si il vient de voter, on ajoute le nouveau vote
			//voter.sendTitle("", GRAY+"Tu as voté pour "+GRAY+""+BOLD+""+voted.getName(), 40);
			if(votes.containsKey(voted))
				votes.get(voted).add(voter);
			else
				votes.put(voted, new ArrayList<>(Collections.singletonList(voter)));
			voter.getCache().set("vote", voted);
			updateVotes(voted);
		}
		
		if(voter.getPlayer() != null) {
			showVoting(voter, voted);
			String message;
			if(voted != null) {
				if(changeVote) {
					message = GRAY+""+BOLD+""+voter.getName()+""+GOLD+" a changé son vote pour "+GRAY+""+BOLD+""+voted.getName()+""+GOLD+".";
					voter.sendMessage(GOLD+"Tu as changé de vote pour "+GRAY+""+BOLD+""+voted.getName()+""+GOLD+".");
				} else {
					message = GRAY+""+BOLD+""+voter.getName()+""+GOLD+" a voté pour "+GRAY+""+BOLD+""+voted.getName()+""+GOLD+".";
					voter.sendMessage(GOLD+"Tu as voté pour "+GRAY+""+BOLD+""+voted.getName()+""+GOLD+".");
				}
			} else {
				message = GRAY+""+BOLD+""+voter.getName()+""+GOLD+" a annulé son vote.";
				voter.sendMessage(GOLD+"Tu as annulé ton vote.");
			}

			if(!hiveViewersMessage)
                for(LGPlayer player : viewers)
                    if(player != voter)
                        player.sendMessage(message);
		}
	}
	
	public List<LGPlayer> getVotes(LGPlayer voted){
		return votes.containsKey(voted) ? votes.get(voted) : new ArrayList<>(0);
	}

    private static final ArmorStand eas = NMSUtils.getInstance().newArmorStand();
	private void updateVotes(LGPlayer voted) {
		updateVotes(voted, false);
	}
	private void updateVotes(LGPlayer voted, boolean kill) {
		int entityId = Integer.MIN_VALUE+voted.getPlayer().getEntityId();
		WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
		destroy.setEntityIds(new int[] {entityId});
		for(LGPlayer lgp : viewers)
			destroy.sendPacket(lgp.getPlayer());
		
		if(!kill) {
			int max = 0;
			for(Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
				if(entry.getValue().size() > max)
					max = entry.getValue().size();
			List<LGPlayer> last = latestTop;
			latestTop = new ArrayList<>();
			for(Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
				if(entry.getValue().size() == max)
					latestTop.add(entry.getKey());
			Bukkit.getPluginManager().callEvent(new LGVoteLeaderChange(game, this, last, latestTop));
		}
		
		if(votes.containsKey(voted) && !kill) {
			Location loc = voted.getPlayer().getLocation();

			WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
			spawn.setEntityID(entityId);
			spawn.setType(EntityType.DROPPED_ITEM);
			//spawn.setMetadata(new WrappedDataWatcher(Arrays.asList(new WrappedWatchableObject(0, (byte)0x20), new WrappedWatchableObject(5, true))));
			spawn.setX(loc.getX());
			spawn.setY(loc.getY()+0.3);
			spawn.setZ(loc.getZ());
			
			int votesNbr = votes.get(voted).size();
			final int numberOfParticipants = participants.size();
			final double votePercentage =  ((double)votesNbr / numberOfParticipants) * 100;
			final String votePercentageFormated = String.format("%.0f%%", votePercentage);
			final String voteContent = GOLD+""+BOLD+"" + votesNbr + " / " + numberOfParticipants + YELLOW+" vote" + (votesNbr > 1 ? "s" : "") + " ("+GOLD+""+BOLD+"" + votePercentageFormated + YELLOW+")";

			/*WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
			meta.setEntityID(entityId);
			meta.setMetadata(Arrays.asList(new WrappedWatchableObject(invisible, (byte)0x20), new WrappedWatchableObject(noGravity, true), new WrappedWatchableObject(customNameVisible, true), new WrappedWatchableObject(customName, IChatBaseComponent.ChatSerializer.b(GOLD+""+BOLD+""+votesNbr+""+YELLOW+" vote"+(votesNbr > 1 ? "s" : "")))));
			*/

            for(LGPlayer lgp : viewers)
                spawn.sendPacket(lgp.getPlayer());
            NMSUtils.getInstance().updateArmorStandNameFor(eas, entityId, voteContent, viewers);
			
			
		/*	EntityArmorStand ea = new EntityArmorStand(((CraftWorld)loc.getWorld()).getHandle(), loc.getX(), loc.getY()+0.3, loc.getZ());
			ea.setPosition(loc.getX(), loc.getY()+0.3, loc.getZ());
			ea.setInvisible(true);
			ea.setCustomNameVisible(true);
			int votesNbr = votes.get(voted).size();
			ea.setCustomName((IChatBaseComponent) WrappedChatComponent.fromText(GOLD+""+BOLD+""+votesNbr+""+YELLOW+" vote"+(votesNbr > 1 ? "s" : "")).getHandle());
			
			PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(ea);
			try {
				Field field = spawn.getClass().getDeclaredField("a");
				field.setAccessible(true);
				field.set(spawn, entityId);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		/*	WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
			spawn.setEntityID(entityId);
			spawn.setType(EntityType.ARMOR_STAND);
			WrappedDataWatcher meta = new WrappedDataWatcher();
			meta.setObject(0, (byte)0x20);
			meta.setObject(2, GOLD+""+BOLD+""+votes.get(voted)+""+YELLOW+" votes");
		//	meta.setObject(3, true);
			spawn.setMetadata(meta);
			Location loc = voted.getPlayer().getLocation();
			spawn.setX(loc.getX());
			spawn.setY(loc.getY()+0.3);
			spawn.setZ(loc.getZ());*/
		/*	for(LGPlayer lgp : viewers)
				((CraftPlayer)lgp.getPlayer()).getHandle().playerConnection.sendPacket(spawn);*/
			//	spawn.sendPacket(lgp.getPlayer());
		}
	}
	WrappedDataWatcher.WrappedDataWatcherObject invisible = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
    WrappedDataWatcher.WrappedDataWatcherObject noGravity = new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));
	private void showVoting(LGPlayer to, LGPlayer ofWho) {
		int entityId = -to.getPlayer().getEntityId();
		WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
		destroy.setEntityIds(new int[] {entityId});
		destroy.sendPacket(to.getPlayer());
		if(ofWho != null) {
			WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
			spawn.setEntityID(entityId);
			spawn.setType(EntityType.DROPPED_ITEM);
			//spawn.setMetadata(new WrappedDataWatcher(Arrays.asList(new WrappedWatchableObject(0, (byte)0x20), new WrappedWatchableObject(5, true))));
			Location loc = ofWho.getPlayer().getLocation();
			spawn.setX(loc.getX());
			spawn.setY(loc.getY()+1.3);
			spawn.setZ(loc.getZ());
			spawn.setHeadPitch(0);
			Location toLoc = to.getPlayer().getLocation();
			double diffX = loc.getX()-toLoc.getX(),
				   diffZ = loc.getZ()-toLoc.getZ();
			float yaw = 180-((float) Math.toDegrees(Math.atan2(diffX, diffZ)));
			
			spawn.setYaw(yaw);
			spawn.sendPacket(to.getPlayer());
			
			WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
			meta.setEntityID(entityId);
			meta.setMetadata(Arrays.asList(new WrappedWatchableObject(invisible, (byte)0x20), new WrappedWatchableObject(noGravity, true)));
			meta.sendPacket(to.getPlayer());
			
			WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
			look.setEntityID(entityId);
			look.setPitch(0);
			look.setYaw(yaw);
			look.sendPacket(to.getPlayer());
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment();
					equip.setEntityID(entityId);
					equip.setItem(ItemSlot.HEAD, new ItemStack(Material.EMERALD));
					equip.sendPacket(to.getPlayer());
				}
			}.runTaskLater(MainLg.getInstance(), 2);
		}
	}
	
	private void showArrow(LGPlayer to, LGPlayer ofWho, int entityId) {
		WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
		destroy.setEntityIds(new int[] {entityId});
		destroy.sendPacket(to.getPlayer());
		if(ofWho != null) {
			WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
			spawn.setEntityID(entityId);
			spawn.setType(EntityType.DROPPED_ITEM);
			//spawn.setMetadata(new WrappedDataWatcher());
			Location loc = ofWho.getPlayer().getLocation();
			spawn.setX(loc.getX());
			spawn.setY(loc.getY()+1.3);
			spawn.setZ(loc.getZ());
			spawn.setHeadPitch(0);
			Location toLoc = to.getPlayer().getLocation();
			double diffX = loc.getX()-toLoc.getX(),
				   diffZ = loc.getZ()-toLoc.getZ();
			float yaw = 180-((float) Math.toDegrees(Math.atan2(diffX, diffZ)));
			
			spawn.setYaw(yaw);
			spawn.sendPacket(to.getPlayer());
			
			WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
			meta.setEntityID(entityId);
			meta.setMetadata(Arrays.asList(new WrappedWatchableObject(invisible, (byte)0x20), new WrappedWatchableObject(noGravity, true)));
			meta.sendPacket(to.getPlayer());
			
			WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
			look.setEntityID(entityId);
			look.setPitch(0);
			look.setYaw(yaw);
			look.sendPacket(to.getPlayer());
			
			new BukkitRunnable() {
				@Override
				public void run() {
					WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment();
					equip.setEntityID(entityId);
					equip.setItem(ItemSlot.HEAD, new ItemStack(Material.EMERALD));
					equip.sendPacket(to.getPlayer());
				}
			}.runTaskLater(MainLg.getInstance(), 2);
		}
	}
	public void remove(LGPlayer killed) {
		participants.remove(killed);
		if(!ended) {
			votes.remove(killed);
			latestTop.remove(killed);
		}
	}
}
