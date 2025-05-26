package ui;

import models.Reservation;
import services.DatabaseManager;
import services.ReservationService;
import ui.panels.ActionsPanel;
import ui.panels.QuickTableAccessPanel;
import ui.panels.ReservationsPanel;
import ui.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Hauptfenster der Anwendung mit modernem Design.
 * Zeigt eine Vorschau der nächsten Reservierungen an.
 */
public class MainMenu extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainMenu.class.getName());
    private final ReservationService service;
    private Timer refreshTimer;
    private ReservationsPanel reservationsPanel;

    // Formatierungen
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public MainMenu(ReservationService service) {
        this.service = service;
        
        // Look and Feel setzen
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            LOGGER.warning("Konnte Look and Feel nicht setzen: " + e.getMessage());
            e.printStackTrace();
        }
        
        // UI-Eigenschaften setzen
        setTitle("Restaurant Tischreservierung");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 700));
        
        // UI aufbauen
        initializeUI();
        
        // Fenstergröße anpassen und zentrieren
        pack();
        setLocationRelativeTo(null);
        
        // Timer einrichten, um die Reservierungen alle 5 Minuten zu aktualisieren
        refreshTimer = new Timer(300000, e -> refreshReservations());
        refreshTimer.start();
        
        // Windowlistener für das Schließen des Fensters
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });
    }
    
    private void initializeUI() {
        // Hauptpanel mit Hintergrundbild erstellen
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.LIGHT_BACKGROUND);
        
        // Header-Bereich
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content-Bereich
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Linke Seite - Aktionsbereich
        ActionsPanel actionsPanel = new ActionsPanel(
            this::showReservationForm,
            this::showFullReservationList,
            this::showTableManagement,
            this::showReservationView
        );
        
        // Rechte Seite - Reservierungsvorschau
        JPanel reservationsOverviewPanel = new JPanel(new BorderLayout());
        reservationsOverviewPanel.setBackground(Color.WHITE);
        reservationsOverviewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Panel für die Liste der kommenden Reservierungen
        reservationsPanel = new ReservationsPanel(
            service,
            this::editReservation,
            this::deleteReservation
        );
        
        reservationsOverviewPanel.add(reservationsPanel, BorderLayout.CENTER);
        
        // Layout mit geteiltem Bereich
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                actionsPanel,
                reservationsOverviewPanel
        );
        splitPane.setDividerLocation(320);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        // Kleinerer Bereich für Tischverwaltung am unteren Rand
        QuickTableAccessPanel tableManagementPanel = new QuickTableAccessPanel(
            this::addPredefinedTable,
            this::showCustomTableDialog
        );
        
        contentPanel.add(tableManagementPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Footer-Bereich
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Hauptpanel zum Frame hinzufügen
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Restaurant-Verwaltung");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Aktuelles Datum im Header anzeigen
        JLabel dateLabel = new JLabel("Heute: " + LocalDate.now().format(DATE_FORMATTER));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(Color.WHITE);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(UIHelper.BUTTON_BACKGROUND);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel footerLabel = new JLabel("© 2023 Restaurant-Verwaltungssystem");
        footerLabel.setForeground(UIHelper.NEUTRAL_COLOR);
        footerPanel.add(footerLabel);
        return footerPanel;
    }
    
    /**
     * Aktualisiert die Liste der anstehenden Reservierungen
     */
    private void refreshReservations() {
        if (reservationsPanel != null) {
            reservationsPanel.refreshReservations();
        }
    }
    
    /**
     * Bearbeitet eine bestehende Reservierung
     */
    private void editReservation(Reservation reservation) {
        // Hier könnte die Bearbeitungsfunktion implementiert werden
        JOptionPane.showMessageDialog(
                this,
                "Die Bearbeitung von Reservierungen ist noch nicht implementiert.",
                "In Entwicklung",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Löscht eine Reservierung nach Bestätigung durch den Benutzer
     */
    private void deleteReservation(Reservation reservation) {
        if (reservation == null || reservation.getCustomer() == null) {
            UIHelper.showErrorMessage(this, "Ungültige Reservierung");
            return;
        }
        
        // Bestätigungsdialog anzeigen
        int option = JOptionPane.showConfirmDialog(
            this,
            "Möchten Sie die Reservierung für " + reservation.getCustomer().getName() + 
            " am " + reservation.getDate().format(DATE_FORMATTER) + " wirklich löschen?",
            "Reservierung löschen",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        // Wenn Benutzer bestätigt, Reservierung löschen
        if (option == JOptionPane.YES_OPTION) {
            boolean success = service.deleteReservation(reservation.getId());
            if (success) {
                UIHelper.showSuccessMessage(this, "Die Reservierung wurde erfolgreich gelöscht.");
                // Liste der anstehenden Reservierungen aktualisieren
                refreshReservations();
            } else {
                UIHelper.showErrorMessage(this, "Beim Löschen der Reservierung ist ein Fehler aufgetreten.");
            }
        }
    }
    
    /**
     * Zeigt eine vollständige Liste aller Reservierungen an
     */
    private void showFullReservationList() {
        showReservationView();
    }
    
    /**
     * Zeigt die Tischverwaltung an
     */
    private void showTableManagement() {
        // Hier eine umfassendere Tischverwaltung öffnen
        JDialog dialog = new JDialog(this, "Tischverwaltung", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        // Inhalt für den Dialog
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Hier könnte eine Tabelle oder Liste mit allen Tischen angezeigt werden
        JLabel placeholderLabel = new JLabel("Diese Funktion ist noch nicht vollständig implementiert.");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(placeholderLabel, BorderLayout.CENTER);
        
        // Schließen-Button
        JButton closeButton = new JButton("Schließen");
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(contentPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Fügt einen Tisch mit vordefinierter Größe hinzu
     */
    private void addPredefinedTable(int capacity) {
        boolean success = service.addNewTable(capacity);
        if (success) {
            UIHelper.showSuccessMessage(this, "Neuer Tisch mit " + capacity + " Plätzen hinzugefügt.");
        } else {
            UIHelper.showErrorMessage(this, "Tisch konnte nicht hinzugefügt werden.");
        }
    }
    
    /**
     * Zeigt einen Dialog für benutzerdefinierte Tischgröße
     */
    private void showCustomTableDialog() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel promptLabel = new JLabel("Kapazität des neuen Tisches (Anzahl Sitzplätze):");
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
        JComponent editor = capacitySpinner.getEditor();
        JFormattedTextField ftf = ((JSpinner.DefaultEditor) editor).getTextField();
        ftf.setColumns(3);
        
        inputPanel.add(promptLabel, BorderLayout.NORTH);
        inputPanel.add(capacitySpinner, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(
                this,
                inputPanel,
                "Tisch anlegen",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            int capacity = (Integer) capacitySpinner.getValue();
            addPredefinedTable(capacity);
        }
    }
    
    /**
     * Öffnet das Reservierungsformular
     */
    private void showReservationForm() {
        ReservationForm form = new ReservationForm(this, service);
        form.setVisible(true);
        // Nach dem Schließen des Formulars die Reservierungen aktualisieren
        refreshReservations();
    }
    
    /**
     * Öffnet die Reservierungsansicht mit allen Reservierungen
     */
    private void showReservationView() {
        ReservationView view = new ReservationView(this, service);
        view.setVisible(true);
        // Nach dem Schließen der Ansicht die anstehenden Reservierungen aktualisieren
        refreshReservations();
    }
    
    /**
     * Bei Schließung des Fensters den Timer stoppen
     */
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }

    /**
     * Hauptmethode zum Starten der Anwendung
     */
    public static void main(String[] args) {
        // Anwendung im Event Dispatch Thread starten
        SwingUtilities.invokeLater(() -> {
            // Datenbank initialisieren und Service einrichten
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.initialize();
            ReservationService service = new ReservationService(dbManager);
            // Hauptfenster anzeigen
            MainMenu menu = new MainMenu(service);
            menu.setVisible(true);
        });
    }
}