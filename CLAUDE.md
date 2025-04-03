# GameCollection Development Guide

## Build Commands
- Build the project: `mvn clean package`
- Run the application: `mvn clean javafx:run`
- Run tests: `mvn test`
- Run a single test: `mvn test -Dtest=TestClassName#testMethodName`

## Code Style Guidelines
- Java version: 21 (source and target)
- Use JavaFX patterns (MVVM with FXML for UI definitions)
- Imports: Group by package, with standard Java imports first
- Formatting: 4-space indentation, braces on same line
- Naming conventions:
  - Classes: PascalCase
  - Methods/variables: camelCase
  - FXML IDs: camelCase with fx:id attribute
- Error handling: Use appropriate exceptions, handle IOException for FXML loading

## Project Architecture
- JavaFX application with FXML views
- FXGL game engine for game implementations
- Package structure: com.example.gamecollection
- Main classes:
  - HelloApplication: Entry point
  - Controllers: One per view for handling UI interactions

## Dependencies
- JavaFX 21 (controls, FXML, web, swing, media)
- FXGL 17.3 game engine
- JUnit 5.10.0 for testing