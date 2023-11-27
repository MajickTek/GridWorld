package testProject;

import info.gridworld.actor.Bug;

public class TestActor extends Bug {
	public boolean canCamFollow=true;
	public String name = NameGenerator.generateName();
	
	
	@Override
	public void act() {
		if(canCamFollow) {
			//Center "camera" on Bug. This required exposing private methods
			TestLauncher.cam.recenterOnActor(this);
		}
		
		//Util.forceOnlyOneActor(this);
		
		//super.act() calls the method to move the Bug. I print the location after super.act() so that the message board is in sync.
		super.act();
		TestLauncher.log.appendMessage(name + " " + getLocation().toString());
	}
	
	public void toggleFollowCam() {
		canCamFollow=!canCamFollow;
	}
	
	public boolean canCamFollow() {
		return canCamFollow;
	}
	
	public String getName() {
		return name;
	}
}
