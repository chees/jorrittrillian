package game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Area {
  public List<Room> rooms;
  public List<Mob> mobs;
  public List<Respawn> respawns;
  
  private Map<Integer, Room> roomsById;
  private Map<Integer, Mob> mobsById;
  
  public Area() {
    roomsById = new HashMap<>();
    mobsById = new HashMap<>();
  }
  
  public void init() {
    for (Room r : rooms) {
      roomsById.put(r.id, r);
    }
    for (Mob m : mobs) {
      mobsById.put(m.id, m);
    }
  }
  
  public void respawn() {
    for (Respawn respawn : respawns) {
      Room room = roomsById.get(respawn.room);
      boolean mobInRoom = false;
      for (Mob m : room.mobs) {
        if (m.id == respawn.mob) {
          mobInRoom = true;
          break;
        }
      }
      if (!mobInRoom) {
        Mob mob = new Mob(mobsById.get(respawn.mob));
        room.mobs.add(mob);
      }
    }
  }
}
