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

public class Shelf {
    DBConnector connector;

    private int id;
    public int creator_id, modifier_id;
    private String creation_datetime, modification_datetime;

    public String title, details, privacy;
    public boolean status;

    public Shelf() {
        id = -1;
        creator_id = -1;
        modifier_id = -1;
        title = null;
        details = null;
        privacy = null;
        status = false;
        creation_datetime = null;
        modification_datetime = null;
    }

    public Shelf(int id) throws SQLException, IOException {
        this.id = id;

        sync(true);
    }

    public Shelf(int creator_id, String title, String details, String privacy, boolean status) {
        this.creator_id = creator_id;
        this.title = title;
        this.details = details;
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
        String sql = "INSERT INTO shelf (creator_id, title, details, privacy, status) VALUES (" + creator_id + ", '"
                + title + "', '" + details + "', '" + privacy + "', '" + status + "')";
        PreparedStatement preparedStatement = connector.connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
        preparedStatement.executeUpdate();

        ResultSet rs = preparedStatement.getGeneratedKeys();
        rs.next();
        this.id = rs.getInt(1);

        rs.close();
        preparedStatement.close();
        connector.close();
    }

    public void update() throws SQLException, IOException {
        sync(false);
    }

    public void sync(boolean update_object) throws SQLException, IOException {
        if (update_object) {
            String sql = "select * from \"shelf\" where id=" + id;
            DBConnector connector = new DBConnector();

            ResultSet resultSet = connector.createStatement().executeQuery(sql);
            resultSet.next();
            from_resultSet_To_Shelf(resultSet);

            resultSet.close();
            connector.close();
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            modification_datetime = dateFormat.format(date);
            String sql = "update \"shelf\" set modifier_id=" + modifier_id + ", title='" + title + "', details='"
                    + details + "', privacy='" + privacy + "', status='" + status + "', modification_datetime='"
                    + modification_datetime + "' where id=" + id;
            DBConnector connector = new DBConnector();
            connector.createStatement().executeUpdate(sql);

            connector.close();
        }
    }

    public void delete() throws SQLException, IOException {
        String sql = "delete from \"shelf\" where id=" + id;
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public void from_resultSet_To_Shelf(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        creator_id = resultSet.getInt("creator_id");
        modifier_id = resultSet.getInt("modifier_id");
        title = resultSet.getString("title");
        details = resultSet.getString("details");
        privacy = resultSet.getString("privacy");
        status = resultSet.getBoolean("status");
        creation_datetime = resultSet.getString("creation_datetime");
        modification_datetime = resultSet.getString("modification_datetime");
    }

    public static Shelf get_by_id(int id) throws SQLException, IOException {
        return new Shelf(id);
    }

    public static ArrayList<Shelf> from_resultSet_To_Shelf_Array(ResultSet resultSet) throws SQLException {

        ArrayList<Shelf> shelves = new ArrayList<Shelf>();
        while (resultSet.next()) {
            Shelf shelf = new Shelf();
            shelf.from_resultSet_To_Shelf(resultSet);
            shelves.add(shelf);
        }

        return shelves;
    }

    public static ArrayList<Shelf> get_by_creator_id(int creator_id) throws SQLException, IOException {
        DBConnector connector = new DBConnector();
        String sql = "select * from \"shelf\" where creator_id=" + creator_id;
        ResultSet resultSet = connector.createStatement().executeQuery(sql);

        ArrayList<Shelf> shelves = Shelf.from_resultSet_To_Shelf_Array(resultSet);

        resultSet.close();
        connector.close();

        return shelves;
    }

    public static ArrayList<Shelf> get_by_tag(String tag) throws SQLException, IOException {
        String sql = "select * from \"shelf\""
                + " join \"shelf-tag\" on \"shelf\".id=\"shelf-tag\".shelf_id"
                + " join \"tag\" on \"shelf-tag\".tag_id=\"tag\".id"
                + " where \"tag\".tag='" + tag + "';";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Shelf> shelves = from_resultSet_To_Shelf_Array(resultSet);
        resultSet.close();
        connector.close();
        return shelves;
    }

    public static ArrayList<Shelf> search(String searchBy, String searchValue) throws SQLException, IOException {
        String sql;
        if (searchBy.equals("Title")) {
            sql = "select * from \"shelf\" where title like '%" + searchValue + "%' and privacy='public';";
        } else {
            if (searchValue.equals("")) {
                sql = "select * from \"shelf\" where privacy='public';";
            } else {
                sql = "select * from \"shelf\""
                        + " join \"shelf-tag\" on \"shelf\".id=\"shelf-tag\".shelf_id"
                        + " join \"tag\" on \"shelf-tag\".tag_id=\"tag\".id"
                        + " where \"tag\".tag='" + searchValue + "' and \"shelf\".privacy='public';";
            }
        }

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Shelf> shelves = from_resultSet_To_Shelf_Array(resultSet);
        resultSet.close();
        connector.close();
        return shelves;
    }

    public static ArrayList<Shelf> get_bookmarked(int user_id) throws SQLException, IOException {
        String sql = "select * from \"shelf\""
                + " join \"shelf-user\" on \"shelf\".id=\"shelf-user\".shelf_id"
                + " where bookmarked=1 and \"user_id\"=" + user_id + ";";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Shelf> shelves = from_resultSet_To_Shelf_Array(resultSet);
        resultSet.close();
        connector.close();
        return shelves;
    }

    public static ArrayList<Shelf> get_shared(int user_id) throws SQLException, IOException {
        String sql = "select * from \"shelf\""
                + " join \"shelf-user\" on \"shelf\".id=\"shelf-user\".shelf_id"
                + " where (permission='view' or permission='edit') and \"user_id\"=" + user_id + ";";

        DBConnector connector = new DBConnector();
        ResultSet resultSet = connector.createStatement().executeQuery(sql);
        ArrayList<Shelf> shelves = from_resultSet_To_Shelf_Array(resultSet);
        resultSet.close();
        connector.close();
        return shelves;
    }

    public static void create_table() throws SQLException, IOException {
        String sql = "create table shelf("
                + "id int identity(1, 1),"

                + "creator_id int,"
                + "creation_datetime datetime constraint df_shelf_creation_datetime default getDate(),"

                + "modifier_id int,"
                + "modification_datetime datetime constraint df_shelf_modification_datetime default getDate(),"

                + "title varchar(50) not null,"
                + "details text,"
                + "privacy varchar(20) constraint df_shelf_privacy default 'private',"
                + "\"status\" bit constraint df_shelf_status default 1,"

                + "constraint pk_shelf_id primary key(id),"
                + "constraint fk_shelf_creator_id foreign key(creator_id) references \"user\"(id),"
                + "constraint fk_shelf_modifier_id foreign key(modifier_id) references \"user\"(id),"

                + "constraint uq_shelf_title_creator_id unique(title, creator_id)"
                + ");";

        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }

    public static void drop_table() throws SQLException, IOException {
        String sql = "drop table \"shelf\"";
        DBConnector connector = new DBConnector();
        connector.createStatement().execute(sql);

        connector.close();
    }
}
