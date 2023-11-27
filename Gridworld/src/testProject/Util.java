package testProject;

import info.gridworld.actor.Actor;
import info.gridworld.grid.Location;

public class Util {
	public static void forceOnlyOneActor(Actor a) {
        int i = 0;
        for(Location l: TestLauncher.world.getGrid().getOccupiedLocations()) {
            if(isOf(TestLauncher.world.getGrid().get(l), a.getClass())) {
                i++;
                if(i > 1) {
                    TestLauncher.world.getGrid().get(l).removeSelfFromGrid();
                }
            }
        }
    }

    //instanceof but as a method
    public static boolean isOf(Object obj, Class<?> clazz){
        return clazz.isInstance(obj);
    }
}

