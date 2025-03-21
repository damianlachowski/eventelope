package com.eventelope.reporting;

import com.eventelope.core.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates test execution reports in various formats.
 */
public class ReportGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Generate a simple text report for a list of test results.
     *
     * @param results List of test results
     * @param outputDir Directory to save the report
     * @return The path to the generated report
     */
    public String generateTextReport(List<TestResult> results, String outputDir) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_FORMATTER);
        String reportFileName = "eventelope_report_" + timestamp + ".txt";
        
        // Ensure output directory exists
        Path outputPath = Paths.get(outputDir);
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            LOGGER.error("Failed to create report directory: {}", outputDir, e);
            return null;
        }
        
        // Create report file
        File reportFile = outputPath.resolve(reportFileName).toFile();
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            // Write report header
            writer.write("Eventelope Test Execution Report\n");
            writer.write("==============================\n");
            writer.write("Generated at: " + LocalDateTime.now() + "\n\n");
            
            // Write summary
            int totalTests = results.size();
            long passedTests = results.stream().filter(TestResult::isPassed).count();
            long failedTests = totalTests - passedTests;
            
            writer.write("Summary:\n");
            writer.write("  Total Tests: " + totalTests + "\n");
            writer.write("  Passed: " + passedTests + "\n");
            writer.write("  Failed: " + failedTests + "\n");
            writer.write("  Success Rate: " + (totalTests > 0 ? (passedTests * 100 / totalTests) : 0) + "%\n\n");
            
            // Write detailed results
            writer.write("Detailed Results:\n");
            writer.write("================\n\n");
            
            for (int i = 0; i < results.size(); i++) {
                TestResult result = results.get(i);
                writer.write((i + 1) + ". " + result.getTestCase().getMetadata().getTestName() + "\n");
                writer.write("   Status: " + (result.isPassed() ? "PASSED" : "FAILED") + "\n");
                writer.write("   Description: " + result.getTestCase().getMetadata().getDescription() + "\n");
                writer.write("   HTTP Method: " + result.getTestCase().getRequest().getMethod() + "\n");
                writer.write("   Endpoint: " + result.getTestCase().getRequest().getEndpoint() + "\n");
                writer.write("   Response Status: " + result.getStatusCode() + "\n");
                
                if (!result.isPassed()) {
                    writer.write("   Failure Reasons:\n");
                    for (String failure : result.getFailureMessages()) {
                        writer.write("     - " + failure + "\n");
                    }
                }
                
                writer.write("\n");
            }
            
            LOGGER.info("Report generated successfully: {}", reportFile.getAbsolutePath());
            return reportFile.getAbsolutePath();
            
        } catch (IOException e) {
            LOGGER.error("Failed to generate report: {}", reportFile.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Generate an HTML report for a list of test results.
     *
     * @param results List of test results
     * @param outputDir Directory to save the report
     * @return The path to the generated report
     */
    public String generateHtmlReport(List<TestResult> results, String outputDir) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_FORMATTER);
        String reportFileName = "eventelope_report_" + timestamp + ".html";
        
        // Ensure output directory exists
        Path outputPath = Paths.get(outputDir);
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            LOGGER.error("Failed to create report directory: {}", outputDir, e);
            return null;
        }
        
        // Create report file
        File reportFile = outputPath.resolve(reportFileName).toFile();
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            // Calculate summary data
            int totalTests = results.size();
            long passedTests = results.stream().filter(TestResult::isPassed).count();
            long failedTests = totalTests - passedTests;
            int successRate = totalTests > 0 ? (int)(passedTests * 100 / totalTests) : 0;
            
            // Start HTML document
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang='en'>\n");
            writer.write("<head>\n");
            writer.write("  <meta charset='UTF-8'>\n");
            writer.write("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
            writer.write("  <title>Eventelope Test Report</title>\n");
            writer.write("  <style>\n");
            writer.write("    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333; }\n");
            writer.write("    h1, h2 { color: #2c3e50; }\n");
            writer.write("    .summary { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
            writer.write("    .test-case { background-color: #fff; border: 1px solid #ddd; border-radius: 5px; padding: 15px; margin-bottom: 15px; }\n");
            writer.write("    .test-case.passed { border-left: 5px solid #28a745; }\n");
            writer.write("    .test-case.failed { border-left: 5px solid #dc3545; }\n");
            writer.write("    .status { font-weight: bold; }\n");
            writer.write("    .status.passed { color: #28a745; }\n");
            writer.write("    .status.failed { color: #dc3545; }\n");
            writer.write("    .details { margin-top: 10px; }\n");
            writer.write("    .failure-reasons { background-color: #f8d7da; padding: 10px; border-radius: 5px; margin-top: 10px; }\n");
            writer.write("    .progress-bar { height: 20px; border-radius: 5px; overflow: hidden; background-color: #e9ecef; }\n");
            writer.write("    .progress-bar-inner { height: 100%; background-color: #28a745; width: " + successRate + "%; }\n");
            writer.write("  </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            
            // Report header
            writer.write("  <h1>Eventelope Test Execution Report</h1>\n");
            writer.write("  <p>Generated at: " + LocalDateTime.now() + "</p>\n");
            
            // Summary section
            writer.write("  <div class='summary'>\n");
            writer.write("    <h2>Summary</h2>\n");
            writer.write("    <p><strong>Total Tests:</strong> " + totalTests + "</p>\n");
            writer.write("    <p><strong>Passed:</strong> " + passedTests + "</p>\n");
            writer.write("    <p><strong>Failed:</strong> " + failedTests + "</p>\n");
            writer.write("    <p><strong>Success Rate:</strong> " + successRate + "%</p>\n");
            writer.write("    <div class='progress-bar'><div class='progress-bar-inner'></div></div>\n");
            writer.write("  </div>\n");
            
            // Detailed results
            writer.write("  <h2>Detailed Results</h2>\n");
            
            // Iterate through test results
            for (int i = 0; i < results.size(); i++) {
                TestResult result = results.get(i);
                String testStatus = result.isPassed() ? "passed" : "failed";
                
                writer.write("  <div class='test-case " + testStatus + "'>\n");
                writer.write("    <h3>" + (i + 1) + ". " + result.getTestCase().getMetadata().getTestName() + "</h3>\n");
                writer.write("    <p><span class='status " + testStatus + "'>" + testStatus.toUpperCase() + "</span></p>\n");
                writer.write("    <div class='details'>\n");
                writer.write("      <p><strong>Description:</strong> " + result.getTestCase().getMetadata().getDescription() + "</p>\n");
                writer.write("      <p><strong>HTTP Method:</strong> " + result.getTestCase().getRequest().getMethod() + "</p>\n");
                writer.write("      <p><strong>Endpoint:</strong> " + result.getTestCase().getRequest().getEndpoint() + "</p>\n");
                writer.write("      <p><strong>Response Status:</strong> " + result.getStatusCode() + "</p>\n");
                
                // Add failure reasons for failed tests
                if (!result.isPassed()) {
                    writer.write("      <div class='failure-reasons'>\n");
                    writer.write("        <p><strong>Failure Reasons:</strong></p>\n");
                    writer.write("        <ul>\n");
                    
                    for (String failure : result.getFailureMessages()) {
                        writer.write("          <li>" + failure + "</li>\n");
                    }
                    
                    writer.write("        </ul>\n");
                    writer.write("      </div>\n");
                }
                
                writer.write("    </div>\n");
                writer.write("  </div>\n");
            }
            
            // End HTML document
            writer.write("</body>\n");
            writer.write("</html>\n");
            
            LOGGER.info("HTML report generated successfully: {}", reportFile.getAbsolutePath());
            return reportFile.getAbsolutePath();
            
        } catch (IOException e) {
            LOGGER.error("Failed to generate HTML report: {}", reportFile.getAbsolutePath(), e);
            return null;
        }
    }
}
