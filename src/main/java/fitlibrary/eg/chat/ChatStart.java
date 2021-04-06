package fitlibrary.eg.chat;

import java.util.List;

import fitlibrary.DoFixture;

public class ChatStart extends DoFixture {
	private static ChatSystem chat;

	public ChatStart() {
		super(chat = new ChatSystem());
	}
	public List usersInRoom(String roomName) {
		return chat.findRoom(roomName).getUsers();
	}
	public boolean roomIsEmpty(String roomName) { 
		return chat.findRoom(roomName).getUsers().isEmpty();
	}
	public List usersInLotr() {
		return usersInRoom("lotr");
	}
}
