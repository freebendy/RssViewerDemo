package com.roadmap.db;

public class Message implements Comparable<Message> {

    private String mTitle ="";
    private String mLink ="";
    private String mSource ="";
    private String mCategory ="";
    private String mDate ="";
    private String mDescription ="";
    private String mImageUrl ="";
    private String mImageText ="";
    
    
    public int compareTo(Message another) {
        // TODO Auto-generated method stub
        return 0;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        mTitle = title;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return mTitle;
    }


    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        mLink = link;
    }


    /**
     * @return the link
     */
    public String getLink() {
        return mLink;
    }


    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        mSource = source;
    }


    /**
     * @return the source
     */
    public String getSource() {
        return mSource;
    }


    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        mCategory = category;
    }


    /**
     * @return the category
     */
    public String getCategory() {
        return mCategory;
    }


    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        mDate = date;
    }


    /**
     * @return the date
     */
    public String getDate() {
        return mDate;
    }


    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        mDescription = description;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return mDescription;
    }


    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }


    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        return mImageUrl;
    }


    /**
     * @param imageText the imageText to set
     */
    public void setImageText(String imageText) {
        mImageText = imageText;
    }


    /**
     * @return the imageText
     */
    public String getImageText() {
        return mImageText;
    }
    
}
