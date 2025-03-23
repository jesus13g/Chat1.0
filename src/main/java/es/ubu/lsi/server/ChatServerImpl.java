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
 * Clase ChatServerImpl que immplementa la interfaz ChatServer
 */
public class ChatServerImpl implements ChatServer {

    /**
     * Puerto por defecto
     */
    private static final int DEFAULT_PORT = 1500;
    /**
     * identificador del cliente
     */
    private int clientId;
    /**
     * Formato del mensaje
     */
    private SimpleDateFormat sdf;
    /**
     * Puerto del servidor
     */
    private final int port;
    /**
     * Compueba si el servidor esta levantado
     */
    private boolean alive;
    /**
     * Mapa con los clientes. {id: ClientThread}
     */
    private final HashMap<Integer, ChatServerThreadForClient> clients;
    /**
     * Lista con los nombres de los clientes baneados
     */
    private final ArrayList<String> banList;

    /**
     * Constructor. Inicializa los valores del servidor.
     * @param port puerto del servidor
     */
    public ChatServerImpl(int port) {
        this.port = port;
        this.clients = new HashMap<>();
        this.banList = new ArrayList<>();
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    /**
     * Clase interna concurrente para manejar a los clientes coenctados al servidor
     */
    public class ChatServerThreadForClient extends Thread {
        /**
         * Identificador del cliente
         */
        private int id;
        /**
         * Nombre del cliente
         */
        private String username;
        /**
         * Socket del cliente
         */
        private Socket socket;
        /**
         *  Buffer entrada del cliente
         */
        private final BufferedReader in;
        /**
         * Salida al cliente
         */
        private final PrintWriter out;

        /**
         * Constructor del hilo del cliente.
         * @param id identificador del cliente
         * @param username nombre del cliente
         * @param socket socket asignado al cliente
         * @throws IOException Excepcion de in/out
         */
        public ChatServerThreadForClient(int id, String username, Socket socket) throws IOException {
            this.setId(id);
            this.setUsername(username);
            this.setSocket(socket);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        /**
         * Activa el hilo de un cliente.
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
         * Devuelve el identificador del cliente
         * @return id identificador del cliente
         */
        public long getId(){
            return this.id;
        }

        /**
         * Define el valor del identificador del cliente
         * @param id identificador del cliente
         */
        public void setId(int id){
            this.id = id;
        }

        /**
         * Devuelve el nombre del cliente
         * @return username nombre del cliente
         */
        public String getUsername(){
            return this.username;
        }

        /**
         * Define el valor del nombre del cliente.
         * @param username nombre del cliente.
         */
        public void setUsername(String username){
            this.username = username;
        }

        /**
         * Devuelve socket asignado a un cliente.
         * @return socket socket asignado a un cliente.
         */
        public Socket getSocket(){ return this.socket; }

        /**
         * Define el socket asignado a un cliente.
         * @param socket socket asignado a un cliente.
         */
        public void setSocket(Socket socket){ this.socket = socket; }

        /**
         * Devuelve entrada del buffer.
         * @return in Entrada del buffer.
         */
        public BufferedReader getIn(){ return this.in; }

        /**
         * Define salida del buffer.
         * @return out salida del buffer.
         */
        public PrintWriter getOut(){ return this.out; }
    }

    /**
     * Inicializa y levanta el servidor.
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
     * Banea a un cliente por su username
     * @param username nombre del cliente a banear
     */
    private void ban(String username){
        this.banList.add(username);
    }

    /**
     * desbanea a un cliente por su username
     * @param username nombre del cliente a desbanear
     */
    private void unban (String username){
        this.banList.remove(username);
    }

    /**
     * Apaga el servior.
     */
    @Override
    public void shutdown() {
        System.out.println("Shutting down server...");

        if(!clients.isEmpty()) this.takeOutClients();
        this.alive = false;

        System.out.println("Server shut down...");
    }

    /**
     * Expulsa a los clientes del servidor.
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
     * Envia un mensaje a todos los clientes conectados al servidor.
     * @param msg objeto ChatMessage con el mensaje para hacer el broadcast
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
     * Elimina a un cliente del servidor, cerrando su conexión.
     * @param id identificador del cliente
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
     * Permite mostrar los clientes conectados. Función única desde servidor.
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
     * Inicia el hilo principal del servidor.
     * @param args argumentos de entrada
     */
    public static void main(String[] args) {
        final ChatServerImpl server = new ChatServerImpl(DEFAULT_PORT);
        Thread serverThread;
        serverThread = new Thread(new Runnable() { // ######### RF.empaquetar
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
