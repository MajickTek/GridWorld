package testProject;

import info.gridworld.actor.Actor;
import info.gridworld.actor.ActorWorld;
import info.gridworld.grid.Location;
import info.gridworld.gui.GridPanel;

public class Camera {
	GridPanel baseDisplay;
	
	public Camera(ActorWorld world) {
		baseDisplay = world.getFrame().getDisplay();
	}
	
	public void setLocation(Location loc) {
		baseDisplay.setCurrentLocation(loc);
	}
	
	public void setLocation(int row, int col) {
		baseDisplay.setCurrentLocation(new Location(row,col));
	}
	
	public Location getCurrentLocation() {
		return baseDisplay.getCurrentLocation();
	}
	
	public void recenter(Location loc) {
		baseDisplay.recenter(loc);
	}
	
	public void recenter(int row, int col) {
		baseDisplay.recenter(new Location(row, col));
	}
	
	// Offset adds margin to make the actor visible when the camera is 1 tile to the top/left of the actor
	public void recenterOnActor(Actor a) {
		Location actorLoc = a.getLocation();
		recenter(new Location(actorLoc.getRow()-1, actorLoc.getCol()-1));
	}
}
