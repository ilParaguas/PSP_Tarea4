
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Cliente {

    static final String Host = "localhost";
    static final int Puerto = 2000;

    public Cliente() {

        // Conexión al servidor, el socket se cierra solo al terminar (try-with-resources)
        try (Socket skCliente = new Socket(Host, Puerto)) {
            // Apertura de conexión

            System.out.println("Conectando al servidor en: " + InetAddress.getByName("localhost") + " \n");
            System.out.println("Conexión establecida\n");

            // Defino el flujo de entrada de datos desde el servidor
            InputStream entrada = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(entrada);

            // Defino el flujo de salida de datos hacia el servidor
            OutputStream salida = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(salida);

            // Generación aleatoria de pedido. 
            // Envío de pedido
            // Producto
            flujo_salida.writeUTF("" + (int) (Math.random() * 10));
            // Cantidad
            flujo_salida.writeUTF("" + (int) (Math.random() * 100));
            // Recepción de respuesta
            // Muestra respuesta en consola
            System.out.println(flujo_entrada.readUTF());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Cliente desconectado.");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Cliente aux = new Cliente();
        }
    }
}
