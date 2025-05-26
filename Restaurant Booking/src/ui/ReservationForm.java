package ui;

import models.Reservation;
import services.ReservationService;
import ui.utils.UIHelper;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservationForm extends JDialog {
    private final ReservationService service;
    private JTextField nameField;
    private JTextField contactField;
    private JPanel datePanel;
    private JSpinner daySpinner;
    private JSpinner monthSpinner;
    private JSpinner yearSpinner;
    private JComboBox<String> timeComboBox;
    private JSpinner personsSpinner;

    public ReservationForm(Frame parent, ReservationService service) {
        super(parent, "Neue Reservierung", true);
        this.service = service;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        initializeUI();
        
        setSize(600, 650);
        setLocationRelativeTo(parent);
        setModal(true);
    }
    
    private void initializeUI() {
        // Haupt-Panel mit Hintergrund
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = UIHelper.createHeaderPanel("Neue Reservierung erstellen");
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Formular-Panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        
        // Button-Panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Standardeinstellungen für das Dialogfenster
        setContentPane(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        
        // Kundeninformationen Gruppe
        JPanel customerPanel = UIHelper.createSectionPanel("Kundeninformationen");
        
        // Name Feld
        JPanel namePanel = UIHelper.createFieldPanel("Name:", 
                "Vollständigen Namen des Gastes eingeben");
        nameField = (JTextField) namePanel.getComponent(1);
        customerPanel.add(namePanel);
        
        // Kontakt Feld
        JPanel contactPanel = UIHelper.createFieldPanel("Kontakt:", 
                "Telefonnummer oder E-Mail-Adresse");
        contactField = (JTextField) contactPanel.getComponent(1);
        customerPanel.add(contactPanel);
        
        formPanel.add(customerPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Reservierungsdetails Gruppe
        JPanel reservationPanel = UIHelper.createSectionPanel("Reservierungsdetails");
        
        // Datum, Zeit und Personen
        reservationPanel.add(createDatePanel());
        reservationPanel.add(createTimePanel());
        reservationPanel.add(createPersonsPanel());
        
        formPanel.add(reservationPanel);
        
        return formPanel;
    }
    
    private JPanel createDatePanel() {
        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        dateContainer.setBackground(Color.WHITE);
        
        JLabel dateLabel = new JLabel("Datum:");
        dateLabel.setPreferredSize(new Dimension(100, 25));
        dateContainer.add(dateLabel, BorderLayout.WEST);
        
        datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(Color.WHITE);
        
        // Aktuelle Werte für Spinner
        LocalDate now = LocalDate.now();
        
        daySpinner = new JSpinner(new SpinnerNumberModel(now.getDayOfMonth(), 1, 31, 1));
        JComponent dayEditor = daySpinner.getEditor();
        ((JSpinner.DefaultEditor)dayEditor).getTextField().setColumns(2);
        
        monthSpinner = new JSpinner(new SpinnerNumberModel(now.getMonthValue(), 1, 12, 1));
        JComponent monthEditor = monthSpinner.getEditor();
        ((JSpinner.DefaultEditor)monthEditor).getTextField().setColumns(2);
        
        yearSpinner = new JSpinner(new SpinnerNumberModel(now.getYear(), now.getYear(), now.getYear() + 5, 1));
        JComponent yearEditor = yearSpinner.getEditor();
        ((JSpinner.DefaultEditor)yearEditor).getTextField().setColumns(4);
        
        datePanel.add(daySpinner);
        datePanel.add(new JLabel("."));
        datePanel.add(monthSpinner);
        datePanel.add(new JLabel("."));
        datePanel.add(yearSpinner);
        
        dateContainer.add(datePanel, BorderLayout.CENTER);
        return dateContainer;
    }
    
    private JPanel createTimePanel() {
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        timePanel.setBackground(Color.WHITE);
        
        JLabel timeLabel = new JLabel("Uhrzeit:");
        timeLabel.setPreferredSize(new Dimension(100, 25));
        timePanel.add(timeLabel, BorderLayout.WEST);
        
        String[] zeitSlots = {"12:00", "12:30", "13:00", "13:30", "14:00", 
                "18:00", "18:30", "19:00", "19:30", "20:00", "20:30"};
        timeComboBox = new JComboBox<>(zeitSlots);
        timeComboBox.setBackground(Color.WHITE);
        timeComboBox.setPreferredSize(new Dimension(100, 25));
        timePanel.add(timeComboBox, BorderLayout.CENTER);
        
        return timePanel;
    }
    
    private JPanel createPersonsPanel() {
        JPanel personsPanel = new JPanel(new BorderLayout());
        personsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        personsPanel.setBackground(Color.WHITE);
        
        JLabel personsLabel = new JLabel("Personen:");
        personsLabel.setPreferredSize(new Dimension(100, 25));
        personsPanel.add(personsLabel, BorderLayout.WEST);
        
        personsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        personsSpinner.setPreferredSize(new Dimension(60, 25));
        personsPanel.add(personsSpinner, BorderLayout.CENTER);
        
        return personsPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIHelper.BUTTON_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JButton saveButton = UIHelper.createStyledButton("Reservieren", UIHelper.PRIMARY_COLOR);
        saveButton.addActionListener(e -> saveReservation());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = UIHelper.createStyledButton("Abbrechen", UIHelper.NEUTRAL_COLOR);
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        return buttonPanel;
    }
    
    private void saveReservation() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        int day = (int) daySpinner.getValue();
        int month = (int) monthSpinner.getValue();
        int year = (int) yearSpinner.getValue();
        String selectedTime = (String) timeComboBox.getSelectedItem();
        int persons = (int) personsSpinner.getValue();
        
        if (name.isEmpty() || contact.isEmpty()) {
            UIHelper.showErrorMessage(this, "Bitte alle Felder ausfüllen.");
            return;
        }
        
        LocalDate date = LocalDate.of(year, month, day);
        
        Reservation reservation = service.createReservation(name, contact, date, persons);
        if (reservation == null) {
            UIHelper.showErrorMessage(this, "Für diesen Termin ist leider kein Tisch verfügbar.");
        } else {
            UIHelper.showSuccessMessage(this,
                    "Reservierung erfolgreich für Tisch " + reservation.getTable().getId() +
                            " am " + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                            " um " + selectedTime + " Uhr gespeichert.");
            dispose();
        }
    }
}