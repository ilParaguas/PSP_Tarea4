La clase producto define los datos de cada producto, en el caso de esta tarea, cada producto tiene la información únicamente del nombre y de la cantidad de stock disponible. 

La clase cliente genera una cantidad aleatoria de peticiones (en este caso, entre 1 y 7, para simular todos los casos de respuesta posibles). Cada petición tiene también una cantidad solicitada aleatoria. Cada cliente generado se conecta con el mismo servidor, y recibe la respuesta una vez se ha procesado el pedido, mostrando el resultado por pantalla.

La clase servidor, al iniciar, realiza un carga de datos leyendo la información del archivo .properties, almacenando los datos de los productos en una lista de productos. Tras la carga de datos, el servidor abre la comunicación a la espera de la conexión por parte de un cliente. Cuando un cliente se conecta, gestiona las operaciones iniciando un thread, para poder así gestionar múltiples clientes de manera simultánea. 

Para gestionar la petición de un cliente, el servidor primero comprueba si el producto pedido se encuentra en el almacén. Si no es así, devuelve un mensaje de error notificándoselo al cliente. En caso de que el producto se encuentre en el almacén, entonces comprueba si el stock disponible es mayor o igual que la cantidad pedida. Si no es así, devuelve otro mensaje de error al cliente. En caso de que el stock sea suficiente, actualiza el stock del almacén, y devuelve la confirmación del pedido al cliente. 

Cuando el servidor termina de procesar un pedido, registra los datos y el resultado del pedido en el archivo de registro .log.

El servidor no cierra nunca la conexión, para simular que es un servidor real que siempre está disponible y a la espera de conexiones de clientes.
