package models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.DBConnector;

public class ShelfTag {
    DBConnector connector;

    private int id;
    public int shelf_id, tag_id;


    public ShelfTag(){
        id = -1;    
        shelf_id = -1;
        tag_id = -1;    
    }

    public ShelfTag(int id) throws SQLException, IOException{
        this.id = id;

        sync(true);
    }

    public ShelfTag(int shelf_id, int tag_id){
        this.shelf_id = shelf_id;
        this.tag_id = tag_id;
    }

    public int get_id(){
        return this.id;
    }

    public void insert() throws SQLException, IOException{
        connector = new DBConnector();
        connector.createStatement().executeUpdate("INSERT INTO \"shelf-tag\" (shelf_id, tag_id) VALUES (" + shelf_id + ", " + tag_id + ");");
        connector.close();
    }

    public void update() throws SQLException, IOException{
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException{
        if(update_object){
            String sql = "select * from \"shelf-tag\" where id=" + id;
            DBConnector connector = new DBConnector();
            
            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_ShelfTag(resultSet);
            
            resultSet.close();
            connector.close();
        }else{
            String sql = "update \"shelf-tag\" set shelf_id=" + shelf_id + ", tag_id=" + tag_id + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
    }

    public void delete() throws SQLException, IOException{
        String sql = "delete from \"shelf-tag\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);
        
        connector.close();
    }

    public void from_resultSet_To_ShelfTag(ResultSet resultSet) throws SQLException{
        id = resultSet.getInt("id");
        shelf_id = resultSet.getInt("shelf_id");
        tag_id = resultSet.getInt("tag_id");
    }

    public static ShelfTag get_by_id(int id) throws SQLException, IOException{
        return new ShelfTag(id);
    }


    public static ShelfTag get_by_unique_constraint(int shelf_id, int tag_id) throws SQLException, IOException{
        String sql = "select * from \"shelf-tag\" where shelf_id=" + shelf_id + " and tag_id=" + tag_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        ShelfTag shelfTag = new ShelfTag();
        shelfTag.from_resultSet_To_ShelfTag(resultSet);

        resultSet.close();
        connector.close();

        return shelfTag;
    }

    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"shelf-tag\"("
            + "id int identity(1,1),"
            + "shelf_id int,"
            + "tag_id int,"
            
            + "constraint \"pk-shelf-tag_id\" primary key (id),"
            + "constraint \"uq_shelf-tag_shelf_id_tag_id\" unique(shelf_id, tag_id),"
            + "constraint \"fk_shelf-tag_shelf_id\" foreign key(shelf_id) references shelf(id) on delete cascade,"
            + "constraint \"fk_shelf-tag_tag_id\" foreign key(tag_id) references tag(id) on delete cascade"
        + ");";
        
        
        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException{
        String sql = "drop table \"shelf-tag\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
