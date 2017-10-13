/**
 * Добавил 4 метода
 * добавление нового ползователя
 * удаление пользователя
 * смена пароля
 * печать базы
 * в первых 3 методах после каждого изменения базы печатается новая база
 *
 *
 */
import java.sql.*;

class MakeDBFile implements IConstants {

    static final String NAME_TABLE = "users";
    final String SQL_CREATE_TABLE = "CREATE TABLE " + NAME_TABLE +
        "(login  CHAR(6) PRIMARY KEY NOT NULL," +
        " passwd CHAR(6) NOT NULL);";
    final String SQL_INSERT_MIKE = "INSERT INTO " + NAME_TABLE +
        " (login, passwd) " +
        "VALUES ('mike', 'qwerty');";
    final String SQL_INSERT_JONH = "INSERT INTO " + NAME_TABLE +
        " (login, passwd) " +
        "VALUES ('john', '12345');";
    static final String SQL_SELECTE = "SELECT * FROM " + NAME_TABLE + ";";

    static final String SQL_INSERT = "INSERT INTO " + NAME_TABLE +
            " (login, passwd) ";

    static Connection connect;
    static Statement stmt;
    static ResultSet rs;
    String sql;

    public static void main(String[] args) {
       new MakeDBFile();
      /* greatNewLogin("mike","qwe");
      delLogin("mike");
       changePas("mike","qwerty");*/

    }

    MakeDBFile() {
        // open db file
        try {
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
        } catch (Exception e) { }

        // create table
        try {
            stmt = connect.createStatement();
            stmt.executeUpdate(SQL_CREATE_TABLE);
        } catch (Exception e) { }

        // insert record(s)
        try {
            stmt.executeUpdate(SQL_INSERT_MIKE);
            stmt.executeUpdate(SQL_INSERT_JONH);
        } catch (Exception e) { }

        // print records
        try {
            rs = stmt.executeQuery(SQL_SELECTE);
            System.out.println("LOGIN\tPASSWD");
            while (rs.next()) {
                System.out.println(rs.getString("login") + "\t" +
                    rs.getString(PASSWD_COL));
            }
        } catch (Exception e) { }
    }
// great Login
    static void greatNewLogin(String login,String pas){
        boolean result = false;
        try{
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            stmt = connect.createStatement();
            rs = stmt.executeQuery(SQL_SELECT.replace("?", login));
            while (rs.next())
               result = rs.getString("login").equals(login);

            if (result){
               System.out.println("This login is already in there.");
            }
            else {
                stmt.executeUpdate(SQL_INSERT+"VALUES ('"+login+"', '"+pas+"');");
            }

            rs = stmt.executeQuery(SQL_SELECTE);
            System.out.println("LOGIN\tPASSWD");
            while (rs.next()) {
                System.out.println(rs.getString("login") + "\t" +
                        rs.getString(PASSWD_COL));
            }
            rs.close();
            stmt.close();
            connect.close();
        } catch (Exception e) { }

    }
    // Del Login
    static void delLogin(String login){
        boolean result = false;
        try{
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            stmt = connect.createStatement();
            rs = stmt.executeQuery(SQL_SELECT.replace("?", login));
            while (rs.next())
                result = rs.getString("login").equals(login);
            if (result){
                stmt.executeUpdate("DELETE from " + NAME_TABLE + " where login = '"+login+"';");
            }
            else {
                System.out.println("No such login.");
            }
            //print db
            rs = stmt.executeQuery(SQL_SELECTE);
            System.out.println("LOGIN\tPASSWD");
            while (rs.next()) {
                System.out.println(rs.getString("login") + "\t" +
                        rs.getString(PASSWD_COL));
            }
            rs.close();
            stmt.close();
            connect.close();

        } catch (Exception e) { }

    }
    //change password
    static void changePas(String login, String pas){
        boolean result = false;
        try{
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            stmt = connect.createStatement();
            rs = stmt.executeQuery(SQL_SELECT.replace("?", login));
            while (rs.next())
                result = rs.getString("login").equals(login);
            if (result){
                stmt.executeUpdate("UPDATE " + NAME_TABLE + " set "+PASSWD_COL+" = '"+pas+"' where login='"+login+"';");
            }
            else {
                System.out.println("No such login.");
            }
            //print db
            rs = stmt.executeQuery(SQL_SELECTE);
            System.out.println("LOGIN\tPASSWD");
            while (rs.next()) {
                System.out.println(rs.getString("login") + "\t" +
                        rs.getString(PASSWD_COL));
            }
            rs.close();
            stmt.close();
            connect.close();

        } catch (Exception e) { }

    }
    //print base
    static void printBas(){
        boolean result = false;
        try{
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            stmt = connect.createStatement();
            rs = stmt.executeQuery(SQL_SELECTE);
            System.out.println("LOGIN\tPASSWD");
            while (rs.next()) {
                System.out.println(rs.getString("login") + "\t" +
                        rs.getString(PASSWD_COL));
            }
            rs.close();
            stmt.close();
            connect.close();

        } catch (Exception e) { }

    }

}