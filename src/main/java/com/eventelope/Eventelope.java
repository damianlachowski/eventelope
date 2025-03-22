package com.eventelope;

import com.eventelope.core.TestCase;
import com.eventelope.core.TestExecutor;
import com.eventelope.core.TestResult;
import com.eventelope.parser.YamlParser;
import com.eventelope.reporting.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Main entry point for the Eventelope API testing framework.
 */
public class Eventelope {
    private static final Logger LOGGER = LoggerFactory.getLogger(Eventelope.class);
    
    // Default paths
    private static final String DEFAULT_TEST_DIR = "src/test/resources/testcases";
    private static final String DEFAULT_REPORT_DIR = "build/reports";
    
    private final YamlParser yamlParser;
    private final TestExecutor testExecutor;
    private final ReportGenerator reportGenerator;
    
    public Eventelope() {
        this.yamlParser = new YamlParser();
        this.testExecutor = new TestExecutor();
        this.reportGenerator = new ReportGenerator();
    }
    
    /**
     * Run tests from a directory of YAML test files.
     *
     * @param testDir Directory containing test files
     * @param reportDir Directory to store reports
     * @return True if all tests pass, false otherwise
     */
    public boolean runTests(String testDir, String reportDir) {
        LOGGER.info("Starting Eventelope test execution");
        LOGGER.info("Test directory: {}", testDir);
        LOGGER.info("Report directory: {}", reportDir);
        
        // Check if test directory exists
        Path testPath = Paths.get(testDir);
        if (!Files.exists(testPath)) {
            LOGGER.error("Test directory does not exist: {}", testDir);
            return false;
        }
        
        // Parse test cases
        List<TestCase> testCases = yamlParser.parseTestCases(testDir);
        
        if (testCases.isEmpty()) {
            LOGGER.warn("No test cases found in {}", testDir);
            return false;
        }
        
        LOGGER.info("Found {} test cases", testCases.size());
        
        // Execute tests
        LOGGER.info("Executing tests...");
        List<TestResult> results = testExecutor.executeTests(testCases);
        
        // Generate reports
        LOGGER.info("Generating reports...");
        reportGenerator.generateTextReport(results, reportDir);
        reportGenerator.generateHtmlReport(results, reportDir);
        
        // Calculate pass/fail summary
        long passedTests = results.stream().filter(TestResult::isPassed).count();
        long failedTests = results.size() - passedTests;
        
        LOGGER.info("Test execution complete");
        LOGGER.info("Total: {}, Passed: {}, Failed: {}", results.size(), passedTests, failedTests);
        
        return failedTests == 0;
    }
    
    /**
     * Run a single test file.
     *
     * @param testFile Path to the YAML test file
     * @param reportDir Directory to store reports
     * @return True if the test passes, false otherwise
     */
    public boolean runTest(String testFile, String reportDir) {
        LOGGER.info("Starting Eventelope single test execution");
        LOGGER.info("Test file: {}", testFile);
        
        // Check if test file exists
        File file = new File(testFile);
        if (!file.exists() || !file.isFile()) {
            LOGGER.error("Test file does not exist: {}", testFile);
            return false;
        }
        
        // Parse test case
        TestCase testCase = yamlParser.parseTestCase(file);
        
        // Execute test
        LOGGER.info("Executing test: {}", testCase.getTestName());
        TestResult result = testExecutor.executeTest(testCase);
        
        // Generate reports
        LOGGER.info("Generating reports...");
        List<TestResult> results = new ArrayList<>();
        results.add(result);
        reportGenerator.generateTextReport(results, reportDir);
        reportGenerator.generateHtmlReport(results, reportDir);
        
        LOGGER.info("Test execution complete");
        LOGGER.info("Result: {}", result.isPassed() ? "PASSED" : "FAILED");
        
        return result.isPassed();
    }
    
    /**
     * Application entry point.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // Parse command-line arguments
        String testDir = DEFAULT_TEST_DIR;
        String reportDir = DEFAULT_REPORT_DIR;
        String singleTestFile = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--testDir") && i + 1 < args.length) {
                testDir = args[i + 1];
                i++;
            } else if (args[i].equals("--reportDir") && i + 1 < args.length) {
                reportDir = args[i + 1];
                i++;
            } else if (args[i].equals("--testFile") && i + 1 < args.length) {
                singleTestFile = args[i + 1];
                i++;
            } else if (args[i].equals("--help") || args[i].equals("-h")) {
                printHelp();
                return;
            }
        }
        
        // Create the Eventelope instance
        Eventelope eventelope = new Eventelope();
        
        // Run tests based on arguments
        boolean allPassed;
        if (singleTestFile != null) {
            allPassed = eventelope.runTest(singleTestFile, reportDir);
        } else {
            allPassed = eventelope.runTests(testDir, reportDir);
        }
        
        // Exit with appropriate status code
        System.exit(allPassed ? 0 : 1);
    }
    
    /**
     * Print usage help information.
     */
    private static void printHelp() {
        System.out.println("Eventelope - REST API Testing Framework");
        System.out.println("Usage:");
        System.out.println("  java -jar eventelope.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --testDir <dir>     Directory containing test YAML files (default: src/test/resources/testcases)");
        System.out.println("  --reportDir <dir>   Directory for reports (default: build/reports)");
        System.out.println("  --testFile <file>   Run a single test file");
        System.out.println("  --help, -h          Show this help message");
    }
}
