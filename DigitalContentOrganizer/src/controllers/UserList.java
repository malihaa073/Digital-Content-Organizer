package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import models.User;


public class UserList {
    
    User current_user;

    public TableView<UserRow> table;
    public TableColumn<UserRow, String> username_col, name_col, email_col, role_col, status_col;
    public TextField search_value_field;
    public ChoiceBox<String> search_by_choice_box, filter_by_choice_box;
    public RadioButton radio_btn1, radio_btn2;

    Pane contentArea;
    
    public void setData(User current_user){
        this.current_user = current_user;
    }

    public void start(Pane contentAreaPane) throws IOException, SQLException {
        FXMLLoader fl = new FXMLLoader();

        Pane root = fl.load(getClass().getResource("/views/user_list.fxml").openStream());

        UserList controller = fl.getController();
        controller.contentArea = contentAreaPane;
        controller.setData(current_user);
        controller.populate_table(true);

        controller.search_by_choice_box.getItems().addAll("Username", "Name", "Email");
        controller.search_by_choice_box.getSelectionModel().select(0);
        controller.filter_by_choice_box.getItems().addAll("None", "Role", "Status");
        controller.filter_by_choice_box.getSelectionModel().select(0);

        controller.filter_by_choice_box.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            if(newValue.equals("None")){
                controller.radio_btn1.setVisible(false);
                controller.radio_btn2.setVisible(false);
            }
            else if(newValue.equals("Role")){
                controller.radio_btn1.setVisible(true);
                controller.radio_btn2.setVisible(true);

                controller.radio_btn1.setText("Admin");
                controller.radio_btn2.setText("User");
            }
            else if(newValue.equals("Status")){
                controller.radio_btn1.setVisible(true);
                controller.radio_btn2.setVisible(true);

                controller.radio_btn1.setText("Active");
                controller.radio_btn2.setText("Banned");
            }
        });

        contentAreaPane.getChildren().removeAll();
        contentAreaPane.getChildren().setAll(root);
    }

    public void populate_table(boolean allUsers) throws SQLException, IOException{
        ArrayList<User> users;
        if(allUsers){
            users = User.get_all();
        }
        else{
            String filterValue;
            if(radio_btn1.isSelected()){
                filterValue = radio_btn1.getText();
            }
            else{
                filterValue = radio_btn2.getText();
            }
            users = User.search(search_by_choice_box.getValue(), search_value_field.getText(), filter_by_choice_box.getValue(), filterValue);
        }
        
        table.getItems().clear();
        
        ObservableList<UserRow> user_rows = UserRow.from_users(users);
        table.setItems(user_rows);

        username_col.setCellValueFactory(new PropertyValueFactory<UserRow, String>("username"));
        name_col.setCellValueFactory(new PropertyValueFactory<UserRow, String>("name"));
        email_col.setCellValueFactory(new PropertyValueFactory<UserRow, String>("email"));
        role_col.setCellValueFactory(new PropertyValueFactory<UserRow, String>("role"));
        status_col.setCellValueFactory(new PropertyValueFactory<UserRow, String>("status"));
    }

    public void search_btn_click(Event event) throws IOException, SQLException {
        populate_table(false);
    }

    public void mouse_click_event_handler(MouseEvent event) throws IOException{
        if(event.getClickCount() == 2){
            UserRow row = table.getSelectionModel().getSelectedItem();
            if(row != null){
                if(row.user != null){
                    UserPage user_page = new UserPage();
                    user_page.setData(current_user, row.user);
                    user_page.start(contentArea);
                }
            }
        }
    }
}

