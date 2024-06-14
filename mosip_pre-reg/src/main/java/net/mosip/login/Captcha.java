package net.mosip.login;

import java.io.IOException;
import java.io.Console;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import net.mosip.envManager;

import okhttp3.*;

public class Captcha {
    public static void main(String[] args) throws IOException{
        String response = sendOtpWithCaptcha();
        System.out.println(response);
    }

    public static String sendOtpWithCaptcha() throws IOException{
        new envManager();

        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        OffsetDateTime gmtTime = now.withOffsetSameInstant(ZoneOffset.UTC);
        String formattedTime = formatter.format(gmtTime);
        envManager.updateEnv("currentTime", formattedTime);

        String userId = null;
        Console console = System.console();
        if (console != null) {
            userId = console.readLine("Enter User ID: ");
        } else{
            System.err.println("ERROR: Could not take in User ID!");
        }
        envManager.updateEnv("userId", userId);

        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\r\n    \"id\": \"mosip.pre-registration.login.sendotp\",\r\n    \"request\": {\r\n        \"langCode\": \"eng\",\r\n        \"userId\": \"" + userId + "\",\r\n        \"captchaToken\": null\r\n    },\r\n    \"version\": \"1.0\",\r\n    \"requesttime\": \"" + formattedTime + "\"\r\n}", mediaType);
        Request request = new Request.Builder()
            .url("https://uat2.mosip.net//preregistration/v1/login/sendOtpWithCaptcha")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build();
        Response response = client.newCall(request).execute();

        if(response.header("Set-Cookie") != null){
            String auth = ((response.header("Set-Cookie")).split(";")[0]).split("=")[1];
            envManager.updateEnv("auth", auth);
        }
        else{
            System.err.println("ERROR: NO HEADERS! LOGIN UNSUCCESSFUL!");
        }

        return response.body().string();
    }
}