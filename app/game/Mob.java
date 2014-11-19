package game;


public class Mob extends Character {
    
  public int id;
  public String[] keywords;

  public Mob() {}
  public Mob(Mob original) {
    id = original.id;
    name = original.name;
    keywords = original.keywords;
    hp = original.hp;
    hpMax = hp;
    state = State.STANDING;
  }
  
  @Override
  public String toString() {
    return "Mob [id=" + id + ", name=" + name + "]";
  }
  
  public void setName(String name) {
    this.name = name;
  }
}
