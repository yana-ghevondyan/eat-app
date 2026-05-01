package com.example.yanagh.api;

import com.example.yanagh.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Sends welcome email after sign-up via Mailjet REST API v3.1.
 * <p>
 * Add to {@code local.properties} (do not commit):
 * <pre>
 * MAILJET_API_KEY=your_api_key
 * MAILJET_SECRET_KEY=your_secret_key
 * MAILJET_FROM_EMAIL=verified_sender@yourdomain.com
 * MAILJET_FROM_NAME=Yanagh
 * </pre>
 * The sender address must be verified in the Mailjet dashboard.
 */
public final class MailjetMailService {

    private static final String SEND_URL = "https://api.mailjet.com/v3.1/send";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build();

    private MailjetMailService() {}

    /** Non-blocking; safe to call from UI thread. */
    public static void sendWelcomeEmailAsync(String toEmail, String recipientDisplayName) {
        String api = BuildConfig.MAILJET_API_KEY;
        String secret = BuildConfig.MAILJET_SECRET_KEY;
        String from = BuildConfig.MAILJET_FROM_EMAIL;
        String fromName = BuildConfig.MAILJET_FROM_NAME;
        if (api == null || api.isEmpty() || secret == null || secret.isEmpty()
                || from == null || from.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                sendBlocking(toEmail, recipientDisplayName, api, secret, from, fromName);
            } catch (Exception ignored) {
            }
        }, "mailjet-welcome").start();
    }

    private static void sendBlocking(
            String toEmail,
            String recipientName,
            String apiKey,
            String secretKey,
            String fromEmail,
            String fromDisplayName
    ) throws IOException, JSONException {
        String display = fromDisplayName != null && !fromDisplayName.isEmpty()
                ? fromDisplayName
                : "Yanagh";

        JSONObject from = new JSONObject();
        from.put("Email", fromEmail);
        from.put("Name", display);

        JSONObject to = new JSONObject();
        to.put("Email", toEmail);
        to.put("Name", recipientName != null && !recipientName.isEmpty() ? recipientName : toEmail);

        JSONObject message = new JSONObject();
        message.put("From", from);
        message.put("To", new JSONArray().put(to));
        message.put("Subject", "Yanagh - welcome");
        message.put("TextPart", buildTextPart(recipientName));
        message.put("HTMLPart", buildHtmlPart(recipientName));

        JSONObject root = new JSONObject();
        root.put("Messages", new JSONArray().put(message));

        String auth = Credentials.basic(apiKey, secretKey);
        RequestBody body = RequestBody.create(root.toString(), JSON);
        Request request = new Request.Builder()
                .url(SEND_URL)
                .header("Authorization", auth)
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            // Intentionally ignored — fire-and-forget from the app.
        }
    }

    private static String buildTextPart(String name) {
        String who = name != null && !name.isEmpty() ? name : "friend";
        return "Hello " + who + ",\n\n"
                + "Welcome to Yanagh — your food & nutrition app.\n"
                + "You can explore recipes, plan meals, and use the AI food assistant.\n\n"
                + "— The Yanagh team\n\n"
                + "---\n"
                + "Բարև " + who + ",\n\n"
                + "Բարի գալուստ «Յանաղ» սննդի ծրագիր։ Այստեղ կգտնեք բաղադրատոմսեր, օրվա սնունդ և AI օգնական։\n";
    }

    private static String buildHtmlPart(String name) {
        String who = name != null && !name.isEmpty() ? escapeHtml(name) : "there";
        return "<!DOCTYPE html><html><body style=\"font-family:sans-serif;line-height:1.5;color:#1B4332;\">"
                + "<h2 style=\"color:#2D6A4F;\">Welcome to Yanagh</h2>"
                + "<p>Hi " + who + ",</p>"
                + "<p>Thanks for joining our <strong>food &amp; nutrition</strong> app. "
                + "Explore recipes, daily meal ideas, and the AI assistant.</p>"
                + "<hr style=\"border:none;border-top:1px solid #B7E4C7;margin:24px 0;\"/>"
                + "<h3 style=\"color:#2D6A4F;\">Բարի գալուստ Յանաղ</h3>"
                + "<p>Շնորհակալություն գրանցման համար։ Սա <strong>սննդի ծրագիր</strong> է՝ բաղադրատոմսերով, օրվա սննդով և AI օգնականով։</p>"
                + "</body></html>";
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
