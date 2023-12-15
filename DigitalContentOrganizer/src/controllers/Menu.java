package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import models.User;

public class Menu {
    public static Label page_title_label;
    
    public User current_user;

    public Pane contentArea;
    public Label username_label, title_label;
    public Button userlist_btn;
    
    public void setData(User current_user){
        this.current_user = current_user;
    }

    public void start(Stage primaryStage) throws IOException, SQLException {
        FXMLLoader fl = new FXMLLoader();

        Parent root = fl.load(getClass().getResource("/views/menu.fxml").openStream());
        Menu controller = fl.getController();
        controller.setData(current_user);
        controller.from_user_obj_to_view();
        controller.title_label.setText("Dashboard");
        page_title_label = controller.title_label;
        
        primaryStage.setTitle("Digital Content Organizer");
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/theme.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        controller.gotoDashboard(new ActionEvent());
    }

    private void from_user_obj_to_view(){
        if(current_user.role.equals("admin")){
            username_label.setText("username: " + current_user.username + " (admin)");
        }
        else{
            username_label.setText("username: " + current_user.username + " (" + current_user.role + ")");
            userlist_btn.setVisible(false);
        }
    }

    public void gotoProfile(Event event) throws IOException{

        Profile profile = new Profile();
        profile.setData(current_user);
        profile.start(contentArea);

        title_label.setText("Profile");
    }

    public void logout(Event event) throws IOException{
        Signup signup = new Signup();
        signup.start((Stage) ((Button) event.getSource()).getScene().getWindow());
    }

    public void gotoCreateContent(Event event) throws IOException{
        CreateContent createContent = new CreateContent();
        createContent.setData(current_user);
        createContent.start(contentArea);

        title_label.setText("Create Content");
    }

    public void gotoCreateShelf(Event event) throws IOException{
        CreateShelf createShelf = new CreateShelf();
        createShelf.setData(current_user);
        createShelf.start(contentArea);

        title_label.setText("Create Shelf");
    }

    public void gotoDashboard(Event event) throws IOException, SQLException{
        Dashboard dashboard = new Dashboard();
        dashboard.setData(current_user);
        dashboard.start(contentArea);

        title_label.setText("Dashboard");
    }

    public void gotoUserList(Event event) throws IOException, SQLException{
        UserList userList = new UserList();
        userList.setData(current_user);
        userList.start(contentArea);

        title_label.setText("User List");
    }

    public void gotoDiscover(Event event) throws IOException, SQLException{
        Discover discover = new Discover();
        discover.setData(current_user, null);
        discover.start(contentArea);

        title_label.setText("Discover");
    }
}
