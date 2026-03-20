package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherScreen extends BaseScreen {

    public TeacherScreen(String username) {
        super(username);
    }

    @Override protected String roleName() {
        return "Teacher";
    }

    @Override protected String[][] navItems() {
        return new String[][]{
            {"🏠","Home","home"},
            {"📋","Attendance","att"},
            {"📢","Notices","notices"},
            {"👥","Students","students"},
            {"🎓","Grades","grades"},
            {"---","ACCOUNT",""},
            {"👤","Profile","profile"}
        };
    }

    @Override
    protected Node pageFor(String key) {
        return switch (key) {
            case "att"      -> attPage();
            case "notices"  -> noticesPage();
            case "students" -> studentsPage();
            case "grades"   -> gradesPage();
            case "profile"  -> profilePage();
            default         -> homePage();
        };
    }

    // ════════════════════════════════════════════
    //  HOME
    // ════════════════════════════════════════════
    @Override
    protected VBox homePage() {
        VBox page = page("Dashboard");
        var notices = DB.getNotices();

        page.getChildren().add(statsRow(
            statCard("Students",
                String.valueOf(
                    DB.countByRole("STUDENT")),
                true),
            statCard("Notices",
                String.valueOf(notices.size()),
                false),
            statCard("Subjects",
                String.valueOf(
                    DB.subjectNames().size()),
                false)));

        VBox rows = new VBox(0);
        notices.stream().limit(4).forEach(n ->
            rows.getChildren().add(
                noticeItem(n[1],
                    n[3] + " · " + n[4])));
        if (rows.getChildren().isEmpty())
            rows.getChildren().add(
                emptyLbl("No notices yet."));

        page.getChildren().add(
            body(card("Recent Notices", rows)));
        return page;
    }

    // ════════════════════════════════════════════
    //  ATTENDANCE
    // ════════════════════════════════════════════
    private VBox attPage() {
        VBox page = page("Take Attendance");

        var allStudents = DB.getStudents();
        final List<String[]>[] shown = new List[]{
            new ArrayList<>(allStudents)};
        final ToggleGroup[][] groups =
            new ToggleGroup[1][];
        final String[][] status =
            new String[1][];

        ComboBox<String> subBox = combo(
            DB.subjectNames(),
            "Select subject", 200);
        DatePicker datePicker = new DatePicker(
            java.time.LocalDate.now());
        datePicker.getStyleClass().add("field");
        datePicker.setPrefWidth(160);
        TextField search = field("Search...");
        search.setPrefWidth(200);

        Label warn = new Label("");
        warn.setStyle("-fx-text-fill:#ef4444;" +
            "-fx-font-size:11px;" +
            "-fx-font-weight:bold;");

        VBox tableBox = new VBox();
        Runnable[] build = {null};
        build[0] = () -> {
            tableBox.getChildren().clear();
            warn.setText("");
            String subj = subBox.getValue();
            String date = datePicker.getValue()
                == null ? ""
                : datePicker.getValue().toString();

            if (subj != null && !date.isEmpty()
                    && DB.alreadyMarked(subj,date))
                warn.setText("⚠ Already marked "
                    + "for " + subj
                    + " on " + date);

            if (shown[0].isEmpty()) {
                tableBox.getChildren().add(
                    emptyLbl("No students."));
                groups[0] = new ToggleGroup[0];
                status[0] = new String[0];
                return;
            }

            GridPane g = attGrid();
            groups[0] =
                new ToggleGroup[shown[0].size()];
            status[0] =
                new String[shown[0].size()];

            for (int i=0;i<shown[0].size();i++) {
                var s = shown[0].get(i);
                String st = i%2==0?"td":"td-alt";
                boolean leave = !date.isEmpty()
                    && DB.onLeave(s[0], date);
                status[0][i] =
                    leave ? "LEAVE" : "OK";

                g.add(lbl(String.valueOf(i+1),
                    st), 0, i+1);
                g.add(lbl(s[1], st), 1, i+1);

                if (leave) {
                    g.add(leaveBadgeCell(st),
                        2, i+1);
                    g.add(emptyCell(st), 3, i+1);
                    g.add(emptyCell(st), 4, i+1);
                    groups[0][i] = null;
                } else {
                    g.add(lbl("—", st), 2, i+1);
                    groups[0][i] =
                        new ToggleGroup();
                    g.add(radioCell("P",
                        groups[0][i], true, st),
                        3, i+1);
                    g.add(radioCell("A",
                        groups[0][i], false, st),
                        4, i+1);
                }
            }
            tableBox.getChildren().add(g);
        };
        build[0].run();

        subBox.setOnAction(e -> build[0].run());
        datePicker.setOnAction(e -> build[0].run());
        search.textProperty().addListener(
            (obs,o,n) -> {
                String q = n.trim().toLowerCase();
                shown[0] = q.isEmpty()
                    ? new ArrayList<>(allStudents)
                    : allStudents.stream()
                        .filter(s ->
                            s[1].toLowerCase()
                                .contains(q)
                            || s[0].toLowerCase()
                                .contains(q))
                        .collect(
                            Collectors.toList());
                build[0].run();
            });

        Button submit = btn(
            "Save Attendance","btn-primary");
        submit.setOnAction(e -> {
            String subj = subBox.getValue();
            String date = datePicker.getValue()
                == null ? ""
                : datePicker.getValue().toString();
            if (subj==null||date.isEmpty()) {
                alert("Error",
                    "Select subject and date.");
                return;
            }
            if (DB.alreadyMarked(subj, date)) {
                alert("Error",
                    "Already marked for "
                    + subj + " on " + date);
                return;
            }
            int saved = 0;
            for (int i=0;
                    i<shown[0].size();i++) {
                if ("LEAVE".equals(status[0][i])
                        || groups[0][i]==null)
                    continue;
                RadioButton sel = (RadioButton)
                    groups[0][i]
                        .getSelectedToggle();
                boolean present = sel != null
                    && sel.getText().equals("P");
                boolean result = DB.markAttendance(
                	    shown[0].get(i)[0],
                	    subj, username,
                	    present, date);
                	System.out.println("Mark "
                	    + shown[0].get(i)[0]
                	    + " = " + result);
                	if (result) saved++;
            }
            alert("Success",
                saved + " records saved!");
        });

        VBox content = body(
        	    card("Mark Attendance",
        	        new HBox(12,
        	            labeled("Subject", subBox),
        	            labeled("Date", datePicker),
        	            labeled("Search", search)),
        	        warn, tableBox, submit));
        	page.getChildren().add(scrollWrap(content));
        	return page;
    }

    // ════════════════════════════════════════════
    //  NOTICES
    // ════════════════════════════════════════════
    private VBox noticesPage() {
        VBox page = page("Notices");
        TextField titleF = field("Title");
        TextField subjF  = field("Subject");
        TextArea bodyA = textArea("Notice body...");

        Button send = btn("Send","btn-primary");
        VBox body = bodyBox();

        send.setOnAction(e -> {
            String t = titleF.getText().trim();
            String s = subjF.getText().trim();
            String b = bodyA.getText().trim();
            if (t.isEmpty()||s.isEmpty()
                    ||b.isEmpty()) {
                alert("Error","All fields required.");
                return;
            }
            if (DB.addNotice(t,b,username,s)) {
                alert("Success","Notice sent!");
                titleF.clear();
                subjF.clear();
                bodyA.clear();
                if (body.getChildren().size() >= 2)
                    body.getChildren().set(1,
                        noticeList(true));
            } else {
                alert("Error","Failed.");
            }
        });

        body.getChildren().addAll(
            card("Post Notice",
                new HBox(12,
                    labeled("Title",titleF),
                    labeled("Subject",subjF)),
                labeled("Body",bodyA), send),
            noticeList(true));
        page.getChildren().add(scrollWrap(body));
        return page;
    }

    private VBox noticeList(boolean canDelete) {
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
                n[3]+" · "+n[4]+" · "+n[5]);
            m.getStyleClass().add("notice-meta");
            VBox info = new VBox(2, t, m);
            HBox.setHgrow(info, Priority.ALWAYS);
            HBox row = new HBox(info);
            if (canDelete) {
                Button del = new Button("✕");
                del.setStyle(
                    "-fx-background-color:" +
                    "transparent;" +
                    "-fx-text-fill:#ef4444;" +
                    "-fx-cursor:hand;");
                del.setOnAction(e -> {
                    if (confirm("Delete notice?"))
                        DB.deleteNotice(id);
                });
                row.getChildren().add(del);
            }
            row.getStyleClass().add("notice-item");
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }
        return card("All Notices", rows);
    }

    // ════════════════════════════════════════════
    //  STUDENTS
    // ════════════════════════════════════════════
    private VBox studentsPage() {
        VBox page = page("Students");
        var all   = DB.getStudents();
        var shown = new ArrayList<>(all);
        TextField search = field("Search...");
        search.setPrefWidth(260);

        VBox tableBox = new VBox();
        Runnable[] build = {null};
        build[0] = () -> {
            tableBox.getChildren().clear();
            if (shown.isEmpty()) {
                tableBox.getChildren().add(
                    emptyLbl("No students."));
                return;
            }
            List<String[]> rows =
                new ArrayList<>();
            int i = 1;
            for (var s : shown)
                rows.add(new String[]{
                    String.valueOf(i++),
                    s[1], s[0], s[2],
                    s[3].isEmpty()?"—":s[3]});
            tableBox.getChildren().add(
                table(new String[]{
                    "#","Name","Username",
                    "Email","ID"},
                    rows, -1));
        };
        build[0].run();

        search.textProperty().addListener(
            (obs,o,n) -> {
                String q = n.trim().toLowerCase();
                shown.clear();
                if (q.isEmpty())
                    shown.addAll(all);
                else
                    all.stream()
                        .filter(s ->
                            s[1].toLowerCase()
                                .contains(q)
                            || s[0].toLowerCase()
                                .contains(q))
                        .forEach(shown::add);
                build[0].run();
            });

        page.getChildren().add(body(
            card("Student List",
                labeled("Search", search),
                tableBox)));
        return page;
    }

    // ════════════════════════════════════════════
    //  GRADES
    // ════════════════════════════════════════════
    private VBox gradesPage() {
        VBox page = page("Manage Grades");

        ComboBox<String> studentBox =
            new ComboBox<>();
        DB.getStudents().forEach(s ->
            studentBox.getItems().add(
                s[1] + " (" + s[0] + ")"));
        studentBox.setPromptText(
            "Select student...");
        studentBox.getStyleClass().add("field");
        studentBox.setPrefWidth(220);

        ComboBox<String> subjectBox = combo(
            DB.subjectNames(), "Subject", 180);
        ComboBox<String> examBox = combo(
            List.of("Unit Test","Mid-term",
                "Final","Assignment"),
            "Exam type", 160);

        TextField marksF = field("Marks 0-100");
        marksF.setPrefWidth(120);

        Label gradePreview = new Label("Grade: —");
        gradePreview.setStyle(
            "-fx-font-size:13px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#4f46e5;");
        marksF.textProperty().addListener(
            (obs,o,n) -> {
                try {
                    int m = Integer.parseInt(
                        n.trim());
                    gradePreview.setText(
                        m>=0&&m<=100
                            ? "Grade: "
                                +DB.calcGrade(m)
                            : "Grade: —");
                } catch (Exception e) {
                    gradePreview.setText("Grade: —");
                }
            });

        VBox tableBox = new VBox();
        Runnable[] refresh = {null};
        refresh[0] = () -> {
            tableBox.getChildren().clear();
            if (studentBox.getValue() == null) {
                tableBox.getChildren().add(
                    emptyLbl(
                        "Select a student."));
                return;
            }
            String sel = studentBox.getValue();
            String sid = sel.substring(
                sel.lastIndexOf("(")+1,
                sel.lastIndexOf(")"));
            var grades = DB.getGrades(sid);
            if (grades.isEmpty()) {
                tableBox.getChildren().add(
                    emptyLbl("No grades yet."));
                return;
            }
            GridPane g = new GridPane();
            g.getStyleClass().add("table");
            for (var h : new String[]{
                    "Subject","Exam",
                    "Marks","Grade","✕"}) {
                Label hl = new Label(h);
                hl.getStyleClass().add("th");
                hl.setMaxWidth(Double.MAX_VALUE);
                g.add(hl, java.util.Arrays
                    .asList("Subject","Exam",
                        "Marks","Grade","✕")
                    .indexOf(h), 0);
            }
            for (int r=0;r<grades.size();r++) {
                var gr = grades.get(r);
                String s = r%2==0?"td":"td-alt";
                int gid = Integer.parseInt(gr[0]);
                g.add(lbl(gr[1],s),0,r+1);
                g.add(lbl(gr[2],s),1,r+1);
                g.add(lbl(gr[3]+"/100",s),2,r+1);
                Label gl = new Label(gr[4]);
                gl.getStyleClass().addAll(s,
                    gr[4].startsWith("A")
                        ?"green"
                        :gr[4].startsWith("B")
                        ?"orange":"red");
                gl.setMaxWidth(Double.MAX_VALUE);
                g.add(gl,3,r+1);
                Button del = new Button("✕");
                del.setStyle(
                    "-fx-background-color:" +
                    "transparent;" +
                    "-fx-text-fill:#ef4444;" +
                    "-fx-cursor:hand;");
                del.setOnAction(e -> {
                    if (confirm("Delete grade?")) {
                        DB.deleteGrade(gid);
                        refresh[0].run();
                    }
                });
                HBox dc = new HBox(del);
                dc.getStyleClass().add(s);
                dc.setPadding(
                    new Insets(4,10,4,10));
                g.add(dc,4,r+1);
            }
            var cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            for (int i=0;i<5;i++)
                g.getColumnConstraints().add(cc);
            tableBox.getChildren().add(g);
        };
        studentBox.setOnAction(
            e -> refresh[0].run());

        Button addBtn = btn("Add","btn-primary");
        addBtn.setOnAction(e -> {
            if (studentBox.getValue()==null
                    ||subjectBox.getValue()==null
                    ||examBox.getValue()==null
                    ||marksF.getText().isEmpty()){
                alert("Error","Fill all fields.");
                return;
            }
            try {
                int m = Integer.parseInt(
                    marksF.getText().trim());
                if (m<0||m>100) {
                    alert("Error","0-100 only.");
                    return;
                }
                String sel = studentBox.getValue();
                String sid = sel.substring(
                    sel.lastIndexOf("(")+1,
                    sel.lastIndexOf(")"));
                if (DB.addGrade(sid,
                        subjectBox.getValue(),
                        examBox.getValue(), m)) {
                    alert("Success","Grade added!");
                    subjectBox.setValue(null);
                    examBox.setValue(null);
                    marksF.clear();
                    gradePreview.setText("Grade: —");
                    refresh[0].run();
                }
            } catch (Exception ex) {
                alert("Error","Enter a number.");
            }
        });

        page.getChildren().add(body(
            card("Add Grade",
                new HBox(10,
                    labeled("Student",studentBox),
                    labeled("Subject",subjectBox),
                    labeled("Exam",examBox)),
                new HBox(10,
                    labeled("Marks",marksF),
                    gradePreview, addBtn),
                tableBox)));
        return page;
    }

    // ════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ════════════════════════════════════════════
    private VBox page(String title) {
        VBox p = new VBox(0);
        p.getStyleClass().add("page");
        p.getChildren().add(topBar(title));
        return p;
    }

    private VBox body(Node... nodes) {
        VBox b = new VBox(14, nodes);
        b.setPadding(new Insets(14,20,20,20));
        return b;
    }

    private VBox bodyBox() {
        VBox b = new VBox(14);
        b.setPadding(new Insets(14,20,20,20));
        return b;
    }

    private ComboBox<String> combo(
            List<String> items, String prompt,
            double width) {
        ComboBox<String> c = new ComboBox<>();
        c.getItems().addAll(items);
        c.setPromptText(prompt);
        c.getStyleClass().add("field");
        c.setPrefWidth(width);
        return c;
    }

    private TextArea textArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefHeight(80);
        ta.setWrapText(true);
        ta.getStyleClass().add("field");
        return ta;
    }

    private GridPane attGrid() {
        GridPane g = new GridPane();
        g.getStyleClass().add("table");
        String[] hdrs = {
            "#","Name","Status","P","A"};
        for (int i=0;i<hdrs.length;i++) {
            Label h = new Label(hdrs[i]);
            h.getStyleClass().add("th");
            h.setMaxWidth(Double.MAX_VALUE);
            g.add(h, i, 0);
        }
        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        for (int i=0;i<hdrs.length;i++)
            g.getColumnConstraints().add(cc);
        return g;
    }

    private HBox radioCell(String text,
            ToggleGroup group, boolean selected,
            String style) {
        RadioButton r = new RadioButton(text);
        r.setToggleGroup(group);
        r.setSelected(selected);
        HBox c = new HBox(r);
        c.getStyleClass().add(style);
        c.setPadding(new Insets(8,12,8,12));
        return c;
    }

    private HBox emptyCell(String style) {
        HBox c = new HBox();
        c.getStyleClass().add(style);
        c.setPadding(new Insets(8,12,8,12));
        return c;
    }

    private HBox leaveBadgeCell(String style) {
        Label l = new Label("ON LEAVE");
        l.setStyle(
            "-fx-background-color:#fffbeb;" +
            "-fx-text-fill:#f59e0b;" +
            "-fx-font-size:9px;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:20px;" +
            "-fx-padding:2px 10px;");
        HBox c = new HBox(l);
        c.getStyleClass().add(style);
        c.setAlignment(Pos.CENTER_LEFT);
        c.setPadding(new Insets(8,12,8,12));
        return c;
    }

    private HBox noticeItem(String t, String m) {
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill:#f59e0b;" +
            "-fx-font-size:16px;");
        Label title = new Label(t);
        title.getStyleClass().add("notice-title");
        Label meta = new Label(m);
        meta.getStyleClass().add("notice-meta");
        HBox row = new HBox(10, dot,
            new VBox(2, title, meta));
        row.getStyleClass().add("notice-item");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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