package backend.controller;

import backend.dto.response.ApiResponse;
import backend.model.CartelSignal;
import backend.repository.CartelSignalRepository;
import backend.service.OrchestrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Collusion Radar — exposes:
 *   POST /collusion-check/{tenderId}   trigger AI collusion analysis + persist results
 *   GET  /collusion-check/{tenderId}   retrieve persisted cartel-signal clusters
 */
@RestController
@RequestMapping("/collusion-check")
public class CollusionController {

    private final OrchestrationService orchestrationService;
    private final CartelSignalRepository cartelSignalRepository;

    public CollusionController(
            OrchestrationService orchestrationService,
            CartelSignalRepository cartelSignalRepository
    ) {
        this.orchestrationService = orchestrationService;
        this.cartelSignalRepository = cartelSignalRepository;
    }

    /**
     * Trigger a collusion check for the given tender.
     * The check calls the AI engine and persists cluster results into cartel_signals.
     * Never throws a 5xx if the AI call fails — error is wrapped in ApiResponse.
     */
    @PostMapping("/{tenderId}")
    public ResponseEntity<ApiResponse<Void>> triggerCollusionCheck(
            @PathVariable UUID tenderId
    ) {
        try {
            orchestrationService.triggerCollusionCheck(tenderId);
            return response(null, "Collusion check initiated and results persisted", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return response(null, "Collusion check failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieve all persisted cartel-signal clusters for a given tender.
     * Returns an empty list if no analysis has been run yet.
     */
    @GetMapping("/{tenderId}")
    public ResponseEntity<ApiResponse<List<CartelSignal>>> getCollusionSignals(
            @PathVariable UUID tenderId
    ) {
        List<CartelSignal> signals = cartelSignalRepository.findByTenderId(tenderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK.value(), signals, "Cartel signals retrieved"));
    }

    private <T> ResponseEntity<ApiResponse<T>> response(T data, String message, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(status.value(), data, message));
    }
}
