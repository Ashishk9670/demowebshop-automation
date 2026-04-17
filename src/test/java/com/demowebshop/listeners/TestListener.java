package com.demowebshop.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.demowebshop.core.DriverManager;
import com.demowebshop.reporting.ExtentManager;
import com.demowebshop.utils.ScreenshotUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();
    private static final ExtentReports REPORT = ExtentManager.getInstance();

    @Override
    public void onStart(ITestContext context) {
        REPORT.setSystemInfo("Suite", context.getSuite().getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = REPORT.createTest(result.getMethod().getMethodName());
        TEST.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TEST.get().pass("Test passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest extentTest = TEST.get();
        extentTest.fail(result.getThrowable());

        try {
            String screenshotPath = ScreenshotUtils.capture(DriverManager.getDriver(), result.getMethod().getMethodName());
            extentTest.fail("Failure screenshot",
                    MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
        } catch (Exception exception) {
            extentTest.warning("Screenshot capture failed: " + exception.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TEST.get().skip("Test skipped.");
    }

    @Override
    public void onFinish(ITestContext context) {
        REPORT.flush();
        TEST.remove();
    }
}

