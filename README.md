# 🗂️ TaskManger

> A simple Java task management project built to practice **OOP**, **domain modeling**, and clean project structure.

## ✨ Overview

**TaskManger** is a lightweight Java project that models a task system with support for:

- ✅ unique task IDs
- ✅ task history tracking
- ✅ priority levels
- ✅ task status types
- ✅ task categories
- ✅ notification types
- ✅ task dependencies

This project is a solid starting point for building a more complete **Task Manager application** in Java.

---

## 🚀 Features

### ✅ Implemented

- Create a task with a unique UUID
- Store a task title
- Automatically create a history entry when a task is created
- Add custom history entries
- Update task priority
- Compare task importance
- Organize task-related domain types with enums:
  - `PriorityLevel`
  - `TaskStatus`
  - `TaskCategory`
  - `NotificationType`

---

## 🧱 Project Structure

```bash
src/
├── Main.java
└── task/
    ├── Task.java
    └── domain/
        ├── HistoryEntry.java
        ├── NotificationType.java
        ├── PriorityLevel.java
        ├── TaskCategory.java
        └── TaskStatus.java
```

---

## 🛠️ Tech Stack

- **Java**
- **OOP**
- **UUID**
- **LocalDateTime**
- **Vector**

---

## 📌 Current Project State

This project is currently in an **early development stage**.

Right now, it focuses on the **core domain model** of a task manager rather than a full application with UI or database support.

That makes it a great project for:

- learning Java
- practicing object-oriented programming
- understanding domain-driven structure
- extending functionality step by step

---

## ▶️ Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Ngongangromels/TaskManger.git
cd TaskManger
```

### 2. Compile the project

```bash
javac -d out src/task/domain/*.java src/task/*.java src/Main.java
```

### 3. Run the application

```bash
java -cp out Main
```

---

## 💡 Example

Current `Main.java` creates a task like this:

```java
Task task = new Task("push to main branch");
System.out.print(task);
```

Example output will look similar to:

```text
Task: <generated-uuid> with title: push to main branch
history: [Task { action: task <generated-uuid> is created, modified: <timestamp> }]
```

---

## 🧠 Core Domain Types

### `PriorityLevel`

- `LOW`
- `MEDIUM`
- `HIGH`
- `CRITICAL`

### `TaskStatus`

- `TODO`
- `BLOCKED`
- `IN_PROGRESS`
- `DONE`

### `TaskCategory`

- `BUGFIX`
- `FEATURE`
- `DOCUMENTATION`
- `RESEARCH`

### `NotificationType`

- `EMAIL`
- `SMS`
- `CONSOLE`

---

## 🔮 Roadmap

Planned improvements for the project:

- [ ] Add getters and setters
- [ ] Add support for task status updates
- [ ] Add category and notification assignment
- [ ] Add dependency management methods
- [ ] Add input validation
- [ ] Improve `toString()` formatting
- [ ] Add unit tests
- [ ] Build a CLI version
- [ ] Add file or database persistence
- [ ] Create a GUI or web version

---

## 🎯 Learning Goals

This project is useful for practicing:

- object-oriented design
- class relationships
- enums and domain modeling
- history tracking
- scalable Java project structure

---

## 🤝 Contributing

Contributions, ideas, and improvements are welcome.

1. Fork the repository
2. Create a new branch
3. Make your changes
4. Open a pull request

---

## ⭐ Support

If you like this project, give it a **star** on GitHub and follow its progress 🚀
