package com.simonebasile.sampleapp.dto;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;


public enum CPMType {
        //S to C
        CONNECTED,
        ALREADY_CONNECTED,
        CHAT_CONNECTED, //Username
        CHAT_DISCONNECTED,
        MESSAGE_RECEIVED, //Message
        MESSAGE_SENT, //Message

        //S to E
        CHAT_AVAILABLE,
        NO_CHAT_AVAILABLE,

        //U to S
        WANT_TO_CHAT(Role.user),

        //E to S
        ACCEPT_CHAT(Role.employee),

        //C to S
        SEND_MESSAGE(Role.user, Role.employee), //Message
        END_CHAT(Role.employee, Role.employee),;

        private final Role[] canSend;

        CPMType(Role... canSend) {
            this.canSend = canSend;
        }

        public boolean canBeSentBy(User u) {
            for (Role role : this.canSend) {
                if(u.getRole() == role) return true;
            }
            return false;
        }
    }

