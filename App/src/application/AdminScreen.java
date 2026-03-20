package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminScreen extends BaseScreen {

    public AdminScreen(String username) {
        super(username);
    }

    @Override protected String roleName() {
        return "Admin";
    }

    @Override protected String[][] navItems() {
        return new String[][]{
            {"🏠","Home","home"},
            {"👥","Users","users"},
            {"📋","Leave Requests","leave"},
            {"📢","Notices","notices"},
            {"📚","Subjects","subjects"},
            {"---","ACCOUNT",""},
            {"👤","Profile","profile"}
        };
    }

    @Override
    protected Node pageFor(String key) {
        return switch (key) {
            case "users"    -> usersPage();
            case "leave"    -> leavePage();
            case "notices"  -> noticesPage();
            case "subjects" -> subjectsPage();
            case "profile"  -> profilePage();
            default         -> homePage();
        };
    }

    // ════════════════════════════════════════════
    //  HOME
    // ════════════════════════════════════════════
    @Override
    protected VBox homePage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Admin Dashboard"));

        var notices = DB.getNotices();
        page.getChildren().add(statsRow(
            statCard("Students",
                String.valueOf(
                    DB.countByRole("STUDENT")),
                true),
            statCard("Teachers",
                String.valueOf(
                    DB.countByRole("TEACHER")),
                false),
            statCard("Pending Leave",
                String.valueOf(
                    DB.countPendingLeave()),
                false),
            statCard("Notices",
                String.valueOf(notices.size()),
                false)
        ));

        VBox leaveRows = new VBox(0);
        DB.getAllLeave().stream()
            .filter(l -> l[6].equals("PENDING"))
            .limit(5)
            .forEach(l -> {
                Label t = new Label(
                    l[1] + " — " + l[2]);
                t.getStyleClass().add(
                    "notice-title");
                Label m = new Label(
                    l[3] + " to " + l[4]);
                m.getStyleClass().add(
                    "notice-meta");
                HBox row = new HBox(10,
                    new VBox(2, t, m),
                    statusBadge(l[6]));
                row.getStyleClass().add(
                    "notice-item");
                row.setAlignment(
                    Pos.CENTER_LEFT);
                leaveRows.getChildren().add(row);
            });

        if (leaveRows.getChildren().isEmpty())
            leaveRows.getChildren().add(
                emptyLbl(
                    "No pending requests."));

        VBox body = new VBox(14,
            card("Pending Leave Requests",
                leaveRows));
        body.setPadding(
            new Insets(14, 20, 20, 20));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    // ════════════════════════════════════════════
    //  USERS
    // ════════════════════════════════════════════
    private VBox usersPage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(topBar("Users"));

        var all   = DB.getAllUsers();
        var shown = new ArrayList<>(all);

        TextField search = field("Search...");
        search.setPrefWidth(240);

        Button addS = btn("+ Student",
            "btn-primary");
        Button addT = btn("+ Teacher",
            "btn-ghost");

        VBox tableBox = new VBox();

        // ── Runnable array trick ─────────────────
        Runnable[] build = {null};
        build[0] = () -> {
            tableBox.getChildren().clear();
            if (shown.isEmpty()) {
                tableBox.getChildren().add(
                    emptyLbl("No users."));
                return;
            }
            tableBox.getChildren().add(
                usersTable(shown, all,
                    build));
        };
        build[0].run();

        search.textProperty().addListener(
            (obs, o, n) -> {
                String q = n.trim().toLowerCase();
                shown.clear();
                if (q.isEmpty())
                    shown.addAll(all);
                else
                    all.stream()
                        .filter(u ->
                            u[1].toLowerCase()
                                .contains(q)
                            || u[0].toLowerCase()
                                .contains(q)
                            || u[3].toLowerCase()
                                .contains(q))
                        .forEach(shown::add);
                build[0].run();
            });

        addS.setOnAction(e ->
            showAddUser("STUDENT",
                all, shown, build));
        addT.setOnAction(e ->
            showAddUser("TEACHER",
                all, shown, build));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(10,
            search, sp, addT, addS);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(14,
            card("All Users", topRow, tableBox));
        body.setPadding(
            new Insets(14, 20, 20, 20));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    private GridPane usersTable(
            List<String[]> shown,
            List<String[]> all,
            Runnable[] build) {
        GridPane g = new GridPane();
        g.getStyleClass().add("table");

        String[] hdrs = {"#", "Name",
            "Username", "Role", "Actions"};
        for (int i = 0; i < hdrs.length; i++) {
            Label h = new Label(hdrs[i]);
            h.getStyleClass().add("th");
            h.setMaxWidth(Double.MAX_VALUE);
            g.add(h, i, 0);
        }

        for (int r = 0; r < shown.size(); r++) {
            var u = shown.get(r);
            String s = r % 2 == 0
                ? "td" : "td-alt";
            g.add(lbl(String.valueOf(r+1), s),
                0, r+1);
            g.add(lbl(u[1], s), 1, r+1);
            g.add(lbl(u[0], s), 2, r+1);
            g.add(lbl(u[3], s), 3, r+1);

            Button del = smallBtn(
                "Delete", "#ef4444");
            Button reset = smallBtn(
                "Reset Pwd", "#f59e0b");

            final String uname = u[0];
            final String uname2 = u[1];
            del.setOnAction(e -> {
                if (confirm(
                        "Delete " + uname2
                        + "?")) {
                    DB.deleteUser(uname);
                    all.clear();
                    all.addAll(DB.getAllUsers());
                    shown.clear();
                    shown.addAll(all);
                    build[0].run();
                }
            });
            reset.setOnAction(e ->
                showResetPwd(uname));

            HBox acts = new HBox(6, del, reset);
            acts.getStyleClass().add(s);
            acts.setPadding(
                new Insets(6, 12, 6, 12));
            g.add(acts, 4, r+1);
        }

        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        for (int i = 0; i < hdrs.length; i++)
            g.getColumnConstraints().add(cc);
        return g;
    }

    private void showAddUser(String role,
            List<String[]> all,
            List<String[]> shown,
            Runnable[] build) {
        TextField nameF  = field("Full name");
        TextField emailF = field("Email");
        TextField userF  = field("Username");
        PasswordField passF =
            passField("Password");
        TextField extraF = field(
            role.equals("STUDENT")
                ? "Student ID" : "Department");

        VBox content = new VBox(10,
            labeled("Name", nameF),
            labeled("Email", emailF),
            new HBox(10,
                labeled("Username", userF),
                labeled("Password", passF)),
            labeled(
                role.equals("STUDENT")
                    ? "Student ID"
                    : "Department",
                extraF));
        content.setPadding(new Insets(16));
        content.setPrefWidth(360);

        showDialog("Add " + role, content,
            () -> {
                String n = nameF.getText().trim();
                String u = userF.getText().trim();
                String p = passF.getText().trim();
                if (n.isEmpty() || u.isEmpty()
                        || p.isEmpty()) {
                    alert("Error",
                        "Name, user, " +
                        "pass needed.");
                    return;
                }
                if (DB.userExists(u)) {
                    alert("Error",
                        "Username taken.");
                    return;
                }
                if (DB.register(n,
                        emailF.getText().trim(),
                        u, p, role,
                        extraF.getText()
                            .trim())) {
                    alert("Success",
                        role + " added!");
                    all.clear();
                    all.addAll(DB.getAllUsers());
                    shown.clear();
                    shown.addAll(all);
                    build[0].run();
                }
            });
    }

    private void showResetPwd(String username) {
        PasswordField pf =
            passField("New password");
        VBox content = new VBox(10,
            labeled("New Password", pf));
        content.setPadding(new Insets(16));
        content.setPrefWidth(280);

        showDialog("Reset Password", content,
            () -> {
                String p = pf.getText().trim();
                if (p.length() < 6) {
                    alert("Error",
                        "Min 6 chars.");
                    return;
                }
                DB.resetPassword(username, p);
                alert("Success",
                    "Password reset!");
            });
    }

    // ════════════════════════════════════════════
    //  LEAVE
    // ════════════════════════════════════════════
    private VBox leavePage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Leave Requests"));

        final String[] filter = {"ALL"};
        Button[] tabs = {
            btn("All",     "btn-primary"),
            btn("Pending", "btn-ghost"),
            btn("Approved","btn-ghost"),
            btn("Rejected","btn-ghost")
        };
        String[] keys = {
            "ALL","PENDING",
            "APPROVED","REJECTED"};

        var allLeave = DB.getAllLeave();
        VBox tableBox = new VBox();

        Runnable[] build = {null};
        build[0] = () -> {
            tableBox.getChildren().clear();
            var shown = allLeave.stream()
                .filter(l ->
                    filter[0].equals("ALL")
                    || l[6].equals(filter[0]))
                .collect(Collectors.toList());

            if (shown.isEmpty()) {
                tableBox.getChildren().add(
                    emptyLbl("No records."));
                return;
            }
            tableBox.getChildren().add(
                leaveTable(shown,
                    allLeave, build));
        };
        build[0].run();

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnAction(e -> {
                filter[0] = keys[idx];
                for (Button tb : tabs)
                    tb.getStyleClass()
                        .setAll("btn","btn-ghost");
                tabs[idx].getStyleClass()
                    .setAll("btn","btn-primary");
                build[0].run();
            });
        }

        HBox tabRow = new HBox(8);
        tabRow.getChildren().addAll(tabs);
        tabRow.setPadding(
            new Insets(0, 0, 10, 0));

        VBox body = new VBox(14,
            card("Leave Requests",
                tabRow, tableBox));
        body.setPadding(
            new Insets(14, 20, 20, 20));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    private GridPane leaveTable(
            List<String[]> shown,
            List<String[]> allLeave,
            Runnable[] build) {
        GridPane g = new GridPane();
        g.getStyleClass().add("table");

        String[] hdrs = {"Student", "Type",
            "From", "To", "Status", "Actions"};
        for (int i = 0; i < hdrs.length; i++) {
            Label h = new Label(hdrs[i]);
            h.getStyleClass().add("th");
            h.setMaxWidth(Double.MAX_VALUE);
            g.add(h, i, 0);
        }

        for (int r = 0; r < shown.size(); r++) {
            var l = shown.get(r);
            String s = r % 2 == 0
                ? "td" : "td-alt";
            int id = Integer.parseInt(l[0]);

            g.add(lbl(l[1], s), 0, r+1);
            g.add(lbl(l[2], s), 1, r+1);
            g.add(lbl(l[3], s), 2, r+1);
            g.add(lbl(l[4], s), 3, r+1);

            HBox bc = new HBox(
                statusBadge(l[6]));
            bc.getStyleClass().add(s);
            bc.setPadding(
                new Insets(8, 12, 8, 12));
            g.add(bc, 4, r+1);

            HBox acts = new HBox(6);
            acts.getStyleClass().add(s);
            acts.setPadding(
                new Insets(6, 10, 6, 10));

            if (l[6].equals("PENDING")) {
                Button app = smallBtn(
                    "✓ Approve", "#10b981");
                Button rej = smallBtn(
                    "✕ Reject", "#ef4444");
                app.setOnAction(e -> {
                    DB.updateLeave(id,
                        "APPROVED", "");
                    allLeave.clear();
                    allLeave.addAll(
                        DB.getAllLeave());
                    build[0].run();
                });
                rej.setOnAction(e -> {
                    DB.updateLeave(id,
                        "REJECTED", "");
                    allLeave.clear();
                    allLeave.addAll(
                        DB.getAllLeave());
                    build[0].run();
                });
                acts.getChildren()
                    .addAll(app, rej);
            }
            g.add(acts, 5, r+1);
        }

        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        for (int i = 0; i < hdrs.length; i++)
            g.getColumnConstraints().add(cc);
        return g;
    }

    // ════════════════════════════════════════════
    //  NOTICES
    // ════════════════════════════════════════════
    private VBox noticesPage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Notices"));

        TextField titleF = field("Title");
        TextField subjF  = field("Category");
        TextArea bodyA = new TextArea();
        bodyA.setPromptText("Notice body...");
        bodyA.setPrefHeight(80);
        bodyA.setWrapText(true);
        bodyA.getStyleClass().add("field");

        Button send = btn("Post","btn-primary");
        VBox body = new VBox(14);
        body.setPadding(
            new Insets(14, 20, 20, 20));

        send.setOnAction(e -> {
            String t = titleF.getText().trim();
            String s = subjF.getText().trim();
            String b = bodyA.getText().trim();
            if (t.isEmpty() || s.isEmpty()
                    || b.isEmpty()) {
                alert("Error",
                    "All fields required.");
                return;
            }
            if (DB.addNotice(t, b,
                    username, s)) {
                alert("Success", "Posted!");
                titleF.clear();
                subjF.clear();
                bodyA.clear();
                if (body.getChildren().size() >= 2)
                    body.getChildren().set(1,
                        buildNoticeList());
            } else {
                alert("Error", "Failed.");
            }
        });

        body.getChildren().addAll(
            card("Post Notice",
                new HBox(12,
                    labeled("Title", titleF),
                    labeled("Category", subjF)),
                labeled("Body", bodyA),
                send),
            buildNoticeList());

        page.getChildren().add(scrollWrap(body));
        return page;
    }

    private VBox buildNoticeList() {
        var notices = DB.getNotices();
        if (notices.isEmpty())
            return card("All Notices",
                emptyLbl("No notices."));

        VBox rows = new VBox(0);
        for (var n : notices) {
            int id = Integer.parseInt(n[0]);
            Label t = new Label(n[1]);
            t.getStyleClass().add("notice-title");
            Label m = new Label(
                n[3] + " · " + n[4]
                    + " · " + n[5]);
            m.getStyleClass().add("notice-meta");
            VBox info = new VBox(2, t, m);
            HBox.setHgrow(info, Priority.ALWAYS);
            Button del = smallBtn("✕","#ef4444");
            del.setOnAction(e -> {
                if (confirm("Delete notice?"))
                    DB.deleteNotice(id);
            });
            HBox row = new HBox(10, info, del);
            row.getStyleClass().add("notice-item");
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }
        return card("All Notices", rows);
    }

    // ════════════════════════════════════════════
    //  SUBJECTS
    // ════════════════════════════════════════════
    private VBox subjectsPage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Subjects"));

        TextField nameF = field("Subject name");
        nameF.setPrefWidth(240);
        Button addBtn = btn("Add","btn-primary");

        VBox tableBox = new VBox();

        Runnable[] build = {null};
        build[0] = () -> {
            tableBox.getChildren().clear();
            var subjects = DB.getSubjects();
            if (subjects.isEmpty()) {
                tableBox.getChildren().add(
                    emptyLbl("No subjects yet."));
                return;
            }
            GridPane g = new GridPane();
            g.getStyleClass().add("table");
            for (var h : new String[]{
                    "#","Name","Action"}) {
                Label hl = new Label(h);
                hl.getStyleClass().add("th");
                hl.setMaxWidth(Double.MAX_VALUE);
                g.add(hl, java.util.Arrays
                    .asList("#","Name","Action")
                    .indexOf(h), 0);
            }
            for (int r = 0;
                    r < subjects.size(); r++) {
                var s  = subjects.get(r);
                int id = Integer.parseInt(s[0]);
                String st = r % 2 == 0
                    ? "td" : "td-alt";
                g.add(lbl(String.valueOf(r+1),
                    st), 0, r+1);
                g.add(lbl(s[1], st), 1, r+1);
                Button del = smallBtn(
                    "Delete", "#ef4444");
                del.setOnAction(e -> {
                    if (confirm(
                            "Delete " + s[1]
                            + "?")) {
                        DB.deleteSubject(id);
                        build[0].run();
                    }
                });
                HBox dc = new HBox(del);
                dc.getStyleClass().add(st);
                dc.setPadding(
                    new Insets(6, 12, 6, 12));
                g.add(dc, 2, r+1);
            }
            var cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
            g.getColumnConstraints().add(cc);
            g.getColumnConstraints().add(cc);
            tableBox.getChildren().add(g);
        };
        build[0].run();

        addBtn.setOnAction(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) {
                alert("Error","Name required.");
                return;
            }
            if (DB.addSubject(name)) {
                alert("Success",
                    "Subject added!");
                nameF.clear();
                build[0].run();
            }
        });

        VBox body = new VBox(14,
            card("Manage Subjects",
                new HBox(10,
                    labeled("Name", nameF),
                    addBtn) {{
                        setAlignment(
                            Pos.BOTTOM_LEFT);}},
                tableBox));
        body.setPadding(
            new Insets(14, 20, 20, 20));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    // ════════════════════════════════════════════
    //  SHARED HELPERS
    // ════════════════════════════════════════════

    // Reusable dialog runner
    private void showDialog(String title,
            VBox content, Runnable onOk) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(title);
        dlg.setHeaderText(null);
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes()
            .addAll(ButtonType.OK,
                ButtonType.CANCEL);
        dlg.getDialogPane().setStyle(
            "-fx-background-color:white;");
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK)
                onOk.run();
        });
    }

    // Small inline action button
    private Button smallBtn(String text,
                              String color) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-text-fill:" + color + ";" +
            "-fx-font-size:10px;" +
            "-fx-cursor:hand;");
        return b;
    }

    private Label lbl(String t, String style) {
        Label l = new Label(t);
        l.getStyleClass().add(style);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private Label emptyLbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:#aaa;" +
            "-fx-font-size:12px;" +
            "-fx-padding:10px;");
        return l;
    }
}