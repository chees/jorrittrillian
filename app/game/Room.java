package game;

import java.util.ArrayList;
import java.util.List;

public class Room {

  public int id;
  public String title;
  public String description;
  public int[] exits; // n, e, s, w, u, d
  
  public List<Player> players;
  public List<Mob> mobs;

  public Room() {
    players = new ArrayList<>();
    mobs = new ArrayList<>();
  }

  public String display(Player perspective) {
    return "<div class=\"room\">" +
        "<h2>" + title + "</h2>" +
        displayExits() +
        description + "<br>" +
        displayMobs(perspective) +
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
  
  private String displayMobs(Player perspective) {
    String output = "";
    for (Mob m : mobs)
      output += m.display(perspective);
    return output;
  }
  
  private String displayPlayers(Player perspective) {
    String output = "";
    for (Player p : players)
      if (p != perspective)
        output += p.display(perspective);
    return output;
  }

  public Mob getMob(String keyword) {
    for (Mob m : mobs)
      for (String kw : m.keywords)
        if (kw.startsWith(keyword.toLowerCase()))
          return m;
    return null;
  }
  
  public boolean containsMob(int id) {
    for (Mob m : mobs)
      if (m.id == id)
        return true;
    return false;
  }
}
