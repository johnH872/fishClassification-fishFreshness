package hcmute.edu.project_06_fishclassification.model;

import com.google.firebase.Timestamp;

public class Image {
    private String name;
    private String imageUrl;
    private String relatedLinkUrl;
    private Timestamp saveDate;

    private String userId;

    public Image() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRelatedLinkUrl() {
        return relatedLinkUrl;
    }

    public void setRelatedLinkUrl(String relatedLinkUrl) {
        this.relatedLinkUrl = relatedLinkUrl;
    }

    public Timestamp getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Timestamp saveDate) {
        this.saveDate = saveDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
