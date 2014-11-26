package game;

public enum Achievement {
  KILL_KYLIDRA("killed Kylidra"),
  GRIN("grinned"),
  KILL_STEALER("tried to Kill Steal"),
  DIE("died");
  
  /*
  LEVEL_10("reached level 10"),
  FINISH("finished the game"),
  FINISH_IN_10_MINUTES("finished the game in 10 minutes"),
  RICKROLL("got Rickrolled");
  */
  
  public final String description;
  
  Achievement(String description) {
    this.description = description;
  }
}
