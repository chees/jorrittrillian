package game;


public class Mob {
  public int id;
  public String name;
  public String[] keywords;

  public Mob() {}
  public Mob(Mob original) {
    id = original.id;
    name = original.name;
    keywords = original.keywords;
  }
  
  public String display() {
    return name + " is here<br>";
  }
  
  @Override
  public String toString() {
    return "Mob [id=" + id + ", name=" + name + "]";
  }
}
