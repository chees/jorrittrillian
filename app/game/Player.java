package game;

import actors.Connection;
import akka.actor.ActorRef;

public class Player {

  enum State { WAITING_FOR_NAME, STANDING }
  
  private ActorRef connection;
  private String name;
  State state;

  
  public Player(ActorRef connection) {
    this.connection = connection;
    state = State.WAITING_FOR_NAME;
    name = "New player";
  }

  public void send(String msg) {
    connection.tell(new Connection.OutputMsg(msg), null);
  }

  public void setName(String name) {
    this.name = name
        .replaceAll("[^a-zA-Z]", "")
        .substring(0, Math.min(16, name.length()));
  }
  
  public String getName() {
    return name;
  }
}
