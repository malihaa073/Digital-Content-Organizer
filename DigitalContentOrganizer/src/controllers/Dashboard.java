package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import models.User;
import models.Content;
import models.Shelf;

public class Dashboard {
    
    User current_user;

    public TableView<Row> table;
    public TableColumn<Row, String> title_col, mod_dt_col, url_col, type_col;
    public ChoiceBox<String> category_choice_box;

    Pane contentArea;
    
    public void setData(User current_user){
        this.current_user = current_user;
    }

    public void start(Pane contentAreaPane) throws IOException, SQLException {
        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/dashboard.fxml").openStream());

        Dashboard controller = fl.getController();
        controller.contentArea = contentAreaPane;
        controller.setData(current_user);
        controller.populate_table("My Contents");
        controller.category_choice_box.getItems().addAll("My Contents", "Bookmarked", "Shared Contents");
        controller.category_choice_box.getSelectionModel().select(0);
        controller.category_choice_box.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            if(newValue.equals("My Contents")){
                try {
                    controller.populate_table("My Contents");
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
            else if(newValue.equals("Bookmarked")){
                try {
                    controller.populate_table("Bookmarked");
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
            else if(newValue.equals("Shared Contents")){
                try {
                    controller.populate_table("Shared Contents");
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }   
            }
        });
        
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    public void populate_table(String category) throws SQLException, IOException{
        ArrayList<Content> contents;
        ArrayList<Shelf> shelves;

        if(category.equals("My Contents")){
            contents = Content.get_by_creator_id(current_user.get_id());
            shelves = Shelf.get_by_creator_id(current_user.get_id());
        }
        else if(category.equals("Bookmarked")){
            contents = Content.get_bookmarked(current_user.get_id());
            shelves = Shelf.get_bookmarked(current_user.get_id());
        }
        else {
            contents = Content.get_shared(current_user.get_id());
            shelves = Shelf.get_shared(current_user.get_id());
        }
        
        table.getItems().clear();
        
        ObservableList<Row> content_rows = Row.from_contents(contents);
        table.setItems(content_rows);

        ObservableList<Row> shelf_rows = Row.from_shelves(shelves);
        table.getItems().addAll(shelf_rows);

        title_col.setCellValueFactory(new PropertyValueFactory<Row, String>("title"));
        mod_dt_col.setCellValueFactory(new PropertyValueFactory<Row, String>("modification_datetime"));
        url_col.setCellValueFactory(new PropertyValueFactory<Row, String>("url"));
        type_col.setCellValueFactory(new PropertyValueFactory<Row, String>("type"));
    }

    public void mouse_click_event_handler(MouseEvent event) throws IOException, SQLException{
        if(event.getClickCount() == 2){
            Row row = table.getSelectionModel().getSelectedItem();
            if(row != null){
                if(row.content != null){
                    ContentPage content_page = new ContentPage();
                    content_page.setData(current_user, row.content);
                    content_page.start(contentArea);
                }else if(row.shelf != null){
                    ShelfContents shelf_contents_page = new ShelfContents();
                    shelf_contents_page.setData(current_user, row.shelf);
                    shelf_contents_page.start(contentArea);
                }
            }
        }
    }
}

