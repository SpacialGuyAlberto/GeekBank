package com.geekbank.bank.support.receipt.utils;

import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import com.geekbank.bank.order.dto.OrderRequest;
import com.geekbank.bank.support.receipt.model.Receipt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generador de recibos en PDF con estética de e-commerce premium.
 */
@Service
public class PdfGeneratorService {

    // ====================== PARÁMETROS DE DISEÑO =======================
    private static final float PAGE_WIDTH  = 226f;   // 80 mm
    private static final float PAGE_HEIGHT = 1500f;  // Alto generoso (se recorta al final)
    private static final float MARGIN      = 12f;

    private static final float LINE_SPACING    = 14f;
    private static final float SECTION_SPACING = 12f;

    // Columnas de la tabla de productos
    private static final float COL_ID_X    = MARGIN;
    private static final float COL_DESC_X  = MARGIN + 34;
    private static final float COL_QTY_X   = PAGE_WIDTH - MARGIN - 64; // 30 pt de ancho para Cant.
    private static final float COL_PRICE_X = PAGE_WIDTH - MARGIN -  6; // margen derecho

    // Fuentes
    private static final PDType1Font FONT_REG  = PDType1Font.HELVETICA;
    private static final PDType1Font FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final PDType1Font FONT_ITAL = PDType1Font.HELVETICA_OBLIQUE;

    // Tamaños de fuente
    private static final int SIZE_H1  = 16;
    private static final int SIZE_H2  = 13;
    private static final int SIZE_TXT = 9;

    @Autowired
    private KinguinService kinguinService;

    // ============================ API =================================

    public void generateReceiptPdf(Receipt receipt, String outputPath) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            doc.addPage(page);
            drawTicket(doc, page, receipt);
            doc.save(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] generateReceiptPdfBytes(Receipt receipt) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            doc.addPage(page);
            drawTicket(doc, page, receipt);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                doc.save(baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== LÓGICA PRINCIPAL DE DIBUJO ===================

    private void drawTicket(PDDocument doc, PDPage page, Receipt r) throws IOException {
        float y = page.getMediaBox().getHeight() - MARGIN;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

            // ---------- ENCABEZADO ----------
            y = center(cs, "ASTRALISBANK", FONT_BOLD, SIZE_H1, y);
            y = center(cs, "RECIBO DE COMPRA", FONT_BOLD, SIZE_H2, y - 2);
            y = solidLine(cs, y);

            // ---------- DATOS DEL CLIENTE ----------
            y -= SECTION_SPACING;
            y = keyVal(cs, "Cliente", safe(r.getCustomerName()), y);
            y = keyVal(cs, "Correo",  safe(r.getCustomerEmail()), y);
            y = keyVal(cs, "Orden",   safe(r.getTransactionId()), y);
            String fecha = r.getDate() != null
                    ? r.getDate()
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            y = keyVal(cs, "Fecha", fecha, y);
            y = dottedLine(cs, y - 2);

            // ---------- TABLA DE PRODUCTOS ----------
            List<OrderRequest.Product> prods = r.getProducts();
            double subtotal = 0d;
            if (prods != null && !prods.isEmpty()) {
                y -= SECTION_SPACING;
                y = left(cs, "Detalle de productos:", FONT_BOLD, SIZE_TXT + 1, MARGIN, y);
                y -= 2;

                // Encabezados de la tabla
                y = tableRow(cs,
                        new String[] { "ID", "Descripción", "Cant.", "Precio" },
                        y,
                        true);
                y = solidLine(cs, y - 2);

                // Cada producto
                for (OrderRequest.Product p : prods) {
                    KinguinGiftCard card = kinguinService.fetchGiftCardById(String.valueOf(p.getKinguinId()));
                    String name = card != null ? card.getName() : "Producto";

                    List<String> wrapped = wrap(name, COL_QTY_X - COL_DESC_X - 2, SIZE_TXT);

                    // Primera línea
                    y = tableRow(cs,
                            new String[] {
                                    String.valueOf(p.getKinguinId()),
                                    wrapped.get(0),
                                    String.valueOf(p.getQty()),
                                    hnl(p.getPrice() * p.getQty())
                            },
                            y,
                            false);

                    // Líneas adicionales de la descripción
                    for (int i = 1; i < wrapped.size(); i++) {
                        y = tableRow(cs, new String[] { "", wrapped.get(i), "", "" }, y, false);
                    }
                    y -= 1; // espacio entre productos
                    subtotal += p.getPrice() * p.getQty();
                }
                y = dottedLine(cs, y - 2);
            }

            // ---------- RESUMEN DE PAGO ----------
            double impuesto = subtotal * 0.15; // 15 % de ejemplo
            double total    = subtotal + impuesto;

            y = left(cs, "Resumen de pago:", FONT_BOLD, SIZE_TXT + 1, MARGIN, y - SECTION_SPACING);
            y = moneyLine(cs, "Subtotal", subtotal, y);
            y = moneyLine(cs, "Impuesto (15 %)", impuesto, y);
            y = moneyLine(cs, "Total a pagar", total, y, FONT_BOLD);
            y = solidLine(cs, y - 2);

            // ---------- PIE DE PÁGINA ----------
            y = center(cs, "¡Gracias por su preferencia!", FONT_ITAL, SIZE_TXT + 1, y - SECTION_SPACING);
        }
    }

    // ========================= HELPERS ================================

    private String hnl(double amount) {
        return String.format(Locale.US, "L%,.2f", amount);
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "N/D" : v;
    }

    // Texto centrado
    private float center(PDPageContentStream cs, String txt, PDType1Font font, int size, float y)
            throws IOException {
        float w = font.getStringWidth(txt) / 1000 * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset((PAGE_WIDTH - w) / 2, y);
        cs.showText(txt);
        cs.endText();
        return y - size - 4;
    }

    // Texto alineado a la izquierda
    private float left(PDPageContentStream cs, String txt, PDType1Font font, int size,
                       float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(txt);
        cs.endText();
        return y - LINE_SPACING;
    }

    // Clave:Valor
    private float keyVal(PDPageContentStream cs, String key, String val, float y) throws IOException {
        cs.beginText();
        cs.setFont(FONT_BOLD, SIZE_TXT);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(key + ": ");
        float kw = FONT_BOLD.getStringWidth(key + ": ") / 1000 * SIZE_TXT;
        cs.setFont(FONT_REG, SIZE_TXT);
        cs.newLineAtOffset(kw, 0);
        cs.showText(val);
        cs.endText();
        return y - LINE_SPACING;
    }

    // Línea de subtotal / impuesto / total
    private float moneyLine(PDPageContentStream cs, String label, double amount, float y)
            throws IOException {
        return moneyLine(cs, label, amount, y, FONT_REG);
    }

    private float moneyLine(PDPageContentStream cs, String label, double amount, float y,
                            PDType1Font font) throws IOException {
        cs.beginText();
        cs.setFont(font, SIZE_TXT);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(label);
        cs.endText();

        String val = hnl(amount);
        float w = font.getStringWidth(val) / 1000 * SIZE_TXT;
        drawText(cs, val, font, SIZE_TXT, COL_PRICE_X - w, y);
        return y - LINE_SPACING;
    }

    // Utilidad de texto genérica
    private void drawText(PDPageContentStream cs, String txt, PDType1Font font, int size,
                          float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(txt);
        cs.endText();
    }

    // Fila de la tabla de productos
    private float tableRow(PDPageContentStream cs, String[] cols, float y, boolean header)
            throws IOException {
        PDType1Font font = header ? FONT_BOLD : FONT_REG;

        // ID
        drawText(cs, cols[0], font, SIZE_TXT, COL_ID_X, y);

        // Descripción
        drawText(cs, cols[1], font, SIZE_TXT, COL_DESC_X, y);

        // Cantidad
        float qtyW = font.getStringWidth(cols[2]) / 1000 * SIZE_TXT;
        drawText(cs, cols[2], font, SIZE_TXT,
                COL_QTY_X + (30 - qtyW) / 2, y);

        // Precio
        float priceW = font.getStringWidth(cols[3]) / 1000 * SIZE_TXT;
        drawText(cs, cols[3], font, SIZE_TXT,
                COL_PRICE_X - priceW, y);

        return y - LINE_SPACING;
    }

    // Línea sólida
    private float solidLine(PDPageContentStream cs, float y) throws IOException {
        cs.setLineWidth(0.7f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_WIDTH - MARGIN, y);
        cs.stroke();
        return y - 8f;
    }

    // Línea punteada
    private float dottedLine(PDPageContentStream cs, float y) throws IOException {
        cs.setLineWidth(0.7f);
        cs.setLineDashPattern(new float[] { 2, 2 }, 0);
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_WIDTH - MARGIN, y);
        cs.stroke();
        cs.setLineDashPattern(new float[0], 0); // reset
        return y - 8f;
    }

    /**
     * Envuelve texto para que se ajuste a un ancho máximo.
     *
     * @param text      Texto de entrada
     * @param maxWidth  Ancho disponible en puntos
     * @param fontSize  Tamaño de fuente
     * @return Lista de líneas envueltas
     */
    private List<String> wrap(String text, float maxWidth, int fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        float spaceW = FONT_REG.getStringWidth(" ") / 1000 * fontSize;
        float currentW = 0f;

        for (String w : words) {
            float wordW = FONT_REG.getStringWidth(w) / 1000 * fontSize;
            if (currentW + wordW + spaceW > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current.setLength(0);
                currentW = 0f;
            }
            if (currentW > 0) {
                current.append(' ');
                currentW += spaceW;
            }
            current.append(w);
            currentW += wordW;
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }
}
