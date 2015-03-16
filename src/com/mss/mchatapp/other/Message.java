package com.mss.mchatapp.other;

public class Message {
	private String fromName, message,color;
	private boolean isSelf;

	public Message() {
	}

	public Message(String fromName, String message, boolean isSelf, String color) {
		this.fromName = fromName;
		this.message = message;
		this.isSelf = isSelf;
		this.color=color;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSelf() {
		return isSelf;
	}

	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

}
