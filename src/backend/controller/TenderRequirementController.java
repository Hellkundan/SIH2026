package backend.controller;

import backend.dto.request.TenderRequirementRequest;
import backend.dto.response.ApiResponse;
import backend.dto.response.TenderRequirementResponse;
import backend.model.TenderRequirement;
import backend.service.TenderRequirementService;
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
@RequestMapping("/tender-requirements")
public class TenderRequirementController {

    private final TenderRequirementService tenderRequirementService;


    public TenderRequirementController(
            TenderRequirementService tenderRequirementService
    ) {

        this.tenderRequirementService = tenderRequirementService;
    }


    @PostMapping
    public ResponseEntity<?> createRequirement(
            @Valid @RequestBody TenderRequirementRequest request
    ) {

        if (request.getRequirements() != null) {
            List<TenderRequirement> saved = tenderRequirementService.saveRequirementsForTender(
                    request.getTenderId(),
                    request.getRequirements()
            );

            List<TenderRequirementResponse> responses = saved.stream()
                    .map(TenderRequirementResponse::new)
                    .toList();

            return response(
                    responses,
                    "Tender requirements saved",
                    HttpStatus.OK
            );
        }

        if (request.getRequirement() == null || request.getRequirement().isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }

        TenderRequirement requirement = tenderRequirementService.createRequirement(
                request.getTenderId(),
                request.getRequirement(),
                request.isMandatory()
        );

        return response(
                new TenderRequirementResponse(requirement),
                "Tender requirement created",
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<TenderRequirementResponse>>> getAllRequirements() {

        List<TenderRequirementResponse> requirements = tenderRequirementService
                .getAllRequirements()
                .stream()
                .map(TenderRequirementResponse::new)
                .toList();

        return response(requirements, "Tender requirements retrieved", HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenderRequirementResponse>> getRequirementById(
            @PathVariable UUID id
    ) {

        return response(
                new TenderRequirementResponse(
                        tenderRequirementService.getRequirementById(id)
                ),
                "Tender requirement retrieved",
                HttpStatus.OK
        );
    }


    @GetMapping("/tender/{tenderId}")
    public ResponseEntity<ApiResponse<List<TenderRequirementResponse>>> getRequirementsByTenderId(
            @PathVariable UUID tenderId
    ) {

        List<TenderRequirementResponse> requirements = tenderRequirementService
                .getRequirementsByTenderId(tenderId)
                .stream()
                .map(TenderRequirementResponse::new)
                .toList();

        return response(requirements, "Tender requirements retrieved", HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRequirement(
            @PathVariable UUID id,
            @Valid @RequestBody TenderRequirementRequest request
    ) {

        tenderRequirementService.updateRequirement(
                id,
                request.getRequirement(),
                request.isMandatory()
        );

        return response(null, "Tender requirement updated", HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRequirement(
            @PathVariable UUID id
    ) {

        tenderRequirementService.deleteRequirement(id);

        return response(null, "Tender requirement deleted", HttpStatus.OK);
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
