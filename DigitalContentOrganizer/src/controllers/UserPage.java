package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import models.User;

public class UserPage{
    public User current_user, user;

    public TextField name_field, email_field, username_field;
    public Label msg_label, creation_datetime_label, modification_datetime_label; 
    public ChoiceBox<String> role_choice_box, status_choice_box;

    public Button edit_btn, cancel_btn;

    public void setData(User current_user, User user){
        this.current_user = current_user;
        this.user = user;
    }

    public void start(Pane contentAreaPane) throws IOException {
        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/user_page.fxml").openStream());

        UserPage controller = fl.getController();
        controller.setData(current_user, user);
        controller.role_choice_box.getItems().addAll("user", "admin");
        controller.status_choice_box.getItems().addAll("Active", "Banned");
        controller.from_user_obj_to_view();
        
        Menu.page_title_label.setText("User Information");
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    private void from_user_obj_to_view(){
        name_field.setText(user.name);
        email_field.setText(user.email);
        username_field.setText(user.username);
        creation_datetime_label.setText(user.get_creation_datetime());
        modification_datetime_label.setText(user.get_modification_datetime());

        role_choice_box.setValue(user.role);
        status_choice_box.setValue(user.status ? "Active" : "Banned");
    }

    private void from_view_to_user_obj(){
        user.role = role_choice_box.getValue();
        if(status_choice_box.getValue().equals("Active")){
            user.status = true;
        }
        else{
            user.status = false;
        }
    }

    private void set_editability(boolean editable){
        role_choice_box.setDisable(!editable);
        status_choice_box.setDisable(!editable);
    }
    
    public void editButtonOnclick(Event event) throws SQLException, IOException{
        
        if(edit_btn.getText().equals("Edit")){
            set_editability(true);
            edit_btn.setText("Save");
            cancel_btn.setVisible(true);
        }
        else{
            set_editability(false);
            edit_btn.setText("Edit");
            cancel_btn.setVisible(false);
            
            try {
                from_view_to_user_obj();
                user.update();
                current_user.sync(true);
                modification_datetime_label.setText(user.get_modification_datetime());
                msg_label.setText("User updated successfully");
            } catch (SQLException e) {
                user.sync(true);
                from_user_obj_to_view();
                msg_label.setText(e.getMessage());
            }
        }

    }

    public void cancelButtonOnclick(Event event) throws IOException{
        from_user_obj_to_view();
        set_editability(false);
        edit_btn.setText("Edit");
        cancel_btn.setVisible(false);
    }

}
