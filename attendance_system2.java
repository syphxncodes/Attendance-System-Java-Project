import com.formdev.flatlaf.*;
import net.miginfocom.swing.MigLayout;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

public class attendance_system2 {
    static int countdown = 15; 
    //Geofencing- for setting a boundary around the classroom.
    static class Geofencing {
        private final double targetLat; 
        private final double targetLon;
        private final double threshold; 
        public Geofencing(double targetLat, double targetLon, double threshold) {
            this.targetLat = targetLat;
            this.targetLon = targetLon;
            this.threshold = threshold;
        }

        public boolean isWithinGeofence(double userLat, double userLon) {
            double earthRadius = 6371000; 
            double dLat = Math.toRadians(userLat - targetLat);
            double dLon = Math.toRadians(userLon - targetLon);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(targetLat)) * Math.cos(Math.toRadians(userLat))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = earthRadius * c;

            return distance <= threshold;
        }
    }
    static final String API_KEY = "AIzaSyA8kdv6Ul5t16JrPJsUwy7qRda8lQKe5wA"; //final because we should'nt be able to change the APIKEY
    static final String DB_URL = "jdbc:mysql://localhost:3306/attendance_system";//final because we should'nt be able to change the DB_URL
    static final String DB_USER = "attendance_user2";
    static final String DB_PASSWORD = "password12345";
    static String generatedCode = "";
    
    public static void main(String[] args) {
        // Set FlatLaf Look and Feel and Roboto Font
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            FlatRobotoFont.install();
            UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
            
            // Custom UI Defaults
            UIManager.put("Button.borderColor", new Color(100, 100, 100));
            UIManager.put("Button.disabledText", new Color(128, 128, 128));
            UIManager.put("Button.gradient", null);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame mainFrame = new JFrame("Attendance System");
        mainFrame.setSize(800, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,250:280"));

        JLabel lbTitle = new JLabel("Welcome back!");
        JLabel description = new JLabel("Please sign in to access your account");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, 
            "font:bold +10;" +
            "foreground:#ffffff;"
        );
        description.putClientProperty(FlatClientProperties.STYLE, 
            "foreground:#a0a0a0;"
        );

        JButton studentButton = new JButton("Students");
        JButton teacherButton = new JButton("Teachers");

        studentButton.putClientProperty(FlatClientProperties.STYLE,
            "border:10,10,10,10;" +
            "foreground:#ffffff;" +
            "background:#3498db;" +  // Soft blue
            "font:medium;" +
            "selectedBackground:#2980b9;" + // Darker blue when selected
            "hoverBackground:#2980b9;");  // Hover effect 
 
        studentButton.setOpaque(true);
        studentButton.setFocusPainted(false);
        studentButton.setPreferredSize(new Dimension(250, 60)); 
        studentButton.setMargin(new Insets(10, 20, 10, 20)); 

        teacherButton.putClientProperty(FlatClientProperties.STYLE,
            "border:10,10,10,10;" +
            "foreground:#ffffff;" +
            "background:#e74c3c;" + // Vibrant red
            "font:medium;" +
            "selectedBackground:#c0392b;" + // Darker red when selected
            "hoverBackground:#c0392b;"); 
        teacherButton.setOpaque(true);
        teacherButton.setFocusPainted(false);
        teacherButton.setPreferredSize(new Dimension(250, 60)); 
        teacherButton.setMargin(new Insets(10, 20, 10, 20)); 

        studentButton.addActionListener(e -> showLoginSignupWindow("Student"));
        teacherButton.addActionListener(e -> showLoginSignupWindow("Teacher"));

        mainFrame.add(lbTitle, "growx");
        mainFrame.add(description, "growx");
        mainFrame.add(studentButton, "growx");
        mainFrame.add(teacherButton, "growx");
        mainFrame.setVisible(true);
    }

    static void showLoginSignupWindow(String userType) {
        JFrame loginFrame = new JFrame(userType + " Login/Signup");
        loginFrame.setSize(400, 300);
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginFrame.setLayout(new MigLayout("wrap 2", "[grow, left][grow, fill]"));

        JLabel welcomeLabel = new JLabel("Welcome to " + userType + " Portal");
        welcomeLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        welcomeLabel.putClientProperty(FlatClientProperties.STYLE, 
            "foreground:#ffffff;"
        );

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        usernameField.putClientProperty(FlatClientProperties.STYLE, 
            "foreground:#a0a0a0;" +
            "background:#3a3a3a;"
        );
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Password");
        passwordField.putClientProperty(FlatClientProperties.STYLE, 
            "showRevealButton:true;" +
            "foreground:#a0a0a0;" +
            "background:#3a3a3a;"
        );

        JButton loginButton = new JButton("Login");
        loginButton.putClientProperty(FlatClientProperties.STYLE, 
            "background:#27ae60;" + // Green submit button
            "foreground:#ffffff;" +
            "hoverBackground:#2ecc71;" +
            "borderWidth:0;"
        );

        JButton signupButton = new JButton("New User? Press here to Sign Up");
        signupButton.setFocusPainted(false);
        signupButton.setBorderPainted(false);
        signupButton.setContentAreaFilled(false);
        signupButton.putClientProperty(FlatClientProperties.STYLE, 
            "foreground:#3498db;" + // Blue text
            "hoverForeground:#2980b9;"
        );
        signupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginFrame.add(welcomeLabel, "span, center");
        loginFrame.add(new JLabel("Username:"),"gapy 3");
        loginFrame.add(usernameField, "gapy 3");
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(passwordField,"growx");
        loginFrame.add(loginButton,  "span, center");
        loginFrame.add(signupButton,"growx");   

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            if (authenticateUser(userType, username, password)) {
                loginFrame.dispose();
                if (userType.equals("Teacher")) {
                    showTeacherWindow();
                } else {
                    showStudentWindow(username);
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid Credentials!");
            }
        });

        signupButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            if (registerUser(userType, username, password)) {
                JOptionPane.showMessageDialog(loginFrame, "Signup Successful! Please Login.");
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Signup Failed! User may already exist.");
            }
        });

        loginFrame.setVisible(true);
    }

    static boolean authenticateUser(String userType, String username, String password) {
        String table = userType.equals("Teacher") ? "teachers" : "students";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + table + " WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean registerUser(String userType, String username, String password) {
        String table = userType.equals("Teacher") ? "teachers" : "students";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + table + " (username, password) VALUES (?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static void showTeacherWindow() {
        JFrame teacherFrame = new JFrame("Teacher Portal");
        teacherFrame.setSize(400, 200);
        teacherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        teacherFrame.setLayout(new MigLayout("wrap 1", "[grow, center]"));

        JButton generateCodeButton = new JButton("Generate Code");
        generateCodeButton.putClientProperty(FlatClientProperties.STYLE, 
            "background:#3498db;" +
            "foreground:#ffffff;" +
            "hoverBackground:#2980b9;" +
            "borderWidth:0;"
        );

        JLabel codeLabel = new JLabel("Code: N/A");
        JLabel timerLabel = new JLabel("Timer: " + countdown + "s"); 

        generateCodeButton.setPreferredSize(new Dimension(200, 50));
        generateCodeButton.setFont(new Font("Roboto", Font.PLAIN, 16));
        codeLabel.setFont(new Font("Roboto", Font.PLAIN, 24));
        timerLabel.setFont(new Font("Roboto", Font.PLAIN, 17));
        
        Timer timer = new Timer();
        //Anonymous class concept used.
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (countdown > 0) {
                    countdown--; 
                    timerLabel.setText("Time left: " + countdown + "s");
                } else {
                    generatedCode = String.valueOf((int) (Math.random() * 9000) + 1000);
                    codeLabel.setText("Code: " + generatedCode);
                    countdown = 15; 
                }
            }
        };

        generateCodeButton.addActionListener(e -> {
            generatedCode = String.valueOf((int) (Math.random() * 9000) + 1000);
            codeLabel.setText("Code: " + generatedCode);
            countdown = 15; 
        });

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
        teacherFrame.add(codeLabel, "growx");
        teacherFrame.add(timerLabel, "growx");
        teacherFrame.add(generateCodeButton, "growx");
        teacherFrame.setVisible(true);
    }

    static void showStudentWindow(String username) {
        JFrame studentFrame = new JFrame("Student Portal");
        studentFrame.setSize(400, 300);
        studentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        studentFrame.setLayout(new MigLayout("wrap 2", "[center][center]"));
    
        // Call the showLocationPopup method
        boolean isLocationAccessGranted = showLocationPopup(studentFrame);
    
        if (!isLocationAccessGranted) {
            JOptionPane.showMessageDialog(studentFrame, 
                    "Location access denied. Redirecting to home page.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            studentFrame.dispose();
            return; // Redirect to login
        }
    
        // Attendance submission UI
        JTextField codeField = new JTextField(10);
        JButton submitButton = new JButton("Submit Code");
        submitButton.putClientProperty(FlatClientProperties.STYLE, 
            "background:#27ae60;" +
            "foreground:#ffffff;" +
            "hoverBackground:#2ecc71;" +
            "borderWidth:0;"
        );
    
        submitButton.addActionListener(e -> {
            String enteredCode = codeField.getText();
            if (enteredCode.equals(generatedCode)) {
                double[] location = getCurrentLocation();
                if (location != null) {
                    Geofencing geofence = new Geofencing(25.3098336, 55.27792, 100);
                    if (geofence.isWithinGeofence(location[0], location[1])) {
                        updateAttendance(username);  // Username is passed.
                        JOptionPane.showMessageDialog(studentFrame, "Attendance Submitted!");
                    } else {
                        JOptionPane.showMessageDialog(studentFrame, "Out of Range!");
                    }
                } else {
                    JOptionPane.showMessageDialog(studentFrame, "Unable to fetch location!");
                }
            } else {
                JOptionPane.showMessageDialog(studentFrame, "Invalid Attendance Code!");
            }
        });
    
        studentFrame.add(new JLabel("Enter Code:"));
        studentFrame.add(codeField);
        studentFrame.add(submitButton);
        studentFrame.setVisible(true);
    }
    
    
    // Location Popup
    static boolean showLocationPopup(JFrame parentFrame) {
        int result = JOptionPane.showConfirmDialog(
                parentFrame,
                "This application requires location access to mark attendance. Allow access?",
                "Location Access Required",
                JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }
     

    static double[] getCurrentLocation() {
        try {
            URL url = new URL("https://www.googleapis.com/geolocation/v1/geolocate?key=" + API_KEY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write("{}".getBytes("utf-8"));
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject location = jsonResponse.getJSONObject("location");
                return new double[]{location.getDouble("lat"), location.getDouble("lng")};
            } else {
                System.out.println("Failed to get location. HTTP response code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void updateAttendance(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String updateQuery = "UPDATE students SET attendance = TRUE WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}