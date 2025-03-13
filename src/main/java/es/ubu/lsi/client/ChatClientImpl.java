package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientImpl implements ChatClient {

    // Clase interna para escuchar mensajes del servidor
    public class ChatClientListener implements Runnable {

        private Socket socket;

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
                while ((message = in.readLine()) != null) {
                    System.out.println("Mensaje del servidor: " + message);
                }
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

    private String server;
    private String username;
    private int port;
    private boolean carryOn;
    private Socket socket;
    private PrintWriter out;

    public ChatClientImpl(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
        carryOn = true;
    }

    @Override
    public boolean start() {
        try {
            System.out.println("Conectando con el servidor: " + server + ":" + port);
            // Conectar con el servidor
            this.socket = new Socket(server, port);
            this.out = new PrintWriter(socket.getOutputStream(), true);  // Inicialización de out
            this.out.println(username);
            Thread listener = new Thread(new ChatClientListener(socket));
            listener.start();

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
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void sendMessage(ChatMessage msg) {
        if (out != null) {
            out.println(msg.getMessage());
        } else {
            System.out.println("No se pudo enviar el mensaje. El cliente no está conectado.");
        }
    }

    @Override
    public void disconnect() {
        carryOn = false;
        try {
            if (out != null) {
                out.println("logout de " + username);
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Desconectado del servidor.");
        } catch (IOException e) {
            System.out.println("Error al desconectar: " + e.getMessage());
        }
    }

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
