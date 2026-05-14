
import java.io.*;
import java.net.*;

public class Cliente {

    static final String Host = "localhost";
    static final int Puerto = 2000;

    public Cliente() {

        // Conexión al servidor, el socket se cierra solo al terminar (try-with-resources)
        try (Socket skCliente = new Socket(Host, Puerto)) {
            System.out.println("Conectado al servidor de pedidos.");

            // Defino el flujo de entrada de datos desde el servidor
            InputStream entrada = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(entrada);

            // Defino el flujo de salida de datos hacia el servidor
            OutputStream salida = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(salida);

            // Generación aleatoria de pedido. 
            // Producto aleatorio
            String nombre = "" + (int) (Math.random() * 7);
            // Envío de nombre
            flujo_salida.writeUTF(nombre);
            // Cantidad aleatoria
            String cantidad = "" + (int) (Math.random() * 300);
            // Envío de cantida
            flujo_salida.writeUTF(cantidad);

            System.out.println("Ingrese su pedido: producto" + nombre + "," + cantidad);
            // Recepción de respuesta
            // Muestra respuesta en consola
            System.out.println(flujo_entrada.readUTF() + "\n");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Cliente aux = new Cliente();
        }
    }
}
