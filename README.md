# Chat1.0

## Descripción

Es un chat en modo texto (tanto servidor
como cliente), con una solución basada en sockets. Permite levantar un servidor y 
ejecutar clientes que se conecten al servidor.

## Comandos de Ejecución

### Compilación del Proyecto

Para compilar el proyecto, utiliza el siguiente comando:
> ant compile

### Ejecución del servidor

Para levantar el servidor utiliza el siguiente comando:
> ant run-server

### Ejecución de un cliente

Para conectarse al servidor como un cliente utiliza el siguiente comando:
> ant run-client

(Es posible que pida introducir el nombre que desea tener el cliente)

### Generar el JavaDoc
> ant javadoc


## Ejecución del Chat
Se muestra en la siguiente imagen una prueba, se levanta el servidor y se van conectando 
distintos clientes, que a su vez mandan mensajes según van entrando al servidor.

![Img de la prueba](Captura_prueba%20conexion..png)

## Requisitos
Se ha utilizado las siguientes versiones para crear este proyecto:
- java version 1.8.0_361
- Apache Ant(TM) version 1.10.15
- Apache Maven 3.8.8

Se ha trabajado desde el IDE intelliJ con el arquetipo de proyecto de maven *quickstart*.



