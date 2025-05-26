package services;

import java.sql.*;
import java.time.LocalDate;
import models.Table;
import models.Customer;
import java.util.ArrayList;
import java.util.List;
import models.Reservation;

/**
 * DatabaseManager verwaltet die Verbindung zur SQLite-Datenbank
 * und führt alle Datenbankzugriffe (Tabellen erstellen, CRUD-Operationen) durch.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:restaurant.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            // SQLite JDBC-Treiber laden (falls erforderlich)
            Class.forName("org.sqlite.JDBC");
            // Verbindung zur SQLite-Datenbank herstellen (Datei "restaurant.db")
            connection = DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.err.println("Datenbankverbindung konnte nicht hergestellt werden.");
        }
    }

    /**
     * Initialisiert die Datenbankstruktur: erstellt Tabellen und fügt Default-Daten ein.
     * Diese Methode wird beim Programmstart einmalig aufgerufen.
     */
    public void initialize() {
        createTables();
        insertDefaultTablesIfEmpty();
    }

    /**
     * Legt die benötigten Tabellen (tables, customers, reservations) in der DB an, falls noch nicht vorhanden.
     */
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Tabelle für Tische
            String sqlTable = "CREATE TABLE IF NOT EXISTS tables ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "capacity INTEGER"
                    + ")";
            stmt.execute(sqlTable);

            // Tabelle für Kunden
            String sqlCustomer = "CREATE TABLE IF NOT EXISTS customers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact TEXT"
                    + ")";
            stmt.execute(sqlCustomer);

            // Tabelle für Reservierungen (mit Fremdschlüsseln auf Tisch und Kunde)
            String sqlReservation = "CREATE TABLE IF NOT EXISTS reservations ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "date TEXT,"
                    + "persons INTEGER,"
                    + "table_id INTEGER,"
                    + "customer_id INTEGER,"
                    + "FOREIGN KEY(table_id) REFERENCES tables(id),"
                    + "FOREIGN KEY(customer_id) REFERENCES customers(id)"
                    + ")";
            stmt.execute(sqlReservation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prüft, ob bereits Tische in der DB vorhanden sind, und fügt ggf. einige Standard-Tische hinzu.
     */
    private void insertDefaultTablesIfEmpty() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tables")) {
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count == 0) {
                    // Noch keine Tische vorhanden: einige Default-Tische anlegen
                    addTable(2);  // Tisch mit 2 Plätzen
                    addTable(4);  // Tisch mit 4 Plätzen
                    addTable(4);  // weiterer Tisch mit 4 Plätzen
                    addTable(6);  // Tisch mit 6 Plätzen
                    addTable(8);  // Tisch mit 8 Plätzen
                    System.out.println("Standard-Tische wurden hinzugefügt.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fügt einen neuen Tisch mit gegebener Kapazität in die Datenbank ein.
     * @param capacity Anzahl der Sitzplätze des neuen Tisches
     * @return Table-Objekt des neu eingefügten Tisches (mit gesetzter ID) oder null bei Fehler
     */
    public Table addTable(int capacity) {
        String sql = "INSERT INTO tables (capacity) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, capacity);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Generierte ID des neuen Datensatzes abrufen
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        return new Table(newId, capacity);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fügt einen neuen Kunden in die Datenbank ein.
     * @param name    Name des Kunden
     * @param contact Kontaktinformation (Telefon, E-Mail, o.ä.)
     * @return Customer-Objekt mit gesetzter ID, oder null bei Fehler
     */
    public Customer addCustomer(String name, String contact) {
        String sql = "INSERT INTO customers (name, contact) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        return new Customer(newId, name, contact);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Speichert eine neue Reservierung in der Datenbank.
     * @param date       Datum der Reservierung
     * @param persons    Anzahl der Personen
     * @param tableId    ID des reservierten Tisches
     * @param customerId ID des reservierenden Kunden
     * @return die generierte Reservierungs-ID (oder -1 bei Fehler)
     */
    public int addReservation(LocalDate date, int persons, int tableId, int customerId) {
        String sql = "INSERT INTO reservations (date, persons, table_id, customer_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, date.toString());
            pstmt.setInt(2, persons);
            pstmt.setInt(3, tableId);
            pstmt.setInt(4, customerId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Sucht einen verfügbaren Tisch für eine bestimmte Personenanzahl am gegebenen Datum.
     * @param date    Datum der Reservierung
     * @param persons benötigte Anzahl an Sitzplätzen
     * @return ein verfügbares Table-Objekt, oder null wenn kein passender Tisch frei ist
     */
    public Table findAvailableTable(LocalDate date, int persons) {
        String sql = "SELECT id, capacity FROM tables "
                + "WHERE capacity >= ? "
                + "AND id NOT IN (SELECT table_id FROM reservations WHERE date = ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, persons);
            pstmt.setString(2, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int tableId = rs.getInt("id");
                    int cap = rs.getInt("capacity");
                    return new Table(tableId, cap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * In DatabaseManager hinzufügen:
     */
    public boolean deleteReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
/**
 * Ruft alle zukünftigen Reservierungen aus der Datenbank ab.
 * @return Liste mit Reservation-Objekten
 */
public List<Reservation> getUpcomingReservations() {
    List<Reservation> reservations = new ArrayList<>();
    String sql = "SELECT r.id, r.date, r.persons, r.table_id, r.customer_id, " +
                 "c.name, c.contact, t.capacity " +
                 "FROM reservations r " +
                 "JOIN customers c ON r.customer_id = c.id " +
                 "JOIN tables t ON r.table_id = t.id " +
                 "WHERE r.date >= ? " +
                 "ORDER BY r.date, r.id";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, LocalDate.now().toString());
        
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                int persons = rs.getInt("persons");
                int tableId = rs.getInt("table_id");
                int tableCapacity = rs.getInt("capacity");
                int customerId = rs.getInt("customer_id");
                
                String name = rs.getString("name");
                String contact = rs.getString("contact");
                
                Table table = new Table(tableId, tableCapacity);
                Customer customer = new Customer(customerId, name, contact);
                
                Reservation reservation = new Reservation(id, date, persons, table, customer);
                reservations.add(reservation);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return reservations;
}
}