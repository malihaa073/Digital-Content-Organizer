package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import models.Shelf;
import models.ShelfTag;
import models.ShelfUser;
import models.Tag;
import models.User;

public class ShelfInfoPage {
    
    public TextField title_field, tag_field, username_field;
    public ChoiceBox<String> privacy_choice_box, permission_choice_box;
    public TextArea details_area;
    public Label msg_label;
    public Button edit_btn, save_btn, add_tag_btn, delete_btn, bookmark_btn;
    public VBox user_list_vbox;
    public HBox tag_list_hbox;
    public Pane add_tag_pane, user_permission_pane, add_permission_pane, shelfAreaPane;

    User current_user, shelf_owner;
    Shelf current_shelf;
    ArrayList <Tag> tags = new ArrayList<Tag>();
    ArrayList<ShelfUser> shelfusers = new ArrayList<ShelfUser>();
    ShelfUser current_shelfuser;

    public void setData(User current_user, Shelf current_shelf){
        this.current_user = current_user;
        this.current_shelf = current_shelf;

        try {
            tags = Tag.get_by_shelf_id(current_shelf.get_id());
            shelf_owner = User.get_by_id(current_shelf.creator_id);
            shelfusers = ShelfUser.get_by_shelf_id(current_shelf.get_id());
            current_shelfuser = ShelfUser.get_by_unique_constraint(current_shelf.get_id(), current_user.get_id());
        } catch (SQLException | IOException e) {
            // e.printStackTrace();
        }
    }

    public void start(Pane shelfAreaPane) throws IOException, SQLException {
        if(current_shelf.creator_id != current_user.get_id() && (
            current_shelf.privacy.equals("Private") ||
            (current_shelf.privacy.equals("Custom") && (current_shelfuser == null || current_shelfuser.permission == null || current_shelfuser.permission.equals("null"))))
            ){
            if(current_shelfuser != null){
                current_shelfuser.delete();    
            }
            
            shelfAreaPane.getChildren().clear();
            Dashboard dashboard = new Dashboard();
            dashboard.setData(current_user);
            dashboard.start(shelfAreaPane);
            return;
        }

        FXMLLoader fl = new FXMLLoader();

        Parent root = fl.load(getClass().getResource("/views/shelf_info_page.fxml").openStream());

        ShelfInfoPage controller = fl.getController();
        
        controller.setData(current_user, current_shelf);
        controller.from_shelf_obj_to_view();
        
        controller.shelfAreaPane = shelfAreaPane;

        controller.privacy_choice_box.getItems().addAll("Private", "Custom", "Public");
        controller.privacy_choice_box.getSelectionModel().select(current_shelf.privacy);
        
        controller.permission_choice_box.getItems().addAll("View", "Edit");
        controller.permission_choice_box.getSelectionModel().select(0);

        if(current_shelf.privacy.equals("Custom") || current_shelf.privacy.equals("Public")){
            controller.add_permission_pane.setDisable(false);
        }
        controller.privacy_choice_box.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            if(newValue.equals("Custom") || newValue.equals("Public")){
                controller.add_permission_pane.setDisable(false);
            }
            else{
                controller.add_permission_pane.setDisable(true);
            }
        });
        
        if(current_shelf.creator_id == current_user.get_id()){
            controller.edit_btn.setVisible(true);
            controller.edit_btn.setDisable(false);
            controller.delete_btn.setVisible(true);
            controller.delete_btn.setDisable(false);
        }
        if(current_shelfuser != null){
            if(current_shelfuser.permission.equals("Edit")){
                controller.edit_btn.setVisible(true);
                controller.edit_btn.setDisable(false);
            }
            if(current_shelfuser.bookmarked){
                controller.bookmark_btn.setText("Bookmarked");
            }
        }

        Menu.page_title_label.setText("Shelf Information");
        shelfAreaPane.getChildren().removeAll();
        shelfAreaPane.getChildren().setAll(root);
    }

    private void add_user_row(User user, String permission){
        if (permission == null || permission.equals("null")) return;
        HBox row = new HBox();
        row.setSpacing(10);
        row.getChildren().add(new Label(user.username));
        row.getChildren().add(new Label("-"));
        
        if(permission.equals("Owner")){ 
            row.getChildren().add(new Label("Owner"));
        }else if((user.get_id() == -1 && permission.equals("View") && current_shelf.privacy.equals("Public"))){
            row.getChildren().add(new Label("View"));
        }
        else{
            row.getChildren().add(new Label(permission));
            row.getChildren().add(new Label("-"));
            Button remove_btn = new Button("Remove");
            remove_btn.setOnAction(e -> {
                user_list_vbox.getChildren().remove(row);
            });
            row.getChildren().add(remove_btn);
        }
        user_list_vbox.getChildren().add(row);
    }
    
    private void add_user_row(ShelfUser shelfuser, boolean isOwner) {
        if (shelfuser.user_id == current_shelf.creator_id) return;
        if (shelfuser.permission == null || shelfuser.permission.equals("null")) return;
        User user;
        try{
            user = User.get_by_id(shelfuser.user_id);
        }catch(Exception e){
            e.printStackTrace();
            return;
        }

        HBox row = new HBox();
        row.setSpacing(10);
        row.getChildren().add(new Label(user.username));
        row.getChildren().add(new Label("-"));
        
        row.getChildren().add(new Label(shelfuser.permission));
        row.getChildren().add(new Label("-"));
        Button remove_btn = new Button("Remove");
        remove_btn.setOnAction(e -> {
            user_list_vbox.getChildren().remove(row);
            shelfusers.remove(shelfuser);
            
            if(shelfuser != null && shelfuser.get_id() != -1) {
                try {
                    shelfuser.delete();
                    if(current_shelfuser != null && current_shelfuser.get_id() == shelfuser.get_id()) {
                        current_shelfuser = null;
                        bookmark_btn.setText("Add Bookmark");
                    }
                } catch (SQLException | IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        row.getChildren().add(remove_btn);
        user_list_vbox.getChildren().add(row);
    }

    public void add_tag_cell(Tag tag) {
        HBox cell = new HBox();
        cell.setSpacing(2);
        Hyperlink tag_hp = new Hyperlink("#" + tag.tag);
        tag_hp.setOnAction(e -> {
            Discover discover = new Discover();
            discover.setData(current_user, tag.tag);
            try {
                discover.start(shelfAreaPane);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }

            Menu.page_title_label.setText("Discover");
        });
        cell.getChildren().add(tag_hp);

        if(current_shelf.creator_id == current_user.get_id() || (current_shelfuser != null && current_shelfuser.permission.equals("Edit"))){
            
            cell.getChildren().add(new Label("|"));

            Hyperlink remove_btn = new Hyperlink("X");
            remove_btn.setOnAction(e -> {
                tags.remove(tag);
                tag_list_hbox.getChildren().remove(cell);
                if(tag.get_id() != -1) {
                    try {
                        ShelfTag shelf_tag = ShelfTag.get_by_unique_constraint(current_shelf.get_id(), tag.get_id());
                        shelf_tag.delete();
                    } catch (SQLException | IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            cell.getChildren().add(remove_btn);
        }

        tag_list_hbox.getChildren().add(cell);
    }
    
    private void from_shelf_obj_to_view(){
        title_field.setText(current_shelf.title);
        details_area.setText(current_shelf.details);

        privacy_choice_box.getSelectionModel().select(current_shelf.privacy);

        tag_list_hbox.getChildren().clear();
        for(int i = 0; i < tags.size(); i++){
            add_tag_cell(tags.get(i));
        }

        user_list_vbox.getChildren().clear();
        add_user_row(shelf_owner, "Owner");
        if(current_shelf.privacy.equals("Public")){
            User everyone = new User();
            everyone.username = "Everyone";
            add_user_row(everyone, "View");
        }
        if(current_shelf.privacy.equals("Custom") || current_shelf.privacy.equals("Public")){ 
            for(int i = 0; i < shelfusers.size(); i++){
                add_user_row(shelfusers.get(i), false);        
            }
        }
    }

    private void from_view_to_shelf_obj(){
        current_shelf.title = title_field.getText();
        current_shelf.details = details_area.getText();
        current_shelf.privacy = privacy_choice_box.getValue();

        // tags are added with it's own button event
        // users ar also added with it's own button event
    }

    private void set_editability(boolean editable){
        title_field.setEditable(editable);
        details_area.setEditable(editable);
        tag_field.setEditable(editable);
        
        privacy_choice_box.setDisable(!editable);
        add_tag_btn.setDisable(!editable);
        user_list_vbox.setDisable(!editable);
        permission_choice_box.setDisable(!editable);

        add_tag_pane.setVisible(editable);
        add_permission_pane.setVisible(editable);
        
        save_btn.setDisable(!editable);
        save_btn.setVisible(editable);
    }
    

    public void addTag(Event event){
        
        if(tag_field.getText().isEmpty()){
            msg_label.setText("Tag field is empty");
            return;
        }
        
        Tag tag = new Tag();
        tag.tag = tag_field.getText();
        add_tag_cell(tag);
        tags.add(tag);
        tag_field.clear();
    }

    
    public void addUser(Event event) throws IOException, SQLException{
        User user = null;
        try{
            user = User.get_by_username(username_field.getText());
        }catch(SQLException e){
            if(e.getMessage().contains("The result set has no current row")){
                msg_label.setText("User does not exist");
                return;
            }else{
                throw e;
            }
        }
        
        ShelfUser shelf_user = new ShelfUser(current_shelf.get_id(), user.get_id(), permission_choice_box.getValue(), false);
        shelfusers.add(shelf_user);
        add_user_row(shelf_user, false);
    }

    
    public void saveShelf(Event event) throws IOException, SQLException{
        from_view_to_shelf_obj();
        
        try {
            current_shelf.modifier_id = current_user.get_id();
            current_shelf.update();
            msg_label.setText("Shelf updated successfully");
        } catch (SQLException e) {
            current_shelf.sync(true);
            if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                msg_label.setText("You have already added a shelf with this Title/URL!");
                editButtonOnclick(new ActionEvent());
                return;
            }
            else{
                editButtonOnclick(new ActionEvent());
                throw e;
            }
        }

        for(int i = 0; i < tags.size(); i++){
            Tag tag = tags.get(i);

            if(tag.get_id() == -1){
                try{
                    tag.creator_id = current_user.get_id();
                    tag.insert();
                } catch (SQLException e) {
                    if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                        tag = Tag.get_by_tag(tag.tag);
                    }else{
                        editButtonOnclick(new ActionEvent());
                        throw e;
                    }
                }
            }else{
                // if tag.id is not -1, then this means it has been loaded as part of the shelf
                // so no need to add it again to the database or to the shelf
                continue;
            }
            
            ShelfTag shelf_tag = null;

            shelf_tag = new ShelfTag(current_shelf.get_id(), tag.get_id());
            
            try{
                shelf_tag.insert();
            } catch (SQLException e) {
                if(e.getMessage().contains("Violation of PRIMARY KEY constraint")){
                    msg_label.setText("You have already added tag '" + tag.tag + "' to this shelf!");
                }else{
                    editButtonOnclick(new ActionEvent());
                    throw e;
                }
            }
        }

        for(int i = 0; i < shelfusers.size(); ++i){
            ShelfUser shelf_user = shelfusers.get(i);
            if(shelf_user.get_id() != -1) continue;

            try{
                shelf_user.insert();
            } catch (SQLException e) {
                if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                    msg_label.setText("Can't add same user more than once!");
                }else{
                    editButtonOnclick(new ActionEvent());
                    throw e;
                }
            }
        }

        if(current_shelf.privacy.equals("Private")){
            shelfusers = ShelfUser.get_by_shelf_id(current_shelf.get_id());
            for(int i = 0; i < shelfusers.size(); i++){
                if(shelfusers.get(i).user_id != shelf_owner.get_id()) shelfusers.get(i).delete();
            }
        }else if(current_shelf.privacy.equals("Custom")){
            shelfusers = ShelfUser.get_by_shelf_id(current_shelf.get_id());
            for(int i = 0; i < shelfusers.size(); i++){
                if(shelfusers.get(i).user_id != shelf_owner.get_id() && (shelfusers.get(i).permission == null || shelfusers.get(i).permission.equals("null"))) 
                    shelfusers.get(i).delete();
            }
        }

        editButtonOnclick(new ActionEvent());
    }
    
    
    public void editButtonOnclick(Event event) throws SQLException, IOException{
        
        if(edit_btn.getText().equals("Edit")){
            set_editability(true);
            edit_btn.setText("Cancel");
        }
        else{
            set_editability(false);
            edit_btn.setText("Edit");
            current_shelf.sync(true);
            tags = Tag.get_by_shelf_id(current_shelf.get_id());
            shelfusers = ShelfUser.get_by_shelf_id(current_shelf.get_id());
            from_shelf_obj_to_view();
        }
    }

    public void deleteButtonOnclick(Event event) throws SQLException, IOException{
        if(current_shelf.creator_id != current_user.get_id()){
            msg_label.setText("You are not the creator of this shelf!");
            return;
        }
        current_shelf.delete();
        Dashboard dashboard = new Dashboard();
        
        dashboard.setData(current_user);
        dashboard.start(shelfAreaPane);
    }

    public void bookmarkButtonOnclick(Event event) throws SQLException, IOException{
        if(current_shelfuser == null){
            current_shelfuser = new ShelfUser(current_shelf.get_id(), current_user.get_id(), null, true);
            current_shelfuser.insert();
            bookmark_btn.setText("Bookmarked");
            return;
        }

        current_shelfuser.bookmarked = !current_shelfuser.bookmarked;
        try{
            current_shelfuser.update();
            current_shelfuser.sync(true);
        }catch(SQLException e){
            if(e.getMessage().contains("The result set has no current row")){
                current_shelfuser.insert();
                current_shelfuser.sync(true);
            }
        }

        if(current_shelfuser.bookmarked){
            bookmark_btn.setText("Bookmarked");
        }else{
            bookmark_btn.setText("Add Bookmark");
        }
    }

    public void viewContents_button_onclick(Event event) throws IOException, SQLException{
        ShelfContents shelf_contents_page = new ShelfContents();
        shelf_contents_page.setData(current_user, current_shelf);
        shelf_contents_page.start(shelfAreaPane);
    }
}
