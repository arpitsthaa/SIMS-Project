package application;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegisterScreen extends HBox {

    public RegisterScreen() {
        setAlignment(Pos.CENTER);
        getStyleClass().add("login-box");

        // Left
        Label brand = new Label("SIMS");
        brand.getStyleClass().add("login-brand");
        Label sub = new Label(
            "Create your account\n" +
            "and get started today.");
        sub.getStyleClass().add("login-sub");
        sub.setWrapText(true);
        Button back = new Button("Sign In");
        back.getStyleClass().addAll(
            "btn","btn-ghost-white");
        back.setOnAction(
            e -> goTo(new LoginScreen()));
        VBox left = new VBox(16, brand, sub, back);
        left.setAlignment(Pos.CENTER_LEFT);
        left.getStyleClass().add("login-left");

        // Right
        Label title = new Label("Register");
        title.getStyleClass().add("login-title");

        TextField nameF  = f("Full name");
        TextField emailF = f("Email");
        TextField userF  = f("Username");
        PasswordField passF = pf("Password");
        PasswordField confF = pf("Confirm");
        TextField extraF = f("Student ID");

        ComboBox<String> roleBox =
            new ComboBox<>();
        roleBox.getItems().addAll(
            "STUDENT","TEACHER");
        roleBox.setValue("STUDENT");
        roleBox.getStyleClass().add("field");
        roleBox.setPrefWidth(160);

        Label extraLbl = new Label("STUDENT ID");
        extraLbl.getStyleClass().add("field-lbl");
        roleBox.setOnAction(e -> {
            boolean t = roleBox.getValue()
                .equals("TEACHER");
            extraLbl.setText(
                t ? "DEPARTMENT" : "STUDENT ID");
            extraF.setPromptText(
                t ? "Department" : "Student ID");
        });

        Label err = new Label("");
        err.setStyle("-fx-text-fill:#ef4444;" +
            "-fx-font-size:12px;");
        err.setWrapText(true);

        Button regBtn = new Button("Register");
        regBtn.getStyleClass().addAll(
            "btn","btn-primary");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setOnAction(e -> {
            String name = nameF.getText().trim();
            String email=emailF.getText().trim();
            String user = userF.getText().trim();
            String pass = passF.getText().trim();
            String conf = confF.getText().trim();
            String extra=extraF.getText().trim();
            String role = roleBox.getValue();

            if (name.isEmpty()||email.isEmpty()
                    ||user.isEmpty()
                    ||pass.isEmpty()
                    ||conf.isEmpty()
                    ||extra.isEmpty()) {
                err.setText(
                    "All fields required.");
                return;
            }
            if (!pass.equals(conf)) {
                err.setText(
                    "Passwords don't match.");
                return;
            }
            if (pass.length() < 6) {
                err.setText("Min 6 characters.");
                return;
            }
            if (DB.userExists(user)) {
                err.setText("Username taken.");
                return;
            }
            if (DB.register(name,email,user,
                    pass,role,extra)) {
                Alert a = new Alert(
                    Alert.AlertType.INFORMATION);
                a.setHeaderText(null);
                a.setContentText(
                    "Account created! Sign in.");
                a.showAndWait();
                goTo(new LoginScreen());
            } else {
                err.setText("Failed. Try again.");
            }
        });

        VBox right = new VBox(10, title,
            new HBox(12,
                lv("FULL NAME",nameF),
                lv("EMAIL",emailF)),
            new HBox(12,
                lv("USERNAME",userF),
                lv("ROLE",roleBox)),
            new HBox(12,
                lv("PASSWORD",passF),
                lv("CONFIRM",confF)),
            new VBox(4,extraLbl,extraF),
            err, regBtn);
        right.setAlignment(Pos.CENTER_LEFT);
        right.getStyleClass().add("login-right");

        getChildren().addAll(left, right);
    }

    private TextField f(String p) {
        TextField t = new TextField();
        t.setPromptText(p);
        t.getStyleClass().add("field");
        return t;
    }

    private PasswordField pf(String p) {
        PasswordField t = new PasswordField();
        t.setPromptText(p);
        t.getStyleClass().add("field");
        return t;
    }

    private VBox lv(String l, Control c) {
        Label lb = new Label(l);
        lb.getStyleClass().add("field-lbl");
        VBox v = new VBox(4, lb, c);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    private void goTo(javafx.scene.Node n) {
        if (getScene() != null)
            ((BorderPane) getScene().getRoot())
                .setCenter(n);
    }
}