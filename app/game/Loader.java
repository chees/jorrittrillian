package game;

import java.util.HashMap;
import java.util.Map;

public class Loader {

  public static Map<Integer, Room> getRooms() {
    Map<Integer, Room> rooms = new HashMap<>();
    rooms.put(100, new Room(100, "The Watchtower",
        "You are floating inside a huge column formed by a purple whirlwind.",
        101, 0, 0, 0, 0, 0));
    rooms.put(101, new Room(101, "The Northern Archway",
        "The walls of the archway are made of an extremely smooth kind of stone. While you're examining it, you see a black substance inside, moving slightly, as if it knows you are watching.",
         0, 0, 100, 0, 0, 0));
    return rooms;
  }

}
