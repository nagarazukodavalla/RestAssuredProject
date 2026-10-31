package apiTestCases;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "classpath:features",
        glue = "apiTestCases",
        tags = "@api",
        plugin = {
            "pretty",
            "html:target/cucumber-reports/cucumber.html",
            "json:target/cucumber-reports/cucumber.json",
            "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        })
public class ApiTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
