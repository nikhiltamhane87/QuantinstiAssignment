package com.morningstar.environmentreadiness.utils;

import org.testng.asserts.SoftAssert;

public class Verify {
    private static final ECLogger logger= new ECLogger(Verify.class);

    public static void checkPoint(boolean condition, String msg) {
        SoftAssert softAssertion= new SoftAssert();
        logger.debug("softAssert Method Was Started");
        softAssertion.assertTrue(condition);
        logger.debug("softAssert Method Was Executed");

        logger.info(msg);
    }
}
