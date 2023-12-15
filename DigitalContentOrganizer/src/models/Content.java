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

public class Content {
    DBConnector connector;

    private int id;
    public int creator_id, modifier_id;
    private String creation_datetime, modification_datetime;

    public String title, details, url, alternative_url, type, privacy;
    public boolean status;

    public Content() {
        id = -1;
        creator_id = -1;
        modifier_id = -1;
        title = null;
        details = null;
        url = null;
        alternative_url = null;
        type = null;
        privacy = null;
        status = false;
        creation_datetime = null;
        modification_datetime = null;
    }

    public Content(int id) throws SQLException, IOException {
        this.id = id;
        sync(true);
    }

    public Content(int creator_id, String title, String url, String alternative_url, String type, String details,
            String privacy, boolean status) {
        this.creator_id = creator_id;
        this.title = title;
        this.details = details;
        this.url = url;
        this.alternative_url = alternative_url;
        this.type = type;
        this.privacy = privacy;
        this.status = status;
    }

    public int get_id() {
        return this.id;
    }

    public String get_creation_datetime() {
        return this.creation_datetime;
    }

    public String get_modification_datetime() {
        return this.modification_datetime;
    }

    public void insert() throws SQLException, IOException {
        connector = new DBConnector();
        String sql = "INSERT INTO content (creator_id, title, details, url, alternative_url, type, privacy, status) VALUES ("
                + creator_id + ", '" + title + "', '" + details + "', '" + url + "', '" + alternative_url + "', '"
                + type + "', '" + privacy + "', '" + status + "');";
        PreparedStatement preparedStatement = connector.connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
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
            String sql = "select * from \"content\" where id=" + id;
            DBConnector connector = new DBConnector();

            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_Content(resultSet);

            resultSet.close();
            connector.close();
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            modification_datetime = dateFormat.format(date);
            String sql = "update \"content\" set creator_id=" + creator_id + ", modifier_id=" + modifier_id
                    + ", title='" + title + "', details='" + details + "', url='" + url + "', alternative_url='"
                    + alternative_url + "', type='" + type + "', privacy='" + privacy + "', status='" + status
                    + "', modification_datetime='" + modification_datetime + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
    }

    public void delete() throws SQLException, IOException {
        String sql = "delete from \"content\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public void from_resultSet_To_Content(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        creator_id = resultSet.getInt("creator_id");
        modifier_id = resultSet.getInt("modifier_id");
        title = resultSet.getString("title");
        details = resultSet.getString("details");
        url = resultSet.getString("url");
        alternative_url = resultSet.getString("alternative_url");
        type = resultSet.getString("type");
        privacy = resultSet.getString("privacy");
        status = resultSet.getBoolean("status");
        creation_datetime = resultSet.getString("creation_datetime");
        modification_datetime = resultSet.getString("modification_datetime");
    }

    public static Content get_by_id(int id) throws SQLException, IOException {
        return new Content(id);
    }

    public static ArrayList<Content> from_resultSet_To_Content_Array(ResultSet resultSet) throws SQLException {
        ArrayList<Content> contents = new ArrayList<Content>();
        while (resultSet.next()) {
            Content content = new Content();
            content.from_resultSet_To_Content(resultSet);
            contents.add(content);
        }
        return contents;
    }

    public static ArrayList<Content> get_by_creator_id(int creator_id) throws SQLException, IOException {
        String sql = "select * from \"content\" where creator_id=" + creator_id;
        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Content> contents = from_resultSet_To_Content_Array(resultSet);
        resultSet.close();
        connector.close();
        return contents;
    }

    public static ArrayList<Content> get_by_tag(String tag) throws SQLException, IOException {
        String sql = "select * from \"content\""
                + " join \"content-tag\" on \"content\".id=\"content-tag\".content_id"
                + " join \"tag\" on \"content-tag\".tag_id=\"tag\".id"
                + " where \"tag\".tag='" + tag + "';";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Content> contents = from_resultSet_To_Content_Array(resultSet);
        resultSet.close();
        connector.close();
        return contents;
    }

    public static ArrayList<Content> search(String searchBy, String searchValue, String filterBy)
            throws SQLException, IOException {
        String sql;
        if (filterBy.equals("All")) {
            if (searchBy.equals("Title")) {
                sql = "select * from \"content\" where title like '%" + searchValue + "%' and privacy='public';";
            } else {
                if(searchValue.equals("")) {
                    sql = "select * from \"content\" where privacy='public';";
                } else {
                    sql = "select * from \"content\""
                        + " join \"content-tag\" on \"content\".id=\"content-tag\".content_id"
                        + " join \"tag\" on \"content-tag\".tag_id=\"tag\".id"
                        + " where \"tag\".tag='" + searchValue + "' and \"content\".privacy='public';";
                }
            }
        } else {
            if (searchBy.equals("Title")) {
                sql = "select * from \"content\" where title like '%" + searchValue + "%' and \"type\"='" + filterBy
                        + "' and privacy='public';";
            } else {
                if (searchValue.equals("")) {
                    sql = "select * from \"content\" where \"type\"='" + filterBy + "' and privacy='public';";
                } else {
                    sql = "select * from \"content\""
                            + " join \"content-tag\" on \"content\".id=\"content-tag\".content_id"
                            + " join \"tag\" on \"content-tag\".tag_id=\"tag\".id"
                            + " where \"tag\".tag='" + searchValue + "' and \"content\".\"type\"='" + filterBy
                            + "' and \"content\".privacy='public';";
                }
            }
        }

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Content> contents = from_resultSet_To_Content_Array(resultSet);
        resultSet.close();
        connector.close();
        return contents;
    }

    public static ArrayList<Content> get_by_shelf_id(int shelf_id) throws SQLException, IOException {
        String sql = "select * from \"content\""
                + " join \"content-shelf\" on \"content\".id=\"content-shelf\".content_id"
                + " where shelf_id=" + shelf_id + ";";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Content> contents = from_resultSet_To_Content_Array(resultSet);
        resultSet.close();
        connector.close();
        return contents;
    }

    public static ArrayList<Content> get_bookmarked(int user_id) throws SQLException, IOException {
        String sql = "select * from \"content\""
                + " join \"content-user\" on \"content\".id=\"content-user\".content_id"
                + " where bookmarked=1 and \"user_id\"=" + user_id + ";";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Content> contents = from_resultSet_To_Content_Array(resultSet);
        resultSet.close();
        connector.close();
        return contents;
    }

    public static ArrayList<Content> get_shared(int user_id) throws SQLException, IOException {
        String sql = "select * from \"content\""
                + " join \"content-user\" on \"content\".id=\"content-user\".content_id"
                + " where (permission='view' or permission='edit') and \"user_id\"=" + user_id + ";";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Content> contents = from_resultSet_To_Content_Array(resultSet);
        resultSet.close();
        connector.close();
        return contents;
    }

    public static void create_table() throws SQLException, IOException {
        String sql = "create table content("
                + "id int identity(1, 1),"

                + "creator_id int,"
                + "creation_datetime datetime constraint df_content_creation_datetime default getDate(),"

                + "modifier_id int,"
                + "modification_datetime datetime constraint df_content_modification_datetime default getDate(),"

                + "title varchar(50) not null,"
                + "details text,"
                + "url varchar(500) not null,"
                + "alternative_url varchar(1000),"
                + "\"type\" varchar(20),"
                + "privacy varchar(20) constraint df_content_privacy default 'private',"
                + "\"status\" bit constraint df_content_status default 1,"

                + "constraint pk_content_id primary key(id),"
                + "constraint fk_content_creator_id foreign key(creator_id) references \"user\"(id),"
                + "constraint fk_content_modifier_id foreign key(modifier_id) references \"user\"(id),"

                + "constraint uq_content_url_creator_id unique(url, creator_id),"
                + "constraint uq_content_title_creator_id unique(title, creator_id)"
                + ");";

        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException {
        String sql = "drop table \"content\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
