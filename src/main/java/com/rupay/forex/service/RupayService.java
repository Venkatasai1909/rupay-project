package com.rupay.forex.service;

import com.rupay.forex.dto.ApiResponse;
import com.rupay.forex.dto.RupayResponse;
import com.rupay.forex.exception.CustomException;
import com.rupay.forex.model.Rupay;
import com.rupay.forex.repository.RupayRepository;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Service
public class RupayService {
    @Autowired
    private RupayRepository rupayRepository;

    @Transactional
    public ResponseEntity<ApiResponse> processData(MultipartFile requestFile) {
        try (InputStream inputStream = requestFile.getInputStream();
             HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(inputStream))) {
            validateFile(requestFile);

            HSSFSheet sheet = workbook.getSheetAt(0);

            validateFileFormat(sheet);

            List<CellRangeAddress> addresses = sheet.getMergedRegions();
            List<Integer> rowIndices = new ArrayList<>();

            for (CellRangeAddress address : addresses) {
                int firstColumn = address.getFirstColumn();
                int lastColumn = address.getLastColumn();

                if (firstColumn == 0 && lastColumn == 0) {
                    rowIndices.add(address.getFirstRow());
                    rowIndices.add(address.getLastRow());
                }
            }

            int count = 0;
            int rowIndicesSize = rowIndices.size();

            for (Integer rowIndex : rowIndices) {
                if (count < rowIndicesSize) {
                    HSSFRow initialRow = sheet.getRow(rowIndices.get(count++));

                    List<HSSFCell> records = new ArrayList<>(IntStream.range(0, RupayConstants.STATUS + 1)
                            .mapToObj(initialRow::getCell)
                            .toList());

                    HSSFRow lastRow = sheet.getRow(rowIndices.get(count++));

                    IntStream.range(RupayConstants.TRANSACTION_CYCLE, lastRow.getLastCellNum())
                            .mapToObj(lastRow::getCell)
                            .forEach(records::add);

                    records.forEach(System.out::println);

                    Rupay rupay = Rupay.createRupayObject(records, requestFile.getOriginalFilename());

                    rupayRepository.save(rupay);
                } else {
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            ApiResponse apiResponse = ApiResponse.builder().
                    fileName(requestFile.getOriginalFilename()).
                    message(exception.getMessage()).
                    build();

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        ApiResponse apiResponse = ApiResponse.builder().
                fileName(requestFile.getOriginalFilename()).
                message("File saved successfully").
                build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    private void validateFile(MultipartFile file) throws FileAlreadyExistsException {
        if (file.isEmpty()) {
            throw new CustomException("Empty file uploaded", "EMPTY_FILE");
        }

        String fileName = file.getOriginalFilename();

        if (fileName != null && !fileName.endsWith(RupayConstants.FILE_EXTENSION)) {
            throw new CustomException("Invalid file format. " +
                    "Only files with .xls extension will be allowed", "UNSUPPORTED_FILE_EXTENSION");
        }

        List<String> productNames = rupayRepository.findProductNameByFileName(fileName);

        if (productNames != null && productNames.size() > 0) {
            String errorMessage = String.format("A file with the name '%s' already exists.", fileName);

            throw new FileAlreadyExistsException(errorMessage);
        }
    }

    private void validateFileFormat(HSSFSheet sheet) {
        HSSFRow headerRow = sheet.getRow(0);

        List<Cell> headerRecords = StreamSupport.stream(headerRow.spliterator(), false).toList();

        List<String> expectedHeaders = Arrays.asList(
                "Product Name", "Bank Name", "Settlement Bin", "Acq ID / ISS Bin", "Inward/Outward",
                "Status(Approved/Declined)", "Transaction Cycle", "Transaction Flag", "Transaction Type",
                "Channel", "TXN CCY", "SET/Bill CCY", "TXNCOUNT", "Txn Amt DR", "Txn Amt Cr",
                "Bill Amt DR", "Bill Amt Cr", "Exchange Rate", "SETAMTDR", "SETAMTCR", "Int Fee Amt DR",
                "Int Fee Amt CR", "Mem Inc Fee Amt DR", "Mem Inc Fee Amt CR", "Oth Fee Amt DR", "Oth Fee Amt CR",
                "Oth Fee  GST DR", "Oth Fee  GST CR", "Final Sum Cr", "Final Sum Dr", "Final Net"
        );

        IntStream.range(0, expectedHeaders.size())
                .forEach(index -> {
                    String expected = expectedHeaders.get(index);
                    String actual = headerRecords.get(index).toString();

                    if (!expected.equals(actual)) {
                        throw new CustomException("Invalid File format", "INVALID_FILE_FORMAT");
                    }
                });
    }

    @Transactional
    public ResponseEntity<Resource> downloadExcelSheet(String requestDate, String productName) {
        validateDate(requestDate);
        LocalDate uploadedDate = LocalDate.parse(requestDate);

        return downloadExcelSheetInternal(uploadedDate, null, null, productName);
    }

    @Transactional
    public ResponseEntity<Resource> downloadExcelSheetForGivenDates(String requestedStartDate, String requestedEndDate,
                                                                    String productName) {
        validateDate(requestedStartDate);
        validateDate(requestedEndDate);

        LocalDate startDate = LocalDate.parse(requestedStartDate);
        LocalDate endDate = LocalDate.parse(requestedEndDate);

        if (startDate.isAfter(endDate)) {
            throw new CustomException("Start date must be before or equal to end date.", "INVALID_DATE_RANGE");
        }

        return downloadExcelSheetInternal(null, startDate, endDate, productName);
    }

    private void validateDate(String date) {
        String dateFormat = "yyyy-MM-dd";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
            LocalDate.parse(date, formatter);
        } catch (DateTimeParseException dateTimeParseException) {
            throw new CustomException("Invalid date format, only yyyy-MM-dd is allowed", "INVALID_DATE_FORMAT");
        }
    }

    private ResponseEntity<Resource> downloadExcelSheetInternal(LocalDate uploadedDate, LocalDate startDate,
                                                                LocalDate endDate, String productName) {
        ByteArrayInputStream byteArrayInputStream = null;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Workbook workbook = createWorkBookForGivenDatesAndProductName(uploadedDate, startDate, endDate, productName)) {
            workbook.write(byteArrayOutputStream);

            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            InputStreamResource file = new InputStreamResource(byteArrayInputStream);

            String fileName = getRequestedName(productName, "fileName");

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; fileName = " + fileName + ".xls")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    private Workbook createWorkBookForGivenDatesAndProductName(LocalDate uploadedDate, LocalDate startDate,
                                                               LocalDate endDate, String requestedProductName) {
        String productName = getRequestedName(requestedProductName, "productName");
        String workbookName = getRequestedName(requestedProductName, "workbookName");

        List<Rupay> rupayList;

        if (startDate != null && endDate != null) {
            rupayList = rupayRepository.findAllByProductNameAndUploadedDateBetweenStartDateAndEndDate(productName,
                                                                                                    startDate, endDate);
        } else {
            rupayList = rupayRepository.findAllByProductNameAndUploadedDate(productName, uploadedDate);
        }

        if (rupayList == null || rupayList.isEmpty()) {
            throw new CustomException("Data not found for provided parameters ", "DATA_NOT_FOUND");
        }

        List<RupayResponse> rupayResponses = rupayList.stream()
                .map(RupayResponse::createRupayResponseObject)
                .toList();

        return createWorkBook(rupayResponses, workbookName);
    }

    private Workbook createWorkBook(List<RupayResponse> rupayResponses, String workbookName) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(workbookName);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Product Name", "Count", "Amount Dr", "Amount Cr", "Int Fee Cr", "Int Fee Dr",
                "Processing fee & Assessment fee", "GST", "NPCI Fees + GST", "NPCI Settlement Summary"};

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle bodyStyle = createBodyStyle(workbook);

        int headerLength = headers.length;
        int headerCount = 0;

        for (String header : headers) {
            Cell cell = headerRow.createCell(headerCount);

            cell.setCellStyle(headerStyle);
            cell.setCellValue(header);

            headerCount++;
        }

        int rowNum = 1;
        for (RupayResponse rupayResponse : rupayResponses) {
            int cellNum = 0;
            Row dataRow = sheet.createRow(rowNum++);

            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getProductName());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getTransactionCount());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getSetAmtDR());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getSetAmtCR());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getIntFeeCR());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getIntFeeDR());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getOthFeeDR());
            dataRow.createCell(cellNum++).setCellValue(rupayResponse.getOthFeeGSTDR());
            dataRow.createCell(cellNum++).setCellFormula("G" + rowNum + "+H" + rowNum);
        }

        Row summaryRow = sheet.createRow(++rowNum);
        Cell summaryCell = summaryRow.createCell(0);

        summaryCell.setCellValue("Summary");
        summaryCell.setCellStyle(bodyStyle);

        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, headerLength - 3));

        Row totalRow = sheet.createRow(++rowNum);

        Cell initialCell = totalRow.createCell(0);
        initialCell.setCellValue("NPCI Summary");
        initialCell.setCellStyle(headerStyle);

        Row initialRow = sheet.getRow(1);

        int finalRowNum = rowNum;

        IntStream.range(1, headerLength - 2).forEach(header -> {
            Cell totalCell = totalRow.createCell(header);

            totalCell.setCellFormula("SUM(" + CellReference.convertNumToColString(header) + "2:" +
                    CellReference.convertNumToColString(header) + finalRowNum + ")");

            totalCell.setCellStyle(headerStyle);
        });

        rowNum = rowNum + 1;

        initialRow.createCell(initialRow.getLastCellNum()).setCellFormula("D" + (rowNum) +
                "+E" + (rowNum) + ("- C" + (rowNum) + "-F" + (rowNum)
                + "-G" + (rowNum) + "-H" + (rowNum)));

        return workbook;
    }

    private String getRequestedName(String productName, String requestType) {
        if (productName.equals("POS")) {
            if (requestType.equals("fileName") || requestType.equals("workbookName")) {
                return "Card91_POS_Summary";
            } else {
                return "Rupay-POS-DMS-Issuer";
            }
        } else if (productName.equals("ATM")) {
            if (requestType.equals("fileName") || requestType.equals("workbookName")) {
                return "Card91_ATM_Summary";
            } else {
                return "Rupay-ATM-DMS-Issuer";
            }
        } else {
            return null;
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Cambria");
        font.setFontHeightInPoints((short) 11);

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }

    private CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        font.setFontName("Cambria");
        font.setFontHeightInPoints((short) 10);

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }
}