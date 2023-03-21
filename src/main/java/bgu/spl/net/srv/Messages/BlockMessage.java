package bgu.spl.net.srv.Messages;

public class BlockMessage extends Message{

    private String username;

    public BlockMessage(short _opcode, String _username) {
        super((short)12);
        username = _username;
    }

    public String getUsername(){
        return username;
    }
}
