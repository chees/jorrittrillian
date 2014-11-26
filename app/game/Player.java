package game;

import java.util.HashSet;
import java.util.Set;

import actors.Connection;
import akka.actor.ActorRef;

public class Player extends Character {
  
  private ActorRef connection;
  
  Room room;
  long exp;
  boolean killedKerrigan;
  boolean caughtClaptrap;
  Set<Achievement> achievements;
  
  public Player(ActorRef connection, Room room) {
    this.connection = connection;
    this.room = room;
    state = State.WAITING_FOR_NAME;
    name = "New player";
    hp = 100;
    hpMax = hp;
    hpRegen = 1;
    level = 1;
    achievements = new HashSet<>();
  }

  @Override
  public void send(String msg) {
    connection.tell(new Connection.OutputMsg(msg), null);
  }

  public void setName(String name) {
    this.name = name.replaceAll("[^a-zA-Z]", "");
    this.name = this.name.substring(0, Math.min(16, this.name.length()));
    // Capitalize:
    if (this.name.length() > 0)
      this.name = this.name.substring(0, 1).toUpperCase() + this.name.substring(1);
  }
  
  public String getName() {
    return name;
  }

  public void addExp(int e) {
    exp += e;
    send("You gained " + e + " exp. You now have: " + exp);
    if (exp >= level * 1000) {
      level++;
      send("You leveled up! You're now level " + level);
    }
  }
  
  @Override
  public boolean isEnemy() {
    return false;
  }
}
