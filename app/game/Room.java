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

  public String display(Player perspective) {
    return "<div class=\"room\">" +
        "<h2>" + title + "</h2>" +
        displayExits() +
        description + "<br>" +
        displayPlayers(perspective) +
        "</div>";
  }
  
  private String displayExits() {
    List<String> exitNames = new ArrayList<>();
    if (exits[0] != 0) exitNames.add("north");
    if (exits[1] != 0) exitNames.add("east");
    if (exits[2] != 0) exitNames.add("south");
    if (exits[3] != 0) exitNames.add("west");
    if (exits[4] != 0) exitNames.add("up");
    if (exits[5] != 0) exitNames.add("down");
    if (exitNames.size() == 0) exitNames.add("none");
    return "[Exits: " + String.join(", ", exitNames) + "]<br>";
  }
  
  private String displayPlayers(Player perspective) {
    String output = "";
    for (Player p : players)
      if (p != perspective)
        output += p.display();
    return output;
  }
}
