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

/**
 *
 */
public class ChatServerImpl implements ChatServer {

    /**
     *
     */
    private static final int DEFAULT_PORT = 1500;
    /**
     *
     */
    private int clientId;
    /**
     *
     */
    private SimpleDateFormat sdf;
    /**
     *
     */
    private final int port;
    /**
     *
     */
    private boolean alive;
    /**
     *
     */
    private final HashMap<Integer, ChatServerThreadForClient> clients;
    /**
     *
     */
    private final ArrayList<String> banList;

    /**
     *
     * @param port
     */
    public ChatServerImpl(int port) {
        this.port = port;
        this.clients = new HashMap<>();
        this.banList = new ArrayList<>();
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    /**
     *
     */
    public class ChatServerThreadForClient extends Thread {
        /**
         *
         */
        private int id;
        /**
         *
         */
        private String username;
        /**
         *
         */
        private Socket socket;
        /**
         *
         */
        private final BufferedReader in;
        /**
         *
         */
        private final PrintWriter out;

        /**
         *
         * @param id
         * @param username
         * @param socket
         * @throws IOException
         */
        public ChatServerThreadForClient(int id, String username, Socket socket) throws IOException {
            this.setId(id);
            this.setUsername(username);
            this.setSocket(socket);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        /**
         *
         */
        public void run() {
            try {
                String inputMsg;
                while ((inputMsg = in.readLine()) != null) {
                    if (inputMsg.equalsIgnoreCase("logout")) {
                        System.out.println("Se ha desconectado " + this.getUsername());
                        remove(this.id);
                        break;
                    } else if (inputMsg.trim().toLowerCase().startsWith("ban ")){
                        String usernameBan = inputMsg.split(" ")[1];
                        ban(usernameBan);
                        System.out.println(this.username + " ha baneado a " + usernameBan);
                    } else if (inputMsg.trim().toLowerCase().startsWith("unban ")){
                        String usernameUnban = inputMsg.split(" ")[1];
                        unban(usernameUnban);
                        System.out.println(this.username + " ha desbaneado a " + usernameUnban);
                    } else {
                        ChatMessage msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, inputMsg);
                        System.out.println( this.username+"> " + inputMsg);
                        broadcast(msg);
                    }
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

        /**
         *
         * @return
         */
        public long getId(){
            return this.id;
        }

        /**
         *
         * @param id
         */
        public void setId(int id){
            this.id = id;
        }

        /**
         *
         * @return
         */
        public String getUsername(){
            return this.username;
        }

        /**
         *
         * @param username
         */
        public void setUsername(String username){
            this.username = username;
        }

        /**
         *
         * @return
         */
        public Socket getSocket(){ return this.socket; }

        /**
         *
         * @param socket
         */
        public void setSocket(Socket socket){ this.socket = socket; }

        /**
         *
         * @return
         */
        public BufferedReader getIn(){ return this.in; }

        /**
         *
         * @return
         */
        public PrintWriter getOut(){ return this.out; }
    }

    /**
     *
     */
    @Override
    public void startup() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
            System.out.println("Starting up...");
            this.alive = true;
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
                this.clients.put(clientId, client);
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

    /**
     *
     * @param username
     */
    private void ban(String username){
        this.banList.add(username);
    }

    /**
     *
     * @param username
     */
    private void unban (String username){
        this.banList.remove(username);
    }

    /**
     *
     */
    @Override
    public void shutdown() {
        System.out.println("Shutting down server...");

        if(!clients.isEmpty()) this.takeOutClients();
        this.alive = false;

        System.out.println("Server shut down...");
    }

    /**
     *
     */
    private void takeOutClients(){
        synchronized (this.clients) {
            for(ChatServerThreadForClient client : clients.values()){
                System.out.println("Taking out client " + client.getUsername());
                remove((int) client.getId());
            }
        }
    }

    /**
     *
     * @param msg
     */
    @Override
    public void broadcast(ChatMessage msg) {
        String timestamp = sdf.format(new java.util.Date());
        ChatServerThreadForClient sender = this.clients.get(msg.getId());

        String messageWithTimestamp = "[" + timestamp + "] " + sender.getUsername() + ">" + msg.getMessage();
        synchronized(this.clients){
            for(ChatServerThreadForClient client : clients.values()){
                try {
                    if (client.getId() != msg.getId() && !this.banList.contains(client.getUsername())){
                        client.out.println(messageWithTimestamp);
                    }
                } catch (Exception e){
                    System.err.println("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }

    /**
     *
     * @param id
     */
    @Override
    public void remove(int id) {
        try {
            ChatServerThreadForClient client = this.clients.remove(id);
            if (client != null) {
                client.getSocket().close();
                client.getIn().close();
                client.getOut().close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public void mostrarClientsConectados(){
        System.out.println("-> Clientes conectados:");
        synchronized(this.clients){
            for(ChatServerThreadForClient client : clients.values()){
                System.out.println("\t-" + client.getId() + ". " + client.getUsername());
            }
        }
    }

    /**
     *
     * @param args
     */
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
