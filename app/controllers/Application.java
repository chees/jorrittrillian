package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;
import actors.Connection;

public class Application extends Controller {

  public static Result index() {
    return ok(index.render("Your new application is ready."));
  }

  public static WebSocket<String> websocket() {
    return WebSocket.withActor(Connection::props);
  }

}
