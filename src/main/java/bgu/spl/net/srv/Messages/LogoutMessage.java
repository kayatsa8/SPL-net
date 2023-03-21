package bgu.spl.net.srv.Messages;

public class LogoutMessage extends Message{

    public LogoutMessage(short _opcode) {
        super((short)3);;
    }
}
