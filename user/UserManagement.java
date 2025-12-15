package user;

import java.util.ArrayList;

public class UserManagement {
    private ArrayList <User> registeredUsers = new ArrayList<>();
    private ArrayList<String> onlineUsers = new ArrayList<>();// String List that only contains name of users to keep the rest of the information private

    public void register(User user){
        registeredUsers.add(user);
    }

    public void setOnline(User user){
        onlineUsers.add(user.getName());
    }

    public void setOffline(User user){
        onlineUsers.remove(user.getName());
    }
    
    public ArrayList<String> getOnlineUsers(){  
        return onlineUsers;
    }

    
}
