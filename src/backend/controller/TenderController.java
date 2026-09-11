package backend.controller;

import backend.dto.request.TenderRequest;
import backend.dto.response.ApiResponse;
import backend.dto.response.TenderResponse;
import backend.model.Tender;
import backend.service.TenderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenders")
public class TenderController {

    private final TenderService tenderService;


    public TenderController(TenderService tenderService) {

        this.tenderService = tenderService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<TenderResponse>> createTender(
            @Valid @RequestBody TenderRequest request
    ) {

        Tender tender = tenderService.createTender(
                request.getTitle(),
                request.getDescription()
        );

        return response(
                new TenderResponse(tender),
                "Tender created",
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<TenderResponse>>> getAllTenders() {

        List<TenderResponse> tenders = tenderService.getAllTenders()
                .stream()
                .map(TenderResponse::new)
                .toList();

        return response(tenders, "Tenders retrieved", HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenderResponse>> getTenderById(
            @PathVariable UUID id
    ) {

        return response(
                new TenderResponse(tenderService.getTenderById(id)),
                "Tender retrieved",
                HttpStatus.OK
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateTender(
            @PathVariable UUID id,
            @Valid @RequestBody TenderRequest request
    ) {

        tenderService.updateTender(
                id,
                request.getTitle(),
                request.getDescription()
        );

        return response(null, "Tender updated", HttpStatus.OK);
    }


    @PostMapping("/{id}/open")
    public ResponseEntity<ApiResponse<TenderResponse>> openTender(
            @PathVariable UUID id
    ) {

        tenderService.openTender(id);

        return response(
                new TenderResponse(tenderService.getTenderById(id)),
                "Tender opened",
                HttpStatus.OK
        );
    }


    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TenderResponse>> closeTender(
            @PathVariable UUID id
    ) {

        tenderService.closeTender(id);

        return response(
                new TenderResponse(tenderService.getTenderById(id)),
                "Tender closed",
                HttpStatus.OK
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTender(
            @PathVariable UUID id
    ) {

        tenderService.deleteTender(id);

        return response(null, "Tender deleted", HttpStatus.OK);
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
