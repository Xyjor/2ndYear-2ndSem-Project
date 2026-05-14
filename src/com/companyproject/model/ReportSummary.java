package com.companyproject.model;

import java.math.BigDecimal;

public class ReportSummary {

    private int customerCount;
    private int vehicleCount;
    private int pendingTransactionCount;
    private int completedTransactionCount;
    private BigDecimal completedRevenue;

    public int getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int vehicleCount) {
        this.vehicleCount = vehicleCount;
    }

    public int getPendingTransactionCount() {
        return pendingTransactionCount;
    }

    public void setPendingTransactionCount(int pendingTransactionCount) {
        this.pendingTransactionCount = pendingTransactionCount;
    }

    public int getCompletedTransactionCount() {
        return completedTransactionCount;
    }

    public void setCompletedTransactionCount(int completedTransactionCount) {
        this.completedTransactionCount = completedTransactionCount;
    }

    public BigDecimal getCompletedRevenue() {
        return completedRevenue;
    }

    public void setCompletedRevenue(BigDecimal completedRevenue) {
        this.completedRevenue = completedRevenue;
    }
}
