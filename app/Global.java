import game.Game;
import play.Application;
import play.GlobalSettings;
import play.libs.Akka;
import akka.actor.Props;

public class Global extends GlobalSettings {
  @Override
  public void onStart(Application app) {
    Akka.system().actorOf(Props.create(Game.class), "game");
  }
}
