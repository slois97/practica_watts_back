package org.watts.shared.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${ftp.server}")
    private String server;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.user}")
    private String user;

    @Value("${ftp.password}")
    private String password;

    @Value("${ftp.path}")
    private String basePath;

    // Metodo auxiliar para conectar con el servidor FTP
    private FTPClient conectar() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);
        ftpClient.enterLocalPassiveMode(); // Necesario para Sered
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // Necesario para imágenes y PDFs
        return ftpClient;
    }

    private void desconectar(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ioe) {
            // Ignoramos errores al desonectar
        }
    }

    public String store(MultipartFile file) {
        FTPClient ftpClient = null;
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: archivo vacío.");
            }

            // Generar nombre único para evitar colisiones
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            ftpClient = conectar();

            // Intentamos cambiar al directorio base
            boolean cambiado = ftpClient.changeWorkingDirectory(basePath);
            if (!cambiado) {
                // Si no pudimos entrar, probamos a crearla
                System.out.println("La ruta " + basePath + " no existe. Creando...");
                boolean creado = ftpClient.makeDirectory(basePath);

                if (creado) {
                    // Si se creó, entramos en ella
                    ftpClient.changeWorkingDirectory(basePath);
                } else {
                    throw new RuntimeException("No se pudo crear el directorio en FTP: " + basePath +
                            ". Verifica permisos o que la ruta padre exista.");
                }
            }
            // Try-with-resources para cerrar el inputStream automáticamente
            try (InputStream inputStream = file.getInputStream()) {
                boolean done = ftpClient.storeFile(filename, inputStream);
                if (!done) {
                    throw new RuntimeException("No se pudo subir el archivo al FTP.");
                }
            }
            return filename;
        } catch (IOException ioe) {
            throw new RuntimeException("Fallo al guardar el archivo en FTP", ioe);
        } finally {
            desconectar(ftpClient);
        }
    }

    public void delete(String filename) {
        FTPClient ftpClient = null;
        try {
            ftpClient = conectar();
            ftpClient.changeWorkingDirectory(basePath);

            boolean borrado = ftpClient.deleteFile(filename);
            if (!borrado) {
                // Si entra aquí, Java no encuentra el archivo
                System.err.println("ADVERTENCIA: No se pudo borrar el archivo del FTP (o no existía): " + filename);
            } else {
                System.out.println("Archivo borrado correctamente.");
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo borrar el archivo físico del FTP: " + filename + ". Error: " + e.getMessage(), e);
        } finally {
            desconectar(ftpClient);
        }
    }

    public Resource loadAsResource(String filename) {
        FTPClient ftpClient = null;
        try {
            ftpClient = conectar();
            ftpClient.changeWorkingDirectory(basePath);

            // Descargamos el archivo a memoria
            try (InputStream inputStream = ftpClient.retrieveFileStream(filename)) {
                if (inputStream == null) {
                    throw new RuntimeException("No se pudo encontrar el archivo en FTP: " + filename);
                }

                byte[] bytes = inputStream.readAllBytes();
                ftpClient.completePendingCommand();

                return new ByteArrayResource(bytes);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error al descargar archivo del FTP: " + filename, ioe);
        } finally {
            desconectar(ftpClient);
        }
    }
}