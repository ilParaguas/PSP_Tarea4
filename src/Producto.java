
public class Producto {

    private String nombre;
    private int stock;

    // Constructor
    public Producto(String nombre, int stock) {
        this.nombre = nombre;
        this.stock = stock;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setCodigo(String nombre) {
        this.nombre = nombre;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

}
