package com.disasterrelief.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResultDto {
    private String filename;
    private int totalRows;
    private int successfulRows;
    private int rejectedRows;
    private List<String> errorMessages = new ArrayList<>();
    private List<String> importedZoneCodes = new ArrayList<>();

    public CsvImportResultDto() {}

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessfulRows() { return successfulRows; }
    public void setSuccessfulRows(int successfulRows) { this.successfulRows = successfulRows; }

    public int getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(int rejectedRows) { this.rejectedRows = rejectedRows; }

    public List<String> getErrorMessages() { return errorMessages; }
    public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }

    public List<String> getImportedZoneCodes() { return importedZoneCodes; }
    public void setImportedZoneCodes(List<String> importedZoneCodes) { this.importedZoneCodes = importedZoneCodes; }
}

