Bookstore API Automation Framework
This project provides an API automation framework for testing the Bookstore API, built using Java, RestAssured, TestNG, and Allure for reporting. The framework is designed to validate all major CRUD operations, handle both positive and negative scenarios, and ensure scalability and maintainability. A GitHub Actions CI/CD pipeline automates test execution.
Prerequisites

Java 17 or higher
Maven 3.8.x
Git
Bookstore API running locally (follow instructions in the Bookstore repository)
Allure command-line tool for report generation (optional, for local viewing)

Setup Instructions

Clone the Repository:
git clone <your-forked-repo-url>
cd bookstore-api-automation


Set Up the Bookstore API:

Fork and clone the Bookstore repository.
Follow its README to start the API locally (default: http://localhost:8000).


Configure Environment:

Update the config.properties file in src/test/resources with the API base URL:baseUrl=http://localhost:8000




Install Dependencies:
mvn clean install


Run Tests:
mvn test


Generate Allure Report:
allure serve target/allure-results



Testing Strategy
Approach to Test Flows

Modular Design: Tests are organized into reusable methods for each CRUD operation (Create, Read, Update, Delete) using RestAssured.
Request Chaining: Outputs from one API call (e.g., book ID from a POST request) are used as inputs for subsequent calls (e.g., GET, PUT, DELETE).
Positive and Negative Scenarios: Tests cover valid inputs, edge cases, and error conditions (e.g., invalid IDs, missing fields).
Assertions: Validate status codes, response payloads, and headers using Hamcrest matchers for precision.

Ensuring Reliability and Maintainability

Configuration Management: Uses config.properties for environment-specific settings, supporting dev, QA, and prod environments.
Reusable Utilities: Common methods for API requests and response validation are centralized in ApiUtils.java.
TestNG Annotations: Organize test execution with @BeforeClass, @Test, and @AfterClass for setup, execution, and cleanup.
Allure Reporting: Provides detailed, visual reports with pass/fail/skip status and request/response logs.

Challenges and Solutions

Dynamic Data Handling: Handled dynamic book IDs by extracting them from responses and reusing them in chained requests.
Error Handling: Implemented robust negative test cases to validate API behavior under failure conditions.
CI/CD Integration: Configured GitHub Actions to ensure consistent test execution across environments.

Project Structure
bookstore-api-automation/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com.bookstore.api/
│   │           ├── utils/
│   │           │   └── ApiUtils.java       # Utility methods for API calls
│   │           └── model/
│   │               └── Book.java           # POJO for book data
│   ├── test/
│   │   └── java/
│   │       └── com.bookstore.api.tests/
│   │           └── BookstoreApiTests.java  # Test cases
│   └── resources/
│       └── config.properties               # Environment configuration
├── .github/
│   └── workflows/
│       └── ci.yml                         # GitHub Actions pipeline
├── pom.xml                                # Maven dependencies
└── README.md                              # This file

Sample Test Report
After running mvn test and allure serve target/allure-results, the Allure report provides:

Overview: Summary of test results (Pass/Fail/Skip).
Suites: Detailed view of each test case with request/response details.
Graphs: Visual representation of test execution status.

CI/CD Pipeline
The GitHub Actions pipeline (.github/workflows/ci.yml) automates:

Environment setup (Java 17, Maven).
Dependency installation (mvn install).
Unit and integration test execution (mvn test).
Allure report generation and upload as an artifact.
