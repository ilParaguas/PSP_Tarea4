
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Servidor extends Thread {

    static final int Puerto = 2000;

    private List<Producto> listaProductos = new ArrayList<>();
    private Socket skCliente = null;

    // Constructor vacio
    public Servidor() {
    }

    /**
     * Constructor con parametros
     *
     * @param skCliente Socket con el que se conecta el cliente
     * @param listaProductos Lista de productos que hay en el almacén
     */
    public Servidor(Socket skCliente, List<Producto> listaProductos) {
        this.skCliente = skCliente;
        this.listaProductos = listaProductos;
    }

    /**
     * Lee los datos del archivo del archivo de configuración, creando un
     * producto por cada línea del archivo
     */
    public void cargaProductos() {
        // Lectura del archivo
        try (BufferedReader br = new BufferedReader(new FileReader("config_stock.properties"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split("=");
                // Creacion de producto con los datos de la línea
                Producto aux = new Producto(datos[0], Integer.parseInt(datos[1]));
                this.listaProductos.add(aux);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Registra en el archivo de registro los detalles de cada pedido realizado.
     *
     * @param nombre Nombre del producto solicitado
     * @param cantidad Cantidad solicitada
     * @param resultado Resultado del pedido (si ha sido aceptado o no)
     */
    public void logPedido(String nombre, int cantidad, String resultado) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("pedidos.log", true))) {
            // Obtengo fecha actual formateada
            LocalDateTime fecha = LocalDateTime.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaFormat = fecha.format(formato);

            // Obtengo mensaje a escribir
            String registro = "[" + fechaFormat + "] PEDIDO - Almacén: "
                    + this.skCliente.getRemoteSocketAddress()
                    + " - Producto: " + nombre + " - Cantidad: " + cantidad + " - " + resultado;

            // Escritura de mensaje
            bw.newLine();
            bw.write(registro);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Método auxiliar para borrar los contenidos del archivo .log
     */
    public void resetLog() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("pedidos.log"))) {
            bw.write("");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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

    /**
     * Comprueba si hay stock suficiente para atender el pedido
     *
     * @param producto Producto solicitado
     * @param cantidad Cantidad solicitada
     * @return Devuelve true si hay más stock que cantidad se ha solicitado y
     * false en caso contrario
     */
    public boolean hayStock(Producto producto, int cantidad) {
        return cantidad <= producto.getStock();
    }

    /**
     * Procesamiento del pedido. Primero se comprueba si se dispone del producto
     * solicitado. Si el producto se encuentra en el almacén, se comprueba si
     * hay suficiente stock para atender la petición. Por último, se devuelve al
     * cliente un mensaje con el resultado del pedido.
     *
     * @param nombreProducto Producto pedido
     * @param cantidad Cantidad solicitada
     * @return Devuelve mensaje de error si no hay suficiente stock del producto
     * o se pide un producto que no existe. En caso de que se pueda atender la
     * peticion, devuelve los detalles solicitados por el cliente.
     */
    public String procesarPedido(String nombreProducto, int cantidad) {
        String result;
        String resultadoLog;
        Producto aux = encontrarProducto(nombreProducto);
        // Buscar producto
        if (aux != null) {
            if (hayStock(aux, cantidad)) {
                // Actualizacion del stock
                aux.setStock(aux.getStock() - cantidad);
                result = "Pedido aceptado: " + nombreProducto + " - Cantidad: " + cantidad;
                resultadoLog = "ACEPTADO";
                System.out.println("Pedido procesado: " + nombreProducto + ", cantidad: " + cantidad + " - ACEPTADO");
            } else {
                result = "Pedido rechazado: Stock insuficiente.";
                resultadoLog = "RECHAZADO (Stock insuficiente)";
                System.out.println("Pedido procesado: " + nombreProducto + ", cantidad: " + cantidad + " - RECHAZADO (Stock insuficiente)");
            }
        } else {
            result = "Pedido rechazado: No hay ningún producto con el nombre solicitado.";
            resultadoLog = "RECHAZADO (Producto no encontrado)";
            System.out.println("Pedido procesado: " + nombreProducto + ", cantidad: " + cantidad + " - RECHAZADO (Producto no encontrado)");
        }

        // Escritura de resultado en archivo .log
        logPedido(nombreProducto, cantidad, resultadoLog);
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
            String nombreProducto = "producto" + flujo_entrada.readUTF();
            // Entra cantidad (parametro 2)
            int cantidadProducto = Integer.parseInt(flujo_entrada.readUTF());

            // Respuesta al cliente. 
            flujo_salida.writeUTF(procesarPedido(nombreProducto, cantidadProducto));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void main(String[] args) {

        // Carga de datos mediante archivo con stock inicial
        cargaProductos();

        // Limpieza de datos del archivo .log en caso de que ya tuviera datos de una ejecución previa
        resetLog();

        // Apertura de conexion del servidor
        try (ServerSocket skServidor = new ServerSocket(Puerto)) {

            System.out.println("Servidor de pedidos iniciado en el puerto " + Puerto);
            while (true) {

                // El servidor espera a que algún cliente se conecte
                Socket skCliente = skServidor.accept();
                // Lanza thread para el cliente conectado
                new Servidor(skCliente, this.listaProductos).start();

                // No hay cierre de conexión, se simula que el servidor está siempre
                //  a la espera de conexiones por parte de los clientes.
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }
}
