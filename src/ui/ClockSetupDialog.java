package ui;

import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;

import logic.Scheduler;

public class ClockSetupDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private Scheduler scheduler;

    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JButton startButton;
    private JButton cancelButton;

    public ClockSetupDialog(JFrame owner, Scheduler scheduler) {
        super(owner, "Set simulation date and time", true);

        this.scheduler = scheduler;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 15));
        content.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(
                new JLabel("Choose the simulation's starting date and time."),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 2, 10, 10));

        dateSpinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(), null, null, Calendar.DAY_OF_MONTH));

        JSpinner.DateEditor dateEditor =
                new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");

        dateEditor.getFormat().setLenient(false);
        dateSpinner.setEditor(dateEditor);

        timeSpinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(), null, null, Calendar.MINUTE));

        JSpinner.DateEditor timeEditor =
                new JSpinner.DateEditor(timeSpinner, "HH:mm");

        timeEditor.getFormat().setLenient(false);
        timeSpinner.setEditor(timeEditor);

        fields.add(new JLabel("Date (dd/MM/yyyy):"));
        fields.add(dateSpinner);
        fields.add(new JLabel("Time (24-hour HH:mm):"));
        fields.add(timeSpinner);

        content.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        startButton = new JButton("Start clock");
        cancelButton = new JButton("Cancel");

        startButton.addActionListener(this);
        cancelButton.addActionListener(this);

        buttons.add(startButton);
        buttons.add(cancelButton);

        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            dispose();
            return;
        }

        if (e.getSource() == startButton) {
            try {
                dateSpinner.commitEdit();
                timeSpinner.commitEdit();

                Date selectedDate = (Date) dateSpinner.getValue();
                Date selectedTime = (Date) timeSpinner.getValue();

                ZoneId zone = ZoneId.systemDefault();

                LocalDate date = selectedDate.toInstant()
                        .atZone(zone).toLocalDate();

                LocalTime time = selectedTime.toInstant()
                        .atZone(zone).toLocalTime()
                        .withSecond(0).withNano(0);

                LocalDateTime start = LocalDateTime.of(date, time);

                scheduler.startClock(start);

                JOptionPane.showMessageDialog(
                        this,
                        "Simulation clock set to " + scheduler.getNowText(),
                        "Clock started",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose();

            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a valid date and time.",
                        "Invalid input",
                        JOptionPane.ERROR_MESSAGE);

            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Cannot start clock",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}