package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)) {
            throw new Exception("User already exists");
        }
        User user = new User(name, mobile);
        userMobile.add(mobile);
        return "SUCCESS";
        
    }
    public Group createGroup(List<User> user) {
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectivel

        if(user.size() == 2) {
            Group group = new Group(user.get(1).getName(), 2);
            groupUserMap.put(group, user);
            groupMessageMap.put(group, new ArrayList<>());
            return group;
        } else {
            customGroupCount++;
            Group group = new Group("Group " + customGroupCount, user.size());
            groupUserMap.put(group, user);
            groupMessageMap.put(group, new ArrayList<>());
            adminMap.put(group,user.get(0));
            return group;
        }
    }
    public int createMessage(String content) {
        messageId++;
        Message m = new Message(messageId, content);
        return m.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

        if(groupUserMap.containsKey(group) == false) {
            throw new Exception("Group does not exist");
        }
        if(groupUserMap.get(group).contains(sender) == false) {
            throw new Exception("You are not allowed to send message");
        }
        List<Message> message_list = new ArrayList<>();
        message_list.add(message);
        groupMessageMap.put(group, message_list);
        senderMap.put(message, sender);
        return message_list.size();
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user
        if(!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if(adminMap.get(group) != approver) {
            throw new Exception("Approver does not have rights");
        }
        if(groupUserMap.get(group).contains(user)) {
            throw new Exception("User is not a participant");
        }
        adminMap.replace(group, user);
        return "SUCCESS";
    }
    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        for(Group gp : groupUserMap.keySet()) {
            List<User> user_list = groupUserMap.get(gp);
            if(user_list.contains(user)) {

                for(User admin : adminMap.values()) {
                    if(admin == user) {
                        throw new Exception("Cannot remove admin");
                    }
                }
                groupUserMap.get(gp).remove(user);

//                delete message from database
                for(Message m : senderMap.keySet()) {
                    User u = senderMap.get(m);
                    if(user == u) {
                        senderMap.remove(m);
                        groupMessageMap.get(gp).remove(m);
                        return groupUserMap.get(gp).size() + groupMessageMap.get(gp).size() + senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }
    public String findMessage(Date start, Date end, int K) throws Exception {
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        TreeMap<Integer,String> map = new TreeMap<>();
        ArrayList <Integer> list = new ArrayList<>();
        for (Message m: senderMap.keySet()){
            if( m.getTimestamp().compareTo(start) > 0 && m.getTimestamp().compareTo(end) < 0){
                map.put(m.getId(),m.getContent());
                list.add(m.getId());
            }
        }
        if (map.size() < K){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(list);
        int k = list.get(list.size()-K);
        return map.get(k);
    }

}
