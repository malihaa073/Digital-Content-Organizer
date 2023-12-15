package models;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.DBConnector;

public class ContentTag {
    DBConnector connector;

    private int id;
    public int content_id, tag_id;


    public ContentTag(){
        id = -1;
        
        content_id = -1;
        tag_id = -1;
    }

    public ContentTag(int id) throws SQLException, IOException{
        this.id = id;

        sync(true);
    }

    public ContentTag(int content_id, int tag_id){
        this.content_id = content_id;
        this.tag_id = tag_id;
    }

    public int get_id(){
        return this.id;
    }

    public void insert() throws SQLException, IOException{
        connector = new DBConnector();
        connector.createStatement().executeUpdate("INSERT INTO \"content-tag\" (content_id, tag_id) VALUES (" + content_id + ", " + tag_id + ");");
        connector.close();
    }

    public void update() throws SQLException, IOException{
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException{
        if(update_object){
            String sql = "select * from \"content-tag\" where id=" + id;
            DBConnector connector = new DBConnector();
            
            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_ContentTag(resultSet);
            
            resultSet.close();
            connector.close();
        }else{
            String sql = "update \"content-tag\" set content_id=" + content_id + ", tag_id=" + tag_id + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
    }

    public void delete() throws SQLException, IOException{
        String sql = "delete from \"content-tag\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);
        
        connector.close();
    }

    public void from_resultSet_To_ContentTag(ResultSet resultSet) throws SQLException{
        id = resultSet.getInt("id");
        content_id = resultSet.getInt("content_id");
        tag_id = resultSet.getInt("tag_id");
    }

    public static ContentTag get_by_id(int id) throws SQLException, IOException{
        return new ContentTag(id);
    }

    public static ContentTag get_by_unique_constraint(int content_id, int tag_id) throws SQLException, IOException{
        String sql = "select * from \"content-tag\" where content_id=" + content_id + " and tag_id=" + tag_id;
        DBConnector connector = new DBConnector();

        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        ContentTag contentTag = new ContentTag();
        contentTag.from_resultSet_To_ContentTag(resultSet);

        resultSet.close();
        connector.close();

        return contentTag;
    }

    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"content-tag\"("
            + "id int identity(1,1),"
            + "content_id int,"
            + "tag_id int,"
            
            + "constraint \"pk-content-tag_id\" primary key (id),"
            + "constraint \"uq_content-tag_content_id_tag_id\" unique(content_id, tag_id),"
            + "constraint \"fk_content-tag_content_id\" foreign key(content_id) references content(id) on delete cascade,"
            + "constraint \"fk_content-tag_tag_id\" foreign key(tag_id) references tag(id) on delete cascade"
        + ");";
        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException{
        String sql = "drop table \"content-tag\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
