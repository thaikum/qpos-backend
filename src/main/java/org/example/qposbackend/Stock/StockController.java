package org.example.qposbackend.Stock;

import lombok.RequiredArgsConstructor;
import org.example.qposbackend.DTOs.DataResponse;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.DTOs.StockDTO;
import org.example.qposbackend.Exceptions.GenericExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("stock")
@RequiredArgsConstructor
public class StockController {
    private final StockRepository stockRepository;
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<DataResponse> getAllStock(){
        return ResponseEntity.ok(new DataResponse(stockRepository.findAll(), null));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> addStock(@RequestBody StockDTO stockDTO){
        try{
            stockService.addStock(stockDTO);
            return ResponseEntity.ok(new MessageResponse("Stock added successfully"));
        }catch (GenericExceptions ex){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new MessageResponse(ex.getMessage()));
        }
    }
}
