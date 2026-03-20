package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    static Stage window;

    @Override
    public void start(Stage stage) {
        window = stage;
        showLogin();
    }

    public static void showLogin() {
        try {
            BorderPane root = new BorderPane();
            root.setCenter(new LoginScreen());
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                Main.class.getResource(
                    "/application/login.css")
                .toExternalForm());
            window.setTitle("SIMS");
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDashboard(
            String username, String role) {
        try {
            javafx.scene.Parent root =
                role.equals("TEACHER")
                    ? new TeacherScreen(username)
                : role.equals("ADMIN")
                    ? new AdminScreen(username)
                : new StudentScreen(username);

            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                Main.class.getResource(
                    "/application/dashboard.css")
                .toExternalForm());
            window.setTitle("SIMS — " + username);
            window.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}