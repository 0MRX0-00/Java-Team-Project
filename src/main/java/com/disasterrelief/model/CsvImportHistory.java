package com.disasterrelief.model;

import java.time.LocalDateTime;

public class CsvImportHistory {
    private Long id;
    private String filename;
    private int totalRows;
    private int successfulRows;
    private int rejectedRows;
    private String errorSummary;
    private LocalDateTime importedAt;

    public CsvImportHistory() {}

    public CsvImportHistory(String filename, int totalRows, int successfulRows, int rejectedRows, String errorSummary) {
        this.filename = filename;
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.rejectedRows = rejectedRows;
        this.errorSummary = errorSummary;
        this.importedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessfulRows() { return successfulRows; }
    public void setSuccessfulRows(int successfulRows) { this.successfulRows = successfulRows; }

    public int getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(int rejectedRows) { this.rejectedRows = rejectedRows; }

    public String getErrorSummary() { return errorSummary; }
    public void setErrorSummary(String errorSummary) { this.errorSummary = errorSummary; }

    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
}

