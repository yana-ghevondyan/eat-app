package com.example.yanagh;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;


public class UserProfile {
    public static final String COLLECTION_NAME = "users";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PROFILE_PHOTO_URL = "profilePhotoUrl";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    @DocumentId
    private String userId;
    private String displayName;
    private String email;
    private String profilePhotoUrl;
    @ServerTimestamp
    private Timestamp updatedAt;

    public UserProfile() {
        this.userId = "";
        this.displayName = "";
        this.email = "";
        this.profilePhotoUrl = "";
        this.updatedAt = null;
    }

    public UserProfile(String userId, String displayName, String email, String profilePhotoUrl, Timestamp updatedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
