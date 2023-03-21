package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.Messages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MessageEncoderDecoderIMPL implements MessageEncoderDecoder<Message> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private Message constructMessage(){

        short opcode = bytesToShort();
        Message message = null;

        if(opcode == 1){
            message = constructRegister();
        }
        if(opcode == 2){
            message = constructLogin();
        }
        if(opcode == 3){
            message = constructLogout();
        }
        if(opcode == 4){
            message = constructFollow();
        }
        if(opcode == 5){
            message = constructPost();
        }
        if(opcode == 6){
            message = constructPM();
        }
        if(opcode == 7){
            message = constructLogStat();
        }
        if(opcode == 8){
            message = constructStat();
        }
        if(opcode == 9){
            // shouldn't get such message
        }
        if(opcode == 10){
            // shouldn't get such message
        }
        if(opcode == 11){
            // shouldn't get such message
        }
        if(opcode == 12){
            message = constructBlock();
        }

        len = 0;

        return message;
    }

    private short bytesToShort(){
        short result = (short)((bytes[0] & 0xff) << 8);
        result += (short)(bytes[1] & 0xff);
        return result;
    }

    private String extractString(int start){
        int right = start;

        while(right < len && bytes[right] != '\0'){
            right++;
        }

        return new String(bytes, start, right- start, StandardCharsets.UTF_8);
    }

    private int advanceStart(int start){
        while(bytes[start] != '\0'){
            start++;
        }

        start++;

        return start;
    }

    private Message constructRegister(){

        int start = 2;
        String username, password, birthday;

        username = extractString(start);
        start = advanceStart(start);

        password = extractString(start);
        start = advanceStart(start);

        birthday = extractString(start);

        return new RegisterMessage((short) 0, username, password, birthday);
    }

    private Message constructLogin(){
        String username, password;
        byte captcha;

        int start = 2;

        username = extractString(start);
        start = advanceStart(start);

        password = extractString(start);
        start = advanceStart(start);

        captcha = bytes[start];

        return new LoginMessage((short) 0, username, password, captcha == 1);
    }

    private Message constructLogout(){
        return new LogoutMessage((short) 0);
    }

    private Message constructFollow(){
        byte follow = bytes[2];
        String username = extractString(3);

        return new FollowMessage((short) 0, follow==0, username);
    }

    private Message constructPost(){
        String content = extractString(2);

        return new PostMessage((short) 0, content);
    }

    private Message constructPM(){
        String username, content, date;
        int start = 2;

        username = extractString(start);
        start = advanceStart(start);

        content = extractString(start);
        start = advanceStart(start);

        date = extractString(start);

        return new PrivateMessage((short) 0, username, content, date);
    }

    private Message constructLogStat(){
        return new LogStatMessage((short) 0);
    }

    private Message constructStat(){
        String list = extractString(2);

        return new StatMessage((short) 0, list);
    }

    private Message constructBlock(){
        String username = extractString(2);

        return new BlockMessage((short) 0, username);
    }


    @Override
    public Message decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            return constructMessage();
        }

        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(Message message) {
        byte[] result = null;

        if(message.getOpcode() == 9){
            result = encodeNotification((NotificationMessage) message);
        }
        if(message.getOpcode() == 10){
            result = encodeACK((ACKMessage) message);
        }
        if (message.getOpcode() == 11) {
            result = encodeError((ErrorMessage) message);
        }

        return result;
    }

    private byte[] encodeNotification(NotificationMessage message){
        final int numOfFreeBytes = 6; // free byte = byte that is not part of strings
        byte[] sender, content, opcode;
        byte[] result;

        opcode = shortToBytes(message.getOpcode());
        sender = message.getPostingUser().getBytes(StandardCharsets.UTF_8);
        content = message.getContent().getBytes(StandardCharsets.UTF_8);

        result = new byte[numOfFreeBytes + sender.length + content.length];

        result[0] = opcode[0]; result[1] = opcode[1];

        if(message.getType()){
            result[2] = 1;
        }
        else{
            result[2] = 0;
        }

        for(int i=0; i<sender.length; i++){
            result[i+3] = sender[i];
        }

        for(int i=0; i<content.length; i++){
            result[i+3+sender.length] = content[i];
        }

        result[result.length-2] = 0;
        result[result.length-1] = ';';
        return result;
    }

    private byte[] encodeError(ErrorMessage message){
        byte[] opcode = shortToBytes(message.getOpcode());
        byte[] messageOpcode = shortToBytes(message.getMessageOpCode());

        byte[] result = {opcode[0], opcode[1], messageOpcode[0], messageOpcode[1], ';'};

        return result;
    }

    private byte[] encodeACK(ACKMessage message){
        short mCode = message.getMessageOpCode();

        byte[] opcode = shortToBytes(message.getOpcode());
        byte[] messageOpcode = shortToBytes(mCode);

        if(mCode == 4){ // ack for follow
            List<Object> optional = message.getOptional();
            byte[] username = ((String)optional.get(0)).getBytes(StandardCharsets.UTF_8);

            byte[] result = new byte[6 + username.length];

            result[0] = opcode[0]; result[1] = opcode[1]; result[2] = messageOpcode[0]; result[3] = messageOpcode[1];

            for(int i=0; i< username.length; i++){
                result[i+4] = username[i];
            }

            result[result.length-2] = 0;
            result[result.length-1] = ';';

            return result;
        }
        if(mCode == 7 | mCode == 8){ // ack for logStat or log
            byte[] result = new byte[13];

            result[0] = opcode[0]; result[1] = opcode[1]; result[2] = messageOpcode[0]; result[3] = messageOpcode[1];

            List<Object> list = message.getOptional();

            int index = 4;
            byte[] temp;

            for(Object o : list){
                short s = (Short) o;
                temp = shortToBytes(s);
                result[index] = temp[0];
                index++;
                result[index] = temp[1];
                index++;
            }

            result[result.length-1] = ';';

            return result;

        }

        byte[] result = {opcode[0], opcode[1], messageOpcode[0], messageOpcode[1], ';'};

        return result;

    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


}
