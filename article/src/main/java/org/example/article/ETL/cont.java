package org.example.article.ETL;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/mein")
public class cont {

    private final ETLService service;

    public cont(ETLService service) {
        this.service = service;
    }

    @PostMapping("/import")
    public ResponseEntity<Long> importFile(
            @RequestParam("file") MultipartFile file,   // <-- must match Postman key
            @RequestParam("label") String label,
            @RequestParam("importedBy") long importedBy,
            @RequestParam(value = "activateAfter", defaultValue = "true") boolean activateAfter
    ) throws Exception {
        Long versionId = service.importExcel(file.getBytes(),file.getName(), label, importedBy, activateAfter);
        if (versionId == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(versionId);
    }
}