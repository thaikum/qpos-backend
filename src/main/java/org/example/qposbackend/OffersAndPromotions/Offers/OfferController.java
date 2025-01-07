package org.example.qposbackend.OffersAndPromotions.Offers;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.OfferDTO;
import org.example.qposbackend.Order.SaleOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;
    private final OfferRepository offerRepository;

    @PostMapping
    ResponseEntity<MessageResponse> createOffer(@RequestBody OfferDTO offer) {
        try {
            offerService.createOffer(offer);
            return ResponseEntity.ok(new MessageResponse("Successfully created offer"));
        }catch (Exception e) {
            //todo use 406
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating offer"));
        }
    }

    @GetMapping
    ResponseEntity<DataResponse> readAllOffers() {
        return ResponseEntity.ok(new DataResponse(offerRepository.findAll(), null));
    }

    @PostMapping("get-offers_on_order")
    ResponseEntity<DataResponse> retrieveOffers(@RequestBody SaleOrder order) {
        try{
            var offersToApply= offerService.getOffersToApply(order);
            return ResponseEntity.ok(new DataResponse(offersToApply, null));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new DataResponse(null, e.getMessage()));
        }
    }
}
