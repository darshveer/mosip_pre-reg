package net.mosip.login;

import java.io.IOException;
import java.io.Console;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mosip.envManager;
import net.mosip.models.login.captcha.*;

import okhttp3.*;

public class Captcha {
    public static void main(String[] args) throws IOException{
        String userId = null;
        Console console = System.console();
        if (console != null) {
            userId = console.readLine("Enter User ID: ");
        } else{
            System.err.println("ERROR: Could not take in User ID!");
            System.out.println("------------------------------");
        }
        envManager.updateEnv("userId", userId);
        
        try { 
            String response = sendOtpWithCaptcha(userId);
            if (response.substring(0, 5).equals("ERROR")) {
                System.err.println(response);
            } else {
                envManager.updateEnv("auth", response);
            }
        } catch (CaptchaError | CaptchaHeaderError e) {
            System.err.println(e.getMessage());
            System.out.println("------------------------------");
        }
    }

    public static String sendOtpWithCaptcha(String userId) throws IOException, CaptchaHeaderError, CaptchaError {
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        OffsetDateTime gmtTime = now.withOffsetSameInstant(ZoneOffset.UTC);
        String formattedTime = formatter.format(gmtTime);

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
        String responseBody = response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDataCaptcha result = objectMapper.readValue(responseBody, ResponseDataCaptcha.class);

        if (result.errors == null) {
            if(response.header("Set-Cookie") != null){
                String auth = ((response.header("Set-Cookie")).split(";")[0]).split("=")[1];
                return auth;
            }
            else{
                throw new CaptchaHeaderError();
            }
        } else {
                throw new CaptchaError(result);
        }
    }
}