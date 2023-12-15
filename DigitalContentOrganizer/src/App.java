import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

import controllers.Signup;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        primaryStage.setResizable(false);

        Signup signup = new Signup();
        signup.start(primaryStage);
        // primaryStage.setTitle("Home");
        // primaryStage.show();
    }

    public static void main(String args[]) {
        launch(args);
    }

}
