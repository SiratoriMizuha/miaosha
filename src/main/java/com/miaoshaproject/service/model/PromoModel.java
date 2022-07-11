package com.miaoshaproject.service.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;


public class PromoModel {
    private Integer id;

    //秒杀活动状态，1表示还未开始，2表示进行中，3表示已结束
    private Integer status;

    private String promoName;

    private DateTime startDate;

    private DateTime endDate;

    private Integer itemid;

    private BigDecimal promoPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public Integer getItemid() {
        return itemid;
    }

    public void setItemid(Integer itemid) {
        this.itemid = itemid;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }
}
