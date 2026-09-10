package backend.controller;

import backend.dto.request.BidderRequest;
import backend.dto.response.ApiResponse;
import backend.dto.response.BidderResponse;
import backend.model.Bidder;
import backend.service.BidderService;
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
@RequestMapping("/bidders")
public class BidderController {

    private final BidderService bidderService;


    public BidderController(BidderService bidderService) {

        this.bidderService = bidderService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<BidderResponse>> createBidder(
            @Valid @RequestBody BidderRequest request
    ) {

        Bidder bidder = bidderService.createBidder(
                request.getCompanyName(),
                request.getEmail(),
                request.getPhone(),
                request.getPan(),
                request.getGstin()
        );

        return response(
                new BidderResponse(bidder),
                "Bidder created",
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<BidderResponse>>> getAllBidders() {

        List<BidderResponse> bidders = bidderService.getAllBidders()
                .stream()
                .map(BidderResponse::new)
                .toList();

        return response(bidders, "Bidders retrieved", HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BidderResponse>> getBidderById(
            @PathVariable UUID id
    ) {

        return response(
                new BidderResponse(bidderService.getBidderById(id)),
                "Bidder retrieved",
                HttpStatus.OK
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBidder(
            @PathVariable UUID id,
            @Valid @RequestBody BidderRequest request
    ) {

        bidderService.updateBidder(
                id,
                request.getCompanyName(),
                request.getEmail(),
                request.getPhone(),
                request.getPan(),
                request.getGstin()
        );

        return response(null, "Bidder updated", HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBidder(
            @PathVariable UUID id
    ) {

        bidderService.deleteBidder(id);

        return response(null, "Bidder deleted", HttpStatus.OK);
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
