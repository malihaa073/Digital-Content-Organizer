package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import models.Content;
import models.ContentTag;
import models.Tag;
import models.User;

public class CreateContent {
    public TextField title_field, url_field, alternative_url_field, tag_field;
    public ChoiceBox<String> type_choice_box, privacy_choice_box;
    public TextArea details_area;
    public Label msg_label, tags_label;
    public Button reset_btn, create_btn, add_tag_btn;

    User current_user;
    ArrayList <String> tags = new ArrayList<String>();

    public void setData(User current_user){
        this.current_user = current_user;
    }

    public void start(Pane contentAreaPane) throws IOException {
        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/create_content.fxml").openStream());

        CreateContent controller = fl.getController();
        controller.setData(current_user);
        controller.type_choice_box.getItems().addAll("Website", "Text", "E-Book", "Image", "Audio", "Video", "Other");
        controller.type_choice_box.getSelectionModel().select(0);
        controller.privacy_choice_box.getItems().addAll("Private", "Custom", "Public");
        controller.privacy_choice_box.getSelectionModel().select(0);
        
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }
    
    public void addTag(Event event){
        if(tag_field.getText().isEmpty()){
            msg_label.setText("Tag field is empty");
            return;
        }
        tags_label.setText(tags_label.getText() + " #" + tag_field.getText());
        tags.add(tag_field.getText());
        tag_field.clear();
    }
    
    public void createContent(Event event) throws IOException, SQLException{
        if(current_user.status){
            if(title_field.getText().isEmpty() || url_field.getText().isEmpty()){
                msg_label.setText("Please fill all the required fields");
                return;
            }
    
            Content content = new Content(current_user.get_id(), title_field.getText(), url_field.getText(), alternative_url_field.getText(), type_choice_box.getValue(), details_area.getText(), privacy_choice_box.getValue(), true);
            
            try {
                content.insert();
                msg_label.setText("Content created successfully");
            } catch (SQLException e) {
                if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                    msg_label.setText("You already have a content with the same URL or Title");
                    return;
                }
                else{
                    throw e;
                }
            }
    
            for(String tag : tags){
                Tag new_tag = null;
                new_tag = Tag.get_by_tag(tag);
                ContentTag contentTag = null;
                if(new_tag != null){
                    contentTag = new ContentTag(content.get_id(), new_tag.get_id());
                }else{
                    new_tag = new Tag(current_user.get_id(), tag);
                    new_tag.insert();
                    contentTag = new ContentTag(content.get_id(), new_tag.get_id());
                }
                contentTag.insert();
            }
        }
        else{
            msg_label.setText("You are banned!");
        }
    }

    public void reset(Event event) throws IOException {
        title_field.clear();
        url_field.clear();
        alternative_url_field.clear();
        tag_field.clear();
        details_area.clear();
        type_choice_box.getSelectionModel().select(0);
        privacy_choice_box.getSelectionModel().select(0);
        msg_label.setText("");
        tags_label.setText("");
        tags.clear();
    }
}
