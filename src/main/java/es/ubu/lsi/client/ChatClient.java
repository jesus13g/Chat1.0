package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

import java.io.PrintWriter;

public interface ChatClient{
    
    public boolean start();
    
    public void sendMessage(ChatMessage msg);

    public void disconnect();
} 
        