package controllers;

import play.Logger;
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
    Logger.info("New websocket: " + request().remoteAddress());
    return WebSocket.withActor(Connection::props);
  }

}
