# healbot-parser

A Java-based parser for World of Warcraft HealBot SavedVariables files. This tool extracts click-casting configurations and generates HTML reports.

## Project Structure

This is a Maven-based Java 17 project with the following structure:

```
healbot-parser/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/healbotparser/
│   │   │       ├── Main.java           # Entry point
│   │   │       └── HealBotParser.java  # Parser implementation (stub)
│   │   └── resources/
│   │       └── templates/
│   │           └── report.html         # Thymeleaf HTML template
│   └── test/
│       └── java/
├── pom.xml                              # Maven configuration
└── README.md
```

## Dependencies

- **LuaJ 3.0.1**: For parsing Lua SavedVariables files
- **Thymeleaf 3.1.2**: For HTML report generation

## Building

Requires Java 17 and Maven:

```bash
mvn clean package
```

## Usage

```bash
java -jar target/healbot-parser-1.0-SNAPSHOT.jar <wow-directory-path>
```

Example:
```bash
java -jar target/healbot-parser-1.0-SNAPSHOT.jar "C:\Program Files (x86)\World of Warcraft"
```

## Current Status

This is the initial scaffolding implementation. The parser currently contains stub methods with TODO comments for:
- File traversal through WoW directory structure
- Lua file parsing using LuaJ
- HTML report generation using Thymeleaf

Full implementation will be added in follow-up PRs.
