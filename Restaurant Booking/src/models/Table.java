package models;

public class Table {
    protected int id;
    protected int capacity;

    public Table(int capacity) {
        this.id = 0; // ID wird automatisch von der Datenbank vergeben (Auto-Increment)
        this.capacity = capacity;
    }

    public Table(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Tisch #" + id + " (Plätze: " + capacity + ")";
    }
}
