package com.example.yanagh.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request/response DTOs for Google Gemini AI API in Java.
 */
public class GeminiDto {

    public static class GeminiRequest {
        @SerializedName("contents")
        private List<GeminiContent> contents;

        public GeminiRequest(List<GeminiContent> contents) {
            this.contents = contents;
        }

        public List<GeminiContent> getContents() { return contents; }
        public void setContents(List<GeminiContent> contents) { this.contents = contents; }
    }

    public static class GeminiContent {
        @SerializedName("role")
        private String role;

        @SerializedName("parts")
        private List<GeminiPart> parts;

        public GeminiContent(String role, List<GeminiPart> parts) {
            this.role = role;
            this.parts = parts;
        }

        public GeminiContent(List<GeminiPart> parts) {
            this(null, parts);
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public List<GeminiPart> getParts() { return parts; }
        public void setParts(List<GeminiPart> parts) { this.parts = parts; }
    }

    public static class GeminiPart {
        @SerializedName("text")
        private String text;

        @SerializedName("inline_data")
        private GeminiInlineData inlineData;

        public GeminiPart(String text) {
            this.text = text;
        }

        public GeminiPart(GeminiInlineData inlineData) {
            this.inlineData = inlineData;
        }

        public GeminiPart(String text, GeminiInlineData inlineData) {
            this.text = text;
            this.inlineData = inlineData;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public GeminiInlineData getInlineData() { return inlineData; }
        public void setInlineData(GeminiInlineData inlineData) { this.inlineData = inlineData; }
    }

    public static class GeminiInlineData {
        @SerializedName("mime_type")
        private String mimeType;

        @SerializedName("data")
        private String data;

        public GeminiInlineData(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public static class GeminiResponse {
        @SerializedName("candidates")
        private List<GeminiCandidate> candidates;

        public List<GeminiCandidate> getCandidates() { return candidates; }
        public void setCandidates(List<GeminiCandidate> candidates) { this.candidates = candidates; }
    }

    public static class GeminiCandidate {
        @SerializedName("content")
        private GeminiContent content;

        public GeminiContent getContent() { return content; }
        public void setContent(GeminiContent content) { this.content = content; }
    }
}
