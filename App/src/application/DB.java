package application;

import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {

    private static Connection conn;

    // ════════════════════════════════════════════
    //  CONNECTION
    // ════════════════════════════════════════════
    static Connection connect() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName(
                    "com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" +
                    "sims_db", "root", "640905");
            }
        } catch (Exception e) {
            System.out.println(
                "Connect error: " + e.getMessage());
        }
        return conn;
    }

    // ════════════════════════════════════════════
    //  CORE HELPERS
    // ════════════════════════════════════════════

    // Hash password using SHA-256
    static String hash(String p) {
        try {
            var md = MessageDigest
                .getInstance("SHA-256");
            var bytes = md.digest(
                p.getBytes("UTF-8"));
            var sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(
                    String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return p;
        }
    }

    // Set parameters safely for any type
    private static void setParams(
            PreparedStatement s,
            Object[] p) throws SQLException {
        for (int i = 0; i < p.length; i++) {
            if (p[i] == null)
                s.setNull(i+1,
                    Types.VARCHAR);
            else if (p[i] instanceof Integer)
                s.setInt(i+1, (Integer) p[i]);
            else if (p[i] instanceof Boolean)
                s.setInt(i+1,
                    (Boolean) p[i] ? 1 : 0);
            else
                s.setString(i+1,
                    p[i].toString());
        }
    }

    // Run SELECT — returns ResultSet
    private static ResultSet query(
            String sql, Object... p) {
        try {
            var s = connect()
                .prepareStatement(sql);
            setParams(s, p);
            return s.executeQuery();
        } catch (Exception e) {
            System.out.println(
                "Query error: " + e.getMessage());
            return null;
        }
    }

    // Run INSERT / UPDATE / DELETE
    private static boolean exec(
            String sql, Object... p) {
        try {
            var s = connect()
                .prepareStatement(sql);
            setParams(s, p);
            s.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(
                "Exec error: " + e.getMessage());
            return false;
        }
    }

    // Get single int result from COUNT query
    private static int count(
            String sql, Object... p) {
        try {
            var rs = query(sql, p);
            if (rs != null && rs.next())
                return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }

    // Null-safe getString
    private static String str(
            ResultSet rs, String col) {
        try {
            String v = rs.getString(col);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    // ════════════════════════════════════════════
    //  USERS
    // ════════════════════════════════════════════

    public static String login(
            String user, String pass) {
        try {
            var rs = query(
                "SELECT role FROM users " +
                "WHERE username=? " +
                "AND password=?",
                user, hash(pass));
            if (rs != null && rs.next()) {
                exec(
                    "UPDATE users SET " +
                    "last_login=NOW() " +
                    "WHERE username=?", user);
                return rs.getString("role");
            }
        } catch (Exception e) {
            System.out.println(
                "Login error: " + e.getMessage());
        }
        return null;
    }

    public static boolean register(
            String name, String email,
            String user, String pass,
            String role, String extra) {
        return exec(
            "INSERT INTO users(full_name," +
            "email,username,password," +
            "role,extra_field) " +
            "VALUES(?,?,?,?,?,?)",
            name, email, user,
            hash(pass), role, extra);
    }

    public static boolean userExists(
            String user) {
        return count(
            "SELECT COUNT(*) FROM users " +
            "WHERE username=?", user) > 0;
    }

    public static int countByRole(String role) {
        return count(
            "SELECT COUNT(*) FROM users " +
            "WHERE role=?", role);
    }

    // [username, fullName, email, extra]
    public static List<String[]> getStudents() {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT username,full_name," +
                "email,extra_field FROM users " +
                "WHERE role='STUDENT' " +
                "ORDER BY full_name");
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"username"),
                    str(rs,"full_name"),
                    str(rs,"email"),
                    str(rs,"extra_field")
                });
        } catch (Exception e) {
            System.out.println(
                "getStudents: " + e.getMessage());
        }
        return list;
    }

    // [user,name,email,role,extra,lastLogin]
    public static List<String[]> getAllUsers() {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT username,full_name," +
                "email,role,extra_field," +
                "last_login FROM users " +
                "WHERE role!='ADMIN' " +
                "ORDER BY role,full_name");
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"username"),
                    str(rs,"full_name"),
                    str(rs,"email"),
                    str(rs,"role"),
                    str(rs,"extra_field"),
                    str(rs,"last_login")
                });
        } catch (Exception e) {
            System.out.println(
                "getAllUsers: " + e.getMessage());
        }
        return list;
    }

    public static boolean deleteUser(
            String user) {
        return exec(
            "DELETE FROM users " +
            "WHERE username=? " +
            "AND role!='ADMIN'", user);
    }

    public static boolean resetPassword(
            String user, String pass) {
        return exec(
            "UPDATE users SET password=? " +
            "WHERE username=?",
            hash(pass), user);
    }

    // ════════════════════════════════════════════
    //  SUBJECTS
    // ════════════════════════════════════════════

    // [id, name]
    public static List<String[]> getSubjects() {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT id,name FROM subjects " +
                "ORDER BY name");
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"id"),
                    str(rs,"name")
                });
        } catch (Exception e) {
            System.out.println(
                "getSubjects: " + e.getMessage());
        }
        return list;
    }

    public static List<String> subjectNames() {
        var list = new ArrayList<String>();
        for (var s : getSubjects())
            list.add(s[1]);
        return list;
    }

    public static boolean addSubject(
            String name) {
        return exec(
            "INSERT INTO subjects(name) " +
            "VALUES(?)", name);
    }

    public static boolean deleteSubject(int id) {
        return exec(
            "DELETE FROM subjects WHERE id=?",
            Integer.valueOf(id));
    }

    // ════════════════════════════════════════════
    //  NOTICES
    // ════════════════════════════════════════════

    // [id,title,body,teacher,subject,date]
    public static List<String[]> getNotices() {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT id,title,body," +
                "teacher,subject,created_at " +
                "FROM notices " +
                "ORDER BY created_at DESC");
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"id"),
                    str(rs,"title"),
                    str(rs,"body"),
                    str(rs,"teacher"),
                    str(rs,"subject"),
                    str(rs,"created_at")
                });
        } catch (Exception e) {
            System.out.println(
                "getNotices: " + e.getMessage());
        }
        return list;
    }

    public static boolean addNotice(
            String title, String body,
            String teacher, String subject) {
        return exec(
            "INSERT INTO notices" +
            "(title,body,teacher,subject) " +
            "VALUES(?,?,?,?)",
            title, body, teacher, subject);
    }

    public static boolean deleteNotice(int id) {
        return exec(
            "DELETE FROM notices WHERE id=?",
            Integer.valueOf(id));
    }

    // ════════════════════════════════════════════
    //  ATTENDANCE
    // ════════════════════════════════════════════

    // [subject,teacher,total,present,absent,%]
    public static List<String[]> getAttendance(
            String sid) {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT subject,teacher," +
                "SUM(total) AS total," +
                "SUM(present) AS present " +
                "FROM attendance " +
                "WHERE student_id=? " +
                "GROUP BY subject,teacher",
                sid);
            while (rs != null && rs.next()) {
                int t = rs.getInt("total");
                int p = rs.getInt("present");
                int pct = t==0?0:(p*100)/t;
                list.add(new String[]{
                    str(rs,"subject"),
                    str(rs,"teacher"),
                    String.valueOf(t),
                    String.valueOf(p),
                    String.valueOf(t-p),
                    pct+"%"
                });
            }
        } catch (Exception e) {
            System.out.println(
                "getAttendance: " +
                e.getMessage());
        }
        return list;
    }

    public static int getOverallAtt(String sid) {
        var list = getAttendance(sid);
        if (list.isEmpty()) return 0;
        int t = 0, p = 0;
        for (var r : list) {
            t += Integer.parseInt(r[2]);
            p += Integer.parseInt(r[3]);
        }
        return t==0 ? 0 : (p*100)/t;
    }

    // Check if attendance already marked
    // for a subject on a date
    public static boolean alreadyMarked(
            String subject, String date) {
        return count(
            "SELECT COUNT(*) FROM attendance " +
            "WHERE subject=? AND att_date=?",
            subject, date) > 0;
    }

    // Mark attendance for one student
    public static boolean markAttendance(
            String sid, String subject,
            String teacher, boolean present,
            String date) {
        try {
            // 1 — Block same student+subject+date
            int already = count(
                "SELECT COUNT(*) FROM attendance " +
                "WHERE student_id=? " +
                "AND subject=? AND att_date=?",
                sid, subject, date);
            if (already > 0) return false;

            // 2 — Check if summary row exists
            var rs = query(
                "SELECT id,total,present " +
                "FROM attendance " +
                "WHERE student_id=? " +
                "AND subject=? " +
                "AND att_date != ?",
                sid, subject, date);

            if (rs != null && rs.next()) {
                // Update existing summary row
                int rowId = rs.getInt("id");
                int t = rs.getInt("total") + 1;
                int p = rs.getInt("present")
                    + (present ? 1 : 0);
                return exec(
                    "UPDATE attendance " +
                    "SET total=?,present=?," +
                    "teacher=?,att_date=? " +
                    "WHERE id=?",
                    Integer.valueOf(t),
                    Integer.valueOf(p),
                    teacher,
                    date,
                    Integer.valueOf(rowId));
            } else {
                // Insert brand new row
                return exec(
                    "INSERT INTO attendance" +
                    "(student_id,subject," +
                    "teacher,total,present," +
                    "att_date) " +
                    "VALUES(?,?,?,?,?,?)",
                    sid,
                    subject,
                    teacher,
                    Integer.valueOf(1),
                    Integer.valueOf(present?1:0),
                    date);
            }
        } catch (Exception e) {
            System.out.println(
                "markAttendance error: "
                + e.getMessage());
            return false;
        }
    }

    // Check if student is on approved leave
    public static boolean onLeave(
            String sid, String date) {
        return count(
            "SELECT COUNT(*) FROM leave_requests " +
            "WHERE student_id=? " +
            "AND status='APPROVED' " +
            "AND from_date<=? AND to_date>=?",
            sid, date, date) > 0;
    }

    // [name,username,total,present,absent,%]
    public static List<String[]>
            getAllAttendance() {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT u.full_name," +
                "u.username," +
                "COALESCE(SUM(a.total),0) t," +
                "COALESCE(SUM(a.present),0) p " +
                "FROM users u " +
                "LEFT JOIN attendance a " +
                "ON u.username=a.student_id " +
                "WHERE u.role='STUDENT' " +
                "GROUP BY u.username," +
                "u.full_name " +
                "ORDER BY u.full_name");
            while (rs != null && rs.next()) {
                int t = rs.getInt("t");
                int p = rs.getInt("p");
                int pct = t==0?0:(p*100)/t;
                list.add(new String[]{
                    str(rs,"full_name"),
                    str(rs,"username"),
                    String.valueOf(t),
                    String.valueOf(p),
                    String.valueOf(t-p),
                    pct+"%"
                });
            }
        } catch (Exception e) {
            System.out.println(
                "getAllAttendance: "
                + e.getMessage());
        }
        return list;
    }

    // ════════════════════════════════════════════
    //  LEAVE
    // ════════════════════════════════════════════

    // [id,type,from,to,reason,status,note]
    public static List<String[]> getLeave(
            String sid) {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT id,leave_type," +
                "from_date,to_date,reason," +
                "status,admin_note " +
                "FROM leave_requests " +
                "WHERE student_id=? " +
                "ORDER BY created_at DESC",
                sid);
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"id"),
                    str(rs,"leave_type"),
                    str(rs,"from_date"),
                    str(rs,"to_date"),
                    str(rs,"reason"),
                    str(rs,"status"),
                    str(rs,"admin_note")
                });
        } catch (Exception e) {
            System.out.println(
                "getLeave: " + e.getMessage());
        }
        return list;
    }

    // [id,student,type,from,to,reason,status,note]
    public static List<String[]> getAllLeave() {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT id,student_id," +
                "leave_type,from_date,to_date," +
                "reason,status,admin_note " +
                "FROM leave_requests " +
                "ORDER BY created_at DESC");
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"id"),
                    str(rs,"student_id"),
                    str(rs,"leave_type"),
                    str(rs,"from_date"),
                    str(rs,"to_date"),
                    str(rs,"reason"),
                    str(rs,"status"),
                    str(rs,"admin_note")
                });
        } catch (Exception e) {
            System.out.println(
                "getAllLeave: " + e.getMessage());
        }
        return list;
    }

    public static boolean applyLeave(
            String sid, String type,
            String from, String to,
            String reason) {
        return exec(
            "INSERT INTO leave_requests" +
            "(student_id,leave_type," +
            "from_date,to_date,reason) " +
            "VALUES(?,?,?,?,?)",
            sid, type, from, to, reason);
    }

    public static boolean updateLeave(
            int id, String status, String note) {
        return exec(
            "UPDATE leave_requests SET " +
            "status=?,admin_note=? WHERE id=?",
            status, note, Integer.valueOf(id));
    }

    public static int countPendingLeave() {
        return count(
            "SELECT COUNT(*) FROM leave_requests " +
            "WHERE status='PENDING'");
    }

    // ════════════════════════════════════════════
    //  GRADES
    // ════════════════════════════════════════════

    public static String calcGrade(int m) {
        if (m >= 90) return "A+";
        if (m >= 80) return "A";
        if (m >= 70) return "B+";
        if (m >= 60) return "B";
        if (m >= 50) return "C+";
        if (m >= 40) return "C";
        if (m >= 30) return "D";
        return "F";
    }

    // [id,subject,examType,marks,grade]
    public static List<String[]> getGrades(
            String sid) {
        var list = new ArrayList<String[]>();
        try {
            var rs = query(
                "SELECT id,subject,exam_type," +
                "marks,grade FROM grades " +
                "WHERE student_id=? " +
                "ORDER BY subject",
                sid);
            while (rs != null && rs.next())
                list.add(new String[]{
                    str(rs,"id"),
                    str(rs,"subject"),
                    str(rs,"exam_type"),
                    str(rs,"marks"),
                    str(rs,"grade")
                });
        } catch (Exception e) {
            System.out.println(
                "getGrades: " + e.getMessage());
        }
        return list;
    }

    public static boolean addGrade(
            String sid, String subject,
            String exam, int marks) {
        return exec(
            "INSERT INTO grades(student_id," +
            "subject,exam_type,marks,grade) " +
            "VALUES(?,?,?,?,?)",
            sid, subject, exam,
            Integer.valueOf(marks),
            calcGrade(marks));
    }

    public static boolean deleteGrade(int id) {
        return exec(
            "DELETE FROM grades WHERE id=?",
            Integer.valueOf(id));
    }
}