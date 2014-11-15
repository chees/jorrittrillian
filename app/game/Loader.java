package game;

import java.util.HashMap;
import java.util.Map;

public class Loader {
  
  public static Map<Integer, Room> getRooms() {
    Map<Integer, Room> rooms = new HashMap<>();
    addRoom(rooms, new Room(100, "The Watchtower",
        "You are floating inside a huge column formed by a purple whirlwind.",
        101, 102, 103, 104, 105, 106));
    addRoom(rooms, new Room(101, "The Northern Archway",
        "The walls of the archway are made of an extremely smooth kind of stone. While you're examining it, you see a black substance inside, moving slightly, as if it knows you are watching.",
         0, 0, 100, 0, 0, 0));
    addRoom(rooms, new Room(102, "The Eastern Archway",
        "The walls of the archway are made of an extremely smooth kind of stone. While you're examining it, you see a black substance inside, moving slightly, as if it knows you are watching.",
         0, 0, 0, 100, 0, 0));
    addRoom(rooms, new Room(103, "The Southern Archway",
        "The walls of the archway are made of an extremely smooth kind of stone. While you're examining it, you see a black substance inside, moving slightly, as if it knows you are watching.",
         100, 0, 0, 0, 0, 0));
    addRoom(rooms, new Room(104, "The Western Archway",
        "The walls of the archway are made of an extremely smooth kind of stone. While you're examining it, you see a black substance inside, moving slightly, as if it knows you are watching.",
         0, 100, 0, 0, 0, 0));
    addRoom(rooms, new Room(105, "The Top Of The Watchtower",
        "From up here you can see the Drakwald forest all around you.",
         0, 0, 0, 0, 0, 100));
    addRoom(rooms, new Room(106, "The Donation Room Watchtower",
        "If you have anything to donate you can drop it in this room.",
         0, 0, 0, 0, 100, 0));
    return rooms;
  }
  
  private static void addRoom(Map<Integer, Room> rooms, Room room) {
    rooms.put(room.id, room);
  }
}