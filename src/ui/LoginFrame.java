package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import logic.AccountManager;
import model.User;

public class LoginFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private AccountManager accounts;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame(AccountManager accounts) {
        this.accounts = accounts;

        setTitle("GPU Management - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

                JOptionPane.showMessageDialog(
                        this,
                        "Welcome, " + user.getFullName()
                                + "\nRole: " + user.getRole(),
                        "Login successful",
                        JOptionPane.INFORMATION_MESSAGE);

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
                AccountManager accounts = new AccountManager();
                accounts.addDefaultAdministrator();

                LoginFrame frame = new LoginFrame(accounts);
                frame.setVisible(true);
            }
        });
    }
}