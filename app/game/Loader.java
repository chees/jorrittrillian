package game;

import java.util.Arrays;
import java.util.List;

public class Loader {

  public static List<Room> getRooms() {
    return Arrays.asList(new Room(
        "The Watchtower",
        "You are floating inside a huge column formed by a purple whirlwind."));
  }

}
