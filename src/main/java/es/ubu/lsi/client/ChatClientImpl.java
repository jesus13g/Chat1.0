package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Clase cliente que hereda de chatClient
 */
public class ChatClientImpl implements ChatClient {

    /**
     * Clase interna para escuchar mensajes del servidor
     */
    public static class ChatClientListener implements Runnable {

        private final Socket socket;

        public ChatClientListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                // Mientras la conexión esté activa, escuchar mensajes del servidor
                while ((message = in.readLine()) != null) System.out.println(message);

            } catch (IOException e) {
                System.out.println("Error al recibir el mensaje: " + e.getMessage());
            } finally {
                if (in != null) {
                    try{
                        in.close();
                    } catch (IOException e){
                        System.out.println("Error al cerrar el buffer: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * nombre/direccion del servidor
     */
    private final String server;
    /**
     * nombre del cliente
     */
    private final String username;
    /**
     * puerto del servidor
     */
    private final int port;
    /**
     * Ccomprueba si esta cargado el cliente
     */
    private boolean carryOn;
    /**
     * Socket del cliente
     */
    private Socket socket;
    /**
     * Salida del cliente
     */
    private PrintWriter out;

    /**
     * Constructor. Inicializa los datos de un cliente.
     * @param server nombre/direccion del servidor
     * @param port puerto del servidor
     * @param username nombre del cliente
     */
    public ChatClientImpl(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.carryOn = true;
    }

    /**
     * Conecta con el servidor, permitiendo enviar y recibir mensajes.
     * @return true si conecta, false en caso contrario.
     */
    @Override
    public boolean start() {
        try {
            this.inicializaConexion();

            // Enviar mensaje de conexión
            out.println("Connect " + username);
            System.out.println("Conexion establecida.");

            // Leer mensajes de la consola y enviarlos al servidor
            Scanner scanner = new Scanner(System.in);
            while (this.carryOn) {
                String message = scanner.nextLine();
                if ("logout".equalsIgnoreCase(message)) {
                    disconnect();
                    break;
                }
                sendMessage(new ChatMessage(0, ChatMessage.MessageType.MESSAGE, message));
            }
        } catch (IOException e) {
            System.err.println("Error al conectar el servidor: " + e.getMessage());
        }
        return true;
    }

    /**
     * Inicializa los sockets y la salida al servidor.
     * @throws IOException en caso de fallo de conexión.
     */
    private void inicializaConexion() throws IOException {
        System.out.println("Conectando con el servidor: " + server + ":" + port);
        // Conectar con el servidor
        this.socket = new Socket(server, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);  // Inicialización de out
        this.out.println(username);
        Thread listener = new Thread(new ChatClientListener(socket));
        listener.start();
    }

    /**
     * Permite enviar un mensaje al servidor.
     * @param msg mesaje a enviar por el cliente.
     */
    @Override
    public void sendMessage(ChatMessage msg) {
        if (out != null) {
            out.println(msg.getMessage());
        } else {
            System.out.println("No se pudo enviar el mensaje. El cliente no está conectado.");
        }
    }

    /**
     * Envia mensaje de desconexión y se desconecta del servidor.
     */
    @Override
    public void disconnect() {
        ChatMessage msg = new ChatMessage(0, ChatMessage.MessageType.LOGOUT, "logout");
        sendMessage(msg);

        carryOn = false;
        System.out.println("Desconectado del servidor.");
    }

    /**
     * Inicia la conexion con el servidor mediante los valores pasados como argumento.
     * @param args entrada de valores para definir el servidor
     */
    public static void main(String[] args) {
        String server = "localhost";
        String username = null;
        if (args.length == 0) {
            System.exit(1);
        } else if (args.length == 1) {
            server = "localhost";
            username = args[0];
        } else {
            server = args[0];
            username = args[1];
        }

        int port = 1500;
        ChatClientImpl client = new ChatClientImpl(server, port, username);
        client.start();
    }
}
