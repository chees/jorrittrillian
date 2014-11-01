package actors;

import akka.actor.*;

public class WebSocketActor extends UntypedActor {

  public static Props props(ActorRef out) {
    return Props.create(WebSocketActor.class, out);
  }

  private final ActorRef out;

  public WebSocketActor(ActorRef out) {
    this.out = out;
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof String) {
      out.tell("I received your message: " + message, self());
    }
  }
  
  @Override
  public void postStop() throws Exception {
    System.out.println("Client disconnected");
  }
  
}
