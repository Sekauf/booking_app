package ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel für den schnellen Zugriff auf die Tischverwaltung
 */
public class QuickTableAccessPanel extends JPanel {
    private final Consumer<Integer> onAddTable;
    private final Runnable onCustomTable;
    
    public QuickTableAccessPanel(Consumer<Integer> onAddTable, Runnable onCustomTable) {
        super();
        this.onAddTable = onAddTable;
        this.onCustomTable = onCustomTable;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        "Schnellzugriff: Tisch hinzufügen",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 12)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Panel für vorgefertigte Tische
        JPanel predefinedTablesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        predefinedTablesPanel.setBackground(new Color(245, 245, 245));
        predefinedTablesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Vorgefertigte Tischgrößen
        Integer[] tableSizes = {2, 4, 6, 8, 10};
        
        // Buttons für vorgefertigte Tischgrößen
        for (Integer size : tableSizes) {
            JButton tableButton = new JButton(size.toString());
            tableButton.setFont(new Font("Arial", Font.PLAIN, 12));
            tableButton.setPreferredSize(new Dimension(45, 30));
            tableButton.setMargin(new Insets(2, 2, 2, 2));
            tableButton.setFocusPainted(false);
            
            tableButton.addActionListener(e -> onAddTable.accept(size));
            
            predefinedTablesPanel.add(tableButton);
        }
        
        // Button für benutzerdefinierte Tischgröße
        JButton customTableButton = new JButton("Benutzerdefiniert");
        customTableButton.setFont(new Font("Arial", Font.PLAIN, 12));
        customTableButton.setFocusPainted(false);
        customTableButton.addActionListener(e -> onCustomTable.run());
        predefinedTablesPanel.add(customTableButton);
        
        add(predefinedTablesPanel);
        
        // Info-Text
        JLabel infoLabel = new JLabel(
                "<html><div style='text-align: center; width: 100%'>" +
                "Die Zahlen geben die Anzahl der Sitzplätze pro Tisch an." +
                "</div></html>"
        );
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        add(infoLabel);
    }
}