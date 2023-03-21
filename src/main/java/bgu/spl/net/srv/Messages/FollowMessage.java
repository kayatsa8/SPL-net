package bgu.spl.net.srv.Messages;

public class FollowMessage extends Message{

    private boolean follow; // true if the user want to follow, otherwise false
    private String userToFollow;

    public FollowMessage(short _opcode, boolean _follow, String _user) {
        super((short)4);
        follow = _follow;
        userToFollow = _user;
    }

    public boolean getFollow(){
        return follow;
    }

    public String getUserToFollow(){
        return userToFollow;
    }
}
