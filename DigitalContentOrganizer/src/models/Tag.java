package models;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utils.DBConnector;

public class Tag {
    DBConnector connector;

    private int id;
    public int creator_id, modifier_id;
    private String creation_datetime, modification_datetime;

    public String tag;


    public Tag(){
        id = -1;
        creator_id = -1;
        modifier_id = -1;
        tag = null;
        creation_datetime = null;
        modification_datetime = null;
    }

    public Tag(int id) throws SQLException, IOException{
        this.id = id;

        sync(true);
    }

    public Tag(int creator_id, String tag){
        this.creator_id = creator_id;
        this.tag = tag;
    }

    public int get_id(){
        return this.id;
    }

    public String get_creation_datetime(){
        return this.creation_datetime;
    }

    public String get_modification_datetime(){
        return this.modification_datetime;
    }

    public void insert() throws SQLException, IOException{
        connector = new DBConnector();
        
        String sql = "INSERT INTO tag (creator_id, tag) VALUES (" + creator_id + ", '" + tag + "');";
        PreparedStatement preparedStatement = connector.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.executeUpdate();
        ResultSet rs = preparedStatement.getGeneratedKeys();
        rs.next();
        id = rs.getInt(1);

        rs.close();
        preparedStatement.close();
        connector.close();
    }

    public void update() throws SQLException, IOException{
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException{
        if(update_object){
            String sql = "select * from \"tag\" where id=" + id;
            DBConnector connector = new DBConnector();
            
            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_Tag(resultSet);
            
            resultSet.close();
            connector.close();
        }else{
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            modification_datetime = dateFormat.format(date);
            String sql = "update \"tag\" set modifier_id=" + modifier_id + ", tag='" + tag + "', url='" + "', modification_datetime='"+ modification_datetime + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
    }

    public void delete() throws SQLException, IOException{
        String sql = "delete from \"tag\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);
        
        connector.close();
    }

    public void from_resultSet_To_Tag(ResultSet resultSet) throws SQLException{
        id = resultSet.getInt("id");
        creator_id = resultSet.getInt("creator_id");
        modifier_id = resultSet.getInt("modifier_id");
        tag = resultSet.getString("tag");
        creation_datetime = resultSet.getString("creation_datetime");
        modification_datetime = resultSet.getString("modification_datetime");
    }

    @Override
    public String toString(){
        return "Tag["+tag+"]";
    }

    public static Tag get_by_id(int id) throws SQLException, IOException{
        return new Tag(id);
    }

    public static Tag get_by_tag(String tag) throws SQLException, IOException{
        String sql = "select * from \"tag\" where tag='" + tag + "'";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        resultSet.next();
        Tag tag_obj = new Tag();
        try{
            tag_obj.from_resultSet_To_Tag(resultSet);
        }catch(SQLException e){
            if(e.getMessage().contains("The result set has no current row")){
                return null;
            }else{
                throw e;
            }
        }

        resultSet.close();
        connector.close();
        return tag_obj;
    }

    public static ArrayList<Tag> get_by_content_id(int content_id) throws SQLException, IOException{
        String sql = "select * from \"tag\" join \"content-tag\" on \"tag\".id=\"content-tag\".tag_id where content_id=" + content_id;
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        while(resultSet.next()){
            Tag tag_obj = new Tag();
            tag_obj.from_resultSet_To_Tag(resultSet);
            tags.add(tag_obj);
        }

        resultSet.close();
        connector.close();
        return tags;
    }

    
    public static ArrayList<Tag> get_by_shelf_id(int shelf_id)throws SQLException, IOException{
        String sql = "select * from \"tag\" join \"shelf-tag\" on \"tag\".id=\"shelf-tag\".tag_id where shelf_id=" + shelf_id;
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        while(resultSet.next()){
            Tag tag_obj = new Tag();
            tag_obj.from_resultSet_To_Tag(resultSet);
            tags.add(tag_obj);
        }

        resultSet.close();
        connector.close();
        return tags;
    }

    public static void create_table() throws SQLException, IOException{
        String sql = "create table \"tag\"("
            + "id int identity(1, 1),"
            
            + "creator_id int,"
            + "creation_datetime datetime constraint df_tag_creation_datetime default getDate(),"
            
            + "modifier_id int,"
            + "modification_datetime datetime constraint df_tag_modification_datetime default getDate(),"
            
            + "tag varchar(50) not null,"
            
            + "constraint pk_tag_id primary key(id),"
            + "constraint fk_tag_creator_id foreign key(creator_id) references \"user\"(id),"
            + "constraint fk_tag_modifier_id foreign key(modifier_id) references \"user\"(id),"
            
            + "constraint uq_tag_tag_creator_id unique(tag, creator_id)"
            + ");";
        
        
        
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException{
        String sql = "drop table \"tag\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
