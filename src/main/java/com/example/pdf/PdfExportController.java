package com.example.pdf;

import com.example.examplefeature.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class PdfExportController {

    private final TaskService taskService;
    private final PdfExportService pdfExportService;

    public PdfExportController(TaskService taskService) {
        this.taskService = taskService;
        this.pdfExportService = new PdfExportService();
    }

    @GetMapping(value = "/api/exports/tasks.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportTasksPdf() {
        var tasks = taskService.list(Pageable.unpaged());
        var in = pdfExportService.exportTasks(tasks);

        var now = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
        var filename = "tasks-" + now + ".pdf";

        byte[] bytes = in.readAllBytes();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
