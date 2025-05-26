package ui.panels;

import models.Reservation;
import services.ReservationService;
import ui.components.ReservationCard;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel zur Anzeige der kommenden Reservierungen
 */
public class ReservationsPanel extends JPanel {
    private final ReservationService service;
    private final JPanel contentPanel;
    private final Consumer<Reservation> onEditReservation;
    private final Consumer<Reservation> onDeleteReservation;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public ReservationsPanel(ReservationService service, 
                             Consumer<Reservation> onEdit, 
                             Consumer<Reservation> onDelete) {
        super(new BorderLayout());
        this.service = service;
        this.onEditReservation = onEdit;
        this.onDeleteReservation = onDelete;
        
        // Panel für die Liste der kommenden Reservierungen
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Header-Bereich für Reservierungen
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel reservationsTitle = new JLabel("Nächste Reservierungen");
        reservationsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(reservationsTitle, BorderLayout.WEST);
        
        // Aktualisieren-Button
        JButton refreshButton = new JButton("Aktualisieren");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 12));
        refreshButton.addActionListener(e -> refreshReservations());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Scrollpane für Reservierungen
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Füge die nächsten Reservierungen hinzu
        refreshReservations();
    }
    
    /**
     * Aktualisiert die Liste der anstehenden Reservierungen
     */
    public void refreshReservations() {
        SwingUtilities.invokeLater(this::loadUpcomingReservations);
    }
    
    /**
     * Lädt die Liste der anstehenden Reservierungen aus dem Service und zeigt sie an
     */
    private void loadUpcomingReservations() {
        contentPanel.removeAll();
        
        // Reservierungen für heute und die nächsten 7 Tage abrufen
        List<Reservation> reservations = service.getUpcomingReservations();
        
        if (reservations.isEmpty()) {
            // Keine Reservierungen vorhanden
            JLabel noReservationsLabel = new JLabel("Keine bevorstehenden Reservierungen");
            noReservationsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noReservationsLabel.setForeground(new Color(100, 100, 100));
            noReservationsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(noReservationsLabel);
        } else {
            // Sortierung nach Datum und Zeit
            reservations.sort((r1, r2) -> {
                LocalDateTime dt1 = LocalDateTime.of(r1.getDate(), LocalTime.parse(r1.getTime()));
                LocalDateTime dt2 = LocalDateTime.of(r2.getDate(), LocalTime.parse(r2.getTime()));
                return dt1.compareTo(dt2);
            });
            
            // Gruppiert nach Datum anzeigen
            LocalDate currentDate = null;
            
            for (Reservation reservation : reservations) {
                // Wenn das Datum sich ändert, füge einen Datumstrennbalken ein
                if (currentDate == null || !currentDate.equals(reservation.getDate())) {
                    currentDate = reservation.getDate();
                    
                    // Abstand zum vorherigen Datum
                    if (contentPanel.getComponentCount() > 0) {
                        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                    }
                    
                    // Datumstrennbalken
                    JPanel datePanel = new JPanel(new BorderLayout());
                    datePanel.setBackground(new Color(240, 240, 240));
                    datePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    datePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
                    
                    // Datum mit besonderem Format für heute und morgen
                    String dateText;
                    if (currentDate.equals(LocalDate.now())) {
                        dateText = "Heute (" + currentDate.format(DATE_FORMATTER) + ")";
                    } else if (currentDate.equals(LocalDate.now().plusDays(1))) {
                        dateText = "Morgen (" + currentDate.format(DATE_FORMATTER) + ")";
                    } else {
                        dateText = currentDate.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy"));
                    }
                    
                    JLabel dateLabel = new JLabel(dateText);
                    dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    datePanel.add(dateLabel, BorderLayout.WEST);
                    
                    contentPanel.add(datePanel);
                }
                
                // Eine Karte für jede Reservierung
                ReservationCard card = new ReservationCard(
                    reservation, 
                    onEditReservation, 
                    onDeleteReservation
                );
                contentPanel.add(card);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}