package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.Messages.NotificationMessage;
import bgu.spl.net.srv.Messages.PostMessage;
import bgu.spl.net.srv.Messages.PrivateMessage;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsIMPL<T> implements Connections<T> {

    private Integer nextConnectionID; // holds the next available id
    private ConcurrentHashMap<String, Integer> username_ID; // usernames and their ids
    private ConcurrentHashMap<Integer, ConnectionHandler> ID_ConnectionHandler; // ids and relevant connection handler
    private ConcurrentHashMap<Integer, User<T>> logged; // user logged in = true, otherwise false
    private ConcurrentHashMap<Integer, T> post_pm;
    private ConcurrentHashMap<Integer, User> registered;
    private List<String> censored;



    public ConnectionsIMPL(){
        nextConnectionID = 0;
        username_ID = new ConcurrentHashMap<>();
        ID_ConnectionHandler = new ConcurrentHashMap<>();
        logged = new ConcurrentHashMap<>();
        post_pm = new ConcurrentHashMap<>();
        censored = new ArrayList<>();
        registered = new ConcurrentHashMap<>();

        initiateCensored();
    }

    public void initiateCensored(){
        censored.add("Alis");
        censored.add("Bob");
        censored.add("censor");
        censored.add("Leonardo");
        censored.add("sleep");
        censored.add("assignment 4");
        censored.add("Corona");
        censored.add("home work");
        censored.add("fun");
        censored.add("failure");
        censored.add("test");
        censored.add("iphone");
        censored.add("synchronized");
        censored.add("police");
        censored.add("Jesus");
        censored.add("1571");
        censored.add("6147");
        censored.add("spoilers");
        censored.add("aliens");
        censored.add("predator");
        censored.add("matrix");
        censored.add("Smith");
        censored.add("La Plaga");
        censored.add("Winter");
        censored.add("Humus");
        censored.add("Bacon");
        censored.add("English");
        censored.add("Arthas");
        censored.add("Uther");
        censored.add("Aviv Gefen");
        censored.add("Avocado");
        censored.add("Mexico");
        censored.add("Aquaman");
        censored.add("Fortnite");
        censored.add("John Wick");
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(!ID_ConnectionHandler.keySet().contains(connectionId)){
            return false;
        }
        else{
            ID_ConnectionHandler.get(connectionId).send(msg);
            return true;
        }
    }

    @Override
    public void broadcast(T msg) {

        for(Integer i : registered.keySet()){
            send(i, msg);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        if(ID_ConnectionHandler.containsKey(connectionId)){
            ID_ConnectionHandler.remove(connectionId);
            if(registered.containsKey(connectionId)){
                username_ID.remove(registered.get(connectionId).getUsername());
                registered.remove(connectionId);
            }
            if(logged.containsKey(connectionId)){
                logged.remove(connectionId);
            }
        }
    }

    public boolean logout(int connectionID){
        if(!logged.containsKey(connectionID)){
            return false;
        }
        else{
            logged.remove(connectionID);
            return true;
        }
    }

    public int addNewConnectionHandler(ConnectionHandler ch){

        synchronized (nextConnectionID){
            ID_ConnectionHandler.put(nextConnectionID, ch);
            nextConnectionID++;
            return nextConnectionID-1;
        }


    }

    public boolean addRegistered(String username, String password, String birthday, int connectionID){
        if(username_ID.contains(username)){
            return false;
        }
        username_ID.put(username, connectionID);
        registered.put(connectionID, new User(username, password, birthday));
        return true;
    }

    public int getConnectionID(String username){
        if(username_ID.keySet().contains(username)){
            return username_ID.get(username);
        }
        else{
            return -1;
        }
    }

    public boolean addLogged(String username, String password){
        if(!username_ID.keySet().contains(username)){
            return false;
        }

        int connectionID = username_ID.get(username);

        if(!registered.keySet().contains(connectionID)){
            return false;
        }

        if(logged.keySet().contains(connectionID)){
            return false;
        }

        if(!registered.get(connectionID).getPassword().equals(password)){
            return false;
        }

        logged.put(connectionID, registered.get(connectionID));

        return true;

    }

    public void sendAwaitingMessages(int connectionID){
        ConcurrentLinkedQueue<T> notifications = registered.get(connectionID).getAwaitingMessages();

        for(T t : notifications){
            send(connectionID, t);
        }

        registered.get(connectionID).clearAwaitingMessages();
    }

    public boolean follow(int followerConnectionID, boolean isFollow, String userToFollow){
        int toFollowID = username_ID.get(userToFollow);

        if(!isRegistered(followerConnectionID) || !isRegistered(toFollowID)){
            return false;
        }

        if(isBlocked(followerConnectionID, toFollowID)){
            return false;
        }

        if(!logged.keySet().contains(followerConnectionID)){
            return false;
        }

        User follower = registered.get(followerConnectionID);
        User toFollow = registered.get(toFollowID);

        if(isFollow){
            if(follower.isInFollowing(toFollow.getUsername())){
                return false;
            }
            else{
                follower.addToFollowing(toFollow.getUsername());
                toFollow.addToFollowers(follower.getUsername());
                return true;
            }
        }
        else{
            if(!follower.isInFollowing(toFollow.getUsername())){
                return false;
            }
            follower.removeFromFollowing(toFollow.getUsername());
            toFollow.removeFromFollowers(follower.getUsername());
            return true;
        }
    }

    private boolean isBlocked(int conID1, int conID2){
        return registered.get(conID1).isInBlocked(registered.get(conID2).getUsername())
                ||
                registered.get(conID2).isInBlocked(registered.get(conID1).getUsername());
    }

    public boolean isRegistered(int conID){
        return registered.containsKey(conID);
    }

    public boolean isRegistered(String user){
        if(!username_ID.containsKey(user)){
            return false;
        }
        return isRegistered(username_ID.get(user));
    }

    public List<List<Object>> logStat(int connectionID){
        List<List<Object>> statList = new ArrayList<>();

        if(!logged.containsKey(connectionID)){
            return statList;
        }

        List<Object> tempList;
        User u;

        for(Integer i : logged.keySet()){
            if(!isBlocked(connectionID, i)){
                tempList = new ArrayList<>();
                u = registered.get(i);
                tempList.add((short) u.getAge());
                tempList.add((short)u.getNumOfPosts());
                tempList.add((short) u.getNumOfFollowers());
                tempList.add((short) u.getNumOfFollowing());

                statList.add(tempList);
            }
        }

        return statList;

    }

    public List<List<Object>> stat(int connectionID, String[] users){
        List<List<Object>> statList = new ArrayList<>();

        if(!logged.containsKey(connectionID)){
            return  statList;
        }

        List<Object> tempList;
        User u;

        for(String s : users){
            if(!registered.containsKey(s)){
                return null;
            }

            if(!isBlocked(connectionID, username_ID.get(s))){
                tempList = new ArrayList<>();
                u = registered.get(username_ID.get(s));
                tempList.add((short) u.getAge());
                tempList.add((short)u.getNumOfPosts());
                tempList.add((short) u.getNumOfFollowers());
                tempList.add((short) u.getNumOfFollowing());

                statList.add(tempList);
            }
        }

        return statList;
    }

    public boolean blockUser(int connectionID, String user){
        if(!logged.containsKey(connectionID)){
            return false;
        }

        if(!username_ID.containsKey(user)){
            return false;
        }

        int toBlockID = username_ID.get(user);

        if(!isRegistered(toBlockID)){
            return false;
        }

        User curr = registered.get(connectionID);
        User toBlock = registered.get(toBlockID);

        if(curr.isInBlocked(user)){
            return false;
        }

        if(curr.isInFollowing(user)){
            curr.removeFromFollowing(user);
            toBlock.removeFromFollowers(curr.getUsername());
        }
        if(toBlock.isInFollowing(curr.getUsername())){
            toBlock.removeFromFollowing(curr.getUsername());
            curr.removeFromFollowers(toBlock.getUsername());
        }

        curr.addToBlocked(user);

        return true;
    }

    public boolean isLogged(int connectionID){
        return logged.containsKey(connectionID);
    }

    public List<Integer> getUsersFollowers(int connectionID){
        List<Integer> followers = new ArrayList<>();
        ConcurrentLinkedQueue<String> followersNames = registered.get(connectionID).getFollowers();

        for(String fol : followersNames){
            followers.add(username_ID.get(fol));
        }

        return followers;
    }

    public List<Integer> getUsersConnectionID(String[] username){
        List<Integer> ids = new ArrayList<>();

        for(int i=0; i<username.length; i++){
            if(username_ID.containsKey(username[i])){
                ids.add(username_ID.get(username[i])) ;
            }
        }

        return ids;

    }

    public void addToUsersAwaitingMessage(int dest, T message){
        registered.get(dest).addNewMessage(message);
    }

    public void addToListOfPost_PM(int dest, T message){
        post_pm.put(dest, message);
    }

    public String getUsername(int connectionID){
        return registered.get(connectionID).getUsername();
    }

    public void incNumOfPosts(int connectionID){
        registered.get(connectionID).incNumOfPosts();
    }

    public List<String> getCensoredWords(){
        return censored;
    }

    public int fixConnectionID(int conID, String username){
        if(username_ID.get(username) == conID){
            return -1;
        }

        int originalID = username_ID.get(username);
        ID_ConnectionHandler.remove(originalID);

        ConnectionHandler ch = ID_ConnectionHandler.get(conID);

        ID_ConnectionHandler.put(originalID, ch);

        return originalID;
    }
}
