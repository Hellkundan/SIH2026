package backend.controller;

import backend.dto.request.DocumentRequest;
import backend.dto.response.ApiResponse;
import backend.dto.response.DocumentResponse;
import backend.model.Document;
import backend.service.DocumentService;
import backend.service.OrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
        private final OrchestrationService orchestrationService;
        private final String documentsDirectory;


        public DocumentController(
                        DocumentService documentService,
                            OrchestrationService orchestrationService,
                            @Value("${app.storage.documents-dir:./data/documents}") String documentsDirectory
        ) {

        this.documentService = documentService;
                this.orchestrationService = orchestrationService;
        this.documentsDirectory = documentsDirectory;
    }


        @PostMapping("/{id}/upload")
        public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
                        @PathVariable UUID id,
                        @RequestPart("file") MultipartFile file
        ) {
                try {
                        if (file.isEmpty()) {
                                throw new IllegalArgumentException("Uploaded file is empty");
                        }
                        documentService.getDocumentById(id);
                            Path directory = Path.of(documentsDirectory);
                        Files.createDirectories(directory);
                            String originalFileName = Path.of(
                                    file.getOriginalFilename() == null ? "document" : file.getOriginalFilename()
                            ).getFileName().toString();
                            Path storedFile = directory.resolve(id + "-" + originalFileName);
                        Files.write(storedFile, file.getBytes());
                        documentService.setFilePath(id, storedFile);

                        // Synchronous for the demo; production should enqueue this work.
                        orchestrationService.triggerDocumentProcessing(id);
                        return response(
                                        new DocumentResponse(documentService.getDocumentById(id)),
                                        "Document uploaded and processed",
                                        HttpStatus.OK
                        );
                } catch (Exception exception) {
                        return response(null, "Document upload failed: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
                }
        }


    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @Valid @RequestBody DocumentRequest request
    ) {

        Document document = documentService.createDocument(
                request.getTenderBidId(),
                request.getDocumentType(),
                request.getFileName()
        );

        return response(
                new DocumentResponse(document),
                "Document created",
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {

        List<DocumentResponse> documents = documentService.getAllDocuments()
                .stream()
                .map(DocumentResponse::new)
                .toList();

        return response(documents, "Documents retrieved", HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(
            @PathVariable UUID id
    ) {

        return response(
                new DocumentResponse(documentService.getDocumentById(id)),
                "Document retrieved",
                HttpStatus.OK
        );
    }


    @GetMapping("/tender-bid/{tenderBidId}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByTenderBidId(
            @PathVariable UUID tenderBidId
    ) {

        List<DocumentResponse> documents = documentService
                .getDocumentsByTenderBidId(tenderBidId)
                .stream()
                .map(DocumentResponse::new)
                .toList();

        return response(documents, "Documents retrieved", HttpStatus.OK);
    }


    @PostMapping("/{id}/processing")
    public ResponseEntity<ApiResponse<Void>> startDocumentProcessing(
            @PathVariable UUID id
    ) {

        documentService.startDocumentProcessing(id);

        return response(null, "Document processing started", HttpStatus.OK);
    }


    @PostMapping("/{id}/processed")
    public ResponseEntity<ApiResponse<Void>> markDocumentAsProcessed(
            @PathVariable UUID id
    ) {

        documentService.markDocumentAsProcessed(id);

        return response(null, "Document marked as processed", HttpStatus.OK);
    }


    @PostMapping("/{id}/failed")
    public ResponseEntity<ApiResponse<Void>> markDocumentAsFailed(
            @PathVariable UUID id
    ) {

        documentService.markDocumentAsFailed(id);

        return response(null, "Document marked as failed", HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable UUID id
    ) {

        documentService.deleteDocument(id);

        return response(null, "Document deleted", HttpStatus.OK);
    }


    private <T> ResponseEntity<ApiResponse<T>> response(
            T data,
            String message,
            HttpStatus status
    ) {

        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(status.value(), data, message));
    }
}
