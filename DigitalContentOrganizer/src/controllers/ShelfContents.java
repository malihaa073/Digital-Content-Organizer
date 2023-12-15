package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import models.User;
import models.Content;
import models.ContentShelf;
import models.Shelf;
import models.ShelfUser;

public class ShelfContents {
    
    User current_user;
    Shelf current_shelf;
    ShelfUser current_shelfuser;

    boolean is_auth;

    public TableView<Row> table;
    public TableColumn<Row, String> title_col, mod_dt_col, url_col, type_col;
    public Button add_btn, remove_btn;
    
    Pane contentArea;
    
    public void setData(User current_user, Shelf current_shelf){
        this.current_user = current_user;
        this.current_shelf = current_shelf;

        try{
            this.current_shelfuser = ShelfUser.get_by_unique_constraint(current_shelf.get_id(), current_user.get_id());
        }catch(SQLException | IOException e){
            this.current_shelfuser = null;
        }
    }

    public void start(Pane contentAreaPane) throws IOException, SQLException {
        if(current_shelf.creator_id != current_user.get_id() && (
            current_shelf.privacy.equals("Private") ||
            (current_shelf.privacy.equals("Custom") && (current_shelfuser == null || current_shelfuser.permission == null || current_shelfuser.permission.equals("null"))))
            ){
            if(current_shelfuser != null){
                current_shelfuser.delete();    
            }
            
            contentAreaPane.getChildren().clear();
            Dashboard dashboard = new Dashboard();
            dashboard.setData(current_user);
            dashboard.start(contentAreaPane);
            return;
        }


        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/shelf_contents.fxml").openStream());

        ShelfContents controller = fl.getController();
        controller.contentArea = contentAreaPane;
        controller.setData(current_user, current_shelf);
        
        if(current_shelf.creator_id == current_user.get_id() || (current_shelfuser != null && current_shelfuser.permission.equals("Editor"))){
            controller.add_btn.setVisible(true);
            controller.add_btn.setDisable(false);
            controller.is_auth = true;
        }else{
            controller.is_auth = false;
        }
        controller.populate_table(current_shelf.title + " Contents");
        
        Menu.page_title_label.setText("Shelf Contents");
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    public void populate_table(String category) throws SQLException, IOException{
        ArrayList<Content> contents;
        // ArrayList<Shelf> shelves;

        // shelves = Shelf.get_by_creator_id(current_user.get_id());
        contents = Content.get_by_shelf_id(current_shelf.get_id());
        
        table.getItems().clear();
        
        ObservableList<Row> content_rows = Row.from_contents(contents);
        table.setItems(content_rows);

        // ObservableList<Row> shelf_rows = Row.from_shelves(shelves);
        // table.getItems().addAll(shelf_rows);

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
        }else{
            if(is_auth){
                Row row = table.getSelectionModel().getSelectedItem();
                if(row != null){
                    remove_btn.setDisable(false);
                    remove_btn.setVisible(true);
                }else{
                    remove_btn.setDisable(true);
                    remove_btn.setVisible(false);
                }
            }
        }
    }

    public void details_btn_handler() throws IOException, SQLException{
        ShelfInfoPage shelf_contents_page = new ShelfInfoPage();
        shelf_contents_page.setData(current_user, current_shelf);
        shelf_contents_page.start(contentArea);
    }

    public void add_btn_hanlder() throws IOException, SQLException{
        AddShelfContents add_shelf_content_page = new AddShelfContents();
        add_shelf_content_page.setData(current_user, current_shelf);
        add_shelf_content_page.start(contentArea);
    }
    
    public void remove_btn_handler() throws SQLException, IOException{
        Row row = table.getSelectionModel().getSelectedItem();
        if(row != null){
            table.getItems().remove(row);
            if(row.content != null){
                ContentShelf content_shelf = ContentShelf.get_by_unique_constraint(row.content.get_id(), current_shelf.get_id());
                content_shelf.delete();
            }
        }
    }
}

