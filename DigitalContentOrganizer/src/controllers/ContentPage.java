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
import models.Content;
import models.ContentTag;
import models.ContentUser;
import models.Tag;
import models.User;

public class ContentPage {
    
    public TextField title_field, url_field, alternative_url_field, tag_field, username_field;
    public ChoiceBox<String> type_choice_box, privacy_choice_box, permission_choice_box;
    public TextArea details_area;
    public Label msg_label;
    public Button edit_btn, save_btn, add_tag_btn, delete_btn, bookmark_btn;
    public VBox user_list_vbox;
    public HBox tag_list_hbox;
    public Pane add_tag_pane, user_permission_pane, add_permission_pane, contentAreaPane;

    User current_user, content_owner;
    Content current_content;
    ArrayList <Tag> tags = new ArrayList<Tag>();
    ArrayList<ContentUser> contentusers = new ArrayList<ContentUser>();
    ContentUser current_contentuser;

    public void setData(User current_user, Content current_content){
        this.current_user = current_user;
        this.current_content = current_content;

        try {
            tags = Tag.get_by_content_id(current_content.get_id());
            content_owner = User.get_by_id(current_content.creator_id);
            contentusers = ContentUser.get_by_content_id(current_content.get_id());
            current_contentuser = ContentUser.get_by_unique_constraint(current_content.get_id(), current_user.get_id());
        } catch (SQLException | IOException e) {
            // e.printStackTrace();
        }
    }

    public void start(Pane contentAreaPane) throws IOException, SQLException {
        if(current_content.creator_id != current_user.get_id() && (
            current_content.privacy.equals("Private") ||
            (current_content.privacy.equals("Custom") && (current_contentuser == null || current_contentuser.permission == null || current_contentuser.permission.equals("null"))))
            ){
            if(current_contentuser != null){
                current_contentuser.delete();    
            }
            
            contentAreaPane.getChildren().clear();
            Dashboard dashboard = new Dashboard();
            dashboard.setData(current_user);
            dashboard.start(contentAreaPane);
            return;
        }

        FXMLLoader fl = new FXMLLoader();

        Parent root = fl.load(getClass().getResource("/views/content_page.fxml").openStream());

        ContentPage controller = fl.getController();
        
        controller.setData(current_user, current_content);
        controller.from_content_obj_to_view();
        
        controller.contentAreaPane = contentAreaPane;
        controller.type_choice_box.getItems().addAll("Website", "Text", "E-Book", "Image", "Audio", "Video", "Other");
        controller.type_choice_box.getSelectionModel().select(current_content.type);

        controller.privacy_choice_box.getItems().addAll("Private", "Custom", "Public");
        controller.privacy_choice_box.getSelectionModel().select(current_content.privacy);
        
        controller.permission_choice_box.getItems().addAll("View", "Edit");
        controller.permission_choice_box.getSelectionModel().select(0);

        if(current_content.privacy.equals("Custom") || current_content.privacy.equals("Public")){
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
        
        if(current_content.creator_id == current_user.get_id()){
            controller.edit_btn.setVisible(true);
            controller.edit_btn.setDisable(false);
            controller.delete_btn.setVisible(true);
            controller.delete_btn.setDisable(false);
        }
        if(current_contentuser != null){
            if(current_contentuser.permission.equals("Edit")){
                controller.edit_btn.setVisible(true);
                controller.edit_btn.setDisable(false);
            }
            if(current_contentuser.bookmarked){
                controller.bookmark_btn.setText("Bookmarked");
            }
        }

        Menu.page_title_label.setText("Content Information");
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    private void add_user_row(User user, String permission){
        if (permission == null || permission.equals("null")) return;
        HBox row = new HBox();
        row.setSpacing(10);
        row.getChildren().add(new Label(user.username));
        row.getChildren().add(new Label("-"));
        
        if(permission.equals("Owner")){ 
            row.getChildren().add(new Label("Owner"));
        }else if((user.get_id() == -1 && permission.equals("View") && current_content.privacy.equals("Public"))){
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
    
    private void add_user_row(ContentUser contentuser, boolean isOwner) {
        if (contentuser.user_id == current_content.creator_id) return;
        if (contentuser.permission == null || contentuser.permission.equals("null")) return;
        User user;
        try{
            user = User.get_by_id(contentuser.user_id);
        }catch(Exception e){
            e.printStackTrace();
            return;
        }

        HBox row = new HBox();
        row.setSpacing(10);
        row.getChildren().add(new Label(user.username));
        row.getChildren().add(new Label("-"));
        
        row.getChildren().add(new Label(contentuser.permission));
        row.getChildren().add(new Label("-"));
        Button remove_btn = new Button("Remove");
        remove_btn.setOnAction(e -> {
            user_list_vbox.getChildren().remove(row);
            contentusers.remove(contentuser);
            
            if(contentuser != null && contentuser.get_id() != -1) {
                try {
                    contentuser.delete();
                    if(current_contentuser != null && current_contentuser.get_id() == contentuser.get_id()) {
                        current_contentuser = null;
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
                discover.start(contentAreaPane);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }

            Menu.page_title_label.setText("Discover");
        });
        cell.getChildren().add(tag_hp);

        if(current_content.creator_id == current_user.get_id() || (current_contentuser != null && current_contentuser.permission.equals("Edit"))){
            
            cell.getChildren().add(new Label("|"));

            Hyperlink remove_btn = new Hyperlink("X");
            remove_btn.setOnAction(e -> {
                tags.remove(tag);
                tag_list_hbox.getChildren().remove(cell);
                if(tag.get_id() != -1) {
                    try {
                        ContentTag content_tag = ContentTag.get_by_unique_constraint(current_content.get_id(), tag.get_id());
                        content_tag.delete();
                    } catch (SQLException | IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            cell.getChildren().add(remove_btn);
        }

        tag_list_hbox.getChildren().add(cell);
    }
    
    private void from_content_obj_to_view(){
        title_field.setText(current_content.title);
        url_field.setText(current_content.url);
        alternative_url_field.setText(current_content.alternative_url);
        details_area.setText(current_content.details);

        type_choice_box.getSelectionModel().select(current_content.type);
        privacy_choice_box.getSelectionModel().select(current_content.privacy);

        tag_list_hbox.getChildren().clear();
        for(int i = 0; i < tags.size(); i++){
            add_tag_cell(tags.get(i));
        }

        user_list_vbox.getChildren().clear();
        add_user_row(content_owner, "Owner");
        if(current_content.privacy.equals("Public")){
            User everyone = new User();
            everyone.username = "Everyone";
            add_user_row(everyone, "View");
        }
        if(current_content.privacy.equals("Custom") || current_content.privacy.equals("Public")){ 
            for(int i = 0; i < contentusers.size(); i++){
                add_user_row(contentusers.get(i), false);        
            }
        }
    }

    private void from_view_to_content_obj(){
        current_content.title = title_field.getText();
        current_content.url = url_field.getText();
        current_content.alternative_url = alternative_url_field.getText();
        current_content.details = details_area.getText();
        current_content.type = type_choice_box.getValue();
        current_content.privacy = privacy_choice_box.getValue();

        // tags are added with it's own button event
        // users ar also added with it's own button event
    }

    private void set_editability(boolean editable){
        title_field.setEditable(editable);
        url_field.setEditable(editable);
        alternative_url_field.setEditable(editable);
        details_area.setEditable(editable);
        tag_field.setEditable(editable);
        
        type_choice_box.setDisable(!editable);
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
        
        ContentUser content_user = new ContentUser(current_content.get_id(), user.get_id(), permission_choice_box.getValue(), false);
        contentusers.add(content_user);
        add_user_row(content_user, false);
    }

    
    public void saveContent(Event event) throws IOException, SQLException{
        from_view_to_content_obj();

        if(title_field.getText().isEmpty() || url_field.getText().isEmpty()){
            msg_label.setText("Please fill all the required fields");
            return;
        }

        
        try {
            current_content.modifier_id = current_user.get_id();
            current_content.update();
            msg_label.setText("Content updated successfully");
        } catch (SQLException e) {
            current_content.sync(true);
            if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                msg_label.setText("You have already added a content with this Title/URL!");
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
                // if tag.id is not -1, then this means it has been loaded as part of the content
                // so no need to add it again to the database or to the content
                continue;
            }
            
            ContentTag contentTag = null;

            contentTag = new ContentTag(current_content.get_id(), tag.get_id());
            
            try{
                contentTag.insert();
            } catch (SQLException e) {
                if(e.getMessage().contains("Violation of PRIMARY KEY constraint")){
                    msg_label.setText("You have already added tag '" + tag.tag + "' to this content!");
                }else{
                    editButtonOnclick(new ActionEvent());
                    throw e;
                }
            }
        }

        for(int i = 0; i < contentusers.size(); ++i){
            ContentUser content_user = contentusers.get(i);
            if(content_user.get_id() != -1) continue;

            try{
                content_user.insert();
            } catch (SQLException e) {
                if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                    msg_label.setText("Can't add same user more than once!");
                }else{
                    editButtonOnclick(new ActionEvent());
                    throw e;
                }
            }
        }
        
        if(current_content.privacy.equals("Private")){
            contentusers = ContentUser.get_by_content_id(current_content.get_id());
            for(int i = 0; i < contentusers.size(); i++){
                if(contentusers.get(i).user_id != content_owner.get_id()) contentusers.get(i).delete();
            }
        }else if(current_content.privacy.equals("Custom")){
            contentusers = ContentUser.get_by_content_id(current_content.get_id());
            for(int i = 0; i < contentusers.size(); i++){
                if(contentusers.get(i).user_id != content_owner.get_id() && (contentusers.get(i).permission == null || contentusers.get(i).permission.equals("null"))) 
                    contentusers.get(i).delete();
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
            current_content.sync(true);
            tags = Tag.get_by_content_id(current_content.get_id());
            contentusers = ContentUser.get_by_content_id(current_content.get_id());
            from_content_obj_to_view();
        }
    }

    public void deleteButtonOnclick(Event event) throws SQLException, IOException{
        if(current_content.creator_id != current_user.get_id()){
            msg_label.setText("You are not the creator of this content!");
            return;
        }
        current_content.delete();
        Dashboard dashboard = new Dashboard();
        
        dashboard.setData(current_user);
        dashboard.start(contentAreaPane);
    }

    public void bookmarkButtonOnclick(Event event) throws SQLException, IOException{
        if(current_contentuser == null){
            current_contentuser = new ContentUser(current_content.get_id(), current_user.get_id(), null, true);
            current_contentuser.insert();
            bookmark_btn.setText("Bookmarked");
            return;
        }

        current_contentuser.bookmarked = !current_contentuser.bookmarked;
        try{
            current_contentuser.update();
            current_contentuser.sync(true);
        }catch(SQLException e){
            if(e.getMessage().contains("The result set has no current row")){
                current_contentuser.insert();
                current_contentuser.sync(true);
            }
        }

        if(current_contentuser.bookmarked){
            bookmark_btn.setText("Bookmarked");
        }else{
            bookmark_btn.setText("Add Bookmark");
        }
    }
}
