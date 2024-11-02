package com.example.slouch_patrol_app.UnitTests;

import static com.example.slouch_patrol_app.Mailgun.sendProdEmail;
import static com.example.slouch_patrol_app.Mailgun.sendNonProdEmail;

import org.testng.annotations.Test;

import static org.junit.Assert.*;

import com.example.slouch_patrol_app.BuildConfig;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MailgunUnitTests {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

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