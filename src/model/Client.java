package model;

import view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;

public class Client implements ActionListener {
    private BufferedImage bim;
    private JFrame frame;
    private MainMenu menu;
    private DrawPain drawPain;
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private Graphics2D g2;
    private int prevX, prevY, newX, newY;
    private Color line;
    private float strokeSize;

    public class DrawArea extends JComponent {

        private class MyMouseAdapter extends MouseAdapter{

            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                newX = e.getX();
                newY = e.getY();
                if (g2 != null) {
                    g2.drawLine(prevX, prevY, newX, newY);
                    repaint();
                    String message = "Draw " + line.getRGB() + " " + prevX + " " + prevY + " " + newX + " " + newY + " "+ (int) strokeSize +"\n";
                    try {
                        out.write(message);
                        out.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    prevX = newX;
                    prevY = newY;
                }
            }
        }

        public DrawArea(){
            setSize(1500,1000);
            MyMouseAdapter adapter = new MyMouseAdapter();
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
            line = new Color(0,0,0);
            strokeSize = 2.0f;
        }

        @Override
        public void paintComponent(Graphics g) {
            g.drawImage(bim, 0, 0, null);
        }

        public void clear() {
            g2.setPaint(Color.white);
            g2.fillRect(0, 0, 1500, 1000);
            g2.setPaint(line);
            repaint();
        }

        public void setStrokeSize(float f){
            strokeSize = f;
        }

        public float getStrokeSize() {
            return strokeSize;
        }

        public void setLineSize(float size){
            strokeSize = size;
            g2.setStroke(new BasicStroke(strokeSize));
        }

        public void setLineColor(Color c){
            line = c;
            g2.setPaint(line);
        }

    /*addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            // save coord x,y when mouse is pressed
            oldX = e.getX();
            oldY = e.getY();
        }
    });*/
    }

    public class DrawPain extends JPanel implements ActionListener {
        JButton clearBtn, eraser, color;
        DrawArea drawArea;
        JTextField red, green, blue;
        MainMenu menu;

        public DrawPain(){
            drawArea = new DrawArea();

            setSize(1500, 1000);
            setLayout(new BorderLayout());
            add(drawArea, BorderLayout.CENTER);
            JPanel controls = new JPanel();
            JPanel colorSelect = new JPanel();
            color = new JButton("color");
            color.addActionListener(this);
            red = new JTextField("0", 3);
            green = new JTextField("0",3);
            blue = new JTextField("0",3);
            colorSelect.add(red, BorderLayout.CENTER);
            colorSelect.add(green, BorderLayout.CENTER);
            colorSelect.add(blue, BorderLayout.CENTER);
            colorSelect.add(color, BorderLayout.CENTER);
            controls.add(colorSelect);

            eraser = new JButton("Eraser");
            eraser.addActionListener(this);
            controls.add(eraser);

            add(controls, BorderLayout.NORTH);
        }

        public void addListenerToDrawArea(MouseAdapter adapter){
            drawArea.addMouseListener(adapter);
            drawArea.addMouseMotionListener(adapter);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == color){
                int r = Integer.parseInt(red.getText());
                int g = Integer.parseInt(green.getText());
                int b = Integer.parseInt(blue.getText());
                if(r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255){
                    JOptionPane.showMessageDialog(null, "Wrong RGB presentation of color.");
                }
                else{
                    Color newColor = new Color(r, g, b);
                    drawArea.setLineColor(newColor);
                    drawArea.setLineSize(2.0f);
                }

            }
            if (e.getSource() == eraser) {
                drawArea.setLineColor(new Color(255,255,255));
                drawArea.setLineSize(20.0f);
            }
        }
    }

    private class Feedback extends Thread{
        private String message;
        private String[] splitMessage;

        public Feedback(){
            this.start();
        }

        public void run(){
            try{
                while(true) {
                    message = in.readLine();
                    splitMessage = message.split(" ");
                    if (message.equals("Panel Already Exists")) {
                        JOptionPane.showMessageDialog(null, "Panel with this name already exists");
                    }
                    if (message.equals("Panel Created")) {
                        bim = new BufferedImage(1500, 1000, BufferedImage.TYPE_INT_RGB);
                        g2 = bim.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        strokeSize = 2.0f;
                        g2.setStroke(new BasicStroke(strokeSize));
                        g2.setPaint(Color.white);
                        g2.fillRect(0, 0, 1500, 1000);
                        g2.setPaint(line);
                        drawPain = new DrawPain();
                        frame.remove(menu);
                        frame.add(drawPain);
                        frame.repaint();
                    }
                    if (message.equals("Wrong Name")) {
                        JOptionPane.showMessageDialog(null, "No panel with this name");
                    }
                    if (splitMessage[0].equals("Create")) {
                        bim = new BufferedImage(1500, 1000, BufferedImage.TYPE_INT_RGB);
                        int[] rgb = new int[1500000];
                        for (int i = 1; i < splitMessage.length; i++) {
                            rgb[i - 1] = Integer.parseInt(splitMessage[i]);
                        }
                        bim.setRGB(0, 0, 1500, 1000, rgb, 0, 1500);
                        g2 = bim.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        strokeSize = 2.0f;
                        g2.setStroke(new BasicStroke(strokeSize));
                        g2.setPaint(new Color(0,0,0));
                        drawPain = new DrawPain();
                        frame.remove(menu);
                        frame.add(drawPain);
                        frame.repaint();
                    }
                    if(splitMessage[0].equals("Draw")){
                        int color = Integer.parseInt(splitMessage[1]);
                        int prevX1 = Integer.parseInt(splitMessage[2]);
                        int prevY1 = Integer.parseInt(splitMessage[3]);
                        int newX1 = Integer.parseInt(splitMessage[4]);
                        int newY1 = Integer.parseInt(splitMessage[5]);
                        float lineSize = (float) (Integer.parseInt(splitMessage[6]));
                        g2.setStroke(new BasicStroke(lineSize));
                        g2.setPaint(new Color(color));
                        g2.drawLine(prevX1, prevY1, newX1, newY1);
                        drawPain.repaint();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public Client(){
        line = new Color(0, 0, 0);
        strokeSize = 2.0f;
        frame = new JFrame("Paint");
        frame.setSize(1600, 1100);
        menu = new MainMenu();
        menu.addListener(this);
        frame.add(menu);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        try{
            clientSocket = new Socket("localhost", 50001);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            Feedback feedback = new Feedback();
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            if(e.getSource() == menu.getCreate()) {
                String message = "Create " + menu.getCreateText().getText() + "\n";
                out.write(message);
                out.flush();
            }
            if(e.getSource() == menu.getOpen()) {
                String message = "Join " + menu.getOpenText().getText() +"\n";
                out.write(message);
                out.flush();
            }
        }catch (Exception ex){
                ex.printStackTrace();
        }
    }
}
