package bgu.spl.net.srv.Messages;

import java.util.List;

public class StatMessage extends Message{

    private String[] users;

    public StatMessage(short _opcode, String _users) {
        super((short)8);
        users = _users.split("|");
    }

    public String[] getUsers(){
        return users;
    }
}
