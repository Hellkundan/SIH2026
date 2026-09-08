package backend.controller;

import backend.dto.request.TenderBidRequest;
import backend.dto.response.ApiResponse;
import backend.dto.response.TenderBidResponse;
import backend.model.TenderBid;
import backend.service.TenderBidService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tender-bids")
public class TenderBidController {

    private final TenderBidService tenderBidService;


    public TenderBidController(TenderBidService tenderBidService) {

        this.tenderBidService = tenderBidService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<TenderBidResponse>> createTenderBid(
            @Valid @RequestBody TenderBidRequest request
    ) {

        TenderBid tenderBid = tenderBidService.createTenderBid(
                request.getTenderId(),
                request.getBidderId()
        );

        return response(
                new TenderBidResponse(tenderBid),
                "Tender bid created",
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<TenderBidResponse>>> getAllTenderBids() {

        List<TenderBidResponse> tenderBids = tenderBidService
                .getAllTenderBids()
                .stream()
                .map(TenderBidResponse::new)
                .toList();

        return response(tenderBids, "Tender bids retrieved", HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenderBidResponse>> getTenderBidById(
            @PathVariable UUID id
    ) {

        return response(
                new TenderBidResponse(tenderBidService.getTenderBidById(id)),
                "Tender bid retrieved",
                HttpStatus.OK
        );
    }


    @GetMapping("/tender/{tenderId}")
    public ResponseEntity<ApiResponse<List<TenderBidResponse>>> getTenderBidsByTenderId(
            @PathVariable UUID tenderId
    ) {

        List<TenderBidResponse> tenderBids = tenderBidService
                .getTenderBidsByTenderId(tenderId)
                .stream()
                .map(TenderBidResponse::new)
                .toList();

        return response(tenderBids, "Tender bids retrieved", HttpStatus.OK);
    }


    @GetMapping("/bidder/{bidderId}")
    public ResponseEntity<ApiResponse<List<TenderBidResponse>>> getTenderBidsByBidderId(
            @PathVariable UUID bidderId
    ) {

        List<TenderBidResponse> tenderBids = tenderBidService
                .getTenderBidsByBidderId(bidderId)
                .stream()
                .map(TenderBidResponse::new)
                .toList();

        return response(tenderBids, "Tender bids retrieved", HttpStatus.OK);
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitTenderBid(
            @PathVariable UUID id
    ) {

        tenderBidService.submitTenderBid(id);

        return response(null, "Tender bid submitted", HttpStatus.OK);
    }


    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<Void>> startBidReview(
            @PathVariable UUID id
    ) {

        tenderBidService.startBidReview(id);

        return response(null, "Tender bid moved to review", HttpStatus.OK);
    }


    @PostMapping("/{id}/qualify")
    public ResponseEntity<ApiResponse<Void>> qualifyBid(
            @PathVariable UUID id
    ) {

        tenderBidService.qualifyBid(id);

        return response(null, "Tender bid qualified", HttpStatus.OK);
    }


    @PostMapping("/{id}/disqualify")
    public ResponseEntity<ApiResponse<Void>> disqualifyBid(
            @PathVariable UUID id
    ) {

        tenderBidService.disqualifyBid(id);

        return response(null, "Tender bid disqualified", HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenderBid(
            @PathVariable UUID id
    ) {

        tenderBidService.deleteTenderBid(id);

        return response(null, "Tender bid deleted", HttpStatus.OK);
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
