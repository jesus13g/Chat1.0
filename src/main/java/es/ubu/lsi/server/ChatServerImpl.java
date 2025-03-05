package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

import java.text.SimpleDateFormat;

public class ChatServerImpl implements ChatServer {

    public class ServerThreadForClient implements Runnable {

        @Override
        public void run() {

        }
    }

    private static int DEFAULT_PORT = 1500;

    private int clientId;

    private SimpleDateFormat sdf;

    private int port;

    private boolean alive;

    public ChatServerImpl(int port) {
        this.port = port;
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void broadcast(ChatMessage msg) {

    }

    @Override
    public void remove(int id) {

    }
}
