package com.example.pdf;

import com.example.examplefeature.Task;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PdfExportService {

    private static final float MARGIN = 50f;
    private static final float LEADING = 16f;
    private static final float TITLE_SIZE = 18f;
    private static final float TEXT_SIZE = 12f;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ByteArrayInputStream exportTasks(List<Task> tasks) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getHeight() - MARGIN;

            // Título
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, TITLE_SIZE);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("ToDoTasks - Lista de Tarefas");
            cs.endText();
            y -= 28f;

            // Cabeçalho
            cs.setFont(PDType1Font.HELVETICA_BOLD, TEXT_SIZE);
            float[] colX = new float[] { MARGIN, MARGIN + 40, MARGIN + 300, MARGIN + 450 };
            y = drawRow(cs, y, colX);
            cs.setFont(PDType1Font.HELVETICA, TEXT_SIZE);

            int idx = 1;
            for (Task t : tasks) {
                String n = String.valueOf(idx++);
                String desc = safe(t.getDescription());
                String due = t.getDueDate() != null ? DATE_FMT.format(t.getDueDate()) : "-";
                String created = DATETIME_FMT.format(t.getCreationDate().atZone(ZoneId.systemDefault()));

                // wrap da descrição
                List<String> wrapped = wrap(desc,
                        page.getMediaBox().getWidth() - colX[1] - 170f);

                // verifica espaço
                int neededLines = Math.max(1, wrapped.size());
                if (y - neededLines * LEADING < MARGIN) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - MARGIN;

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, TITLE_SIZE);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText("ToDoTasks - Lista de Tarefas (cont.)");
                    cs.endText();
                    y -= 28f;

                    cs.setFont(PDType1Font.HELVETICA_BOLD, TEXT_SIZE);
                    y = drawRow(cs, y, colX);
                    cs.setFont(PDType1Font.HELVETICA, TEXT_SIZE);
                }

                // primeira linha
                cs.beginText(); cs.newLineAtOffset(colX[0], y); cs.showText(n); cs.endText();
                //cs.beginText(); cs.newLineAtOffset(colX[1], y); cs.showText(wrapped.getFirst()); cs.endText();
                cs.beginText(); cs.newLineAtOffset(colX[1], y); cs.showText(wrapped.get(0)); cs.endText();
                cs.beginText(); cs.newLineAtOffset(colX[2], y); cs.showText(due); cs.endText();
                cs.beginText(); cs.newLineAtOffset(colX[3], y); cs.showText(created); cs.endText();
                y -= LEADING;

                // linhas extra
                for (int i = 1; i < wrapped.size(); i++) {
                    if (y - LEADING < MARGIN) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = page.getMediaBox().getHeight() - MARGIN;

                        cs.beginText();
                        cs.setFont(PDType1Font.HELVETICA_BOLD, TITLE_SIZE);
                        cs.newLineAtOffset(MARGIN, y);
                        cs.showText("ToDoTasks - Lista de Tarefas (cont.)");
                        cs.endText();
                        y -= 28f;

                        cs.setFont(PDType1Font.HELVETICA, TEXT_SIZE);
                    }
                    cs.beginText(); cs.newLineAtOffset(colX[1], y); cs.showText(wrapped.get(i)); cs.endText();
                    y -= LEADING;
                }
            }

            cs.close();
            doc.save(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Falha ao gerar PDF", e);
        }
    }

    private static float drawRow(PDPageContentStream cs, float y, float[] colX) throws IOException {
        cs.beginText(); cs.newLineAtOffset(colX[0], y); cs.showText("#"); cs.endText();
        cs.beginText(); cs.newLineAtOffset(colX[1], y); cs.showText("Descrição"); cs.endText();
        cs.beginText(); cs.newLineAtOffset(colX[2], y); cs.showText("Prazo"); cs.endText();
        cs.beginText(); cs.newLineAtOffset(colX[3], y); cs.showText("Criada em"); cs.endText();
        return y - LEADING;
    }

    private static List<String> wrap(String text, float maxWidth) throws IOException {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) { out.add(""); return out; }
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String candidate = line.isEmpty() ? w : line + " " + w;
            float width = PDType1Font.HELVETICA.getStringWidth(candidate) / 1000 * 12;
            if (width <= maxWidth) {
                line.setLength(0); line.append(candidate);
            } else {
                if (!line.isEmpty()) out.add(line.toString());
                line.setLength(0); line.append(w);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
