package game;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Loader {
  private List<Area> areas;
  
  public Loader() {
    areas = new ArrayList<>();
    areas.add(loadArea("watchtower"));
  }
  
  private Area loadArea(String name) {
    InputStream stream = Loader.class.getResourceAsStream("/areas/" + name + ".json");    
    try {
      Area area = new ObjectMapper().readValue(stream, Area.class);
      area.init();
      return area;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public List<Area> getAreas() {
    return areas;
  }
  
  public Map<Integer, Room> getRooms() {
    Map<Integer, Room> rooms = new HashMap<>();
    for (Area a : areas) {
      for (Room r : a.rooms) {
        rooms.put(r.id, r);
      }
    }
    return rooms;
  }
  
  public Map<Integer, Mob> getMobs() {
    Map<Integer, Mob> mobs = new HashMap<>();
    for (Area a : areas) {
      for (Mob m : a.mobs) {
        mobs.put(m.id, m);
      }
    }
    return mobs;
  }
}
