package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public abstract class BaseScreen extends HBox {

    protected final String username;
    protected StackPane content;
    protected Label activeNav;

    public BaseScreen(String username) {
        this.username = username;
        getStyleClass().add("app-root");
        content = new StackPane();
        content.getStyleClass()
            .add("content-area");
        HBox.setHgrow(content, Priority.ALWAYS);
        getChildren().addAll(
            buildSidebar(), content);
        show(homePage());
    }

    protected abstract VBox homePage();
    protected abstract String[][] navItems();
    protected abstract String roleName();
    protected abstract Node pageFor(String key);

    // ════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sb = new VBox();
        sb.getStyleClass().add("sidebar");
        sb.setPrefWidth(210);
        sb.setMinWidth(210);
        sb.setMaxWidth(210);

        // Brand
        Label brand = new Label("SIMS");
        brand.getStyleClass().add("brand");
        HBox brandBox = new HBox(brand);
        brandBox.getStyleClass().add("brand-box");

        // User info
        String ini = username.length() >= 2
            ? username.substring(0,2).toUpperCase()
            : username.toUpperCase();
        Label av = new Label(ini);
        av.getStyleClass().add("avatar");
        Label nm = new Label(username);
        nm.getStyleClass().add("sb-name");
        Label rl = new Label(roleName());
        rl.getStyleClass().add("sb-role");
        HBox userBox = new HBox(10, av,
            new VBox(3, nm, rl));
        userBox.getStyleClass().add("user-box");
        userBox.setAlignment(Pos.CENTER_LEFT);

        // Nav
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(8, 0, 0, 0));
        for (String[] item : navItems()) {
            if (item[0].equals("---")) {
                Label sep = new Label(item[1]);
                sep.getStyleClass().add("nav-sep");
                nav.getChildren().add(sep);
            } else {
                Label ni = new Label(
                    item[0] + "  " + item[1]);
                ni.getStyleClass().add("nav-item");
                ni.setMaxWidth(Double.MAX_VALUE);
                ni.setCursor(
                    javafx.scene.Cursor.HAND);
                final String key = item[2];
                ni.setOnMouseClicked(e -> {
                    setActive(ni);
                    show(pageFor(key));
                });
                if (activeNav == null)
                    setActive(ni);
                nav.getChildren().add(ni);
            }
        }

        // Logout
        Button lo = new Button("Sign Out");
        lo.getStyleClass().add("logout-btn");
        lo.setMaxWidth(Double.MAX_VALUE);
        lo.setOnAction(e -> Main.showLogin());
        VBox loBox = new VBox(lo);
        loBox.setPadding(new Insets(10,14,10,14));

        Region sp = new Region();
        VBox.setVgrow(sp, Priority.ALWAYS);
        sb.getChildren().addAll(
            brandBox, userBox, nav, sp, loBox);
        return sb;
    }

    protected void setActive(Label item) {
        if (activeNav != null)
            activeNav.getStyleClass()
                .remove("active");
        activeNav = item;
        item.getStyleClass().add("active");
    }

    protected void show(Node page) {
        content.getChildren().setAll(page);
        StackPane.setAlignment(
            page, Pos.TOP_LEFT);
    }

    // ════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════

    protected HBox topBar(String title) {
        Label t = new Label(title);
        t.getStyleClass().add("page-title");
        HBox bar = new HBox(t);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    protected VBox statCard(String label,
            String value, boolean accent) {
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        VBox c = new VBox(4, l, v);
        c.getStyleClass().add(
            accent ? "stat-accent" : "stat-card");
        c.setPadding(new Insets(14,16,14,16));
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    protected HBox statsRow(VBox... cards) {
        HBox row = new HBox(10);
        row.getChildren().addAll(cards);
        row.setPadding(new Insets(14, 20, 0, 20));
        return row;
    }

    protected VBox card(String title,
                         Node... items) {
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        VBox c = new VBox(0);
        c.getStyleClass().add("card");
        c.getChildren().add(t);
        c.getChildren().addAll(items);
        return c;
    }

    protected GridPane table(String[] headers,
            List<String[]> rows, int colorCol) {
        GridPane g = new GridPane();
        g.getStyleClass().add("table");
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.getStyleClass().add("th");
            h.setMaxWidth(Double.MAX_VALUE);
            g.add(h, i, 0);
        }
        for (int r = 0; r < rows.size(); r++) {
            String s = r % 2 == 0
                ? "td" : "td-alt";
            String[] row = rows.get(r);
            for (int c = 0; c < row.length; c++) {
                Label cl = new Label(row[c]);
                cl.getStyleClass().add(s);
                cl.setMaxWidth(Double.MAX_VALUE);
                if (c == colorCol)
                    colorCell(cl, row[c]);
                g.add(cl, c, r+1);
            }
        }
        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        for (int i = 0; i < headers.length; i++)
            g.getColumnConstraints().add(cc);
        return g;
    }

    private void colorCell(Label l, String v) {
        if (v.endsWith("%")) {
            int p = Integer.parseInt(
                v.replace("%",""));
            l.getStyleClass().add(
                p>=85?"green":p>=75?"orange":"red");
        } else {
            l.getStyleClass().add(
                v.startsWith("A")?"green"
                :v.startsWith("B")?"orange":"red");
        }
    }

    protected Label statusBadge(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("badge");
        l.getStyleClass().add(
            s.equals("APPROVED") ? "badge-green"
            : s.equals("REJECTED") ? "badge-red"
            : "badge-yellow");
        return l;
    }

    protected ScrollPane scrollWrap(Node n) {
        ScrollPane sp = new ScrollPane(n);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll");
        sp.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    protected TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("field");
        return f;
    }

    protected PasswordField passField(
            String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.getStyleClass().add("field");
        return f;
    }

    protected VBox labeled(String lbl,
                             Control c) {
        Label l = new Label(lbl);
        l.getStyleClass().add("field-lbl");
        return new VBox(4, l, c);
    }

    protected Button btn(String text,
                          String style) {
        Button b = new Button(text);
        b.getStyleClass().addAll("btn", style);
        return b;
    }

    protected void alert(String title,
                          String msg) {
        Alert a = new Alert(
            title.equals("Success")
                ? Alert.AlertType.INFORMATION
                : Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    protected boolean confirm(String msg) {
        Alert a = new Alert(
            Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait()
            .orElse(ButtonType.CANCEL)
            == ButtonType.OK;
    }

    // ── Shared profile page ──────────────────────
    protected VBox profilePage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("My Profile"));

        TextField fnF = field("Full name");
        TextField emF = field("Email");
        PasswordField npF = passField("New password");
        PasswordField cpF = passField("Confirm");

        Button save = btn("Save","btn-primary");
        save.setOnAction(e -> {
            String np = npF.getText().trim();
            String cp = cpF.getText().trim();
            if (!np.isEmpty()) {
                if (!np.equals(cp)) {
                    alert("Error",
                        "Passwords do not match.");
                    return;
                }
                if (np.length() < 6) {
                    alert("Error",
                        "Min 6 characters.");
                    return;
                }
                DB.resetPassword(username, np);
            }
            alert("Success","Profile updated!");
        });

        VBox form = card("Edit Profile",
            new HBox(12,
                labeled("Full Name", fnF),
                labeled("Email", emF)),
            new HBox(12,
                labeled("New Password", npF),
                labeled("Confirm", cpF)),
            save);
        form.setPadding(new Insets(14,20,20,20));

        page.getChildren().add(form);
        return page;
    }
}