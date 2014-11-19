package game;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import game.Character.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
      if (cmd.length() > 0)
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
      if (p.state == State.FIGHTING && tick % 5 == 0) {
        p.target.hp--;
        if (p.target.hp <= 0) {
          p.send("You killed " + p.target.name + "!");
          sendRoomBut(p.name + " killed " + p.target.name + "!", p.room, p);
          if ("Kylidra".equals(p.target.name))
            sendRoom("OMG!!! WTF!?!", p.room);
          p.room.mobs.remove(p.target);
          p.state = State.STANDING;
          p.target = null;
        } else {
          p.hp--;
          if (p.hp <= 0) {
            p.send("You died!");
            sendRoomBut(p.name + " died!", p.room, p);
            p.target.state = State.STANDING;
            p.target.target = null;
            p.state = State.STANDING;
            p.target = null;
            p.hp = 1;
            p.room.players.remove(p);
            p.room = rooms.get(100);
            p.room.players.add(p);
            p.send("You are automagically transported back to the watchtower.");
          } else {
            p.send("You: " + p.hp + " | " + p.target.name + ": " + p.target.hp);
            sendRoomBut(p.name + ": " + p.hp + " | " + p.target.name + ": " + p.target.hp, p.room, p);
          }
        }
      }
    }
  }
  
  private void handleCommand(String cmd, Player p) {
    String[] words = cmd.split(" ");
    if (p.state == State.WAITING_FOR_NAME) {
      p.setName(cmd);
      if (p.getName().length() > 0) {
        p.state = State.STANDING;
        p.send(p.getName() + " it is.");
        sendAllButSys(p.getName() + " joined", p);
        p.send(getIntroduction());
        handleCommand("look", p);
      } else {
        p.send("Try typing a name...");
      }
    } else if ("chat".startsWith(words[0])) {
      String msg = escapeHtml4(cmd.substring(words[0].length(), cmd.length()));
      sendAllBut(p.getName() + " chats: " + msg, p);
      p.send("You chat: " + msg);
    } else if ("down".startsWith(words[0])) {
      move(p, "down", 5);
    } else if ("east".startsWith(words[0])) {
      move(p, "east", 1);
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
      p.state = State.FIGHTING;
      p.target = target;
      target.state = State.FIGHTING;
      target.target = p;
      p.send("You attack " + target.name + "!");
      sendRoomBut(p.getName() + " attacks " + target.name + "!", p.room, p);
    } else if ("look".startsWith(words[0])) {
      p.send(p.room.display(p));
    } else if ("north".startsWith(words[0])) {
      move(p, "north", 0);
    } else if ("south".startsWith(words[0])) {
      move(p, "south", 2);
    } else if ("up".startsWith(words[0])) {
      move(p, "up", 4);
    } else if ("west".startsWith(words[0])) {
      move(p, "west", 3);
    } else {
      p.send("Huh?");
    }
  }
  
  private void move(Player p, String direction, int exit) {
    Room origin = p.room;
    if (origin.exits[exit] == 0)
      p.send("You can't go " + direction + " here");
    else {
      origin.players.remove(p);
      sendRoom(p.getName() + " leaves " + direction, origin);
      Room destination = rooms.get(origin.exits[exit]);
      sendRoom(p.getName() + " enters", destination);
      p.room = destination;
      destination.players.add(p);
      handleCommand("look", p);
    }
  }
  
  private void sendAll(String msg) {
    for (Player p : players.values()) {
      p.send(msg);
    }
  }
  
  private void sendAllSys(String msg) {
    sendAll("<span class=\"sys\">" + msg + "</span>");
  }
  
  private void sendAllBut(String msg, Player excluded) {
    for (Player p : players.values()) {
      if (p != excluded)
        p.send(msg);
    }
  }
  
  private void sendAllButSys(String msg, Player excluded) {
    sendAllBut("<span class=\"sys\">" + msg + "</span>", excluded);
  }
  
  private void sendRoom(String msg, Room room) {
    for (Player p : room.players) {
      p.send(msg);
    }
  }
  
  private void sendRoomBut(String msg, Room room, Player excluded) {
    for (Player p : room.players) {
      if (p != excluded)
        p.send(msg);
    }
  }
  
  private String getIntroduction() {
    return "The floor suddenly opens up and you fall through a big hole.<br>" +
        "You pass out...<br><br>" +
        "When you wake up you look around:";
  }
  
  public static class NewConnectionMsg {}
  public static class DisconnectMsg {}
  public static class Tick {}
}
