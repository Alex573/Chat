
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ChatClient extends JFrame implements  IConstants {

    final String TITLE_OF_PROGRAM = "Client for net.chat";
    final int START_LOCATION = 150;
    final int WINDOW_WIDTH = 450;
    final int WINDOW_HEIGHT = 500;
    final String BTN_ENTER = "Enter";
    final String AUTH_INVITATION = "You must login \n";


    JTextArea dialogue; // area for dialog
    JTextField message; // field for entering messages
    boolean isAuthorized = false;// flag of authorization


    Socket socket;
    PrintWriter writer;
    BufferedReader reader;

    public static void main(String[] args) {
        new ChatClient();
    }

    /**
     * Constructor:
     * Creating a window and all the necessary elements on it
     */
    ChatClient() {
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(START_LOCATION, START_LOCATION, WINDOW_WIDTH, WINDOW_HEIGHT);
        addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent event) { // if window closed
                try {
                   disskonekt();
                } catch (Exception ex) {}
            }
            public void windowDeactivated(WindowEvent event) {}
            public void windowActivated(WindowEvent event) {}
            public void windowDeiconified(WindowEvent event) {}
            public void windowIconified(WindowEvent event) {}
            public void windowClosed(WindowEvent event) {}
            public void windowOpened(WindowEvent event) {}
        });
        // area for dialog

        dialogue = new JTextArea();
        dialogue.setLineWrap(true);
        dialogue.setEditable(false);
        JScrollPane scrollBar = new JScrollPane(dialogue);
        // panel for connamd field and button
        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        JPanel bpv = new JPanel();
        bpv.setLayout(new BoxLayout(bpv, BoxLayout.X_AXIS));
        message = new JTextField();
        message.addActionListener(new SendMessage());
        JButton enter = new JButton(BTN_ENTER);
        JButton opensok = new JButton("Connect");
        JButton closesok = new JButton("Diskonect");
        enter.addActionListener(new SendMessage());
        opensok.addActionListener(new Podkluchenie());
        closesok.addActionListener(new Otklichenie());
        // adding all elements to the window
        bpv.add(opensok);
        bpv.add(closesok);
        bp.add(message);
        bp.add(enter);

        add(BorderLayout.NORTH, bpv);

        add(BorderLayout.CENTER, scrollBar);
        add(BorderLayout.SOUTH, bp);
        setVisible(true);
        connect(); // connect to the Server
    }
    class SendMessage implements ActionListener{
        String l = "";
        String p = "";
        @Override
        public void actionPerformed(ActionEvent e) {


           if (message.getText().trim().length() > 0) {
               if (!isAuthorized){
                   try {
                       String[] wds = message.getText().split(" ");
                       dialogue.append(AUTH_SIGN + " " + wds[0] + " " + shifr(wds[1])+"\n");
                       writer.println(AUTH_SIGN + " " + wds[0] + " " + shifr(wds[1]));
                       writer.flush();
                   } catch (Exception e1) {
                       dialogue.append("Nepravilno vveli login i parol"+"\n");
                   }


               }

               if(isAuthorized){
                writer.println(message.getText());
                writer.flush();
               }
            }
            message.setText("");
            message.requestFocusInWindow();

        }
    }
    class Podkluchenie implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            dialogue.append("Podkluchenie"+"\n");
            //connect();

        }
    }
    class Otklichenie implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            //dialogue.append("otkli"+"\n");
            disskonekt();

        }
    }

    void disskonekt() {
        try {
            socket.close();
            isAuthorized = false;
            dialogue.append("Otkluchilsi"  + "\n");

        } catch (NullPointerException e) {
            dialogue.append("Ne byl podkluchen"  + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to the Server
     */
    void connect() {
        String s;
       /* if(isAuthorized){
            dialogue.append("Uze podklichilis"+"\n");
            return;
        }*/
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            new Thread(new ServerListener()).start();// start Server listener

        } catch (Exception ex) { 
            System.out.println(ex.getMessage());
        }
       /* writer.println(getLoginAndPassword()); // send: auth <login> <passwd>
        writer.flush();*/
        dialogue.append(AUTH_INVITATION + "\n");
        isAuthorized = false;


    }

    /**
     * ServerListener: get messages from Server
     */
    class ServerListener implements Runnable {
        String message;
        public void run() {
            try {
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("Hello, ")) // check authorisation
                        isAuthorized = true;
                    if (!message.equals("\0") && isAuthorized)
                        dialogue.append(message + "\n");
                    if (message.equals(AUTH_FAIL)||(message.equals(SERVKAJ_KAJ)))
                        isAuthorized = false;
                        socket.close();// terminate client

                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    private static String shifr(String str)  {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(str.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (byte aByteData : byteData) {
            sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
        }
        str = sb.toString();
        return str;
    }







}