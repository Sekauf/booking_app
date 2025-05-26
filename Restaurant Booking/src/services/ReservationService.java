package services;

import java.time.LocalDate;
import java.util.List;

import models.Table;
import models.Customer;
import models.Reservation;

/**
 * Service-Klasse für Reservierungen. Kapselt die Geschäftslogik und nutzt DatabaseManager für DB-Operationen.
 */
public class ReservationService {
    private final DatabaseManager db;

    public ReservationService(DatabaseManager dbManager) {
        this.db = dbManager;
    }

    /**
     * Neuen Tisch mit gegebener Kapazität hinzufügen.
     * @param capacity Anzahl Sitzplätze des neuen Tisches
     * @return true, wenn erfolgreich hinzugefügt (false bei Fehler)
     */
    public boolean addNewTable(int capacity) {
        if (capacity < 1) {
            return false;
        }
        Table newTable = db.addTable(capacity);
        return (newTable != null);
    }

    /**
     * Versucht, eine neue Reservierung anzulegen. Prüft verfügbare Tische und speichert die Reservierung.
     * @param name     Name des Kunden
     * @param contact  Kontaktinfo des Kunden
     * @param date     Reservierungsdatum
     * @param persons  Anzahl der Personen
     * @return Reservation-Objekt bei Erfolg, oder null falls kein Tisch verfügbar oder Fehler auftrat
     */
    public Reservation createReservation(String name, String contact, LocalDate date, int persons) {
        // 1. Verfügbaren Tisch für Datum und Personenanzahl suchen
        Table availableTable = db.findAvailableTable(date, persons);
        if (availableTable == null) {
            // Kein freier Tisch gefunden
            return null;
        }
        // 2. Kunden in DB anlegen
        Customer customer = db.addCustomer(name, contact);
        if (customer == null) {
            // Fehler beim Speichern des Kunden
            return null;
        }
        // 3. Reservierung in DB speichern (mit zugewiesenem Tisch und Kunde)
        int reservationId = db.addReservation(date, persons, availableTable.getId(), customer.getId());
        if (reservationId <= 0) {
            // Fehler beim Speichern der Reservierung
            return null;
        }
        // 4. Reservation-Objekt erstellen und zurückgeben
        return new Reservation(reservationId, date, persons, availableTable, customer);
    }

    /**
     * Ruft alle zukünftigen Reservierungen ab.
     * @return Liste aller zukünftigen Reservierungen
     */
    public List<Reservation> getUpcomingReservations() {
        return db.getUpcomingReservations();
    }
    
    public boolean deleteReservation(int reservationId) {
        // SQL-Statement für das Löschen vorbereiten und ausführen
        // z.B.: "DELETE FROM reservations WHERE id = ?"
        return db.deleteReservation(reservationId);
    }
}