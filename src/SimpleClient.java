/**
 *
 */
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class SimpleClient implements IConstants {

    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    Scanner scanner;
    String message;
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

    public static void main(String[] args) {
        new SimpleClient();
    }

    SimpleClient() {
        scanner = new Scanner(System.in);
        System.out.println(CONNECT_TO_SERVER);
        System.out.println("Command:");
        System.out.println(EXIT_COMMAND_CL +" -  close server");
        System.out.println("/l  Login  message    -  vsem message");
        System.out.println("/a  message    -  message serveru");
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            writer.println(getLoginAndPassword()); // send: auth <login> <passwd>
            writer.flush();
            new Thread(new ServerListener()).start();
            do {
                message = scanner.nextLine();
                writer.println(message);
                writer.flush();
            } while (!message.equals(EXIT_COMMAND_CL));
            socket.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(CONNECT_CLOSED);
    }

    /**
     * getLoginAndPassword: read login and password from keyboard
     */
    String getLoginAndPassword() {

            System.out.print(LOGIN_PROMPT);
            String login = scanner.nextLine();
            while (login.equals("")) {
                System.out.println("Enter " + LOGIN_PROMPT);
                login = scanner.nextLine();
            }
            System.out.print(PASSWD_PROMPT);
            String password = shifr(scanner.nextLine());
            while (password.equals("")) {
                System.out.println("Enter " + PASSWD_PROMPT);
                password = shifr(scanner.nextLine());
            }


        return AUTH_SIGN + " " + login + " " + password;
    }

    /**
     * ServerListener: get messages from Server
     */
    class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.print(message.equals("\0")?
                        CLIENT_PROMPT : message + "\n");
                    if ((message.equals(AUTH_FAIL))||(message.equals(SERVKAJ_KAJ)))
                        System.exit(-1); // terminate client
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}