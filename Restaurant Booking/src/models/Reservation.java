package models;

import java.time.LocalDate;

/**
 * Model-Klasse für eine Tischreservierung.
 * Beinhaltet das Reservierungsdatum, die Personenanzahl, sowie Referenzen auf den zugewiesenen Tisch und den Kunden.
 */
public class Reservation {
    private final int id;
    private final LocalDate date;
    private final int persons;
    private final Table table;
    private final Customer customer;
    private final String time = "18:00"; // Standardzeit für alle Reservierungen

    public Reservation(int id, LocalDate date, int persons, Table table, Customer customer) {
        this.id = id;
        this.date = date;
        this.persons = persons;
        this.table = table;
        this.customer = customer;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getPersons() {
        return persons;
    }

    public Table getTable() {
        return table;
    }

    public Customer getCustomer() {
        return customer;
    }
    
    public String getName() {
        return customer != null ? customer.getName() : "";
    }
    
    public String getContact() {
        return customer != null ? customer.getContact() : "";
    }

    public String getTime() {
        return time;
    }
}