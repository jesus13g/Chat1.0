package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient{

    public class ChatClientListener implements Runnable{

        @Override
        public void run() {

        }
    }

    private String server;

    private String username;

    private int port;

    private boolean carryOn;

    private int id;

    public ChatClientImpl(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
        carryOn = true;
    }


    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void sendMessage(ChatMessage msg) {

    }

    @Override
    public void disconnect() {

    }

    public static void main(String[] args) {

    }
}
