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
public class DriversLicense {

    private String licenseNumber;
    private int customerId; // Foreign Key linking back to the Customer
    private String licenseType;
    private String restrictions;
    private Date issueDate;
    private Date expiryDate;

    public DriversLicense(String licenseNumber, int customerId, String licenseType, String restrictions, Date issueDate, Date expiryDate) {
        this.licenseNumber = licenseNumber;
        this.customerId = customerId;
        this.licenseType = licenseType;
        this.restrictions = restrictions;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
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

}
