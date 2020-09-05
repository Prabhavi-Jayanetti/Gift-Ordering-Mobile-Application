package com.example.user.giftandroidapp.Model;

/**
 * Created by User on 7/23/2018.
 */

public class Gift {
    private String Name,Image,Description,Price,Discount,GiftCategoryId;

    public Gift() {
    }

    public Gift(String name, String image, String description, String price, String discount, String giftCategoryId) {
        Name = name;
        Image = image;
        Description = description;
        Price = price;
        Discount = discount;
        GiftCategoryId = giftCategoryId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getGiftCategoryId() {
        return GiftCategoryId;
    }

    public void setGiftCategoryId(String giftCategoryId) {
        GiftCategoryId = giftCategoryId;
    }
}
