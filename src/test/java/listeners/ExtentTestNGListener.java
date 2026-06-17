package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ExtentReportManager;

public class ExtentTestNGListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> EXTENT_TEST = new ThreadLocal<>();
    private ExtentReports extentReports;

    @Override
    public void onStart(ITestContext context) {
        extentReports = ExtentReportManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        ExtentTest extentTest = extentReports.createTest(testName, description);
        EXTENT_TEST.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        getExtentTest().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        getExtentTest().log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        getExtentTest().log(Status.SKIP, result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
    }

    private ExtentTest getExtentTest() {
        ExtentTest extentTest = EXTENT_TEST.get();
        if (extentTest == null) {
            throw new IllegalStateException("ExtentTest is not initialized for the current thread");
        }
        return extentTest;
    }
}
