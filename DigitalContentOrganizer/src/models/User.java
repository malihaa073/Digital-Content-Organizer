package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import utils.DBConnector;
import java.io.IOException;

public class User {
    DBConnector connector;

    private int id;
    public String name, email, username, role;
    private String password;
    public boolean status;

    private String creation_datetime, modification_datetime;

    public User(){
        id = -1;
        name = null;
        email = null;
        username = null;
        password = null;
        role = null;
        status = false;
        creation_datetime = null;
        modification_datetime = null;
    }

    public User(int id) throws SQLException, IOException{
        this.id = id;

        sync(true);
    }

    public User(String name, String email, String username, String password, String role, boolean status){
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password; // TODO: Encrypt password
        this.role = role;
        this.status = status;
    }

    public int get_id(){
        return this.id;
    }

    public String get_password(){
        return this.password;
    }

    public String get_creation_datetime(){
        return this.creation_datetime;
    }
    public String get_modification_datetime(){
        return this.modification_datetime;
    }
    

    public void set_password(String password){
        // encrypt password then set it
        this.password = password;
    }

    public void insert() throws SQLException, IOException{
        // need do something so that we can get the id of the newly created user and sync the object to the database

        String sql = "insert into \"user\"(name, email, username, password, role, status) values('" + name + "', '" + email + "', '" + username + "', '" + password + "', '" + role + "', '" + status + "');";
        DBConnector connector = new DBConnector();
        
        
        connector.createStatement().executeUpdate(sql);
        connector.close();
    }

    public void update() throws SQLException, IOException{
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException{
        if(update_object){
            String sql = "select * from \"user\"where id=" + id;
            DBConnector connector = new DBConnector();
            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_User(resultSet);
            
            resultSet.close();
            connector.close();
        }
        else{
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            modification_datetime = dateFormat.format(date);
            String sql = "update \"user\" set name='" + name + "', email='" + email + "', username='" + username + "', password='" + password + "', role='" + role + "', status='" + status + "', modification_datetime='"+ modification_datetime + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
        
    }

    public void delete() throws SQLException, IOException{
        String sql = "delete from \"user\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);
        
        connector.close();
    }

    private void from_resultSet_To_User(ResultSet resultSet) throws SQLException{
        id = resultSet.getInt("id");
        name = resultSet.getString("name");
        email = resultSet.getString("email");
        username = resultSet.getString("username");
        password = resultSet.getString("password");
        role = resultSet.getString("role");
        status = resultSet.getBoolean("status");
        creation_datetime = resultSet.getString("creation_datetime");
        modification_datetime = resultSet.getString("modification_datetime");
    }

    public static ArrayList<User> from_resultSet_To_User_Array(ResultSet resultSet) throws SQLException{
        ArrayList<User> users = new ArrayList<User>();
        while(resultSet.next()){
            User user = new User();
            user.from_resultSet_To_User(resultSet);
            users.add(user);
        }
        return users;
    }

    public static User get_by_id(int id) throws SQLException, IOException{
        return new User(id);
    }

    public static User get_by_username(String username) throws SQLException, IOException{
        String sql = "select * from \"user\" where username='" + username + "'";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        User user = new User();
        user.from_resultSet_To_User(resultSet);

        resultSet.close();
        connector.close();
        return user;
    }

    
    public static ArrayList<User> get_all() throws SQLException, IOException{
        String sql = "select * from \"user\"";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<User> users = from_resultSet_To_User_Array(resultSet);

        resultSet.close();
        connector.close();
        return users;
    }


    public static ArrayList<User> search(String searchBy, String searchValue, String filterBy, String filterValue) throws SQLException, IOException{

        String sql;
        if(filterBy.equals("None")){
            sql = "select * from \"user\" where " + searchBy + " like '%" + searchValue + "%';";
        }
        else if(filterBy.equals("Status")){
            boolean status;
            if(filterValue.equals("Active")){
                status = true;
            }
            else{
                status = false;
            }
            sql = "select * from \"user\" where " + searchBy + " like '%" + searchValue + "%' and status='" + status + "';";
        }
        else{
            sql = "select * from \"user\" where " + searchBy + " like '%" + searchValue + "%' and role='" + filterValue + "';";
        }
        
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<User> users = from_resultSet_To_User_Array(resultSet);

        resultSet.close();
        connector.close();
        return users;
    }


    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"user\"(" +
            "id int identity(1,1)," +
            
            "creation_datetime datetime constraint df_user_creation_datetime default getDate()," +
            "modification_datetime datetime constraint df_user_modification_datetime default getDate(),"+
            
            "name varchar(50) not null," +
            "email varchar(100) not null," +
            "username varchar(20) not null," +
            "\"password\" varchar(20) not null," +
            "\"role\" nvarchar(20) constraint df_user_role default 'user'," +
            "\"status\" bit constraint df_user_status default 1" +
            
            "constraint pk_user_id primary key(id), " +
            "constraint chk_user_email check(email like '%_@%_.%_')," +
            "constraint chk_user_username check(len(username) >= 4)," +
            "constraint chk_user_password check(len(\"password\") >= 4)," +
            "constraint uq_user_username unique(username)," +
            "constraint uq_user_email unique(email)" +
        ");";
        
        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException{
        String sql = "drop table \"user\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}