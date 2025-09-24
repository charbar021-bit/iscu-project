import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LoginWorld manages login and registration with file storage.
 * 
 * It provides input fields for username and password, and buttons to:
 * - Register a new account (stored in a file),
 * - Log in with an existing account,
 * - Return to the main menu.
 * 
 * On successful login, it transitions to SavesWorld.
 * On registration, it appends the credentials to `users.txt` if the username is unique.
 * 
 * File format: Each line in users.txt contains "username:password".
 * 
 * @author Charlie Cruz
 */
public class LoginWorld extends World {
    private TextField userField, passField;
    private Tooltip errorTip;

    /**
     * Constructs the login screen, setting up all input fields and buttons.
     */
    public LoginWorld() {    
        super(640, 480, 1);
        GreenfootImage background = new GreenfootImage("TitlePage.png");
        setBackground(background);

        // Title
        showText("Register an account and login to play.", getWidth() / 2, 120, 26, Color.BLACK);

        // Username field
        userField = new TextField(200, 30, false, "Username...", false);
        addObject(userField, getWidth()/2, getHeight()/2 - 50);

        // Password field
        passField = new TextField(200, 30, true, "Password...", false);
        addObject(passField, getWidth()/2, getHeight()/2);

        // Tooltip for errors and info
        errorTip = new Tooltip();
        addObject(errorTip, getWidth()/2, (getHeight()/2 - 50) - 40);
        errorTip.hide();

        // Login Button: Validates user credentials
        Button login = new Button("Login") {
            @Override
            public void onClick() {
                String user = userField.getText().trim();
                String pass = passField.getText().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    errorTip.showText("Please enter both username and password", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.RED);
                    return;
                }

                errorTip.hide();

                File usersFile = new File("users.txt");
                boolean validUser = false;

                try {
                    if (!usersFile.exists()) usersFile.createNewFile();

                    Scanner scanner = new Scanner(usersFile);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.equals(user + ":" + pass)) {
                            validUser = true;
                            break;
                        }
                    }
                    scanner.close();
                } catch (IOException e) {
                    errorTip.showText("Error reading user file", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.RED);
                    return;
                }

                if (validUser) {
                    Greenfoot.setWorld(new SavesWorld(user));
                } else {
                    errorTip.showText("Invalid username or password", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.RED);
                }
            }
        };
        addObject(login, getWidth()/2 - 80, getHeight()/2 + 60);

        // Register Button: Registers a new user if username isn't taken
        Button register = new Button("Register") {
            @Override
            public void onClick() {
                String user = userField.getText().trim();
                String pass = passField.getText().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    errorTip.showText("Please enter both username and password", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.RED);
                    return;
                }

                errorTip.hide();

                File usersFile = new File("users.txt");

                try {
                    if (!usersFile.exists()) {
                        usersFile.createNewFile();
                    }

                    // Check if username exists
                    Scanner scanner = new Scanner(usersFile);
                    boolean exists = false;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith(user + ":")) {
                            exists = true;
                            break;
                        }
                    }
                    scanner.close();

                    if (exists) {
                        errorTip.showText("Username already taken", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.RED);
                    } else {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile, true));
                        writer.write(user + ":" + pass);
                        writer.newLine();
                        writer.close();

                        errorTip.showText("Registered \"" + user + "\"", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.GREEN);
                        userField.clear();
                        passField.clear();
                    }

                } catch (IOException e) {
                    errorTip.showText("Error writing user file", getWidth()/2, (getHeight()/2 - 50) - 40, 18, Color.RED);
                }
            }
        };
        addObject(register, getWidth()/2 + 80, getHeight()/2 + 60);

        // Back to Menu Button: Returns to the main menu screen
        Button back = new Button("Back to Menu") {
            @Override
            public void onClick() {
                Greenfoot.setWorld(new MenuWorld());
            }
        };
        addObject(back, getWidth()/2, getHeight()/2 - 30 + 192);
    }

    /**
     * Draws centered text onto the background canvas.
     * 
     * @param text The text to display
     * @param x X-coordinate of the center
     * @param y Y-coordinate of the center
     * @param size Font size
     * @param col Text color
     */
    public void showText(String text, int x, int y, int size, Color col) {
        GreenfootImage bg = getBackground();
        GreenfootImage txt = new GreenfootImage(text, size, col, new Color(0,0,0,0));
        int drawX = x - txt.getWidth()/2;
        int drawY = y - txt.getHeight()/2;
        bg.drawImage(txt, drawX, drawY);
    }
}
