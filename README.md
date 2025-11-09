# Networked Calculator

This is a simple client-server application written in Java that provides remote calculation services. The server can handle multiple concurrent clients, evaluate complex mathematical expressions, and return the results.

It's a demonstration of multi-threaded socket programming, custom communication protocols, and the implementation of the Shunting-yard algorithm for parsing mathematical expressions.

## ✨ Features

*   **Client-Server Architecture**: A robust TCP-based server and a simple command-line client.
*   **Multi-Threaded Server**: Can handle multiple client connections simultaneously.
*   **Infix Expression Support**: Evaluates standard mathematical expressions like `(5 + 3) * 2`.
*   **Full Operator Support**: Includes `+`, `-`, `*`, `/`, `%` (modulo), and `^` (power).
*   **Error Handling**: Gracefully handles invalid expressions and provides specific error codes.

## 🚀 Quick Start

1.  **Compile the source code:**
    ```bash
    javac -d bin src/*.java
    ```
2.  **Start the server:**
    ```bash
    java -cp bin CalcServer
    ```
3.  **In a new terminal, start the client:**
    ```bash
    java -cp bin CalcClient
    ```

## 📚 Documentation (Wiki)

For detailed information about the project's architecture, communication protocol, error codes, and a step-by-step guide on how to run the application, please visit our comprehensive **[project Wiki](https://github.com/aiyh0/CloudCalculator/wiki)**.

The wiki is the primary source of documentation for this project.
