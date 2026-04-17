package com.demowebshop.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.nio.file.Path;

public final class ExtentManager {
    private static ExtentReports extentReports;

    private ExtentManager() {
    }

    public static ExtentReports getInstance() {
        if (extentReports == null) {
            Path reportPath = Path.of("test-output", "ExtentReport.html");
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath.toString());
            sparkReporter.config().setReportName("Demo Web Shop Automation Report");
            sparkReporter.config().setDocumentTitle("QA Automation Take Home Assessment");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("Project", "Demo Web Shop");
            extentReports.setSystemInfo("Framework", "Selenium + TestNG");
        }
        return extentReports;
    }
}

