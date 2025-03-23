package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * interfaz ChatServer
 */
public interface ChatServer {

    /**
     * Levanta el servidor
     */
    public void startup();

    /**
     * Apaga el servidor
     */
    public void shutdown();

    /**
     * Envia un mesaje a todos los clientes conectados al servidor.
     * @param msg
     */
    public void broadcast(ChatMessage msg);

    /**
     * Elimina un cliente del servidor
     * @param id
     */
    public void remove(int id);
}
