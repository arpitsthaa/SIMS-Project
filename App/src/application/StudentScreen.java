package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.ArrayList;


public class StudentScreen extends BaseScreen {

    public StudentScreen(String username) {
        super(username);
    }

    @Override protected String roleName() {
        return "Student";
    }

    @Override protected String[][] navItems() {
        return new String[][]{
            {"🏠","Home","home"},
            {"📋","Attendance","att"},
            {"📢","Notices","notices"},
            {"📄","Leave","leave"},
            {"🎓","Grades","grades"},
            {"---","ACCOUNT",""},
            {"👤","Profile","profile"}
        };
    }

    @Override protected Node pageFor(String key){
        return switch (key) {
            case "att"     -> attPage();
            case "notices" -> noticesPage();
            case "leave"   -> leavePage();
            case "grades"  -> gradesPage();
            case "profile" -> profilePage();
            default        -> homePage();
        };
    }

    // ── Home ─────────────────────────────────────
    @Override
    protected VBox homePage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Dashboard"));

        var att = DB.getAttendance(username);
        var notices = DB.getNotices();
        int pct = DB.getOverallAtt(username);
        var leave = DB.getLeave(username);

        page.getChildren().add(statsRow(
            statCard("Attendance",
                pct+"%", true),
            statCard("Subjects",
                String.valueOf(att.size()), false),
            statCard("Notices",
                String.valueOf(notices.size()),
                false),
            statCard("Leave Requests",
                String.valueOf(leave.size()), false)
        ));

        // Recent notices
        VBox noticeRows = new VBox(0);
        List<String[]> recent = notices.subList(
            0, Math.min(4, notices.size()));
        if (recent.isEmpty()) {
            noticeRows.getChildren().add(
                new Label("No notices yet.") {{
                    setStyle("-fx-text-fill:#aaa;" +
                        "-fx-padding:10px;");
                }});
        } else {
            for (var n : recent)
                noticeRows.getChildren().add(
                    noticeItem(n[1],
                        n[3]+" · "+n[4]));
        }

        VBox body = new VBox(14,
            card("Recent Notices", noticeRows));
        body.setPadding(
            new Insets(14,20,20,20));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    // ── Attendance ───────────────────────────────
    private VBox attPage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("My Attendance"));

        var att = DB.getAttendance(username);
        int overall = DB.getOverallAtt(username);

        VBox body = new VBox(14);
        body.setPadding(new Insets(14,20,20,20));

        body.getChildren().add(statsRow(
            statCard("Overall",
                overall+"%", true),
            statCard("Subjects",
                String.valueOf(att.size()), false)
        ));

        if (att.isEmpty()) {
            body.getChildren().add(
                card("Attendance",
                    emptyLabel("No records.")));
        } else {
            body.getChildren().add(
                card("Subject-wise Attendance",
                    table(
                        new String[]{"Subject",
                            "Teacher","Total",
                            "Present","Absent",
                            "Att %"},
                        att, 5)));
        }

        page.getChildren().add(scrollWrap(body));
        return page;
    }

    // ── Notices ──────────────────────────────────
    private VBox noticesPage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Notices"));

        var notices = DB.getNotices();
        VBox rows = new VBox(0);
        if (notices.isEmpty()) {
            rows.getChildren().add(
                emptyLabel("No notices yet."));
        } else {
            for (var n : notices)
                rows.getChildren().add(
                    noticeItem(n[1],
                        n[3]+" · "+n[4]
                            +" · "+n[5]));
        }

        VBox body = new VBox(14,
            card("All Notices", rows));
        body.setPadding(new Insets(14,20,20,20));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    // ── Leave ────────────────────────────────────
    private VBox leavePage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(
            topBar("Leave Request"));

        ComboBox<String> typeBox =
            new ComboBox<>();
        typeBox.getItems().addAll(
            "Sick Leave","Personal Leave","Other");
        typeBox.setPromptText("Select type");
        typeBox.getStyleClass().add("field");
        typeBox.setPrefWidth(180);

        DatePicker fromP = new DatePicker();
        fromP.setPromptText("From");
        fromP.getStyleClass().add("field");
        fromP.setPrefWidth(160);

        DatePicker toP = new DatePicker();
        toP.setPromptText("To");
        toP.getStyleClass().add("field");
        toP.setPrefWidth(160);

        TextArea reasonA = new TextArea();
        reasonA.setPromptText("Reason...");
        reasonA.setPrefHeight(70);
        reasonA.setWrapText(true);
        reasonA.getStyleClass().add("field");

        Button submit = btn(
            "Submit","btn-primary");

        VBox body = new VBox(14);
        body.setPadding(new Insets(14,20,20,20));

        submit.setOnAction(e -> {
            String type = typeBox.getValue();
            var from = fromP.getValue();
            var to   = toP.getValue();
            String reason =
                reasonA.getText().trim();

            if (type==null || from==null
                    || to==null
                    || reason.isEmpty()) {
                alert("Error",
                    "All fields required.");
                return;
            }
            if (to.isBefore(from)) {
                alert("Error",
                    "End before start.");
                return;
            }
            if (DB.applyLeave(username, type,
                    from.toString(),
                    to.toString(), reason)) {
                alert("Success","Submitted!");
                typeBox.setValue(null);
                fromP.setValue(null);
                toP.setValue(null);
                reasonA.clear();
                refreshLeave(body);
            } else {
                alert("Error","Failed.");
            }
        });

        VBox form = card("Apply for Leave",
            new HBox(10,
                labeled("Type",typeBox),
                labeled("From",fromP),
                labeled("To",toP)),
            labeled("Reason",reasonA),
            submit);

        body.getChildren().addAll(
            form, buildLeaveHistory());
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    private VBox buildLeaveHistory() {
        var records = DB.getLeave(username);
        if (records.isEmpty())
            return card("My Requests",
                emptyLabel("No requests yet."));

        VBox rows = new VBox(0);
        for (var r : records) {
            Label type = new Label(r[1]);
            type.getStyleClass().add("td");
            Label dates = new Label(
                r[2]+" → "+r[3]);
            dates.getStyleClass().add("td");
            dates.setStyle(
                "-fx-text-fill:#666;" +
                "-fx-font-size:11px;");
            Label badge = statusBadge(r[5]);
            HBox row = new HBox(10,
                type, dates, badge);
            row.getStyleClass().add(
                "notice-item");
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }
        return card("My Requests", rows);
    }

    private void refreshLeave(VBox body) {
        if (body.getChildren().size() >= 2)
            body.getChildren().set(1,
                buildLeaveHistory());
        else
            body.getChildren().add(
                buildLeaveHistory());
    }
    private VBox gradesPage() {
        VBox page = new VBox(0);
        page.getStyleClass().add("page");
        page.getChildren().add(topBar("My Grades"));

        var grades = DB.getGrades(username);

        VBox body = new VBox(14);
        body.setPadding(new Insets(14, 20, 20, 20));

        if (grades.isEmpty()) {
            body.getChildren().add(
            	card("My Grades",
            	 emptyLabel("No grades yet.")));
        } else {
            // Simple summary stats
            int total = grades.size();
            double avg = grades.stream()
                .mapToInt(g ->
                    Integer.parseInt(g[3]))
                .average()
                .orElse(0);

            page.getChildren().add(statsRow(
                statCard("Total Exams",
                    String.valueOf(total), false),
                statCard("Average",
                    String.format("%.1f", avg),
                    true)
            ));

            // Grade table
            List<String[]> rows =
                new ArrayList<>();
            for (var g : grades)
                rows.add(new String[]{
                    g[1], g[2],
                    g[3] + "/100", g[4]
                });

            body.getChildren().add(
                card("Grade Records",
                    table(
                        new String[]{"Subject",
                            "Exam","Marks","Grade"},
                        rows, 3)));
        }

        page.getChildren().add(scrollWrap(body));
        return page;
    }

    // ── Helpers ──────────────────────────────────
    private HBox noticeItem(String title,
                              String meta) {
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill:#4f46e5;" +
            "-fx-font-size:16px;");
        Label t = new Label(title);
        t.getStyleClass().add("notice-title");
        Label m = new Label(meta);
        m.getStyleClass().add("notice-meta");
        HBox row = new HBox(10, dot,
            new VBox(2, t, m));
        row.getStyleClass().add("notice-item");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#aaa;" +
            "-fx-font-size:12px;" +
            "-fx-padding:10px;");
        return l;
    }
}