package testProject;

import info.gridworld.actor.ActorWorld;

public class Logger {
	ActorWorld world;
	
	public Logger(ActorWorld world) {
		this.world=world;
	}
	
	public void setMessage(String text) {
		world.setMessage(text);
	}
	
	public String getMessage() {
		return world.getMessage();
	}
	
	public void clearMessage() {
		world.setMessage("");
	}
	
	public void appendMessage(String text) {
		String ogMessage = getMessage().stripTrailing();
		String newMessage = ogMessage+"\n"+text;
		setMessage(newMessage);
	}
}
