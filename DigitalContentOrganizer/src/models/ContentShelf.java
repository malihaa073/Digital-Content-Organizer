package models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.DBConnector;

public class ContentShelf {
    DBConnector connector;

    private int id;
    public int content_id, shelf_id;

    public ContentShelf() {
        id = -1;

        content_id = -1;
        shelf_id = -1;
    }

    public ContentShelf(int id) throws SQLException, IOException {
        this.id = id;

        sync(true);
    }

    public ContentShelf(int content_id, int shelf_id) {
        this.content_id = content_id;
        this.shelf_id = shelf_id;
    }

    public int get_id() {
        return this.id;
    }

    public void insert() throws SQLException, IOException {
        connector = new DBConnector();
        connector.createStatement().executeUpdate("INSERT INTO \"content-shelf\" (content_id, shelf_id) VALUES (" + content_id + ", " + shelf_id + ");");
        connector.close();
    }

    public void update() throws SQLException, IOException {
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException {
        if (update_object) {
            String sql = "select * from \"content-shelf\" where id=" + id;
            DBConnector connector = new DBConnector();

            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_ContentShelf(resultSet);

            resultSet.close();
            connector.close();
        } else {
            String sql = "update \"content-shelf\" set content_id=" + content_id + ", shelf_id=" + shelf_id + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
    }

    public void from_resultSet_To_ContentShelf(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        content_id = resultSet.getInt("content_id");
        shelf_id = resultSet.getInt("shelf_id");
    }

    public void delete() throws SQLException, IOException {
        String sql = "delete from \"content-shelf\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
    
    public static ContentShelf get_by_id(int id) throws SQLException, IOException {
        return new ContentShelf(id);
    }

    public static ContentShelf get_by_unique_constraint(int content_id, int shelf_id) throws SQLException, IOException {
        String sql = "select * from \"content-shelf\" where content_id=" + content_id + " and shelf_id=" + shelf_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        ContentShelf content_shelf = new ContentShelf();
        content_shelf.from_resultSet_To_ContentShelf(resultSet);

        resultSet.close();
        connector.close();

        return content_shelf;
    }

    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"content-shelf\"("
            + "id int identity(1,1),"
            + "content_id int,"
            + "shelf_id int,"
            
            + "constraint \"pk-content-shelf_id\" primary key (id),"
            + "constraint \"uq_content-shelf_content_id_shelf_id\" unique(content_id, shelf_id),"
            + "constraint \"fk_content-shelf_content_id\" foreign key(content_id) references content(id) on delete cascade,"
            + "constraint \"fk_content-shelf_shelf_id\" foreign key(shelf_id) references shelf(id) on delete cascade"
        + ");";
        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException {
        String sql = "drop table \"content-shelf\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
