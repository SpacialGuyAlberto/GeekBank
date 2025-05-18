package com.geekbank.bank.support.receipt.utils;

import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.order.dto.OrderRequest;
import com.geekbank.bank.support.receipt.model.Receipt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfGeneratorService {

    private static final String LOGO_URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvQiHd1F1cj2hGKvE6tzoKqjcUgaQMYT3JLg&s";

    @Autowired
    KinguinService kinguinService;

    public void generateReceiptPdf(Receipt receipt, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            drawProfessionalReceipt(document, page, receipt);
            document.save(outputPath);
            System.out.println("PDF generated at: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] generateReceiptPdfBytes(Receipt receipt) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            drawProfessionalReceipt(document, page, receipt);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                document.save(baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void drawProfessionalReceipt(PDDocument document, PDPage page, Receipt receipt) throws IOException {
        float margin = 50;
        float yStart = page.getMediaBox().getHeight() - margin;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // Cargar logo
            PDImageXObject logoImage = null;
            try (InputStream in = new URL(LOGO_URL).openStream()) {
                logoImage = PDImageXObject.createFromByteArray(document, in.readAllBytes(), "logo");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Dibujar el logo
            if (logoImage != null) {
                float scale = 0.3f;
                float imgWidth = logoImage.getWidth() * scale;
                float imgHeight = logoImage.getHeight() * scale;
                float startX = (page.getMediaBox().getWidth() - imgWidth) / 2;
                contentStream.drawImage(logoImage, startX, yStart - imgHeight, imgWidth, imgHeight);
                yStart -= (imgHeight + 20);
            }

            // Título del Banco
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("Astralisbank");
            contentStream.endText();
            yStart -= 30;

            // Título del Recibo
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("Factura de compra");
            contentStream.endText();
            yStart -= 20;

            drawLine(contentStream, margin, yStart, page.getMediaBox().getWidth() - margin);
            yStart -= 20;

            float leftColumnX = margin;
            float rightColumnX = margin + 200;
            float textLeading = 14.5f;

            // Información del cliente y transacción
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.setLeading(textLeading);
            contentStream.newLineAtOffset(leftColumnX, yStart);
            contentStream.showText("Nombre del cliente:");
            contentStream.newLine();
            contentStream.showText("Email del cliente:");
            contentStream.newLine();
            contentStream.showText("Numero de transaccion");
            contentStream.newLine();
            contentStream.showText("Precio (USD)");
            contentStream.newLine();
            contentStream.showText("Fecha:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.setLeading(textLeading);
            contentStream.newLineAtOffset(rightColumnX, yStart);
            contentStream.showText(receipt.getCustomerName() != null ? receipt.getCustomerName() : "N/A");
            contentStream.newLine();
            contentStream.showText(receipt.getCustomerEmail() != null ? receipt.getCustomerEmail() : "N/A");
            contentStream.newLine();
            contentStream.showText(receipt.getTransactionId() != null ? receipt.getTransactionId() : "N/A");
            contentStream.newLine();
            contentStream.showText("$" + String.format("%.2f", receipt.getAmountUsd()));
            contentStream.newLine();
            contentStream.showText(receipt.getDate() != null ? receipt.getDate() : "N/A");
            contentStream.endText();

            yStart -= (5 * textLeading + 20);

            drawLine(contentStream, margin, yStart, page.getMediaBox().getWidth() - margin);
            yStart -= 20;

            // Sección de productos si existen
            List<OrderRequest.Product> products = receipt.getProducts();
            if (products != null && !products.isEmpty()) {
                // Encabezados de la tabla de productos
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText("Products Purchased:");
                contentStream.endText();
                yStart -= 20;

                float tableStartY = yStart;
                float col1X = margin;
                float col2X = margin + 150;
                float col3X = margin + 250;
                float col4X = margin + 350;
                float rowHeight = 15;
                float maxWidthDesc = (page.getMediaBox().getWidth() - margin) - col2X; // ancho para wrap

                // Encabezados
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(col1X, tableStartY);
                contentStream.showText("Product ID");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(col2X, tableStartY);
                contentStream.showText("Description");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(col3X, tableStartY);
                contentStream.showText("Quantity");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(col4X, tableStartY);
                contentStream.showText("Price (HNL)");
                contentStream.endText();

                yStart = tableStartY - 20;
                drawLine(contentStream, margin, yStart, page.getMediaBox().getWidth() - margin);
                yStart -= rowHeight;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (OrderRequest.Product product : products) {
                    KinguinGiftCard giftCard = kinguinService.fetchGiftCardById(String.valueOf(product.getKinguinId()));
                    String productName = giftCard.getName();

                    // Wrap del texto de productName
                    List<String> wrappedLines = wrapText(productName, maxWidthDesc, 12, PDType1Font.HELVETICA);

                    int maxLines = wrappedLines.size();
                    if (maxLines < 1) maxLines = 1;

                    // ID
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col1X, yStart);
                    contentStream.showText(String.valueOf(product.getKinguinId()));
                    contentStream.endText();

                    // Imprimir las líneas envueltas del productName
                    for (int i = 0; i < wrappedLines.size(); i++) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(col2X, yStart - (i * rowHeight));
                        contentStream.showText(wrappedLines.get(i));
                        contentStream.endText();
                    }

                    // Cantidad y Precio en la primera línea
                    contentStream.beginText();
                    contentStream.newLineAtOffset(col3X, yStart);
                    contentStream.showText(String.valueOf(product.getQty()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(col4X, yStart);
                    contentStream.showText("$" + String.format("%.2f", product.getPrice()));
                    contentStream.endText();

                    yStart -= (rowHeight * maxLines) + 5; // espacio entre productos
                }

                yStart -= 20;
                drawLine(contentStream, margin, yStart + rowHeight, page.getMediaBox().getWidth() - margin);
            }

            yStart -= 30;

            // Mensaje de agradecimiento
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("Gracias por confiar en nosotros. Que la fuerza te acompañe.");
            contentStream.endText();

        }
    }

    private List<String> wrapText(String text, float width, float fontSize, PDType1Font font) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null) {
            lines.add("");
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        float spaceWidth = font.getStringWidth(" ") / 1000f * fontSize;
        float currentWidth = 0;

        for (String word : words) {
            float wordWidth = font.getStringWidth(word) / 1000f * fontSize;
            if (currentWidth + wordWidth + spaceWidth > width && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentWidth = 0;
            }
            if (currentWidth > 0) {
                currentLine.append(" ");
                currentWidth += spaceWidth;
            }
            currentLine.append(word);
            currentWidth += wordWidth;
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void drawLine(PDPageContentStream contentStream, float startX, float y, float endX) throws IOException {
        contentStream.setLineWidth(1);
        contentStream.moveTo(startX, y);
        contentStream.lineTo(endX, y);
        contentStream.stroke();
    }
}
