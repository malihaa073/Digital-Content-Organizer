package controllers;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Content;
import models.Shelf;

public class Row {
    public Content content;
    public Shelf shelf;

    public Row(){
        content = null;
        shelf = null;
    }
    public Row(Content content){
        this.content = content;
        this.shelf = null;
        setTitle(content.title);
        setUrl(content.url);
        setModification_datetime(content.get_modification_datetime());
        setType(content.type);
    }

    public Row(Shelf shelf){
        this.shelf = shelf;
        this.content = null;
        setTitle(shelf.title);
        setUrl("-");
        setModification_datetime(shelf.get_modification_datetime());
        setType("Shelf");
    }

    private StringProperty title;
    public void setTitle(String value) { 
        TitleProperty().set(value); 
    }
    public String getTitle() { 
        return TitleProperty().get(); 
    }
    public StringProperty TitleProperty() { 
        if (title == null) title = new SimpleStringProperty(this, "title");
        return title; 
    }

    private StringProperty url;
    public void setUrl(String value) { 
        urlProperty().set(value); 
    }
    public String getUrl() { 
        return urlProperty().get(); 
    }

    public StringProperty urlProperty() { 
        if (url == null) url = new SimpleStringProperty(this, "url");
        return url; 
    }

    private StringProperty modification_datetime;
    public void setModification_datetime(String value) { 
        modification_datetimeProperty().set(value); 
    }
    public String getModification_datetime() { 
        return modification_datetimeProperty().get(); 
    }
    public StringProperty modification_datetimeProperty() { 
        if (modification_datetime == null) modification_datetime = new SimpleStringProperty(this, "modification_datetime");
        return modification_datetime; 
    }


    private StringProperty type;
    public void setType(String value) { 
        TypeProperty().set(value); 
    }
    public String getType() { 
        return TypeProperty().get(); 
    }
    public StringProperty TypeProperty() { 
        if (type == null) type = new SimpleStringProperty(this, "type");
        return type; 
    }


    public static ObservableList<Row> from_contents(ArrayList<Content> contents){
        ObservableList<Row> rows = FXCollections.observableArrayList();
        for(Content content : contents){
            rows.add(new Row(content));
        }
        return rows;
    }

    public static ObservableList<Row> from_shelves(ArrayList<Shelf> shelves){
        ObservableList<Row> rows = FXCollections.observableArrayList();
        for(Shelf shelf : shelves){
            rows.add(new Row(shelf));
        }
        return rows;
    }
}