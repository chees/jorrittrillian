package controllers;

import game.Game;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;
import actors.WebSocketActor;

public class Application extends Controller {

  public static Game game = new Game();
  
  public static Result index() {
    return ok(index.render("Your new application is ready."));
  }

  public static WebSocket<String> websocket() {
    return WebSocket.withActor(WebSocketActor::props);
  }

}
