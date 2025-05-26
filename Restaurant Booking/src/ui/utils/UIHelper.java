package ui.utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.logging.Logger;
import java.net.URL;

/**
 * Hilfsklasse für gemeinsame UI-Funktionen und Komponenten
 */
public class UIHelper {
    private static final Logger LOGGER = Logger.getLogger(UIHelper.class.getName());
    
    // Konstante Farben
    public static final Color PRIMARY_COLOR = new Color(51, 102, 153);
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color LIGHT_BACKGROUND = new Color(245, 245, 245);
    public static final Color BUTTON_BACKGROUND = new Color(240, 240, 240);
    public static final Color BORDER_COLOR = new Color(200, 200, 200);
    public static final Color ERROR_COLOR = new Color(204, 0, 0);
    public static final Color SUCCESS_COLOR = new Color(0, 153, 0);
    public static final Color NEUTRAL_COLOR = new Color(102, 102, 102);
    
    /**
     * Erstellt einen Header-Bereich mit Titel
     */
    public static JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    /**
     * Erstellt ein Panel mit Überschrift für Formularabschnitte
     */
    public static JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                PRIMARY_COLOR));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        return panel;
    }
    
    /**
     * Erstellt ein Feld-Panel mit Label und Textfeld
     */
    public static JPanel createFieldPanel(String labelText, String toolTipText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, 25));
        panel.add(label);
        
        JTextField textField = new JTextField(20);
        textField.setToolTipText(toolTipText);
        panel.add(textField);
        
        return panel;
    }
    
    /**
     * Erstellt einen gestylten Button
     */
    public static JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 30));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(backgroundColor));
        return button;
    }
    
    /**
     * Zeigt eine Erfolgsmeldung an
     */
    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Zeigt eine Fehlermeldung an
     */
    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Fehler",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Versucht, ein Icon aus den Ressourcen zu laden
     * Wenn das Icon nicht gefunden wird, wird null zurückgegeben
     */
    public static ImageIcon loadIcon(String path) {
        try {
            URL resource = UIHelper.class.getResource(path);
            if (resource != null) {
                return new ImageIcon(resource);
            }
            
            // Alternativ versuchen, das Icon über den Klassenpfad zu laden
            resource = UIHelper.class.getClassLoader().getResource(path.startsWith("/") ? path.substring(1) : path);
            if (resource != null) {
                return new ImageIcon(resource);
            }
            
            LOGGER.warning("Icon nicht gefunden: " + path);
            return null;
        } catch (Exception e) {
            LOGGER.warning("Fehler beim Laden des Icons " + path + ": " + e.getMessage());
            return null;
        }
    }
}