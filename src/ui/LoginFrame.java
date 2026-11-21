package ui;

import java.io.File;
import java.io.IOException;
import storage.DataStore;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import logic.AccountManager;
import model.User;

import logic.Scheduler;
import model.Administrator;

import model.Student;

public class LoginFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private AccountManager accounts;
    private Scheduler scheduler;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame(AccountManager accounts) {
        this.accounts = accounts;
        this.scheduler = new Scheduler(accounts);

        setTitle("GPU Management - Login");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    DataStore.saveAccounts(accounts);
                    DataStore.saveRequests(accounts);
                    DataStore.saveScheduler(scheduler);
                    System.exit(0);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Could not save your data:\n" + ex.getMessage()
                                    + "\nThe application will stay open. Try closing it again.",
                            "Save failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel content = new JPanel(new BorderLayout(10, 15));
        content.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(
                "GPU Utilization Management", SwingConstants.CENTER);
        content.add(titleLabel, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 2, 10, 10));

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        fields.add(new JLabel("University ID / Admin username:"));
        fields.add(usernameField);
        fields.add(new JLabel("Password:"));
        fields.add(passwordField);

        content.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));

        loginButton = new JButton("Log in");
        registerButton = new JButton("Register");

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        buttons.add(loginButton);
        buttons.add(registerButton);

        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(loginButton);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == registerButton) {
    	    RegisterFrame registration = new RegisterFrame(accounts);
    	    registration.setVisible(true);
    	    return;
    	}
    	
        if (e.getSource() == loginButton) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter your username and password.",
                        "Missing information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                User user = accounts.login(username, password);
                passwordField.setText("");

                if (user instanceof Administrator) {
                    AdminFrame adminFrame = new AdminFrame(
                            accounts, scheduler,
                            (Administrator) user, this);

                    adminFrame.setVisible(true);
                    setVisible(false);

                } else if (user instanceof Student) {
                    StudentFrame studentFrame = new StudentFrame(
                            accounts, scheduler, (Student) user, this);

                    studentFrame.setVisible(true);
                    setVisible(false);
                }
                

            } catch (IllegalArgumentException | IllegalStateException ex) {
                passwordField.setText("");

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Login failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean anySaveFile =
                            new File("accounts.txt").exists()
                            || new File("requests.txt").exists()
                            || new File("jobs.txt").exists()
                            || new File("clock.txt").exists();

                    if (anySaveFile && !DataStore.saveFilesExist()) {
                        throw new IOException(
                                "Some save files are missing. All four are required:\n"
                                + "accounts.txt, requests.txt, jobs.txt and clock.txt.");
                    }

                    AccountManager accounts;

                    if (DataStore.saveFilesExist()) {
                        accounts = DataStore.loadAccounts();
                        DataStore.loadRequests(accounts);
                    } else {
                        accounts = new AccountManager();
                        accounts.addDefaultAdministrator();
                    }

                    LoginFrame login = new LoginFrame(accounts);

                    if (DataStore.saveFilesExist()) {
                        DataStore.loadScheduler(login.scheduler);
                    }

                    login.setVisible(true);

                } catch (IOException | IllegalArgumentException
                        | IllegalStateException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Could not load saved data:\n" + ex.getMessage()
                                    + "\nExisting save files have not been overwritten.",
                            "Startup failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}