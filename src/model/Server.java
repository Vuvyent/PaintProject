package model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Server {
    private final static int serverPort = 50001;
    private ServerSocket serverSocket;
    private HashMap<String, BufferedImage> panels;
    private ArrayList<ClientS> clientsList;
    private Object logSync;
    private int idCounter;

    private class ClientS extends Thread {
        private int id;
        private Socket clientSocet;
        private BufferedReader in;
        private BufferedWriter out;
        private BufferedImage curImage;
        private String panelName;
        private Graphics2D g2;
        private float strokeSize;
        private Color line;

       public ClientS(Socket client){
           id = idCounter;
           clientSocet = client;
           try{
               in = new BufferedReader(new InputStreamReader(clientSocet.getInputStream()));
               out = new BufferedWriter(new OutputStreamWriter(clientSocet.getOutputStream()));
               curImage = null;
               g2 = null;
           }
           catch(IOException e){
               e.printStackTrace();
           }
       }

       public void run(){
           synchronized (logSync){
               System.out.println("Клиент №" + id + " подключился");
               System.out.println("Количество клиентов:" + clientsList.size());
           }
           try{
               while(true){
                   String message = in.readLine();
                   String[] splitMessage = message.split(" ");
                   if(splitMessage[0].equals("Create")){
                       if(panels.containsKey(splitMessage[1])){
                           synchronized (this){
                               out.write("Panel Already Exists\n");
                               out.flush();
                           }
                       }
                       else{
                            curImage = new BufferedImage(1500, 1000,
                                   BufferedImage.TYPE_INT_RGB);
                           g2 = curImage.createGraphics();
                           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                           strokeSize = 2.0f;
                           g2.setStroke(new BasicStroke(strokeSize));
                           line = new Color(0, 0, 0);
                           g2.setPaint(Color.white);
                           g2.fillRect(0,0,1500,1000);
                           g2.setPaint(line);
                           synchronized (panels){
                               panels.put(splitMessage[1], curImage);
                           }
                           synchronized (this){
                               out.write("Panel Created\n");
                               out.flush();
                           }
                           panelName = splitMessage[1];
                           synchronized (logSync){
                               System.out.println("Панель " + splitMessage[1] + " была создана");
                               System.out.println("Количество панелей: " + panels.size());
                           }
                       }
                   } else if(splitMessage[0].equals("Join")){
                       if(!panels.containsKey(splitMessage[1])){
                           synchronized (this){
                               out.write("Wrong Name\n");
                               out.flush();
                           }
                       } else{
                           g2 = panels.get(splitMessage[1]).createGraphics();
                           int[] rgb = new int[1500000];
                           curImage = panels.get(splitMessage[1]);
                           curImage.getRGB(0, 0, 1500, 1000, rgb, 0,
                                   1500);
                           synchronized (this){
                               out.write("Create ");
                               for(int i = 0; i<rgb.length; i++){
                                   out.write(rgb[i] + " ");
                               }
                               out.write("\n");
                               out.flush();
                           }
                           panelName = splitMessage[1];
                       }
                   } else if (splitMessage[0].equals("Draw")){
                       int color = Integer.parseInt(splitMessage[1]);
                       int prevX = Integer.parseInt(splitMessage[2]);
                       int prevY = Integer.parseInt(splitMessage[3]);
                       int newX = Integer.parseInt(splitMessage[4]);
                       int newY = Integer.parseInt(splitMessage[5]);
                       float lineSize = (float) (Integer.parseInt(splitMessage[6]));
                       if(curImage != null){
                           synchronized (curImage){
                               if(g2 != null){
                                   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                   g2.setStroke(new BasicStroke(lineSize));
                                   g2.setPaint(new Color(color));
                                   g2.drawLine(prevX, prevY, newX, newY);
                               } else{
                                   synchronized (logSync){
                                       System.out.println("Такое вроде не возможно, но пусть будет");
                                   }
                               }
                           }
                           for(ClientS i : clientsList){
                               if(i.panelName.equals(panelName)){
                                   synchronized (i){
                                       i.out.write(message + "\n");
                                       i.out.flush();
                                   }
                               }
                           }
                       } else{
                           synchronized (logSync){
                               System.out.println("Такое, по-идее, тоже");
                           }
                       }

                   }
               }
           } catch (Exception e){
               e.printStackTrace();
           } finally {
               try{
                   clientSocet.close();
                   in.close();
                   out.close();
                   synchronized (clientsList){
                       clientsList.remove(this);
                       synchronized (logSync){
                           System.out.println("Пользователь №" + id + " отключился от сервера");
                           System.out.println("Количество текущих пользователей: " + clientsList.size());
                       }
                   }
                   panelIsUsed(panelName);
               } catch (Exception e){
                   e.printStackTrace();
               }

           }
       }
    }

    public Server(){
        panels = new HashMap<String, BufferedImage>();
        clientsList = new ArrayList<ClientS>();
        logSync = new Object();
        try{
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Сервер запущен на порте: " + serverSocket.getLocalPort());
            while (true){
                ClientS newClient = new ClientS(serverSocket.accept());
                idCounter++;
                synchronized (clientsList){
                    clientsList.add(newClient);
                    newClient.start();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void panelIsUsed(String name){
        for(ClientS i : clientsList){
            if(i.panelName.equals(name)){
                return;
            }
        }
        synchronized (panels){
            panels.remove(name);
            synchronized (logSync){
                System.out.println("Панель " + name + " не используется и была удалена");
                System.out.println("Количество панелей: " + panels.size());
            }
        }
    }
}
