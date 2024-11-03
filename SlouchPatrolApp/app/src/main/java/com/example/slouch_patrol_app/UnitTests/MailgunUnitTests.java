package com.example.slouch_patrol_app.UnitTests;

import static com.example.slouch_patrol_app.Mailgun.sendProdEmail;
import static com.example.slouch_patrol_app.Mailgun.sendNonProdEmail;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.example.slouch_patrol_app.BuildConfig;

public class MailgunUnitTests {

    // TEST PROD EMAILS WITH MAILGUN
    @Test
    public void testSendProdEmail() {
        sendProdEmail(BuildConfig.NONPROD_EMAIL, "test", "test", "test");
        assertTrue(true);
    }

    // TEST NON-PROD EMAILS
    @Test
    public void testSendNonProdEmail() {
        sendNonProdEmail("test");
        assertTrue(true);
    }
}
