package org.watts.shared.service;

import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;
import org.watts.transaction.model.Movimiento;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    // PRODUCTOS
    public void generarPdfProductos(List<Producto> productos, OutputStream outputStream) {
        generarPdfGenerico("Listado de Productos", 4, new float[]{15, 30, 40, 15},
                new String[]{"Código", "Nombre", "Características", "Estado"},
                outputStream,
                table -> {
                    for (Producto p : productos) {
                        addCell(table, p.getCodigoBase());
                        addCell(table, p.getNombre());
                        addCell(table, p.getCaracteristicasTecnicas());
                        addCell(table, p.isActivo() ? "ACTIVO" : "INACTIVO");
                    }
                });
    }

    public void generarCsvProductos(List<Producto> productos, OutputStream outputStream) {
        generarCsvGenerico(outputStream, writer -> {
            writer.println("Código,Nombre,Características,Estado");
            for (Producto p : productos) {
                writer.printf("%s,\"%s\",\"%s\",%s%n",
                        escapeCsv(p.getCodigoBase()),
                        escapeCsv(p.getNombre()),
                        escapeCsv(p.getCaracteristicasTecnicas()),
                        p.isActivo() ? "ACTIVO" : "INACTIVO"
                );
            }
        });
    }

    // VARIANTES
    public void generarPdfVariantes(List<Variante> variantes, OutputStream outputStream) {
        generarPdfGenerico("Listado de Variantes", 6, new float[]{20, 30, 10, 10, 15, 15},
                new String[]{"SKU", "Producto", "Talla", "Color", "P. Compra", "P. Venta"},
                outputStream,
                table -> {
                    for (Variante v : variantes) {
                        addCell(table, v.getSku());
                        addCell(table, v.getProducto().getNombre());
                        addCell(table, v.getTalla().getNombre());
                        addCell(table, v.getColor().getNombre());
                        addCell(table, CURRENCY_FORMATTER.format(v.getPrecioCompra()));
                        addCell(table, CURRENCY_FORMATTER.format(v.getPrecioVenta()));
                    }
                });
    }

    public void generarCsvVariantes(List<Variante> variantes, OutputStream outputStream) {
        generarCsvGenerico(outputStream, writer -> {
            writer.println("SKU,Producto,Talla,Color,Precio Compra,Precio Venta,Estado");
            for (Variante v : variantes) {
                writer.printf("%s,\"%s\",%s,%s,\"%s\",\"%s\",%s%n",
                        escapeCsv(v.getSku()),
                        escapeCsv(v.getProducto().getNombre()),
                        escapeCsv(v.getTalla().getNombre()),
                        escapeCsv(v.getColor().getNombre()),
                        v.getPrecioCompra(),
                        v.getPrecioVenta(),
                        v.isActivo() ? "ACTIVO" : "INACTIVO"
                );
            }
        });
    }

    // MOVIMIENTOS
    public void generarPdfMovimientos(List<Movimiento> movimientos, OutputStream outputStream) {
        generarPdfGenerico("Histórico de Movimientos", 8, new float[]{12, 10, 12, 22, 8, 8, 14, 14},
                new String[]{"Fecha", "Tipo", "SKU", "Producto", "Cant.", "Stock", "Almacén", "Creado por"},
                outputStream,
                table -> {
                    for (Movimiento m : movimientos) {
                        addCell(table, m.getFechaCreacion().format(DATE_FORMATTER));
                        addCell(table, m.getTipo().name());
                        addCell(table, m.getVariante().getSku());
                        String prod = m.getVariante().getProducto().getNombre() + " (" +
                                m.getVariante().getTalla().getNombre() + "/" +
                                m.getVariante().getColor().getNombre() + ")";
                        addCell(table, prod);
                        addCell(table, String.valueOf(m.getCantidad()));
                        addCell(table, String.valueOf(m.getStockResultante()));
                        addCell(table, m.getAlmacen().getCodigo());
                        addCell(table, m.getCreadoPor());
                    }
                });
    }

    public void generarCsvMovimientos(List<Movimiento> movimientos, OutputStream outputStream) {
        generarCsvGenerico(outputStream, writer -> {
            writer.println("Fecha,Tipo,SKU,Producto,Talla,Color,Cantidad,Stock Resultante,Almacén,Creado por,Observaciones");
            for (Movimiento m : movimientos) {
                writer.printf("%s,%s,%s,\"%s\",%s,%s,%d,%d,\"%s\",\"%s\",\"%s\"%n",
                        m.getFechaCreacion().format(DATE_FORMATTER),
                        m.getTipo(),
                        escapeCsv(m.getVariante().getSku()),
                        escapeCsv(m.getVariante().getProducto().getNombre()),
                        escapeCsv(m.getVariante().getTalla().getNombre()),
                        escapeCsv(m.getVariante().getColor().getNombre()),
                        m.getCantidad(),
                        m.getStockResultante(),
                        escapeCsv(m.getAlmacen().getCodigo()),
                        escapeCsv(m.getCreadoPor()),
                        escapeCsv(m.getObservaciones())
                );
            }
        });
    }

    // METODOS PRIVADOS HELPERS

    // Metodo que añade una celda a la tabla PDF
    private void addCell(PdfPTable table, String text) {
        table.addCell(text != null ? text : "");
    }

    // Metodo generico para generar PDF
    private void generarPdfGenerico(String titulo, int numColumnas, float[] anchos, String[] headers,OutputStream outputStream, TableContentProvider contentProvider) {
        try {
            Document document = new Document(PageSize.A4.rotate()); // Horizontal para que quepan más datos
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Título
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph(titulo, fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Tabla
            PdfPTable table = new PdfPTable(numColumnas);
            table.setWidthPercentage(100);
            if (anchos != null) {
                table.setWidths(anchos);
            }

            // Cabeceras
            Stream.of(headers).forEach(header -> {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            });

            // Llenar datos (callback)
            contentProvider.provide(table);

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    // Metodo generico para generar CSV
    private void generarCsvGenerico(OutputStream outputStream, CsvContentProvider contentProvider) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {

            writer.write('\ufeff'); // BOM para Excel
            contentProvider.provide(writer);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar CSV: " + e.getMessage(), e);
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"");
    }

    // Interfaces funcionales internas para los callbacks
    @FunctionalInterface
    interface TableContentProvider {
        void provide(PdfPTable table);
    }

    @FunctionalInterface
    interface CsvContentProvider {
        void provide(PrintWriter writer);
    }
}
