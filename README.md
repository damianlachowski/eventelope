# Eventelope

A comprehensive Java-based REST API testing framework that provides intelligent and flexible test management capabilities with advanced parsing and reporting features.

## Overview

Eventelope is designed to simplify REST API testing through a YAML-based configuration approach. It follows a request-verify model optimized for CI/CD pipelines, allowing testers to define complex multi-service test scenarios without writing code.

## Key Features

- **YAML-based test definitions** - Write tests in a simple, human-readable format
- **Variable flow tracking** - Comprehensive debugging with variable creation and modification history
- **Advanced assertions** - Powerful JSONPath-based response validation
- **Conditional execution** - Run steps based on previous response values
- **Retry and timeout management** - Handle eventual consistency between services
- **Template variables** - Support for both `${variable}` and `{{variable}}` syntax
- **Comprehensive reporting** - Detailed HTML and text reports with execution timeline
- **Service health checks** - Validate service availability before test execution

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.0 or higher

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run all tests
./gradlew run

# Run a specific test file
./gradlew run --args="--testDir src/test/resources/testcases/specific_test.yaml"

# Run all tests in a directory
./gradlew run --args="--testDir src/test/resources/testcases"
```

## Test Structure

Tests are defined in YAML files with the following structure:

```yaml
test:
  name: Sample Test
  description: A sample test demonstrating the framework
  
  preconditions:
    - step:
        name: Verify API Health
        request:
          method: GET
          url: https://api.example.com/health
        verify:
          status: 200
          
  setup:
    - step:
        name: Create Test Data
        request:
          method: POST
          url: https://api.example.com/data
          payload:
            name: "Test Entity"
        verify:
          status: 201
        extractions:
          - from: $.id
            storeTo: entityId
            
  execution:
    - step:
        name: Test API Endpoint
        request:
          method: GET
          url: https://api.example.com/entities/${entityId}
        verify:
          status: 200
          jsonPathAssertions:
            - path: $.name
              type: equals
              expected: "Test Entity"
              
  cleanup:
    - step:
        name: Delete Test Data
        request:
          method: DELETE
          url: https://api.example.com/entities/${entityId}
        verify:
          status: 204
```

## Variable Handling

Eventelope provides powerful variable extraction and substitution capabilities:

- Extract values from responses using JSONPath syntax
- Use variables in URLs, payloads, and assertions
- Track variable creation and modification throughout test execution

Example:
```yaml
- step:
    name: Extract User ID
    request:
      method: GET
      url: https://api.example.com/users/search?name=John
    extractions:
      - from: $.[0].id
        storeTo: userId
        
- step:
    name: Get User Details
    request:
      method: GET
      url: https://api.example.com/users/${userId}
    verify:
      status: 200
```

## Conditional Execution

Steps can be conditionally executed based on previous responses:

```yaml
- step:
    name: Conditionally Execute Step
    condition: "${orderStatus} == 'CREATED'"
    request:
      method: POST
      url: https://api.example.com/orders/${orderId}/process
```

## Authentication Support

Eventelope supports multiple authentication methods:

- Basic Authentication
- API Key Authentication
- JWT Authentication
- OAuth 2.0

## Reporting

After test execution, detailed reports are generated in both text and HTML formats. The HTML report includes:

- Test summary with pass/fail status
- Detailed step execution with timing information
- Request and response details
- Variable flow tracking visualization
- Assertion results

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.