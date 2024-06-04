package org.example.qposbackend.Accounting.Transactions.TranHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTran;
import org.example.qposbackend.Accounting.Transactions.PartTran.PartTranRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranHeaderService {
    private final TranHeaderRepository tranHeaderRepository;
    private final PartTranRepository partTranRepository;

    public void processTransaction(TranHeader tranHeader) {
        double amount = 0.0, net = 0.0;

        for (PartTran partTran : tranHeader.getPartTrans()) {
            amount += partTran.getTranType().equals('C') ? partTran.getAmount() : 0.0;

            if (partTran.getTranType().equals('C')) {
                net -= partTran.getAmount();
            } else {
                net += partTran.getAmount();
            }

            log.info("{}" , partTran);
        }

        System.out.println("Second size is: " + tranHeader.getPartTrans().size() + "And amount is: " + amount);
        if (net != 0.0) {
            throw new RuntimeException("Transaction must balance");
        } else {
//            tranHeader.setTotalAmount(amount);
            List<PartTran> partTrans = partTranRepository.saveAll(tranHeader.getPartTrans());
            tranHeader.setPartTrans(partTrans);
            tranHeaderRepository.save(tranHeader);
        }
    }
}
