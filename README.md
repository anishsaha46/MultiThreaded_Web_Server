# Multi-Threaded Web Server

A robust, multi-threaded web server implementation in Java that provides a flexible framework for handling HTTP requests with features like routing, middleware, session management, and template rendering.

## Features

- **Multi-threaded Request Handling**: Efficiently handles multiple concurrent connections using a thread pool
- **Routing System**: Flexible routing mechanism for handling different HTTP endpoints
- **Middleware Support**: Customizable middleware chain for request processing
- **Session Management**: Built-in session handling capabilities
- **Template Rendering**: Integrated FreeMarker template engine support
- **Static File Serving**: Serves static files and resources
- **Database Integration**: Basic database operations support
- **Logging**: Comprehensive logging system using Java's built-in logging

## Prerequisites

- Java 11 or higher
- Maven for dependency management

## Dependencies

- SLF4J (1.7.30) - Logging facade
- FreeMarker (2.3.31) - Template engine
- JUnit (4.13.2) - Testing framework

## Installation

1. Clone the repository
2. Navigate to the project directory
3. Build the project using Maven:
   ```bash
   mvn clean install
   ```

## Configuration

The server can be configured through the `config.properties` file. Key configuration options include:

- Server port
- Templates directory
- Log file location
- Static files directory

## Usage

### Starting the Server

```java
WebServer server = new WebServer("config.properties");
server.start();
```

### Defining Routes

```java
Router router = server.getRouter();
router.get("/", (request, path) -> {
    HttpResponse response = new HttpResponse();
    response.setStatusCode(200);
    response.setBody("Hello, World!");
    return response;
});
```

### Adding Middleware

```java
Middleware middleware = server.getMiddleware();
middleware.use((request) -> {
    // Process request
    return request;
});
```

### Using Templates

```java
TemplateRenderer renderer = server.getTemplateRenderer();
Map<String, Object> data = new HashMap<>();
data.put("name", "World");
String html = renderer.render("template", data);
```

## Project Structure

```
src/
  ├── server/         # Core server components
  ├── static/         # Static files
  └── templates/      # Template files
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.