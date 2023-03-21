package bgu.spl.net.srv.Messages;

public class NotificationMessage extends Message{

    private boolean type; // post = true; pm = false
    private String postingUser;
    private String content; // take the censored!


    public NotificationMessage(short _opcode, boolean _type, String _postingUser, String _content) {
        super((short)9);

        type = _type;
        postingUser = _postingUser;
        content = _content;
    }

    public boolean getType(){
        return type;
    }

    public String getPostingUser(){
        return postingUser;
    }

    public String getContent(){
        return content;
    }
}
