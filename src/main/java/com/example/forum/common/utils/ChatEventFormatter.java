package com.example.forum.common.utils;

import com.example.forum.domain.Enum.ChatRole;
import com.example.forum.domain.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ChatEventFormatter {
    private ChatEventFormatter() {}

    public static String kickMessage(UserEntity actor, UserEntity targetUser){
        return actor.displayUsername() + " removed " + targetUser.displayUsername() + " from group.";
    }

    public static String roleUpdateMessage(UserEntity actor, UserEntity targetUser, ChatRole role){
        return actor.displayUsername() +" set "+ targetUser.displayUsername()+ " as " + role;
    }

    public static String addMemberMessage(UserEntity actor, List<UserEntity> userList){
        String joinedNames = userList.stream().map(UserEntity::displayUsername).collect(Collectors.joining(", "));
        return actor.displayUsername() + " added " + joinedNames + " to group.";
    }

    public static String leaveChatMessage(UserEntity actor){
        return actor.displayUsername() + " has left the chat.";
    }

    public static String changeGroupChatAvatar(UserEntity actor){
        return actor.displayUsername()+ " has changed the group avatar.";
    }

    public static String changeGroupName(UserEntity actor, String newName){
        return actor.displayUsername()+ " has changed the group's name to " + newName + ".";
    }
}
