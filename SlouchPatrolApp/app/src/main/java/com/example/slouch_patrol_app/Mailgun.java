package com.example.slouch_patrol_app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class Mailgun {


    public static JsonNode sendProdEmail(String receiver, String sender, String subject, String body) throws UnirestException {
        String MESSAGE_ENDPOINT = "https://api.mailgun.net/v3/slouchpatrol.ttaggart.ca/messages";
        String emailFrom = sender;
        String emailRegex = "(.*?)@";

        // Extract the username from the sender email address in case they put the whole address
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(emailFrom);
        if (matcher.find()) {
            emailFrom = matcher.group(1);
        }

        HttpResponse<JsonNode> response = Unirest.post(MESSAGE_ENDPOINT)
                .basicAuth("api", BuildConfig.MAILGUN_KEY)
                .queryString("from", "<" + emailFrom + "@slouchpatrol.ttaggart.ca>")
                .queryString("to", receiver)
                .queryString("subject", subject)
                .queryString("text", body)
                .asJson();
                return response.getBody();

    }

    public static JsonNode sendNonProdEmail(String testBody) {
        String MESSAGE_ENDPOINT = "https://api.mailgun.net/v3/slouchpatrol.ttaggart.ca/messages";

        HttpResponse<JsonNode> response = Unirest.post(MESSAGE_ENDPOINT)
                .basicAuth("api", BuildConfig.MAILGUN_KEY)
                .queryString("from", "<TEST@slouchpatrol.ttaggart.ca>")
                .queryString("to", BuildConfig.NONPROD_EMAIL)
                .queryString("subject", "TEST")
                .queryString("text", testBody)
                .asJson();
                return response.getBody();
    }
}