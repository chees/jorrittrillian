package game;

import java.util.ArrayList;
import java.util.List;

public class Room {

  public final int id;
  public final String title;
  public final String description;
  public final int[] exits; // n, e, s, w, u, d
  
  public List<Player> players;

  public Room(int id, String title, String description, int... exits) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.exits = exits;
    if (exits.length != 6) throw new RuntimeException("Need exactly 6 exits");
    
    players = new ArrayList<>();
  }
}
