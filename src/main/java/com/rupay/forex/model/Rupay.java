package com.rupay.forex.model;

import jakarta.persistence.*;
import lombok.*;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.time.LocalDate;
import java.util.List;

import static com.rupay.forex.service.RupayConstants.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rupay")
public class Rupay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rupay_id")
    private Long id;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "date", nullable = false)
    private LocalDate uploadedDate;
    @Column(name = "transaction_cycle", nullable = false)
    private Integer transactionCycle;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(name = "bank_name", nullable = false)
    private String bankName;
    @Column(name = "settlement_bin", nullable = false)
    private String settlementBin;
    @Column(name = "acq_id", nullable = false)
    private String acqID;
    @Column(name = "transaction_direction", nullable = false)
    private String transactionDirection;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "txn_count", nullable = false)
    private Double txnCount;
    @Column(name = "txn_amt_DR", nullable = false)
    private Double txnAmtDR;
    @Column(name = "txn_amt_CR", nullable = false)
    private Double txnAmtCR;
    @Column(name = "bill_amt_DR", nullable = false)
    private Double billAmtDR;
    @Column(name = "bill_amt_CR", nullable = false)
    private Double billAmtCR;
    @Column(name = "exchange_rate", nullable = false)
    private Double exchangeRate;
    @Column(name = "set_amt_DR", nullable = false)
    private Double setAmtDR;
    @Column(name = "set_amt_CR", nullable = false)
    private Double setAmtCR;
    @Column(name = "int_fee_amt_DR", nullable = false)
    private Double intFeeAmtDR;
    @Column(name = "int_fee_amt_CR", nullable = false)
    private Double intFeeAmtCR;
    @Column(name = "mem_inc_fee_amt_DR", nullable = false)
    private Double memIncFeeAmtDR;
    @Column(name = "mem_inc_fee_amt_CR", nullable = false)
    private Double memIncFeeAmtCR;
    @Column(name = "oth_fee_amt_DR", nullable = false)
    private Double othFeeAmtDR;
    @Column(name = "oth_fee_amt_CR", nullable = false)
    private Double othFeeAmtCR;
    @Column(name = "oth_fee_GST_DR", nullable = false)
    private Double othFeeGSTDR;
    @Column(name = "oth_fee_GST_CR", nullable = false)
    private Double othFeeGSTCR;
    @Column(name = "final_sum_CR", nullable = false)
    private Double finalSumCR;
    @Column(name = "final_sum_DR", nullable = false)
    private Double finalSumDR;
    @Column(name = "final_net", nullable = false)
    private Double finalNet;

    public static Rupay createRupayObject(List<HSSFCell> cells, String fileName){
        String uploadedDate = fileName.substring(fileName.indexOf("2023-"), fileName.indexOf("2023-") + 10);
        String transactionCycle = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".")).trim();

        Rupay rupay = Rupay.builder().
                fileName(fileName).
                productName(cells.get(PRODUCT_NAME).getStringCellValue()).
                bankName(cells.get(BANK_NAME).getStringCellValue()).
                settlementBin(cells.get(SETTLEMENT_BIN).getStringCellValue()).
                acqID(cells.get(ACQID).toString()).
                transactionDirection(cells.get(TRANSACTION_DIRECTION).getStringCellValue()).
                status(cells.get(STATUS).getStringCellValue()).
                txnCount(Double.parseDouble(cells.get(TXNCOUNT).toString())).
                txnAmtDR(Double.parseDouble(cells.get(TXNAMTDR).toString())).
                txnAmtCR(Double.parseDouble(cells.get(TXNAMTCR).toString())).
                billAmtDR(Double.parseDouble(cells.get(BILLAMTDR).toString())).
                billAmtCR(Double.parseDouble(cells.get(BILLAMTCR).toString())).
                setAmtDR(Double.parseDouble(cells.get(SET_AMT_DR).toString())).
                setAmtCR(Double.parseDouble(cells.get(SET_AMT_CR).toString())).
                exchangeRate(cells.get(EXCHANGE_RATE).toString() == null ?
                        Double.parseDouble(cells.get(EXCHANGE_RATE).toString()) : 0.00).
                intFeeAmtDR(Double.parseDouble(cells.get(INT_FEE_AMT_DR).toString())).
                intFeeAmtCR(Double.parseDouble(cells.get(INT_FEE_AMT_CR).toString())).
                memIncFeeAmtDR(Double.parseDouble(cells.get(MEM_INC_FEE_AMT_DR).toString())).
                memIncFeeAmtCR(Double.parseDouble(cells.get(MEM_INC_FEE_AMT_CR).toString())).
                othFeeAmtDR(Double.parseDouble(cells.get(OTH_FEE_AMT_DR).toString())).
                othFeeAmtCR(Double.parseDouble(cells.get(OTH_FEE_AMT_CR).toString())).
                othFeeGSTDR(Double.parseDouble(cells.get(OTH_FEE_GST_DR).toString())).
                othFeeGSTCR(Double.parseDouble(cells.get(OTH_FEE_GST_CR).toString())).
                finalSumDR(Double.parseDouble(cells.get(FINAL_SUM_DR).toString())).
                finalSumCR(Double.parseDouble(cells.get(FINAL_SUM_CR).toString())).
                finalNet(Double.parseDouble(cells.get(FINAL_NET).toString())).
                transactionCycle(Integer.parseInt(transactionCycle)).
                uploadedDate(LocalDate.parse(uploadedDate)).
                build();

        return  rupay;
    }
}