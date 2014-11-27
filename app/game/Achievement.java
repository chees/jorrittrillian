package game;

public enum Achievement {
  GRIN("grinned"),
  KILL_KYLIDRA("killed Kylidra"),
  KERRIGAN("survived Kerrigan"),
  CLAPTRAP("grabbed Claptrap"),
  FINISH("finished the game"),
  RICKROLL("got Rickrolled"),
  KILL_STEALER("tried to Kill Steal"),
  LEVEL_10("reached level 10"),
  DIE("died"),
  FINISH_IN_10_MINUTES("finished the game in less than 10 minutes"),
  ;
  
  public final String description;
  
  Achievement(String description) {
    this.description = description;
  }
}
