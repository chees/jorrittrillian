package game;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import game.Player.State;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class Game extends UntypedActor {
  
  private Map<ActorRef, Player> players;
  private Map<Integer, Room> rooms;
  
  private Game() {
    players = new HashMap<>();
    rooms = Loader.getRooms();
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
    } if (message instanceof DisconnectMsg) {
      Player p = players.remove(getSender());
      p.room.players.remove(p);
      sendAllSys(p.getName() + " disconnected");
    } if (message instanceof String) {
      String cmd = ((String) message).trim();
      if (cmd.length() > 0)
        handleCommand(cmd, players.get(getSender()));
    } else {
      unhandled(message);
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
    } else if ("look".startsWith(words[0])) {
      Room r = p.room;
      p.send("<div class=\"room\"><h2>" + r.title + "</h2>" + r.description + "</div>");
    } else if ("north".startsWith(words[0])) {
      Room origin = p.room;
      if (origin.exits[0] == 0)
        p.send("You can't go north here");
      else {
        origin.players.remove(p);
        sendRoom(p.getName() + " leaves north", origin);
        Room destination = rooms.get(origin.exits[0]);
        sendRoom(p.getName() + " enters", destination);
        p.room = destination;
        destination.players.add(p);
        handleCommand("look", p);
      }
    } else {
      p.send("Huh?");
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
  
  private String getIntroduction() {
    return "The floor suddenly opens up and you fall through a big hole.<br>" +
        "You pass out...<br><br>" +
        "When you wake up you look around:";
  }
  
  public static class NewConnectionMsg {}
  public static class DisconnectMsg {}
}
