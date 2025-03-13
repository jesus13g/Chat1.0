package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatServerImpl implements ChatServer {

    public class ChatServerThreadForClient extends Thread {
        private int id;
        private String username;
        private Socket socket;
        private final BufferedReader in;
        private PrintWriter out;

        public ChatServerThreadForClient(int id, String username, Socket socket) throws IOException {
            this.setId(id);
            this.setUsername(username);
            this.setSocket(socket);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void run() {
            try {
                String inputMsg;
                while (true) {
                    inputMsg = in.readLine();
                    if (inputMsg == null) break;

                    ChatMessage msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, inputMsg);
                    System.out.println( this.username+"> " + inputMsg);
                    broadcast(msg);

                    if (inputMsg.equalsIgnoreCase("logout")) break;
                }
            } catch (IOException e){
                System.out.println("Error al recibir el mensaje: " + e.getMessage());
            } finally {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar la conexión con el cliente: " + e.getMessage());
                }
            }
        }

        public long getId(){
            return this.id;
        }

        public void setId(int id){
            this.id = id;
        }

        public String getUsername(){
            return this.username;
        }

        public void setUsername(String username){
            this.username = username;
        }

        public Socket getSocket(){ return this.socket; }

        public void setSocket(Socket socket){ this.socket = socket; }
    }

    private static final int DEFAULT_PORT = 1500;

    private int clientId;
    private SimpleDateFormat sdf;
    private final int port;
    private boolean alive;
    private List<ChatServerThreadForClient> clients;

    public ChatServerImpl(int port) {
        this.port = port;
        this.clients = new ArrayList<ChatServerThreadForClient>();
    }

    @Override
    public void startup() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
            System.out.println("Starting up...");
            this.alive = true;
            //this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while(alive){
                System.out.println("Waiting for connection...");
                Socket clientSocket = serverSocket.accept();

                BufferedReader temIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String username = temIn.readLine();
                if (username == null || username.isEmpty()){
                    username = "ClientInvitado_" + this.clientId;
                }

                clientId++;
                ChatServerThreadForClient client = new ChatServerThreadForClient(clientId, username, clientSocket);
                clients.add(client);
                client.start();
                System.out.println("New client connected: " + username);
            }
        } catch (IOException e) {
            System.err.println("Error starting up: " + e.getMessage());
        } finally {
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down server...");
        this.alive = false;
        if(!clients.isEmpty()) {
            this.takeOutClients();
        }
        System.out.println("Server shut down...");
    }

    private void takeOutClients(){
        for(ChatServerThreadForClient client : clients){
            try{
                System.out.println("Taking out client " + client.getUsername());
                client.getSocket().close();
            } catch (IOException e) {
                System.err.println("Error shutting down client "+ client.getUsername() +": " + e.getMessage());
            }
        }
    }

    @Override
    public void broadcast(ChatMessage msg) {
        //Por implementar...
    }

    @Override
    public void remove(int id) {
        //Por implementar...
    }

    public static void main(String[] args) {
        final ChatServerImpl server = new ChatServerImpl(DEFAULT_PORT);
        Thread serverThread = new Thread(new Runnable() { // ######### RF.empaquetar
            @Override
            public void run() {
                server.startup();
            }
        });
        serverThread.start();

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String input;
            while ((input = consoleReader.readLine()) != null){
                if("logout".equalsIgnoreCase(input)){
                    server.shutdown();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading console input: " + e.getMessage());
        }
    }
}
