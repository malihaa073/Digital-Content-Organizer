package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import models.User;

public class Profile{
    public User current_user;

    public TextField name_field, email_field, username_field;
    public Label msg_label, creation_datetime_label, modification_datetime_label, password_label, confirm_password_label; 
    public PasswordField password_field, confirm_password_field;
    public ChoiceBox<String> role_choice_box, status_choice_box;

    public Button edit_btn, cancel_btn;

    public void setData(User current_user){
        this.current_user = current_user;
    }

    public void start(Pane contentAreaPane) throws IOException {
        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/profile.fxml").openStream());

        Profile controller = fl.getController();
        controller.setData(current_user);
        controller.role_choice_box.getItems().addAll("user", "admin");
        controller.status_choice_box.getItems().addAll("Active", "Banned");
        controller.from_user_obj_to_view();
        
        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    private void from_user_obj_to_view(){
        name_field.setText(current_user.name);
        email_field.setText(current_user.email);
        username_field.setText(current_user.username);
        creation_datetime_label.setText(current_user.get_creation_datetime());
        modification_datetime_label.setText(current_user.get_modification_datetime());

        role_choice_box.setValue(current_user.role);
        status_choice_box.setValue(current_user.status ? "Active" : "Banned");

        password_field.setText(current_user.get_password());
        confirm_password_field.setText(current_user.get_password());
    }

    private void from_view_to_user_obj(){
        current_user.name = name_field.getText();
        current_user.email = email_field.getText();
        current_user.username = username_field.getText();
        current_user.set_password(password_field.getText());

        if(current_user.role.equals("admin")){
            current_user.role = role_choice_box.getValue();
            if(status_choice_box.getValue().equals("Active")){
                current_user.status = true;
            }
            else{
                current_user.status = false;
            }
        }

    }

    private void set_editability(boolean editable){
        name_field.setEditable(editable);
        email_field.setEditable(editable);
        username_field.setEditable(editable);
        
        if(current_user.role.equals("admin")){
            role_choice_box.setDisable(!editable);
            status_choice_box.setDisable(!editable);
        }
        
        password_label.setVisible(editable);
        password_field.setEditable(editable);
        password_field.setVisible(editable);
        
        confirm_password_label.setVisible(editable);
        confirm_password_field.setEditable(editable);
        confirm_password_field.setVisible(editable);
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
            if(password_field.getText().equals(confirm_password_field.getText())){
                try {
                    from_view_to_user_obj();
                    current_user.update();
                    modification_datetime_label.setText(current_user.get_modification_datetime());
                    msg_label.setText("User updated successfully");
                } catch (SQLException e) {
                    current_user.sync(true);
                    from_user_obj_to_view();
                    msg_label.setText(e.getMessage());
                }
            }
            else{
                current_user.sync(true);
                from_user_obj_to_view();
                msg_label.setText("Passwords do not match");
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
