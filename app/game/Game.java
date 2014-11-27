package game;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import game.Character.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import play.Logger;
import play.Play;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class Game extends UntypedActor {
  
  private Map<ActorRef, Player> players;
  private List<Area> areas;
  private Map<Integer, Room> rooms;
  //private Map<Integer, Mob> mobs;
  private long tick;
  
  private Game() {
    players = new HashMap<>();
    Loader loader = new Loader();
    areas = loader.getAreas();
    rooms = loader.getRooms();
    //mobs = loader.getMobs();
    for (Area a : areas) a.respawn();
    
    getContext().system().scheduler().scheduleOnce(
        Duration.create(250, TimeUnit.MILLISECONDS),
        getSelf(), new Tick(), getContext().dispatcher(), null);
  }
  
  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof NewConnectionMsg) {
      sendAllSys("New connection");
      Room room = rooms.get(100);
      Player p = new Player(getSender(), room);
      players.put(getSender(), p);
      room.players.add(p);
      p.send("What is your name?");
    } else if (message instanceof DisconnectMsg) {
      Player p = players.remove(getSender());
      p.room.players.remove(p);
      if (p.target != null) {
        p.target.target = null;
        p.target.state = State.STANDING;
      }
      sendAllSys(p.getName() + " disconnected");
    } else if (message instanceof String) {
      String cmd = ((String) message).trim();
      handleCommand(cmd, players.get(getSender()));
    } else if (message instanceof Tick) {
      tick();
      getContext().system().scheduler().scheduleOnce(
          Duration.create(200, TimeUnit.MILLISECONDS),
          getSelf(), new Tick(), getContext().dispatcher(), null);
    } else {
      unhandled(message);
    }
  }
  
  private void tick() {
    tick++;
    for (Player p : players.values()) {
      if (p.state == State.FIGHTING && tick % 1 == 0)
        tickFight(p);
      if (tick % (5 * 10) == 0)
        p.regen();
    }
    if (tick % (5 * 60) == 0) {
      sendAllAwake("You feel a strange wind.");
      for (Area a : areas)
        a.respawn();
    }
  }

  private void tickFight(Player p) {
    p.target.hp -= p.level;
    if (p.target.hp <= 0) {
      if (((Mob)p.target).id == 302) {
        sendRoom("Kerrigan says: You are such a lucker.", p.room);
        awardAchievement(Achievement.KERRIGAN, p);
        p.killedKerrigan = true;
      }
      p.send("You killed " + p.target.name + "!");
      sendRoomBut(p.name + " killed " + p.target.name + "!", p.room, p);
      if (((Mob)p.target).id == 100) {
        sendRoom("OMG!!! WTF!?!", p.room);
        awardAchievement(Achievement.KILL_KYLIDRA, p);
      }
      p.room.mobs.remove(p.target);
      p.state = State.STANDING;
      p.addExp(75 + (int)(1.5 * Math.random() * p.target.hpMax));
      if (p.level >= 10)
        awardAchievement(Achievement.LEVEL_10, p);
      p.target = null;
    } else {
      p.hp--;
      if (p.hp <= 0) {
        p.send("You died!");
        sendRoomBut(p.name + " died!", p.room, p);
        awardAchievement(Achievement.DIE, p);
        p.target.state = State.STANDING;
        p.target.target = null;
        p.state = State.STANDING;
        p.target = null;
        p.hp = 1;
        p.room.players.remove(p);
        p.room = rooms.get(100);
        p.room.players.add(p);
        p.exp -= 1000;
        p.send("You lost 1000 exp and you are automagically transported back to the watchtower.");
      } else {
        p.send("You: " + p.hp + " | " + p.target.name + ": " + p.target.hp);
        sendRoomBut(p.name + ": " + p.hp + " | " + p.target.name + ": " + p.target.hp, p.room, p);
      }
    }
  }
  
  private void handleCommand(String cmd, Player p) {
    String[] words = cmd.split(" ");
    // TODO refactor this giant if else:
    if (p.state == State.WAITING_FOR_NAME) {
      p.setName(cmd);
      if (p.getName().length() > 0) {
        p.state = State.STANDING;
        p.send("Welcome " + p.getName() + " :)");
        sendAllButSys(p.getName() + " joined", p);
        p.send(getIntroduction());
        handleCommand("look", p);
      } else {
        p.send("Try typing a name...");
      }
    } else if (words[0].length() == 0) {
      p.send("hp: " + p.hp + " | exp: " + p.exp);
    } else if ("chat".startsWith(words[0])) {
      String msg = escapeHtml4(cmd.substring(words[0].length(), cmd.length()));
      if (msg.isEmpty()) {
        p.send("Chat what?");
      } else {
        sendAllBut(p.getName() + " chats: " + msg, p);
        p.send("You chat: " + msg);
      }
    } else if ("down".startsWith(words[0])) {
      move(p, "down", 5);
    } else if ("click".startsWith(words[0]) && words.length == 2 && "http://goo.gl/RFlt2A".equals(words[1])) {
      awardAchievement(Achievement.RICKROLL, p);
    } else if ("east".startsWith(words[0])) {
      move(p, "east", 1);
    } else if ("grab".startsWith(words[0])) {
      if (p.room.containsMob(400)) {
        p.send("You grab Claptrap! You take a thingy from him before letting him go again.");
        sendRoomBut(p.name + " grabs Claptrap! " + p.name + " takes a thingy from him before letting him go again.", p.room, p);
        p.caughtClaptrap = true;
        awardAchievement(Achievement.CLAPTRAP, p);
      } else {
        p.send("Grab what?");
      }
    } else if ("grin".startsWith(words[0])) {
      p.send("You grin.");
      sendRoomBut(p.name + " grins.", p.room, p);
      awardAchievement(Achievement.GRIN, p);
    } else if ("goto".startsWith(words[0]) && Play.isDev()) {
      if (words.length < 2) {
        p.send("Goto where?");
        return;
      }
      Room target = rooms.get(Integer.parseInt(words[1]));
      if (target == null) {
        p.send("Room not found.");
        return;
      }
      p.room.players.remove(p);
      p.room = target;
      p.room.players.add(p);
    } else if ("hop".startsWith(words[0])) {
      p.send("You hop around like a little kid.");
      sendRoomBut(p.name + " hops around like a little kid.", p.room, p);
    } else if ("kill".startsWith(words[0])) {
      if (words.length < 2) {
        p.send("Kill what?");
        return;
      }
      Mob target = p.room.getMob(words[1]);
      if (target == null) {
        p.send("There's no " + escapeHtml4(words[1]) + " to kill here.");
        return;
      }
      if (target.hp == 0) {
        p.send("You can't attack " + target.name + ".");
        return;
      }
      if (target.state == State.FIGHTING) {
        p.send("OMG KillStealer!");
        sendRoomBut("OMG " + p.name + " is KS-ing!", p.room, p);
        awardAchievement(Achievement.KILL_STEALER, p);
        return;
      }
      p.state = State.FIGHTING;
      p.target = target;
      target.state = State.FIGHTING;
      target.target = p;
      p.send("You attack " + target.name + "!");
      sendRoomBut(p.getName() + " attacks " + target.name + "!", p.room, p);
    } else if ("look".startsWith(words[0])) {
      if (p.state == State.SLEEPING)
        handleCommand("wake", p);
      p.send(p.room.display(p));
    } else if ("north".startsWith(words[0])) {
      move(p, "north", 0);
    } else if ("recall".startsWith(words[0])) {
      switch (p.state) {
      case FIGHTING:
        p.send("You can't recall while fighting.");
        break;
      case SLEEPING:
        p.send("You can't recall while sleeping.");
        break;
      case STANDING:
        p.send("You teleport back to the watchtower.");
        sendRoomBut(p.name + " disappears in a puff of smoke.", p.room, p);
        p.room.players.remove(p);
        p.room = rooms.get(100);
        p.room.players.add(p);
        sendRoomBut(p.name + " appears in a puff of smoke.", p.room, p);
        handleCommand("look", p);
        break;
      case WAITING_FOR_NAME:
        break;
      default:
        Logger.warn("Missing recall case: " + p.state);
        break;
      }
    } else if ("south".startsWith(words[0])) {
      move(p, "south", 2);
    } else if ("setlevel".startsWith(words[0]) && Play.isDev()) {
      p.level = Integer.parseInt(words[1]);
    } else if ("sleep".startsWith(words[0])) {
      switch (p.state) {
      case FIGHTING:
        p.send("You can't sleep while fighting.");
        break;
      case SLEEPING:
        p.send("You're already sleeping.");
        break;
      case STANDING:
        p.send("You go to sleep.");
        sendRoomBut(p.name + " goes to sleep.", p.room, p);
        p.state = State.SLEEPING;
        break;
      case WAITING_FOR_NAME:
        break;
      default:
        Logger.warn("Missing sleep case: " + p.state);
        break;
      }
    } else if ("stare".startsWith(words[0])) {
      if (p.state == State.SLEEPING) {
        p.send("You stare at your dreams.");
        return;
      }
      if (words.length < 2) {
        p.send("You stare into the void.");
        sendRoomBut(p.name + " stares into the void.", p.room, p);
        return;
      }
      Mob target = p.room.getMob(words[1]);
      if (target == null) {
        p.send("There's no " + escapeHtml4(words[1]) + " to stare at.");
        return;
      }
      p.send("You stare at " + target.name + ".");
      sendRoomBut(p.name + " stares at " + target.name + ".", p.room, p);
      if (target.id == 500 || target.id == 501) {
        target.staredAt++;
        if (target.staredAt == 1)
          sendRoom(target.name + " stares back.", p.room);
        else if (target.staredAt == 2)
          sendRoom(target.name + " stares back some more.", p.room);
        else if (target.staredAt == 3)
          sendRoom(target.name + " stares back even more!", p.room);
        else if (target.staredAt == 4) {
          sendRoom(target.name + " says: Fine, you win! meow<br>" + target.name + " walks away.", p.room);
          p.room.mobs.remove(target);
          finishGame(p);
        }
      }
    } else if ("up".startsWith(words[0])) {
      move(p, "up", 4);
    } else if ("west".startsWith(words[0])) {
      move(p, "west", 3);
    } else if ("wake".startsWith(words[0])) {
      if (p.state == State.SLEEPING) {
        p.send("You wake up.");
        sendRoomBut(p.name + " wakes up.", p.room, p);
        p.state = State.STANDING;
      } else {
        p.send("You're not even sleeping.");
      }
    } else {
      p.send("Huh?");
    }
  }
  
  private void move(Player p, String direction, int exit) {
    if (p.state == State.FIGHTING) {
      p.send("You're in the middle of a fight!");
      return;
    }
    if (p.state == State.SLEEPING) {
      p.send("Zzzzzzzzz");
      return;
    }
    Room origin = p.room;
    if (origin.exits[exit] == 0) {
      p.send("You can't go " + direction + " here.");
      return;
    }
    origin.players.remove(p);
    sendRoom(p.getName() + " leaves " + direction + ".", origin);
    Room destination = rooms.get(origin.exits[exit]);
    sendRoom(p.getName() + " enters.", destination);
    p.room = destination;
    destination.players.add(p);
    handleCommand("look", p);
    handleEntry(p);
  }
  
  private void handleEntry(Player p) {
    // TODO refactor this giant if else:
    if (p.room.id == 300) {
      if (p.level < 2) {
        sendRoom("Raynor says: Get out of here kid! Get a bit stronger first.", p.room);
        handleCommand("west", p);
      } else {
        sendRoom("Raynor says: Oh hi, I'm having some trouble with my wife. She's in there somewhere complaining about her hair. Maybe you could talk to her?", p.room);
      }
    }
    else if (p.room.id == 309) {
      Mob kerrigan = p.room.getMob("kerrigan");
      if (kerrigan != null && kerrigan.state == State.STANDING) {
        sendRoom("Kerrigan says: Noooo, my hair looks terrible! Nobody can see me like this! DIE!!", p.room);
        p.send("Kerrigan attacks you!");
        sendRoomBut("Kerrigan attacks " + p.name + "!", p.room, p);
        p.state = State.FIGHTING;
        p.target = kerrigan;
        kerrigan.state = State.FIGHTING;
        kerrigan.target = p;
      }
    }
    else if (p.room.id == 400) {
      if (p.killedKerrigan) {
        p.send("After surviving Kerrigan this little robot is no match for you anymore and you easily walk past it.");
        handleCommand("north", p);
      } else {
        p.send("Oh no, the robot starts shooting at you! Run!");
        handleCommand("south", p);
      }
    }
    else if (p.room.id == 403) {
      moveClaptrap(p.room, "west", 402,
          "<audio src=\"/assets/sounds/Claptrap- N - Lets go this way.mp3\" autoplay></audio>" +
          "Claptrap says: You'll never catch Claptrap! That's me you know. I am faster than your own shadow. " +
          "Or my shadow, but your shadow looks bigger so it's more impressive if I'm faster than that.");
    }
    else if (p.room.id == 402) {
      moveClaptrap(p.room, "north", 405,
          "<audio src=\"/assets/sounds/Claptrap- N - Over here.mp3\" autoplay></audio>");
    }
    else if (p.room.id == 405) {
      moveClaptrap(p.room, "north", 408,
          "<audio src=\"/assets/sounds/Claptrap- N - Onward.mp3\" autoplay></audio>" +
          "Claptrap says: I hope you have your sound on, cause I'm about to sing you a song!");
    }
    else if (p.room.id == 408) {
      moveClaptrap(p.room, "east", 409,
          "<audio src=\"/assets/sounds/Claptrap- Wubwubwub.mp3\" autoplay></audio>");
    }
    else if (p.room.id == 409) {
      moveClaptrap(p.room, "east", 410,
          "<audio src=\"/assets/sounds/Claptrap- N - Just follow the silly sound of my voice.mp3\" autoplay></audio>");
    }
    else if (p.room.id == 410) {
      moveClaptrap(p.room, "south", 407,
          "<audio src=\"/assets/sounds/Claptrap- N - Vamanos Minion.mp3\" autoplay></audio>");
    }
    else if (p.room.id == 407) {
      moveClaptrap(p.room, "south", 404,
          "<audio src=\"/assets/sounds/Claptrap- N - This a way Minion.mp3\" autoplay></audio>");
    }
    else if (p.room.id == 404) {
      moveClaptrap(p.room, "west", 403,
          "<audio src=\"/assets/sounds/Claptrap- N - This way.mp3\" autoplay></audio>");
    }
    else if (p.room.id == 411 && p.caughtClaptrap) {
      p.send("You use your thingy to make the teleporter work.");
      sendRoomBut(p.name + " uses a thingy to make the teleporter work.", p.room, p);
      p.room.players.remove(p);
      p.room = rooms.get(500);
      p.room.players.add(p);
      handleCommand("look", p);
    }
  }
  
  private void moveClaptrap(Room from, String direction, int to, String say) {
    Mob claptrap = from.getMob("claptrap");
    if (claptrap != null) {
      sendRoom(say, from);
      sendRoom("Claptrap leaves " + direction + ".", from);
      from.mobs.remove(claptrap);
      Room destination = rooms.get(to);
      sendRoom("Claptrap rides in. Quick, GRAB him!", destination);
      destination.mobs.add(claptrap);
    }
  }
  
  // TODO refactor send* methods:
  
  private void sendAll(String msg) {
    for (Player p : players.values())
      p.send(msg);
  }
  
  private void sendAllAwake(String msg) {
    for (Player p : players.values())
      if (p.state != State.WAITING_FOR_NAME && p.state != State.SLEEPING)
        p.send(msg);
  }
  
  private void sendAllSys(String msg) {
    sendAll("<span class=\"sys\">" + msg + "</span>");
  }
  
  private void sendAllBut(String msg, Player excluded) {
    for (Player p : players.values())
      if (p != excluded)
        p.send(msg);
  }
  
  private void sendAllButSys(String msg, Player excluded) {
    sendAllBut("<span class=\"sys\">" + msg + "</span>", excluded);
  }
  
  private void sendRoom(String msg, Room room) {
    for (Player p : room.players)
      if (p.state != State.WAITING_FOR_NAME && p.state != State.SLEEPING)
        p.send(msg);
  }
  
  private void sendRoomBut(String msg, Room room, Player excluded) {
    for (Player p : room.players)
      if (p.state != State.WAITING_FOR_NAME && p.state != State.SLEEPING && p != excluded)
        p.send(msg);
  }
  
  private void awardAchievement(Achievement a, Player p) {
    if (p.achievements.contains(a))
      return;
    p.achievements.add(a);
    p.send("<div class=\"achievement\">Achievement unlocked! You " + a.description + ".</div>");
    sendAllBut("<div class=\"achievement\">" + p.name + " got an achievement! " + p.name + " " + a.description + ".</div>", p);
  }
  
  private String getIntroduction() {
    return "<br>Congratulations on your marriage! This is a little game that you can play together (OMG I love multiplayer games). Depending on how well you do there might even be real loot at the end.<br>" +
        "Btw, I only tested it in Chrome, so it might not work in other browsers =)<br>" +
        "Remember that you can type <em>help</em> at any time to to get some help. Have fun!<br><br>" +
        "The floor suddenly opens up and you fall through a big hole.<br>" +
        "You pass out...<br><br>" +
        "When you wake up you look around:";
  }
  
  private void finishGame(Player p) {
    sendRoomBut(p.name + " disappears.", p.room, p);
    p.room.players.remove(p);
    p.room = rooms.get(501);
    p.room.players.add(p);
    handleCommand("look", p);
    awardAchievement(Achievement.FINISH, p);
    if (System.currentTimeMillis() - p.startTimestamp < 1000 * 60 * 10)
      awardAchievement(Achievement.FINISH_IN_10_MINUTES, p);
    
    String output = "You unlocked " + p.achievements.size() + " out of " + Achievement.values().length + " achievements:<ul>";
    for (Achievement a : p.achievements) {
      output += "<li>You " + a.description + " (&euro; 5).";
    }
    output += "</ul>";
    output += "Congratulations " + p.name + "! You won &euro; " + p.achievements.size() * 5 + "<br>";
    output += "Send screenshots of your highest scores to Christiaan for your reward. (Yes really, I didn't have time to implement a decent highscore system :P)";
    p.send(output);
  }
  
  public static class NewConnectionMsg {}
  public static class DisconnectMsg {}
  public static class Tick {}
}
