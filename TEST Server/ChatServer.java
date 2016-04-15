import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class ChatServer{

   public static final int PORT = 16789;
   private ChatServerGUI gui;
   public Vector<ThreadedClient> clients = new Vector<ThreadedClient>();
   

   public static void main(String[] args){
      new ChatServer();
   }
   
   public ChatServer(){
   
      System.out.println("Starting the server...");
      
      gui = new ChatServerGUI(this);
      Thread th = new Thread(gui);
      th.start();
      
      try{
      
         ServerSocket ss = new ServerSocket(PORT);
      
         Socket s = null;
      
         while(true){
            System.out.println("Waiting for a client...");
         
            s = ss.accept();
            
            clients.add(new ThreadedClient(s));
            clients.get(clients.size() - 1).start();
            
         }
         
      }
      catch(IOException ioe){
         ioe.printStackTrace();
      }
      
      
   }

   class ThreadedClient extends Thread{
      
      private Socket s = null;
      private ObjectInputStream in = null;
      private ObjectOutputStream out = null;
      private String name = null;
      private JTextArea receiveText;
   
      
      public ThreadedClient(Socket _s){
         s = _s;
         
         
         receiveText = new JTextArea(10,30);  
         receiveText.setBorder(new EtchedBorder());
         receiveText.setLineWrap(true);
         receiveText.setWrapStyleWord(true);
         receiveText.setEditable(false);
         
         
         try{
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
            
            
         }
         catch(IOException ioe){
            ioe.printStackTrace();
         }
         
         
      }
      
      public void run(){
         try{
            name = (String)in.readObject();
            
            System.out.println(name + " has join the chat.");
            
            sendOut("");
            sendOut(name + " has join the chat.");
            
            synchronized(gui)
               {
                  gui.addClient(name,receiveText);
               }
            
         }
         catch(IOException ioe){
            ioe.printStackTrace();
         }
         catch(ClassNotFoundException cnf){
            cnf.printStackTrace();
         }
         
         
         while(true){
            Object obj = null;
            try {
               obj = in.readObject();
            } 
            catch (IOException e) {
               System.out.println("IO Exception encountered! " + e.getMessage());
            } 
            catch (ClassNotFoundException e) {
               System.out.println("Class Not Found Exception encountered! " + e.getMessage());
            }
         
            if (obj instanceof File) {
               //Create File object and then read in File object from client
               File file = null;
               file = (File)obj;
               
            } 
            else if (obj instanceof String) {
               
               String msg = null;
               
               msg = (String)obj;
                                 
               if(msg.equalsIgnoreCase("quit"))
               {
                     
                  sendOut(name + " has left the chat");
                  sendOut("");
                  break;
               }
               
                  
               sendOut(name + ":");
               sendOut(msg);
                  
               System.out.println(name + ": " + msg);
                              
               
            } 
            else {
               System.out.println("How did we get here?");
            }
            
         }
         
         System.out.println(name + " has disconected");
         
         try
         {
            in.close();
            out.close();
            s.close();
         }
         catch(IOException ioe)
         {
            return;
         }
         
               synchronized(gui)
               {
                  gui.removeClient(name,receiveText);
               }
      }
      
      public void sendOut(String msg){
         try{
            out.writeObject(msg);
            out.flush();
            receiveText.append(msg + "\n");
            receiveText.setCaretPosition(receiveText.getDocument().getLength());
         }
         catch(IOException ioe){
         
            ioe.printStackTrace();   
         }
      }
   }
   
   

}