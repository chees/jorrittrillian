package game;

import game.Player.State;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class Game extends UntypedActor {
  private Map<ActorRef, Player> players;
  
  private Game() {
    players = new HashMap<>();
  }
  
  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof NewConnectionMsg) {
      sendAll("New connection");
      players.put(getSender(), new Player(getSender()));
    } if (message instanceof DisconnectMsg) {
      Player p = players.remove(getSender());
      sendAll(p.getName() + " disconnected");
    } if (message instanceof String) {
      handleCommand((String)message, players.get(getSender()));
    } else {
      unhandled(message);
    }
  }
  
  private void handleCommand(String cmd, Player p) {
    if (p.state == State.WAITING_FOR_NAME) {
      p.setName(cmd);
      if (p.getName().length() > 1) {
        p.state = State.STANDING;
        p.send("Welcome " + p.getName());
      } else {
        p.send("Try typing a name...");
      }
    } else {
      Logger.warn("Unhandled command: " + cmd);
    }
  }
  
  private void sendAll(String msg) {
    for (Player p : players.values()) {
      p.send(msg);
    }
  }
  
  public static class NewConnectionMsg {}
  public static class DisconnectMsg {}
}
