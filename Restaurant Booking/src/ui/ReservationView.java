package ui;

import models.Reservation;
import services.ReservationService;
import ui.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Eine Ansicht, die alle Reservierungen in einer Tabelle anzeigt und das Löschen ermöglicht.
 */
public class ReservationView extends JDialog {
    private final ReservationService service;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private JButton deleteButton;

    // Formatierungen
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public ReservationView(Frame parent, ReservationService service) {
        super(parent, "Reservierungsverwaltung", true);
        this.service = service;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        initializeUI();
        loadReservations();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        // Haupt-Panel mit Hintergrund
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = UIHelper.createHeaderPanel("Reservierungsverwaltung");
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabellen-Panel
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Button-Panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Standardeinstellungen für das Dialogfenster
        setContentPane(mainPanel);
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = UIHelper.createSectionPanel("Alle Reservierungen");
        
        // Tabellen-Modell und Spalten erstellen
        String[] columnNames = {"ID", "Datum", "Uhrzeit", "Kundenname", "Kontakt", "Tisch", "Personen"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabelle nicht editierbar machen
            }
        };
        
        reservationTable = new JTable(tableModel);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.setRowHeight(25);
        reservationTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Listener für Tabellenauswahl
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteButton.setEnabled(reservationTable.getSelectedRow() != -1);
            }
        });
        
        return tablePanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIHelper.BUTTON_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        deleteButton = UIHelper.createStyledButton("Reservierung löschen", UIHelper.ERROR_COLOR);
        JButton refreshButton = UIHelper.createStyledButton("Aktualisieren", UIHelper.SUCCESS_COLOR);
        JButton closeButton = UIHelper.createStyledButton("Schließen", UIHelper.NEUTRAL_COLOR);
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Aktionen
        deleteButton.addActionListener(e -> deleteSelectedReservation());
        refreshButton.addActionListener(e -> loadReservations());
        closeButton.addActionListener(e -> dispose());
        
        // Deaktiviere den Löschen-Button, wenn keine Zeile ausgewählt ist
        deleteButton.setEnabled(false);
        
        return buttonPanel;
    }
    
    /**
     * Lädt alle Reservierungen aus dem Service und zeigt sie in der Tabelle an
     */
    private void loadReservations() {
        // Tabelle leeren
        tableModel.setRowCount(0);
        
        // Reservierungen laden
        List<Reservation> reservations = service.getUpcomingReservations();
        
        // Reservierungen in Tabelle einfügen
        for (Reservation reservation : reservations) {
            Object[] row = {
                reservation.getId(),
                reservation.getDate().format(DATE_FORMATTER),
                reservation.getTime(),
                reservation.getCustomer().getName(),
                reservation.getCustomer().getContact(),
                "Tisch " + reservation.getTable().getId() + " (" + reservation.getTable().getCapacity() + " Plätze)",
                reservation.getPersons()
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Löscht die ausgewählte Reservierung nach Bestätigung
     */
    private void deleteSelectedReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 3);
        String date = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Bestätigungsdialog anzeigen
        int option = JOptionPane.showConfirmDialog(
            this,
            "Möchten Sie die Reservierung für " + customerName + " am " + date + " wirklich löschen?",
            "Reservierung löschen",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        // Wenn Benutzer bestätigt, Reservierung löschen
        if (option == JOptionPane.YES_OPTION) {
            boolean success = service.deleteReservation(reservationId);
            if (success) {
                UIHelper.showSuccessMessage(this, "Die Reservierung wurde erfolgreich gelöscht.");
                // Tabelle aktualisieren
                loadReservations();
            } else {
                UIHelper.showErrorMessage(this, "Beim Löschen der Reservierung ist ein Fehler aufgetreten.");
            }
        }
    }
}