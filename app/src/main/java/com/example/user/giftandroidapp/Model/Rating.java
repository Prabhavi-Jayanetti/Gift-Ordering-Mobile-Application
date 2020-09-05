package com.example.user.giftandroidapp.Model;

/**
 * Created by User on 8/6/2018.
 */

public class Rating {
    private String userPhone; //both key and value
    private String giftId;
    private String rateValue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String giftId, String rateValue, String comment) {
        this.userPhone = userPhone;
        this.giftId = giftId;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
