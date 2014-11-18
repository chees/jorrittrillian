package game;

import actors.Connection;
import akka.actor.ActorRef;

public class Player extends Character {
  
  private ActorRef connection;
  
  Room room;
  
  public Player(ActorRef connection, Room room) {
    this.connection = connection;
    this.room = room;
    state = State.WAITING_FOR_NAME;
    name = "New player";
    hp = 100;
  }

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
}
