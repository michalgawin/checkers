package pl.games.checkers;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Checkers extends Application {

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new Checkerboard().createBoardWithPawns());
        primaryStage.setTitle("Warcaby");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}