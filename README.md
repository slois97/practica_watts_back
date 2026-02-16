# WATTS - Backend

API RESTful desarrollada con **Java 21** y **Spring Boot 3.2.6** para la gestión integral de inventarios, almacenes y proyectos de *Watts Cycling*.

## 🚀 Tecnologías

* **Java 21 (LTS)**
* **Spring Boot 3.2.6** (Web, Security, Data JPA, Validation, Mail)
* **MySQL 8** (Base de datos)
* **JWT (0.11.5)** (Autenticación Stateless)
* **MapStruct 1.6.3** (Mapeo Entidad-DTO)
* **Lombok** (Reducción de boilerplate)
* **OpenPDF (LibrePDF 3.0)** (Generación de reportes)
* **Apache Commons Net** (Cliente FTP para archivos)
* **SpringDoc OpenAPI** (Documentación Swagger)
* **Docker** (Contenerización)

## 📋 Requisitos Previos

* JDK 21 instalado.
* Maven 3.8+ instalado.
* MySQL Server 8 en ejecución.
* Servidor FTP (para almacenamiento de archivos).
* Servidor SMTP (para envío de notificaciones).

## ⚙️ Configuración (Variables de Entorno)

El proyecto utiliza un archivo `application.yaml` que lee variables de entorno del sistema. Si estas variables no existen, se usarán los valores por defecto (indicados entre paréntesis) para el desarrollo local.

### Base de Datos
| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `DB_HOST` | Host del servidor MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `watts_inventario` |
| `DB_USERNAME` | Usuario de conexión | `root` |
| `DB_PASSWORD` | Contraseña de conexión | `root` |

### Seguridad (JWT)
| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `JWT_SECRET` | Clave secreta para firmar tokens (Min 64 chars) | *(Clave de desarrollo insegura)* |
| `JWT_EXPIRATION` | Tiempo de vida del token | `86400000` (24 horas) - *Fijo* |

### Almacenamiento (FTP)
| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `FTP_SERVER` | Host del servidor FTP | `localhost` |
| `FTP_PORT` | Puerto FTP | `21` |
| `FTP_USER` | Usuario FTP | `user` |
| `FTP_PASSWORD` | Contraseña FTP | `user` |
| `FTP_PATH` | Ruta base para subir archivos | `/fileuploads/` |

### Notificaciones (Email)
| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `MAIL_HOST` | Servidor SMTP | `localhost` |
| `MAIL_PORT` | Puerto SMTP | `465` |
| `MAIL_USERNAME` | Usuario de correo | `usuario` |
| `MAIL_PASSWORD` | Contraseña de correo | `contraseña` |
| `MAIL_SSL_ENABLED`| Activar SSL para SMTP | `false` |

## 🛠️ Instalación y Ejecución Local

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/AngelVP-98/watts-back.git](https://github.com/AngelVP-98/watts-back.git)
    cd watts-back
    ```

2.  **Base de Datos:**
    Asegúrate de tener un servidor MySQL corriendo. El sistema intentará conectarse a `localhost:3306` con usuario `root` y contraseña `root` por defecto. La base de datos `watts_inventario` debe existir (o JPA intentará crearla/actualizarla).

3.  **Ejecutar la aplicación:**
    ```bash
    mvn spring-boot:run
    ```
    La API estará disponible en `http://localhost:8080`.

## 🐳 Despliegue con Docker

Para desplegar la aplicación en un contenedor (ej. Coolify, VPS), se recomienda pasar las variables de entorno críticas:

1.  **Construir la imagen:**
    ```bash
    docker build -t watts-backend .
    ```

2.  **Ejecutar el contenedor:**
    ```bash
    docker run -d -p 8080:8080 \
      -e DB_HOST=host.docker.internal \
      -e DB_NAME=watts_inventario \
      -e DB_USERNAME=mi_usuario_prod \
      -e DB_PASSWORD=mi_password_prod \
      -e JWT_SECRET=MI_CLAVE_SECRETA_MUY_LARGA_Y_SEGURA_PARA_PROD \
      -e FTP_SERVER=ftp.midominio.com \
      -e FTP_USER=ftpuser \
      -e FTP_PASSWORD=ftppass \
      watts-backend
    ```

## 📚 Documentación API

Una vez iniciada la aplicación, puedes acceder a la documentación interactiva (Swagger UI) para probar los endpoints:

* **URL:** `http://localhost:8080/swagger-ui.html`

## 👥 Autores

* **Angel Verdeguer Parreño**
* **Sergio Lois Arcas**