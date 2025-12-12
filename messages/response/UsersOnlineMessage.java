package messages.response;


import java.util.ArrayList;


import messages.Message;
import messages.MsgHeader;

public class UsersOnlineMessage implements Message {

    private final MsgHeader header;
    private final ArrayList<String> users_online; 
    //#endregion


    public UsersOnlineMessage(MsgHeader header, ArrayList<String> users_online) {
        this.header = header;
        this.users_online = users_online;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    

    public ArrayList<String> getUsers_online() {
        return users_online;
    }

    @Override
    public String toString() {
        return String.format("USERS_ONLINE %s\r\n", users_online.toString()); 
    }

}
