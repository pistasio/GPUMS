package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import logic.AccountManager;

public class RegisterFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private AccountManager accounts;

    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;

    private JButton registerButton;
    private JButton closeButton;

    public RegisterFrame(AccountManager accounts) {
        this.accounts = accounts;

        setTitle("GPU Management - Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 15));
        content.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(
                new JLabel("Student Registration", SwingConstants.CENTER),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(5, 2, 10, 10));

        idField = new JTextField(20);
        nameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        passwordField = new JPasswordField(20);

        fields.add(new JLabel("University ID:"));
        fields.add(idField);

        fields.add(new JLabel("Full name:"));
        fields.add(nameField);

        fields.add(new JLabel("Email:"));
        fields.add(emailField);

        fields.add(new JLabel("Phone number:"));
        fields.add(phoneField);

        fields.add(new JLabel("Password:"));
        fields.add(passwordField);

        content.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        registerButton = new JButton("Submit registration");
        closeButton = new JButton("Close");

        registerButton.addActionListener(this);
        closeButton.addActionListener(this);

        buttons.add(registerButton);
        buttons.add(closeButton);

        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(registerButton);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeButton) {
            dispose();
            return;
        }

        if (e.getSource() == registerButton) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (id.isEmpty() || name.isEmpty() || email.isEmpty()
                    || phone.isEmpty() || password.trim().isEmpty()) {
                showError("Complete all fields.");
                return;
            }

            if (!id.matches("[0-9]+")) {
                showError("University ID must contain digits only.");
                return;
            }

            if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
                showError("Enter a valid email address.");
                return;
            }

            if (!phone.matches("\\+?[0-9][0-9 ()-]*")) {
                showError("Enter a valid phone number.");
                return;
            }

            String[] values = {id, name, email, phone, password};

            for (String value : values) {
                if (value.contains("|") || value.contains("\n")
                        || value.contains("\r")) {
                    showError("Fields cannot contain | or line breaks.");
                    return;
                }
            }

            try {
                accounts.registerStudent(id, password, name, email, phone);

                JOptionPane.showMessageDialog(
                        this,
                        "Registration submitted."
                                + "\nAn administrator must approve your account"
                                + " before you can log in.",
                        "Registration submitted",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose();

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Registration failed",
                JOptionPane.ERROR_MESSAGE);
    }
}