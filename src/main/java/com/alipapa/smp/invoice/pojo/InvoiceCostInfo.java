package com.alipapa.smp.invoice.pojo;

import java.util.Date;

public class InvoiceCostInfo {
    private Long id;

    private String orderNo;

    private String invoiceNo;

    private String transportChannel;

    private String transportCost;

    private String opUserNo;

    private String opUserName;

    private String remark;

    private Date createdTime;

    private Date updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo == null ? null : invoiceNo.trim();
    }

    public String getTransportChannel() {
        return transportChannel;
    }

    public void setTransportChannel(String transportChannel) {
        this.transportChannel = transportChannel == null ? null : transportChannel.trim();
    }

    public String getTransportCost() {
        return transportCost;
    }

    public void setTransportCost(String transportCost) {
        this.transportCost = transportCost == null ? null : transportCost.trim();
    }

    public String getOpUserNo() {
        return opUserNo;
    }

    public void setOpUserNo(String opUserNo) {
        this.opUserNo = opUserNo == null ? null : opUserNo.trim();
    }

    public String getOpUserName() {
        return opUserName;
    }

    public void setOpUserName(String opUserName) {
        this.opUserName = opUserName == null ? null : opUserName.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}