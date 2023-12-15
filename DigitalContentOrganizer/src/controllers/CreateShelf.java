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
import models.Shelf;
import models.ShelfTag;
import models.Tag;
import models.User;

public class CreateShelf {
    public TextField title_field, tag_field;
    public ChoiceBox<String> privacy_choice_box;
    public TextArea details_area;
    public Label msg_label, tags_label;
    public Button reset_btn, create_btn;

    User current_user;
    ArrayList <String> tags = new ArrayList<String>();

    public void setData(User current_user){
        this.current_user = current_user;
    }

    public void start(Pane contentAreaPane) throws IOException {
        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/create_shelf.fxml").openStream());

        CreateShelf controller = fl.getController();
        controller.setData(current_user);
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
    
    public void createShelf(Event event) throws IOException, SQLException{
        if(current_user.status){
            if(title_field.getText().isEmpty()){
                msg_label.setText("Please fill all the required fields");
                return;
            }
    
            Shelf shelf = new Shelf(current_user.get_id(), title_field.getText(), details_area.getText(), privacy_choice_box.getValue(), true);
            
            try {
                shelf.insert();
                msg_label.setText("Shelf created successfully");
            } catch (SQLException e) {
                if(e.getMessage().contains("Violation of UNIQUE KEY constraint")){
                    msg_label.setText("You already have a shelf with this title");
                    return;
                }
                else{
                    throw e;
                }
            }
            
            for(String tag : tags){
                Tag new_tag = null;
                new_tag = Tag.get_by_tag(tag);
                ShelfTag shelfTag = null;
                if(new_tag != null){
                    shelfTag = new ShelfTag(shelf.get_id(), new_tag.get_id());
                }else{
                    new_tag = new Tag(current_user.get_id(), tag);
                    new_tag.insert();
                    shelfTag = new ShelfTag(shelf.get_id(), new_tag.get_id());
                }
                shelfTag.insert();
            }
        }
        else {
            msg_label.setText("You are banned!");
        }
    }

    public void reset(Event event){
        title_field.clear();
        tag_field.clear();
        details_area.clear();
        privacy_choice_box.getSelectionModel().select(0);
        msg_label.setText("");
        tags_label.setText("");
        tags.clear();
    }
}
