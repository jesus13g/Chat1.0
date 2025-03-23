package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

import java.io.PrintWriter;

/**
 * Interfaz Chat client
 */
public interface ChatClient{

    /**
     * Inicia el cliente
     * @return true en caso de iniciar correctamente, false caso contrario
     */
    public boolean start();

    /**
     * Envia un mensaje al servidor
     * @param msg mesaje a enviar por el cliente
     */
    public void sendMessage(ChatMessage msg);

    /**
     * Desconecta al cliente del servidor
     */
    public void disconnect();
} 
        