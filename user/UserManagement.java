package user;

import java.net.InetAddress;
import java.util.ArrayList;

public class UserManagement {
    private ArrayList<User> registeredUsers = new ArrayList<>();
    private ArrayList<String> onlineUsers = new ArrayList<>();// String List that only contains name of users to keep
                                                              // the rest of the information private

    public void register(User user) {
        registeredUsers.add(user);
    }

    public void setOnline(User user) {
        onlineUsers.add(user.getName());
    }

    public void setOffline(User user) {
        onlineUsers.remove(user.getName());
    }

    public ArrayList<String> getOnlineUsers() {
        return onlineUsers;
    }

    public ArrayList<User> getRegisteredUsers() {
        return registeredUsers;
    }

    public User findRegisteredUser(String key) {
        User user = null;

        for (User u : registeredUsers) {
            if (u.getEmail().equals(key)) {
                user = u;
                break;
            } else if (u.getName().equals(key)) {
                user = u;
                break;
            }
        }
        return user;

    }

    public User findRegisteredUser(InetAddress ip) {
        User user = null;

        for (User u : registeredUsers) {
            if (u.getIpObject() == ip) {
                user = u;
                break;
            }
        }
        return user;

    }

}
