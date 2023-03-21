package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.Messages.*;

import java.util.List;

public class BidiMessagingProtocolIMPL implements BidiMessagingProtocol<Message>{

    private final static short OPCODE = 0; // the opcode we use to create new Message is irrelevant
    private final static short REGISTEROPCODE = 1;
    private final static short LOGINOPCODE = 2;
    private final static short LOGOUTOPCODE = 3;
    private final static short FOLLOWOPCODE = 4;
    private final static short POSTOPCODE = 5;
    private final static short PMOPCODE = 6;
    private final static short LOGSTATOPCODE = 7;
    private final static short STATOPCODE = 8;
    private final static short NOTIFICATIONOPCODE = 9;
    private final static short ACKOPCODE = 10;
    private final static short ERROROPCDOE = 11;
    private final static short BLOCKOPCODE = 12;


    private int connectionID;
    private Connections<Message> connections;
    private boolean started; // this flag is used to check whether start() was used, and therefore the protocol can be used
    private boolean shouldTerminate;

    public BidiMessagingProtocolIMPL(){
        connectionID = -1;
        connections = null;
        started = false;
        shouldTerminate = false;
    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.connectionID = connectionId;
        this.connections = connections;
        started = true;
    }

    @Override
    public void process(Message message) {
        short opcode = message.getOpcode();

        if(opcode == BidiMessagingProtocolIMPL.REGISTEROPCODE){
            processRegister((RegisterMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.LOGINOPCODE){
            processLogin((LoginMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.LOGOUTOPCODE){
            processLogout((LogoutMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.FOLLOWOPCODE){
            processFollow((FollowMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.POSTOPCODE){
            processPost((PostMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.PMOPCODE){
            processPM((PrivateMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.LOGSTATOPCODE){
            processLogStat((LogStatMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.STATOPCODE){
            processStat((StatMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.NOTIFICATIONOPCODE){
            processNotification((NotificationMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.ACKOPCODE){
            processAck((ACKMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.ERROROPCDOE){
            processError((ErrorMessage) message);
        }
        if(opcode == BidiMessagingProtocolIMPL.BLOCKOPCODE){
            processBlock((BlockMessage) message);
        }

    }

    private void processRegister(RegisterMessage message){
        if(connections.addRegistered(message.getUsername(), message.getPassword(), message.getBirthday(), connectionID)){
            connections.send(connectionID, new ACKMessage(OPCODE, REGISTEROPCODE));
        }
        else{
            connections.send(connectionID, new ErrorMessage(OPCODE, REGISTEROPCODE));
        }
    }

    private void processLogin(LoginMessage message){
        if(!message.getCaptcha()){
            connections.send(connectionID, new ErrorMessage(OPCODE, LOGINOPCODE));
        }
        else{
            int tempConID = connections.fixConnectionID(connectionID, message.getUsername());
            if(tempConID != -1){
                connectionID = tempConID;
            }
            if(connections.addLogged(message.getUsername(), message.getPassword())){
                connections.send(connectionID, new ACKMessage(OPCODE, LOGINOPCODE));
                connections.sendAwaitingMessages(connectionID);
            }
            else{
                connections.send(connectionID, new ErrorMessage(OPCODE, LOGINOPCODE));
            }
        }
    }

    private void processLogout(LogoutMessage message){
        if(connections.logout(connectionID)){
            connections.send(connectionID, new ACKMessage(OPCODE, LOGOUTOPCODE));
            shouldTerminate = true;
        }
        else{
            connections.send(connectionID, new ErrorMessage(OPCODE, LOGOUTOPCODE));
        }
    }

    private void processFollow(FollowMessage message){
        if(connections.follow(connectionID, message.getFollow(), message.getUserToFollow())){
            ACKMessage ack = new ACKMessage(OPCODE, FOLLOWOPCODE);
            ack.addOptional(message.getUserToFollow());

            connections.send(connectionID, ack);
        }
        else{
            connections.send(connectionID, new ErrorMessage(OPCODE, FOLLOWOPCODE));
        }
    }

    private void processPost(PostMessage message){
        if(!connections.isLogged(connectionID)){
            connections.send(connectionID, new ErrorMessage(OPCODE, POSTOPCODE));
        }

        List<Integer> followers = connections.getUsersFollowers(connectionID);

        List<Integer> tagged = connections.getUsersConnectionID(message.getTagged().toArray(new String[0]));


        for(Integer dest : followers){
            sendNotification(dest, message.getContent(), 1, connections.getUsername(connectionID));
        }

        for(Integer dest : tagged){
            sendNotification(dest, message.getContent(), 1, connections.getUsername(connectionID));
        }

        connections.incNumOfPosts(connectionID);

        connections.send(connectionID, new ACKMessage(OPCODE, POSTOPCODE));

    }

    private void sendNotification(int dest, String content, int  type, String sender){
        NotificationMessage message = new NotificationMessage(OPCODE, type==1, sender, content);

        if(connections.isLogged(dest)){
            connections.send(dest, message);
        }
        else{
            connections.addToUsersAwaitingMessage(dest, message);
        }

        connections.addToListOfPost_PM(dest, message);
    }

    private void processPM(PrivateMessage message){
        if(!connections.isLogged(connectionID)){
            connections.send(connectionID, new ErrorMessage(OPCODE, PMOPCODE));
        }

        if(!connections.isRegistered(message.getUsername())){
            connections.send(connectionID, new ErrorMessage(OPCODE, PMOPCODE));
        }

        message.censoredContent(connections.getCensoredWords());

        sendNotification(connections.getConnectionID(message.getUsername()),
                message.getContent(),
                0,
                connections.getUsername(connectionID)
        );

        connections.send(connectionID, new ACKMessage(OPCODE, PMOPCODE));
    }

    private void processLogStat(LogStatMessage message){
        List<List<Object>> statList = connections.logStat(connectionID);

        if(statList.isEmpty()){
            connections.send(connectionID, new ErrorMessage(OPCODE, LOGSTATOPCODE));
            return;
        }

        ACKMessage ack;

        for(List<Object> l : statList){
            ack = new ACKMessage(OPCODE, LOGSTATOPCODE);
            for(Object o : l){
                ack.addOptional(o);
            }

            connections.send(connectionID, ack);
        }
    }

    private void processStat(StatMessage message){
        List<List<Object>> statList = connections.stat(connectionID, message.getUsers());

        if(statList.isEmpty()){
            connections.send(connectionID, new ErrorMessage(OPCODE, STATOPCODE));
            return;
        }

        ACKMessage ack;

        for(List<Object> l : statList){
            ack = new ACKMessage(OPCODE, LOGSTATOPCODE);
            for(Object o : l){
                ack.addOptional(o);
            }

            connections.send(connectionID, ack);
        }

    }

    private void processNotification(NotificationMessage message){
        // Should not get here, just for completeness
    }

    private void processAck(ACKMessage message){
        // Should not get here, just for completeness
    }

    private void processError(ErrorMessage message){
        // Should not get here, just for completeness
    }

    private void processBlock(BlockMessage message){
        if(connections.blockUser(connectionID, message.getUsername())){
            connections.send(connectionID, new ACKMessage(OPCODE, BLOCKOPCODE));
        }
        else{
            connections.send(connectionID, new ErrorMessage(OPCODE, BLOCKOPCODE));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
