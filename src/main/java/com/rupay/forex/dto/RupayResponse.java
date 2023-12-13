package com.rupay.forex.dto;

import com.rupay.forex.model.Rupay;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RupayResponse {
    private String productName;
    private Double transactionCount;
    private Double setAmtDR;
    private Double setAmtCR;
    private Double intFeeCR;
    private Double intFeeDR;
    private Double othFeeDR;
    private LocalDate uploadedDate;
    private Double othFeeGSTDR;

    public static RupayResponse createRupayResponseObject(Rupay rupay){
        RupayResponse rupayResponse = RupayResponse.builder().
                productName(rupay.getProductName()).
                transactionCount(rupay.getTxnCount()).
                setAmtDR(rupay.getSetAmtDR()).
                setAmtCR(rupay.getSetAmtCR()).
                intFeeCR(rupay.getIntFeeAmtCR()).
                intFeeDR(rupay.getIntFeeAmtDR()).
                othFeeDR(rupay.getOthFeeAmtDR()).
                othFeeGSTDR(rupay.getOthFeeGSTDR()).
                build();

        return rupayResponse;
    }
}