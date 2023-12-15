package models;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import utils.DBConnector;

public class ContentUser {
    DBConnector connector;

    private int id;
    public int content_id, user_id;
    public String permission;
    public boolean bookmarked;

    public ContentUser() {
        id = -1;

        content_id = -1;
        user_id = -1;
        permission = null;
        bookmarked = false;
    }

    public ContentUser(int id) throws SQLException, IOException {
        this.id = id;

        sync(true);
    }

    public ContentUser(int content_id, int user_id, String permission, boolean bookmarked) {
        this.id = -1;
        this.content_id = content_id;
        this.user_id = user_id;
        this.permission = permission;
        this.bookmarked = bookmarked;
    }

    public int get_id() {
        return this.id;
    }

    public void insert() throws SQLException, IOException {
        connector = new DBConnector();

        String sql = "INSERT INTO \"content-user\" (content_id, user_id, permission, bookmarked) VALUES (" + content_id + ", " + user_id + ", '" + permission + "', '" + bookmarked + "');";
        
        PreparedStatement preparedStatement = connector.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.executeUpdate();
        ResultSet rs = preparedStatement.getGeneratedKeys();
        rs.next();
        id = rs.getInt(1);

        rs.close();
        preparedStatement.close();
        connector.close();
    }

    public void update() throws SQLException, IOException {
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException {
        if (update_object) {
            String sql = "select * from \"content-user\" where id=" + id;
            DBConnector connector = new DBConnector();

            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_ContentUser(resultSet);

            resultSet.close();
            connector.close();
        } else {
            String sql = "update \"content-user\" set content_id=" + content_id + ", \"user_id\"=" + user_id + ", permission='" + permission + "', bookmarked='" + bookmarked + "' where id=" + id;
            
            DBConnector connector = new DBConnector();

            connector.createStatement().executeUpdate(sql);
            connector.close();
        }
    }

    public void from_resultSet_To_ContentUser(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.content_id = resultSet.getInt("content_id");
        this.user_id = resultSet.getInt("user_id");
        this.permission = resultSet.getString("permission");
        this.bookmarked = resultSet.getBoolean("bookmarked");
    }

    public void delete() throws SQLException, IOException {
        connector = new DBConnector();
        connector.createStatement().executeUpdate("DELETE FROM \"content-user\" WHERE id=" + id);
        connector.close();
    }

    public void set_permission(String permission) throws SQLException, IOException {
        this.permission = permission;
        sync(false);
    }

    public void set_bookmarked(boolean bookmarked) throws SQLException, IOException {
        this.bookmarked = bookmarked;
        sync(false);
    }

    public static ContentUser get_by_id(int id) throws SQLException, IOException {
        return new ContentUser(id);
    }

    public static ContentUser get_by_unique_constraint(int content_id, int user_id) throws SQLException, IOException {
        String sql = "select * from \"content-user\" where content_id=" + content_id + " and user_id=" + user_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        ContentUser contentUser = new ContentUser();
        contentUser.from_resultSet_To_ContentUser(resultSet);

        resultSet.close();
        connector.close();

        return contentUser;
    }

    public static ArrayList<ContentUser> get_by_content_id(int content_id) throws SQLException, IOException {
        
        String sql = "select * from \"content-user\" where content_id=" + content_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        
        ArrayList<ContentUser> contentUsers = new ArrayList<ContentUser>();
        while(resultSet.next()){
            ContentUser contentUser = new ContentUser();
            contentUser.from_resultSet_To_ContentUser(resultSet);
            contentUsers.add(contentUser);
        }

        resultSet.close();
        connector.close();
        return contentUsers;
    }

    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"content-user\"("
            + "id int identity(1,1),"
            + "content_id int,"
            + "\"user_id\" int,"
            + "permission varchar(20),"
            + "bookmarked bit constraint \"df_content-user_bookmarked\" default 0,"
            
            + "constraint \"pk-content-user_id\" primary key (id),"
            + "constraint \"uq_content-user_content_id_user_id\" unique(content_id, \"user_id\"),"
            + "constraint \"fk_content-user_content_id\" foreign key(content_id) references content(id) on delete cascade,"
            + "constraint \"fk_content-user_user_id\" foreign key(\"user_id\") references \"user\"(id) on delete cascade"
        + ");";

        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException{
        String sql = "drop table \"content-user\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
