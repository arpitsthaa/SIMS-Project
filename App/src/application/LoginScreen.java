package application;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginScreen extends HBox {

    public LoginScreen() {
        setAlignment(Pos.CENTER);
        getStyleClass().add("login-box");

        // Left
        Label brand = new Label("SIMS");
        brand.getStyleClass().add("login-brand");
        Label sub = new Label(
            "Student Information\n" +
            "Management System");
        sub.getStyleClass().add("login-sub");
        sub.setWrapText(true);
        Button regBtn = new Button(
            "Create Account");
        regBtn.getStyleClass().addAll(
            "btn","btn-ghost-white");
        regBtn.setOnAction(
            e -> goTo(new RegisterScreen()));
        VBox left = new VBox(16,
            brand, sub, regBtn);
        left.setAlignment(Pos.CENTER_LEFT);
        left.getStyleClass().add("login-left");

        // Right
        Label title = new Label("Sign In");
        title.getStyleClass().add("login-title");

        Label eLbl = new Label("USERNAME");
        eLbl.getStyleClass().add("field-lbl");
        TextField userF = new TextField();
        userF.setPromptText("Enter username");
        userF.getStyleClass().add("field");

        Label pLbl = new Label("PASSWORD");
        pLbl.getStyleClass().add("field-lbl");
        PasswordField passF = new PasswordField();
        passF.setPromptText("Enter password");
        passF.getStyleClass().add("field");

        Label err = new Label("");
        err.setStyle("-fx-text-fill:#ef4444;" +
            "-fx-font-size:12px;");

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().addAll(
            "btn","btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            String u = userF.getText().trim();
            String p = passF.getText().trim();
            if (u.isEmpty()||p.isEmpty()) {
                err.setText("Fill all fields.");
                return;
            }
            String role = DB.login(u, p);
            if (role == null)
                err.setText(
                    "Invalid credentials.");
            else
                Main.showDashboard(u, role);
        });
        passF.setOnAction(
            e -> loginBtn.fire());

        VBox right = new VBox(14,
            title,
            new VBox(5, eLbl, userF),
            new VBox(5, pLbl, passF),
            err, loginBtn);
        right.setAlignment(Pos.CENTER_LEFT);
        right.getStyleClass().add("login-right");

        getChildren().addAll(left, right);
    }

    private void goTo(javafx.scene.Node n) {
        if (getScene() != null)
            ((BorderPane) getScene().getRoot())
                .setCenter(n);
    }
}