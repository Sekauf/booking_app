package ui.components;

import models.Reservation;
import models.Table;
import ui.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Wiederverwendbare Komponente für die Anzeige einer Reservierung als Karte
 */
public class ReservationCard extends JPanel {
    private final Reservation reservation;
    private final Consumer<Reservation> onEdit;
    private final Consumer<Reservation> onDelete;

    public ReservationCard(Reservation reservation, 
                          Consumer<Reservation> onEdit, 
                          Consumer<Reservation> onDelete) {
        super(new BorderLayout(10, 0));
        this.reservation = reservation;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
        
        // Linke Seite - Zeit und Tischnummer
        add(createLeftPanel(), BorderLayout.WEST);
        
        // Mittlerer Bereich - Gast und Kontakt
        add(createCenterPanel(), BorderLayout.CENTER);
        
        // Rechte Seite - Personenzahl und Aktionen
        add(createRightPanel(), BorderLayout.EAST);
        
        // Hover-Effekt
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(250, 250, 250));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
            }
        });
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(100, 0));
        
        // Zeit-Anzeige
        JLabel timeLabel = new JLabel(reservation.getTime() + " Uhr");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        leftPanel.add(timeLabel, BorderLayout.NORTH);
        
        // Tischnummer
        Table table = reservation.getTable();
        if (table != null) {
            JLabel tableLabel = new JLabel("Tisch " + table.getId() + " (" + table.getCapacity() + " Plätze)");
            tableLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            tableLabel.setForeground(new Color(100, 100, 100));
            leftPanel.add(tableLabel, BorderLayout.SOUTH);
        }
        
        return leftPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Gast-Name - Verwende Kundennamen konsistent
        String name = reservation.getCustomer() != null ? 
                      reservation.getCustomer().getName() : 
                      "Unbekannt";
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        centerPanel.add(nameLabel, BorderLayout.NORTH);
        
        // Kontakt - Verwende Kundenkontakt konsistent
        String contact = reservation.getCustomer() != null ? 
                        reservation.getCustomer().getContact() : 
                        "Keine Kontaktdaten";
        JLabel contactLabel = new JLabel(contact);
        contactLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        contactLabel.setForeground(new Color(100, 100, 100));
        centerPanel.add(contactLabel, BorderLayout.SOUTH);
        
        return centerPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(80, 0));
        
        // Personenzahl
        JLabel personsLabel = new JLabel(reservation.getPersons() + " Pers.");
        personsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        personsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(personsLabel, BorderLayout.NORTH);
        
        // Aktions-Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        
        // Bearbeiten-Button
        JButton editButton = new JButton("✏️");
        ImageIcon editIcon = UIHelper.loadIcon("/icons/edit.png");
        if (editIcon != null) {
            editButton.setIcon(editIcon);
            editButton.setText(null);
        }
        editButton.setToolTipText("Bearbeiten");
        editButton.setPreferredSize(new Dimension(24, 24));
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.addActionListener(e -> onEdit.accept(reservation));
        actionPanel.add(editButton);
        
        // Löschen-Button
        JButton deleteButton = new JButton("🗑️");
        ImageIcon deleteIcon = UIHelper.loadIcon("/icons/delete.png");
        if (deleteIcon != null) {
            deleteButton.setIcon(deleteIcon);
            deleteButton.setText(null);
        }
        deleteButton.setToolTipText("Löschen");
        deleteButton.setPreferredSize(new Dimension(24, 24));
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.addActionListener(e -> onDelete.accept(reservation));
        actionPanel.add(deleteButton);
        
        rightPanel.add(actionPanel, BorderLayout.SOUTH);
        
        return rightPanel;
    }
}