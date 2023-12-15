package controllers;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;

public class UserRow {
    public User user;

    public UserRow(){
        user = null;
    }
    public UserRow(User user){
        this.user = user;
        setUsername(user.username);
        setName(user.name);
        setEmail(user.email);
        setRole(user.role);
        setStatus(user.status ? "Active" : "Banned");
    }

    private StringProperty username;
    public void setUsername(String value) { 
        usernameProperty().set(value); 
    }
    public String getUsername() { 
        return usernameProperty().get(); 
    }
    public StringProperty usernameProperty() { 
        if (username == null) username = new SimpleStringProperty(this, "username");
        return username; 
    }

    private StringProperty name;
    public void setName(String value) { 
        nameProperty().set(value); 
    }
    public String getName() { 
        return nameProperty().get(); 
    }
    public StringProperty nameProperty() { 
        if (name == null) name = new SimpleStringProperty(this, "name");
        return name; 
    }

    private StringProperty email;
    public void setEmail(String value) { 
        emailProperty().set(value); 
    }
    public String getEmail() { 
        return emailProperty().get(); 
    }

    public StringProperty emailProperty() { 
        if (email == null) email = new SimpleStringProperty(this, "email");
        return email; 
    }

    private StringProperty role;
    public void setRole(String value) { 
        roleProperty().set(value); 
    }
    public String getRole() { 
        return roleProperty().get(); 
    }
    public StringProperty roleProperty() { 
        if (role == null) role = new SimpleStringProperty(this, "role");
        return role; 
    }


    private StringProperty status;
    public void setStatus(String value) { 
        statusProperty().set(value); 
    }
    public String getStatus() { 
        return statusProperty().get(); 
    }
    public StringProperty statusProperty() { 
        if (status == null) status = new SimpleStringProperty(this, "status");
        return status; 
    }


    public static ObservableList<UserRow> from_users(ArrayList<User> users){
        ObservableList<UserRow> rows = FXCollections.observableArrayList();
        for(User user : users){
            rows.add(new UserRow(user));
        }
        return rows;
    }

}