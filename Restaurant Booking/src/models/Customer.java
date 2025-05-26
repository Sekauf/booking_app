package models;

public class Customer {
    protected int id;
    protected String name;
    protected String contact;

    public Customer(String name, String contact) {
        this.id = 0; // ID wird von der Datenbank vergeben (Auto-Increment)
        this.name = name;
        this.contact = contact;
    }

    public Customer(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "Kunde #" + id + ": " + name + " (" + contact + ")";
    }
}
