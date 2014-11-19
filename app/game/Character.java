package game;

public abstract class Character {
  public enum State { WAITING_FOR_NAME, STANDING, FIGHTING, SLEEPING }
  
  public String name;
  public State state;
  public int hp;
  public int hpMax;
  public int hpRegen;
  public Character target;
  
  public String display(Player perspective) {
    if (state == State.FIGHTING)
      return name + " is here, fighting " + (target == perspective ? "you" : target.name) + "!<br>";
    return name + " is here.<br>";
  }
  
  public void regen() {
    if (hp < hpMax) {
      int multiplier = 1;
      if (state == State.SLEEPING)
        multiplier = 10;
      hp = Math.min(hpMax, hp + hpRegen * multiplier);
      send("You regenerate. New hp: " + hp);
    }
  }
  
  public void send(String msg) {}
}
