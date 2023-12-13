package com.rupay.forex.controller;

import ch.qos.logback.core.LayoutBase;
import com.rupay.forex.dto.ApiResponse;
import com.rupay.forex.service.RupayService;
import lombok.NonNull;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;


@RestController
@RequestMapping("/api")
public class RupayController {
    @Autowired
    private RupayService rupayService;
    @PostMapping("/read-data")
    public ResponseEntity<ApiResponse> readData(@NonNull @RequestBody MultipartFile requestFile) {
        return rupayService.processData(requestFile);
    }

    @GetMapping("/data/download/pos")
    public ResponseEntity<Resource> downloadExcelSheetForPOS(@NonNull  @RequestParam(value = "date") String requestDate) {
        return rupayService.downloadExcelSheet(requestDate, "POS");
    }

    @GetMapping("/data/download/atm")
    public ResponseEntity<Resource> downloadExcelSheetForATM(@NonNull @RequestParam(value = "date") String requestDate) {
        return rupayService.downloadExcelSheet(requestDate, "ATM");
    }

    @GetMapping("/data/download/dates/pos")
    public ResponseEntity<Resource> downloadExcelSheetForPOSForGivenDates(@NonNull @RequestParam(value = "startDate") String startDate,
                                                                          @NonNull @RequestParam(value = "endDate") String endDate){
        return rupayService.downloadExcelSheetForGivenDates(startDate, endDate, "POS");
    }

    @GetMapping("/data/download/dates/atm")
    public ResponseEntity<Resource> downloadExcelSheetForATMForGivenDates(@NonNull @RequestParam(value = "startDate") String startDate,
                                                                          @NonNull @RequestParam(value = "endDate") String endDate){
        return rupayService.downloadExcelSheetForGivenDates(startDate, endDate, "ATM");
    }
}