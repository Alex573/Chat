/**
 *Переделал обработку команд пользоватял:
 * новые команды работы с базой(новый пользователь, удаление, печать, смена пароля)
 * новые команды для отправки сообщений всем или одному
 * ввод в сервере проверяется и если это не команда выводится сообщение или если неправильная команда тоже выводится
 * также доделан процесс закрытия сервера(отключаются клиенты отключаются процессы)
 * сообщение отправленные клиентами отправляются всем клиентам
 * добавлены  сколько клиентов в чате при входе нового и выходе
 * доделана авторизация войти без логина и пароля правильного нельзя----"наверное".
 * добавлена возможность отправки клинтеом клиенту лично и клиентом серверу лично ну и сервером клиенту.
 * добавил шифрование.
 * Вообщем доделывать еще и доделывать.
 *
 */
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;


class SimpleServer implements IConstants {

    int client_count = 0;
    ServerSocket server;
    Socket socket;
    ArrayList<ClientHandler> clients;
    public synchronized void subscribe ( ClientHandler o ) {
        clients . add ( o );
    }
    //shifrovka
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
        new SimpleServer();
    }

    SimpleServer() {
        System.out.println(SERVER_START);
        System.out.println("Command:");
        System.out.println("/s "+ EXIT_COMMAND +" -  close server");
        System.out.println("/s "+ NEWLOGIN_COMMAND +" - new login");
        System.out.println("/s "+ DELLOGIN_COMMAND +" - del login");
        System.out.println("/s "+ CHANGEPAS_COMMAND +" - change password");
        System.out.println("/s "+ PRINTBAS_COMMAND +" - change password");
        System.out.println("/l  Login  message    -  clientu message");
        System.out.println("/a  message    - all message");
        clients = new ArrayList<>();
        new Thread(new CommandHandler()).start();
        try {
            server = new ServerSocket(SERVER_PORT);
            while (true) {
                socket = server.accept();


                System.out.println(CLIENT_JOINED);
                new Thread(new ClientHandler(socket)).start();

            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(SERVER_STOP);
    }

    /**
     * checkAuthentication: check login and password
     */
    private boolean checkAuthentication(String login, String passwd) {
        Connection connect;
        boolean result = false;
        try {
            // connect db
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            // looking for login && passwd in db
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_SELECT.replace("?", login));
            while (rs.next())
                result = rs.getString(PASSWD_COL).equals(passwd);

            for (ClientHandler o : clients) {
                if (o.name.equals(login)){
                    result=false;
                }
            }

            // close all
            rs.close();
            stmt.close();
            connect.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * CommandHandler: processing of commands from server console
     */
    class CommandHandler implements Runnable {
        Scanner scanner = new Scanner(System.in);

        @Override
        public void run() {
            String command;
            String login;
            String password;
            boolean workserv = true;
            while (workserv){
                command = scanner.nextLine();
                if (command.startsWith("/s")){
                    String[] wds = command.split(" ");
                   try {
                       switch (wds[1]) {
                           case (EXIT_COMMAND):
                               try {
                                   for (ClientHandler o : clients) {
                                       o.sendMsg(SERVKAJ_KAJ);
                                       o.socket.close();
                                   }

                                   server.close();
                                   workserv = false;
                               } catch (Exception ex) {
                                   System.out.println(ex.getMessage());
                               }
                               break;

                           case (NEWLOGIN_COMMAND):
                               System.out.println("Enter the new username:");
                               login = scanner.nextLine();
                               System.out.println("Enter password:");
                               password = shifr(scanner.nextLine());
                               MakeDBFile.greatNewLogin(login, password);
                               break;
                           case (DELLOGIN_COMMAND):
                               System.out.println("Enter the delet username:");
                               login = scanner.nextLine();
                               MakeDBFile.delLogin(login);
                               break;
                           case (CHANGEPAS_COMMAND):
                               System.out.println("Enter the username:");
                               login = scanner.nextLine();
                               System.out.println("Enter new password:");
                               password = shifr(scanner.nextLine());
                               MakeDBFile.changePas(login, password);
                               break;
                           case (PRINTBAS_COMMAND):
                               MakeDBFile.printBas();
                               break;
                           default:
                               System.out.println("There is no such command!!!");

                       }
                   }catch (ArrayIndexOutOfBoundsException e){
                       System.out.println("An empty string!!!");
                   }


                } if (command.startsWith("/l")){
                    try {
                        String[] wds = command.split(" ");
                        command = command.substring(3);
                        for (ClientHandler o : clients) {
                            if (o.name.equals(wds[1])) {
                                o.sendMsg("Server say: "+command);
                                o.sendMsg("\0");
                            }
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("An empty string!!!");
                    }


                } if(command.startsWith("/a")){

                    try {
                        command = command.substring(3);
                        for (ClientHandler o : clients) {
                            o.sendMsg("Server say: "+command);
                            o.sendMsg("\0");
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("An empty string!!!");
                    }

                }
                /*else {

                    System.out.println("Wrong command");
                }*/
            }
        }
    }

    /**
     * ClientHandler: service requests of clients
     */
    class ClientHandler implements Runnable {
        BufferedReader reader;
        PrintWriter writer;
        Socket socket;
        String name;

        ClientHandler(Socket clientSocket) {

            try {
                socket = clientSocket;
                reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                //name = "Client #" + client_count;
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }


        }

        @Override
        public void run() {

            String message;
            try {
                do {
                    message = reader.readLine();
                    if (message != null) {

                        if (message.startsWith(AUTH_SIGN)) {
                            String[] wds = message.split(" ");

                            if (checkAuthentication(wds[1], wds[2])) {
                                name = wds[1];
                                subscribe(this);
                                client_count++;
                                writer.println("Hello, " + name);
                                writer.println("\0");
                                vchod();
                                System.out.println(name+CLIENT_JOINED);

                            } else {
                                System.out.println(name + ": " + AUTH_FAIL);
                                writer.println(AUTH_FAIL);
                                message = EXIT_COMMAND;
                            }
                        }else if (message.startsWith("/l")) {
                            System.out.println(name + ": " + message);
                            String[] wds = message.split(" ");
                            message = message.substring(3);
                            try {
                                for (ClientHandler o : clients) {
                                    if (o.name.equals(wds[1])) {
                                        o.sendMsg(name+"say lichno: "+message);
                                        o.sendMsg("\0");
                                    }
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                writer.println("Server say: An empty string!!!");
                            }

                        }else  if (message.startsWith("/a")){
                            System.out.println(name + ": " + message);
                            message = message.substring(3);
                            try {
                                System.out.println(name + " say lichno serveru: "+ message);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                writer.println("Server say: An empty string!!!");
                            }
                        }else if (!message.equalsIgnoreCase(EXIT_COMMAND_CL)) {
                            System.out.println(name + ": " + message);
                            sendMessageToAllClients(name+": " + message);
                            sendMessageToAllClients("\0");
                        }
                        writer.flush();

                    }
                } while (!message.equalsIgnoreCase(EXIT_COMMAND_CL));
                socket.close();
                closeall();
                System.out.println(name + CLIENT_DISCONNECTED);
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        public  void sendMessageToAllClients(String msg) {

            for (ClientHandler o : clients) {
                o.sendMsg(msg);
            }

        }
        // send message
        public  void sendMsg(String msg) {
            try {
                writer.println(msg);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // client go home
        public void closeall() {

            clients.remove(this);
            client_count--;
            sendMessageToAllClients("Client "+ name +" delet. "+"Client v chat = " + client_count);

        }
        //send mess all
       public void vchod(){
               sendMessageToAllClients("New client "+name+" enter in chat! "+"Client v chat = " + client_count);
       }


    }



}