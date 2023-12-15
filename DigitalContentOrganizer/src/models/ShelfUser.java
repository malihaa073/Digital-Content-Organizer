package models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import utils.DBConnector;

public class ShelfUser {
    DBConnector connector;

    private int id;
    public int shelf_id, user_id;
    public String permission;
    public boolean bookmarked;

    public ShelfUser() {
        id = -1;

        shelf_id = -1;
        user_id = -1;
        permission = null;
        bookmarked = false;
    }

    public ShelfUser(int id) throws SQLException, IOException {
        this.id = id;

        sync(true);
    }

    public ShelfUser(int shelf_id, int user_id, String permission, boolean bookmarked) {
        id = -1;
        this.shelf_id = shelf_id;
        this.user_id = user_id;
        this.permission = permission;
        this.bookmarked = bookmarked;
    }

    public int get_id() {
        return this.id;
    }

    public void insert() throws SQLException, IOException {
        connector = new DBConnector();
        connector.createStatement().executeUpdate("INSERT INTO \"shelf-user\" (shelf_id, \"user_id\", permission, bookmarked) VALUES (" + shelf_id + ", " + user_id + ", '" + permission + "', '" + bookmarked + "');");
        connector.close();
    }

    public void update() throws SQLException, IOException {
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException {
        if (update_object) {
            String sql = "select * from \"shelf-user\" where id=" + id;
            DBConnector connector = new DBConnector();

            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_ShelfUser(resultSet);

            resultSet.close();
            connector.close();
        } else {
            String sql = "update \"shelf-user\" set shelf_id=" + shelf_id + ", \"user_id\"=" + user_id + ", permission='" + permission + "', bookmarked='" + bookmarked + "' where id=" + id;
            DBConnector connector = new DBConnector();

            connector.createStatement().executeUpdate(sql);
            connector.close();
        }
    }

    public void from_resultSet_To_ShelfUser(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.shelf_id = resultSet.getInt("shelf_id");
        this.user_id = resultSet.getInt("user_id");
        this.permission = resultSet.getString("permission");
        this.bookmarked = resultSet.getBoolean("bookmarked");
    }

    public void delete() throws SQLException, IOException {
        connector = new DBConnector();
        connector.createStatement().executeUpdate("DELETE FROM \"shelf-user\" WHERE id=" + id);
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

    public static ShelfUser get_by_id(int id) throws SQLException, IOException {
        return new ShelfUser(id);
    }

    public static ShelfUser get_by_unique_constraint(int shelf_id, int user_id) throws SQLException, IOException {
        String sql = "select * from \"shelf-user\" where shelf_id=" + shelf_id + " and user_id=" + user_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        ShelfUser shelfUser = new ShelfUser();
        shelfUser.from_resultSet_To_ShelfUser(resultSet);

        resultSet.close();
        connector.close();

        return shelfUser;
    }

    public static ArrayList<ShelfUser> get_by_shelf_id(int shelf_id) throws SQLException, IOException {
        
        String sql = "select * from \"shelf-user\" where shelf_id=" + shelf_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        
        ArrayList<ShelfUser> shelfUsers = new ArrayList<ShelfUser>();
        while(resultSet.next()){
            ShelfUser shelfUser = new ShelfUser();
            shelfUser.from_resultSet_To_ShelfUser(resultSet);
            shelfUsers.add(shelfUser);
        }

        resultSet.close();
        connector.close();
        return shelfUsers;
    }

    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"shelf-user\"("
            + "id int identity(1,1),"
            + "shelf_id int,"
            + "\"user_id\" int,"
            + "permission varchar(20),"
            + "bookmarked bit constraint \"df_shelf-user_bookmarked\" default 0,"
            
            + "constraint \"pk-shelf-user_id\" primary key (id),"
            + "constraint \"uq_shelf-user_shelf_id_user_id\" unique(shelf_id, \"user_id\"),"
            + "constraint \"fk_shelf-user_shelf_id\" foreign key(shelf_id) references shelf(id) on delete cascade,"
            + "constraint \"fk_shelf-user_user_id\" foreign key(\"user_id\") references \"user\"(id) on delete cascade"
        + ");";
        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException{
        String sql = "drop table \"shelf-user\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
