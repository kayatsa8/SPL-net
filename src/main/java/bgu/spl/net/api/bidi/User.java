package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.Messages.Message;
import bgu.spl.net.srv.Messages.NotificationMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User<T> {
    private String username;
    private String password;
    private String birthday;
    private ConcurrentLinkedQueue<T> awaitingMessages;
    private ConcurrentLinkedQueue<String> following; // the username of the users current user is following
    private ConcurrentLinkedQueue<String> followers; // the username of the users following current user
    private ConcurrentLinkedQueue<String> blocked; // the username of users blocked by current user
    private int numOfPosts;

    public User(String u, String p, String b){
        username = u;
        password = p;
        birthday = b;
        awaitingMessages = new ConcurrentLinkedQueue<>();
        following = new ConcurrentLinkedQueue<>();
        followers = new ConcurrentLinkedQueue<>();
        blocked = new ConcurrentLinkedQueue<>();
        numOfPosts = 0;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getBirthday(){
        return birthday;
    }

    public void addNewMessage(T message){
        awaitingMessages.add(message);
    }

    public T getNextMessage(){
        return awaitingMessages.poll();
    }

    public boolean addToFollowing(String toFollow){
        if(following.contains(toFollow)){
           return false;
        }

        following.offer(toFollow);
        return true;
    }

    public boolean removeFromFollowing(String toRemove){
        if(!following.contains(toRemove)){
            return false;
        }

        following.remove(toRemove);
        return true;
    }

    public boolean isInFollowing(String toCheck){
        return following.contains(toCheck);
    }

    public boolean addToFollowers(String follower){
        if(followers.contains(follower)){
            return false;
        }

        followers.offer(follower);
        return true;
    }

    public boolean removeFromFollowers(String toRemove){
        if(!followers.contains(toRemove)){
            return false;
        }

        followers.remove(toRemove);
        return true;
    }

    public boolean isInFollowers(String toCheck){
        return followers.contains(toCheck);
    }

    public boolean addToBlocked(String toBlock){
        if(blocked.contains(toBlock)){
            return false;
        }

        blocked.offer(toBlock);
        return true;
    }

    public boolean removeFromBlocked(String toRemove){
        if(!blocked.contains(toRemove)){
            return false;
        }

        blocked.remove(toRemove);
        return true;
    }

    public boolean isInBlocked(String toCheck){
        return blocked.contains(toCheck);
    }

    public ConcurrentLinkedQueue<String> getFollowers(){
        return followers;
    }

    public ConcurrentLinkedQueue<T> getAwaitingMessages(){
        return awaitingMessages;
    }

    public void incNumOfPosts(){
        numOfPosts++;
    }

    public int getAge(){
        int currentYear = Calendar.YEAR;

        int birthYear = 0;

        for(int i=6; i<birthday.length(); i++){
            birthYear = birthYear*10 + (birthday.charAt(i)-'0');
        }

        return currentYear-birthYear;
    }

    public int getNumOfFollowers(){
        return followers.size();
    }

    public int getNumOfFollowing(){
        return following.size();
    }

    public int getNumOfPosts(){
        return numOfPosts;
    }

    public void clearAwaitingMessages(){
        awaitingMessages.clear();
    }



}
