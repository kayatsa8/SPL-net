package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.Messages.Message;
import bgu.spl.net.srv.Messages.PostMessage;
import bgu.spl.net.srv.Messages.PrivateMessage;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);

    boolean addRegistered(String username, String password, String birthday, int connectionID);

    int getConnectionID(String username);

    int addNewConnectionHandler(ConnectionHandler ch);

    boolean addLogged(String username, String password);

    boolean logout(int connectionID);

    boolean follow(int followerConnectionID, boolean isFollow, String userToFollow);

    List<List<Object>> logStat(int connectionID);

    List<List<Object>> stat(int connectionID, String[] users);

    boolean blockUser(int connectionID, String user);

    boolean isLogged(int connectionID);

    List<Integer> getUsersFollowers(int connectionID);

    List<Integer> getUsersConnectionID(String[] username);

    void addToUsersAwaitingMessage(int dest, T message);

    void addToListOfPost_PM(int dest, T message);

    String getUsername(int connectionID);

    void incNumOfPosts(int connectionID);

    boolean isRegistered(int conID);

    boolean isRegistered(String user);

    List<String> getCensoredWords();

    int fixConnectionID(int conID, String username);

    void sendAwaitingMessages(int connectionID);
}
