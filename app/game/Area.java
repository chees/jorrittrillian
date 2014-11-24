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
    for (Room r : rooms)
      roomsById.put(r.id, r);
    for (Mob m : mobs)
      mobsById.put(m.id, m);
  }
  
  public void respawn() {
    for (Respawn respawn : respawns) {
      Room room = roomsById.get(respawn.room);
      boolean shouldSpawn = true;
      
      if (respawn.areaUnique) {
        for (Room r : rooms)
          if (r.containsMob(respawn.mob))
            shouldSpawn = false;
      } else {
        for (Mob m : room.mobs)
          if (m.id == respawn.mob)
            shouldSpawn = false;
      }
      
      if (shouldSpawn) {
        Mob mob = new Mob(mobsById.get(respawn.mob));
        room.mobs.add(mob);
      }
    }
  }
}
