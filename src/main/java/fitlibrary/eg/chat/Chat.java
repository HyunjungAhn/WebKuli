/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.eg.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fitlibrary.exception.FitLibraryException;

// Minimalistic code to show how FitLibrary works	
public class Chat {
	private List users = new ArrayList();
	private List rooms = new ArrayList();
	private Map roomsMap = new HashMap();
	private Map usersMap = new HashMap();
	
	public List getUsers() {
		return users;
	}
	public void setUsers(List users) {
		for (Iterator it = users.iterator(); it.hasNext(); )
			addUser((User)it.next());
	}
	public List getRooms() {
		return rooms;
	}
	public void setRooms(List rooms) {
		for (Iterator it = rooms.iterator(); it.hasNext(); ) {
			addRoom((Room) it.next());
		}
	}
	public User getUser(String userName) {
		return (User)usersMap.get(userName);
	}
	public Room getRoom(String roomName) {
		return (Room)roomsMap.get(roomName);
	}
	public boolean connectUser(String userName) {
		addUser(new User(userName));
		return true;
	}
	public void disconnectUser(String userName) {
		User user = getUser(userName);
		for (Iterator it = roomsMap.values().iterator(); it.hasNext(); ) {
			Room room = (Room) it.next();
			room.removeUser(user);
		}
		users.remove(user);
		usersMap.remove(userName);
	}
	public boolean userCreatesRoom(User user, String roomName) {
		addRoom(new Room(roomName));
		return true;
	}
	public boolean userEntersRoom(User user, Room room) {
		room.addUser(user);
		return true;
	}
	public boolean userRemovesRoom(User user, Room room) {
		if (room.isEmpty()) {
			rooms.remove(room);
			roomsMap.remove(room.getName());
		}
		throw new RuntimeException("Unable to remove room "+room.getName()+
				" because it's not empty");
	}
	private void addUser(User user) {
		if (usersMap.get(user.getName()) != null)
			throw new FitLibraryException("User already exists");
		users.add(user);
		usersMap.put(user.getName(),user);
	}
	private void addRoom(Room room) {
		if (roomsMap.get(room.getName()) != null)
			throw new FitLibraryException("Room already exists");
		rooms.add(room);
		roomsMap.put(room.getName(),room);
	}
}
