package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import models.User;
import models.Content;
import models.ContentShelf;
import models.Shelf;
import models.ShelfUser;

public class AddShelfContents {
    
    User current_user;
    Shelf current_shelf;
    ShelfUser current_shelfuser;

    ArrayList<Content> contents_to_be_added;
    
    Pane contentAreaPane;

    public TextField title_field;
    public Label privacy_lbl;
    public TextArea details_txt_area;
    public VBox content_list_vbox;
    
    public void setData(User current_user, Shelf current_shelf){
        this.current_user = current_user;
        this.current_shelf = current_shelf;

        contents_to_be_added = new ArrayList<Content>();

        try{
            this.current_shelfuser = ShelfUser.get_by_unique_constraint(current_shelf.get_id(), current_user.get_id());
        }catch(SQLException | IOException e){
            this.current_shelfuser = null;
        }
    }

    public void start(Pane contentAreaPane) throws IOException, SQLException {
        if(current_shelf.creator_id != current_user.get_id() && 
            (current_shelfuser == null || current_shelfuser.permission == null || !current_shelfuser.permission.equals("Edit") 
            )){
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

        Pane root = fl.load(getClass().getResource("/views/add_shelf_contents.fxml").openStream());

        AddShelfContents controller = fl.getController();
        controller.contentAreaPane = contentAreaPane;
        controller.setData(current_user, current_shelf);
        controller.populate_view();
         
        Menu.page_title_label.setText("Add Contents to Shelf");
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    private void add_btn_grp(String lbl_str, ArrayList<Content> contents){
        Label lbl = new Label(lbl_str);
        lbl.setStyle("-fx-font-weight: bold");
        content_list_vbox.getChildren().add(lbl);
        for(int i = 0; i < contents.size(); i++){
            Content content = contents.get(i);
            HBox row = new HBox();
            row.setSpacing(10);
            row.getChildren().add(new Label(i+1 + ". " + content.title));
            
            CheckBox chk_bx = new CheckBox("Select");
            chk_bx.setOnAction(e -> {
                if(chk_bx.getText().equals("Select")) {
                    chk_bx.setText("Selected");
                    contents_to_be_added.add(content);
                }else{
                    chk_bx.setText("Select");
                    contents_to_be_added.remove(content);
                }
            });
            row.getChildren().add(chk_bx);
            
            content_list_vbox.getChildren().add(row);
        }
    }

    public void populate_view() throws SQLException, IOException{
        title_field.setText(current_shelf.title);
        privacy_lbl.setText(current_shelf.privacy);
        details_txt_area.setText(current_shelf.details);

        
        ArrayList<Content> contents, shared_withme_contents, public_contents;
        
        contents = Content.get_by_creator_id(current_user.get_id());
        shared_withme_contents = Content.get_shared(current_user.get_id());
        public_contents =Content.search("Title", "", "All");
        
        content_list_vbox.getChildren().clear();
        content_list_vbox.setSpacing(10);
        
        add_btn_grp("My Contents:", contents);
        add_btn_grp("Shared with me:", shared_withme_contents);
        add_btn_grp("Public Contents:", public_contents);
    }

    public void save_btn_handler() throws IOException, SQLException{
        for(int i = 0; i < contents_to_be_added.size(); ++i){
            Content content = contents_to_be_added.get(i);
            ContentShelf content_shelf = new ContentShelf();
            content_shelf.content_id = content.get_id();
            content_shelf.shelf_id = current_shelf.get_id();
            try{
                content_shelf.insert();
            }catch(SQLException e){
                if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                    continue;
                }else{
                    throw e;
                }
            }
            
        }

        cancel_btn_handler();
    }

    public void cancel_btn_handler() throws IOException, SQLException{
        ShelfContents shelf_contents_page = new ShelfContents();
        shelf_contents_page.setData(current_user, current_shelf);
        shelf_contents_page.start(contentAreaPane);
    }
}

