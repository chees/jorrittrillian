package actors;

import game.Game;
import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class Connection extends UntypedActor {

  public static Props props(ActorRef out) {
    return Props.create(Connection.class, out);
  }

  private final ActorRef out;
  
  private ActorSelection game;

  public Connection(ActorRef out) {
    this.out = out;
    
    game = Akka.system().actorSelection("/user/game");
    game.tell(new Game.NewConnectionMsg(), getSelf());
  }
  
  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof String) {
      if ("ping".equals(message))
        out.tell("pong", getSelf());
      else
        game.tell(message, getSelf());
    }
    else if (message instanceof OutputMsg)
      out.tell(((OutputMsg)message).msg, getSelf());
    else unhandled(message);
  }
  
  @Override
  public void postStop() throws Exception {
    game.tell(new Game.DisconnectMsg(), getSelf());
  }
  
  public static class OutputMsg {
    private final String msg;
    public OutputMsg(String msg) {
      this.msg = msg;
    }
  }
}
