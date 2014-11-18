package game;

public abstract class Character {
  public enum State { WAITING_FOR_NAME, STANDING, FIGHTING }
  
  public String name;
  public State state;
  public int hp;
  public Character target;
  
  public String display(Player perspective) {
    if (state == State.FIGHTING)
      return name + " is here, fighting " + (target == perspective ? "you" : target.name) + "!<br>";
    return name + " is here.<br>";
  }
}
