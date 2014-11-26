package game;

public abstract class Character {
  public enum State { WAITING_FOR_NAME, STANDING, FIGHTING, SLEEPING }
  
  public String name;
  public State state;
  public int hp;
  public int hpMax;
  public int hpRegen;
  public Character target;
  public int level;
  
  public String display(Player perspective) {
    String enemy = isEnemy() ? "enemy" : "";
    if (state == State.FIGHTING)
      return "<div class=\"character " + enemy + "\">" + name + " is here, fighting " + (target == perspective ? "you" : target.name) + "!</div>";
    return "<div class=\"character " + enemy + "\">" + name + " is here.</div>";
  }
  
  public void regen() {
    if (hp < hpMax) {
      int multiplier = 1;
      if (state == State.SLEEPING)
        multiplier = 40;
      hp = Math.min(hpMax, hp + hpRegen * multiplier);
      send("You regenerate. New hp: " + hp);
    }
  }
  
  public void send(String msg) {}
  
  public abstract boolean isEnemy();
}
