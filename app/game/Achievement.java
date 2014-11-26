package game;

public enum Achievement {
  KILL_KYLIDRA("killed Kylidra"),
  GRIN("grinned"),
  KILL_STEALER("tried to Kill Steal"),
  LEVEL_10("reached level 10"),
  DIE("died"),
  FINISH("finished the game"),
  FINISH_IN_10_MINUTES("finished the game in less than 10 minutes"),
  RICKROLL("got Rickrolled");
  
  public final String description;
  
  Achievement(String description) {
    this.description = description;
  }
}
