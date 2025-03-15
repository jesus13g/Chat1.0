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
import java.util.HashMap;
import java.util.List;

public class ChatServerImpl implements ChatServer {

    public class ChatServerThreadForClient extends Thread {
        private int id;
        private String username;
        private Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

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
                    inputMsg = this.getIn().readLine();
                    if (inputMsg == null) break;

                    if (inputMsg.trim().toLowerCase().startsWith("ban ")){

                    } else if (inputMsg.trim().toLowerCase().startsWith("unban ")){

                    } else {
                        ChatMessage msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, inputMsg);
                        System.out.println( this.username+"> " + inputMsg);
                        broadcast(msg);
                    }

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

        public BufferedReader getIn(){ return this.in; }

        public PrintWriter getOut(){ return this.out; }
    }

    private static final int DEFAULT_PORT = 1500;

    private int clientId;
    private SimpleDateFormat sdf;
    private final int port;
    private boolean alive;
    private final HashMap<Integer, ChatServerThreadForClient> clients;


    public ChatServerImpl(int port) {
        this.port = port;
        this.clients = new HashMap<>();
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
                synchronized (this.clients) {
                    this.clients.put(clientId, client);
                }

                client.start();
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
        synchronized (this.clients) {
            for(ChatServerThreadForClient client : clients.values()){
                try{
                    System.out.println("Taking out client " + client.getUsername());
                    client.getSocket().close();
                    client.getIn().close();
                    client.getOut().close();
                } catch (IOException e) {
                    System.err.println("Error shutting down client "+ client.getUsername() +": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void broadcast(ChatMessage msg) {
        String timestamp = sdf.format(new java.util.Date());
        ChatServerThreadForClient sender = this.clients.get(clientId);
        String messageWithTimestamp = "[" + timestamp + "] " + sender.getUsername() + ">" + msg.getMessage();
        synchronized(this.clients){
            for(ChatServerThreadForClient client : clients.values()){
                try {
                    client.out.println(messageWithTimestamp);
                } catch (Exception e){
                    System.err.println("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void remove(int id) {
        synchronized(this.clients){
            clients.remove(id);
        }
    }

    public void mostrarClientsConectados(){
        System.out.println("-> Clientes conectados:");
        synchronized(this.clients){
            for(ChatServerThreadForClient client : clients.values()){
                System.out.println("\t-" + client.getId() + ". " + client.getUsername());
            }
        }
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
                if ("clientes".equals(input)){
                    server.mostrarClientsConectados();
                }

                if("close server".equalsIgnoreCase(input)){
                    server.shutdown();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading console input: " + e.getMessage());
        }
    }
}
