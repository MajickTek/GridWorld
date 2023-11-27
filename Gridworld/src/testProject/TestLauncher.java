package testProject;

import info.gridworld.actor.ActorWorld;
import info.gridworld.grid.Location;

public class TestLauncher {
    public static ActorWorld world;
    public static Camera cam;
    public static Logger log;

    public TestLauncher() {
        world = new ActorWorld();
        world.add(new Location(0, 0), new TestActor());
        world.show();
        cam = new Camera(world);
        log = new Logger(world);
    }
    public static void main(String[] args) {
        /*TestLauncher tl = */new TestLauncher();
    }
}