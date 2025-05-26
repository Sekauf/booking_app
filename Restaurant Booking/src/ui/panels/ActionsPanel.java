package ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

/**
 * Panel mit den Hauptaktionen für die Anwendung
 */
public class ActionsPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ActionsPanel.class.getName());

    public ActionsPanel(Runnable newReservation, 
                        Runnable showAllReservations,
                        Runnable manageTablesAction, 
                        Runnable viewReservationsAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Überschrift für Aktionsbereich
        JLabel actionTitle = new JLabel("Aktionen");
        actionTitle.setFont(new Font("Arial", Font.BOLD, 18));
        actionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(actionTitle);
        
        // Hauptmenü-Button für neue Reservierung
        StyledButton newReservationButton = new StyledButton("Neue Reservierung erfassen", null);
        newReservationButton.addActionListener(e -> newReservation.run());
        add(newReservationButton);
        
        // Alle Reservierungen Button
        StyledButton allReservationsButton = new StyledButton("Alle Reservierungen anzeigen", null);
        allReservationsButton.addActionListener(e -> showAllReservations.run());
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(allReservationsButton);
        
        // Tische verwalten Button
        StyledButton manageTablesButton = new StyledButton("Tische verwalten", null);
        manageTablesButton.addActionListener(e -> manageTablesAction.run());
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(manageTablesButton);
        
        // Reservierungen anzeigen Button
        StyledButton viewReservationsButton = new StyledButton("Reservierungen anzeigen", null);
        viewReservationsButton.addActionListener(e -> viewReservationsAction.run());
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(viewReservationsButton);
    }
    
    /**
     * Interne Klasse für die Buttons mit einheitlichem Styling
     */
    private static class StyledButton extends JButton {
        public StyledButton(String text, String iconPath) {
            super(text);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(280, 50));
            setPreferredSize(new Dimension(280, 50));
            setFont(new Font("Arial", Font.PLAIN, 14));
            setFocusPainted(false);
            setBorderPainted(true);
            setBackground(new Color(240, 240, 240));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            
            // Optional: Icon hinzufügen
            if (iconPath != null) {
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                    setIcon(icon);
                } catch (Exception e) {
                    LOGGER.warning("Icon nicht gefunden: " + iconPath);
                }
            }
            
            // Hover-Effekt
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(220, 220, 220));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(new Color(240, 240, 240));
                }
            });
        }
    }
}