/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;

/**
 *
 * @author Xyjor
 */
public class StudentPermit {

    private String permitNumber; // This is your Primary Key!
    private int customerId;      // Foreign Key
    private Date issueDate;      // Properly set to sql.Date
    private Date expiryDate;     // Properly set to sql.Date
    private String status;

    public StudentPermit() {
    }

    public StudentPermit(String permitNumber, int customerId, Date issueDate, Date expiryDate, String status) {
        this.permitNumber = permitNumber;
        this.customerId = customerId;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    // --- GETTERS AND SETTERS ---
    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
