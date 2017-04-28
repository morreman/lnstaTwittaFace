package org.jjam.instatwittaface;

import java.util.Date;

import android.graphics.Bitmap;

/**
 * Representerar ett inlägg i listan
 * @author Jerry Pedersen, Jonas Remgård, Anton Nilsson, Mårten Persson
 */
public class Post implements Comparable<Post>{

    private String text, company;
    private String smallImage, userName, largeImage;
    private Bitmap smallImageBitmap;
    private Date time;
    private String id;

    public Post(String text, String company, Date time) {
        this.text = text;
        this.company = company;
        this.time = time;
    }

    public Post(String company){
        this.company = company;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return this.text;
    }

    public String getCompany() {
        return this.company;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSmallImageBitmap(Bitmap smallImageBitmap) {
        this.smallImageBitmap = smallImageBitmap;
    }

    public void setLargeImageBitmap(Bitmap largeImageBitmap) {
    }

    public Bitmap getSmallImageBitmap() {
        return smallImageBitmap;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public int compareTo(Post another) {
        return getTime().compareTo(another.getTime());
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
