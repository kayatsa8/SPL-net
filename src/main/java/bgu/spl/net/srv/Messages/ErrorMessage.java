package bgu.spl.net.srv.Messages;

public class ErrorMessage extends Message{

    private short messageOpCode;

    public ErrorMessage(short _opcode, short _messageOpCode) {
        super((short)11);
        messageOpCode = _messageOpCode;
    }

    public short getMessageOpCode(){
        return messageOpCode;
    }
}
