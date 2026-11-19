package com.mycompany.prog5121_poe_part1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginTest {
    private Login user() {
        Login value = new Login();
        value.firstName = "Ada";
        value.lastName = "Lovelace";
        value.username = "a_da";
        value.password = "Welcome9!";
        value.cellPhoneNumber = "+27831234567";
        return value;
    }

    @Test void correctRegistrationAndLogin() {
        Login login = user();
        assertTrue(login.checkUserName());
        assertTrue(login.checkPasswordComplexity());
        assertTrue(login.checkCellPhoneNumber());
        assertTrue(login.registerUser().contains("successfully"));
        assertTrue(login.loginUser("a_da", "Welcome9!"));
        assertTrue(login.returnLoginStatus(true).contains("Ada"));
    }

    @Test void usernameRequiresUnderscoreAndAtMostFiveCharacters() {
        Login login = user();
        login.username = "abcdef";
        assertFalse(login.checkUserName());
        login.username = "alice";
        assertFalse(login.checkUserName());
        login.username = "a_bc";
        assertTrue(login.checkUserName());
    }

    @Test void passwordNeedsUppercaseDigitSpecialAndEightCharacters() {
        Login login = user();
        login.password = "hello123!";
        assertFalse(login.checkPasswordComplexity());
        login.password = "Helloabcd!";
        assertFalse(login.checkPasswordComplexity());
        login.password = "Hello123";
        assertFalse(login.checkPasswordComplexity());
        login.password = "A1!abcd";
        assertFalse(login.checkPasswordComplexity());
    }

    @Test void phoneMustUseSouthAfricanInternationalCode() {
        Login login = user();
        login.cellPhoneNumber = "0831234567";
        assertFalse(login.checkCellPhoneNumber());
        login.cellPhoneNumber = "+27831234567";
        assertTrue(login.checkCellPhoneNumber());
    }

    @Test void invalidLoginIsRejected() {
        assertFalse(user().loginUser("a_da", "Incorrect!9"));
    }
}
