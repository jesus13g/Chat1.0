package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class ChatServerImpl implements ChatServer {

    public class ChatServerThreadForClient extends Thread {
        private int id;
        private String username;
        private Socket socket;
        private BufferedReader in;
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
                    System.out.println("Mensaje recibido: " + inputMsg);
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
    private int port;
    private boolean alive;


    public ChatServerImpl(int port) {
        this.port = port;
    }

    @Override
    public void startup() {
        try(
            ServerSocket serverSocket = new ServerSocket(this.port);
        ) {
            System.out.println("Starting up...");
            this.alive = true;
            //this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while(alive){
                //Por implementar...
            }
        } catch (IOException e) {
            System.err.println("Error starting up: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        //Por implementar...
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
        ChatServerImpl server = new ChatServerImpl(DEFAULT_PORT);
        server.startup();
    }
}
