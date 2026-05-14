
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor extends Thread {

    static final int Puerto = 2000;

    private List<Producto> listaProductos = new ArrayList<>();
    Socket skCliente = null;

    public Servidor(Socket skCliente, List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
        this.skCliente = skCliente;
    }

    /**
     * Lee los datos del archivo del archivo de configuración, creando un
     * producto por cada línea del archivo
     */
    public void cargaProductos() {
        System.out.println("\nCargando stock de productos...");
        // Lectura del archivo
        try (BufferedReader br = new BufferedReader(new FileReader("config_stock.properties"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                // Creacion de producto con los datos de la línea
                Producto aux = new Producto(datos[0], Integer.parseInt(datos[1]));
                this.listaProductos.add(aux);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Productos cargados: " + this.listaProductos.size());
    }

    /**
     * Busca en la lista de productos del servidor el que coincida con el nombre
     * proporcionado. Si no lo encuentra devuelve null por defecto.
     *
     * @param nombre Nombre del producto a buscar.
     * @return Resultado de la busqueda. Sera null si ningun producto coincide
     * con el nombre
     */
    public Producto encontrarProducto(String nombre) {
        Producto result = null;
        boolean enc = false;
        int i = 0;

        while (i < listaProductos.size() && !enc) {
            Producto aux = listaProductos.get(i);
            if (aux.getNombre().equals(nombre)) {
                enc = true;
                result = aux;
            }
            i++;
        }

        return result;
    }

    public boolean hayStock(Producto producto, int cantidad) {
        return cantidad <= producto.getStock();
    }

    public String procesarPedido(String nombreProducto, int cantidad) {
        String result;
        Producto aux = encontrarProducto(nombreProducto);
        // Buscar producto
        if (aux != null) {
            if (hayStock(aux, cantidad)) {
                // Actualizacion del stock
                aux.setStock(aux.getStock() - cantidad);
                result = "Pedido aceptado: " + nombreProducto + " - Cantidad: " + cantidad;
            } else {
                result = "Pedido rechazadi: Stock insuficiente.";
            }
        } else {
            result = "ERROR:::No hay ningún producto con el nombre solicitado: " + nombreProducto + ".";
        }
        return result;
    }

    @Override
    public void run() {
        try {
            // ProcesaPedido
            // Defino flujo de salida del servidor
            OutputStream aux = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(aux);

            // Defino flujo de entrada del servidor
            InputStream entrada = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(entrada);

            // Entra nombre (parametro 1)
            String nombreProducto = flujo_entrada.readUTF();
            if (encontrarProducto(nombreProducto) != null) {
                // Entrada del cliente. ¿array? Necesito nombre + cantidad
                // Entra cantidad (parametro 2)
                flujo_salida.writeUTF(procesarPedido(nombreProducto, Integer.parseInt(flujo_entrada.readUTF())));
            } else {
                System.out.println("ERROR:::PRODUCTO NO ENCONTRADO");
                flujo_salida.writeUTF("ERROR:::PRODUCTO NO ENCONTRADO");
            }
            System.out.println("PEDIDO PROCESADO");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void main(String[] args) {

        // Apertura de conexion del servidor
        try (ServerSocket skServidor = new ServerSocket(Puerto)) {
            // Carga de datos mediante archivo con stock inicial
            cargaProductos();

            // El servidor espera a que algún cliente se conecte
            Socket skCliente = skServidor.accept();

            // Lanza thread para el cliente conectado
            new Servidor(skCliente, this.listaProductos).start();

            // Cierre de conexion
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Servidor finalizado.");
    }
}
