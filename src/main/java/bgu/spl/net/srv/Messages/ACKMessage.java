package bgu.spl.net.srv.Messages;

import java.util.ArrayList;
import java.util.List;

public class ACKMessage extends Message{

    private short messageOpCode;
    private List<Object> optional;

    public ACKMessage(short _opcode, short _messageOpCode) {
        super((short)10);
        messageOpCode = _messageOpCode;
        optional = new ArrayList<>();
    }

    public void addOptional(Object o){
        optional.add(o);
    }

    public List<Object> getOptional(){
        return optional;
    }

    public short getMessageOpCode(){
        return messageOpCode;
    }

}
