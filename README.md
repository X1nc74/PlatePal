# PlatePal

PlatePal is an object-oriented restaurant discovery, rating, and social recommendation system developed in Java.

The application allows users to discover restaurants, keep personal restaurant lists, submit ratings and reviews, view personal rankings, and interact with other users. Administrators can also manage restaurant information.

## Team Members

- Xinpeng Cheng
- Sunny Chen
- Nicole Zhang

## Core Features

### Account Management
- User registration
- User login and logout
- User profiles

### Restaurant Discovery
- Search restaurants by name, cuisine, location, and price category
- View restaurant details
- Sort restaurants by name, price, and average rating
- View personalized restaurant rankings

### Personal Restaurant Lists
- Add restaurants to a Visited list
- Add restaurants to a Want to Try list
- Remove restaurants from personal lists
- Automatically remove a restaurant from Want to Try when it is marked as Visited

### Ratings and Reviews
- Rate restaurants on a 1–10 scale
- Update an existing rating
- Write restaurant reviews
- Update an existing review
- View average restaurant ratings and rating counts
- View written reviews

### Social Features
- Browse registered users
- View user profiles
- Follow and unfollow users
- View restaurant activity from followed users

### Administrator Features
- Add restaurants
- Update restaurant information
- Remove restaurants
- Restrict restaurant management operations to administrators

## Technologies

- Java 17
- Maven
- JUnit 5
- Gson
- JSON file persistence
- PlantUML
- Git and GitHub

## Project Structure

PlatePal uses a layered object-oriented design to separate responsibilities between different parts of the application.

```text
PlatePal/
├── data/
│   └── platepal-data.json
│
├── docs/
│   ├── class-diagrams/
│   └── sequence-diagrams/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── platepal/
│   │           ├── controller/
│   │           ├── exception/
│   │           ├── model/
│   │           ├── persistence/
│   │           ├── repository/
│   │           ├── service/
│   │           ├── strategy/
│   │           └── ui/
│   │
│   └── test/
│       └── java/
│           └── platepal/
│
├── README.md
└── pom.xml
```

### Main Packages

- `model` – Contains domain objects such as User, Restaurant, Rating, and Review.
- `controller` – Handles requests from the user interface and communicates with the service layer.
- `service` – Contains application and business logic.
- `repository` – Provides data access operations for application objects.
- `persistence` – Handles loading and saving application data using JSON.
- `strategy` – Contains interchangeable restaurant sorting strategies.
- `ui` – Provides the menu-based user interface.
- `exception` – Contains application-specific exceptions.

## Object-Oriented Design

PlatePal applies the main object-oriented programming concepts covered in the course.

### Encapsulation

Application data and behavior are grouped into appropriate classes. Domain objects control their own state while services handle application-level business rules.

### Inheritance

User roles and related application structures use inheritance where an "is-a" relationship is appropriate.

### Abstraction

Repository and strategy abstractions separate higher-level application logic from specific implementations.

### Polymorphism

Restaurant sorting behavior can be selected through a common sorting strategy interface without requiring the service layer to depend on one specific sorting implementation.

## Design Pattern

PlatePal uses the **Strategy Pattern** for restaurant sorting.

Different sorting behaviors implement a common `RestaurantSortStrategy` interface. This allows the application to change how restaurants are sorted without changing the main restaurant service logic.

Examples include sorting restaurants by:

- Name
- Price
- Average rating

This design keeps sorting behavior separate from restaurant management and makes additional sorting strategies easier to add.

## Application Architecture

The main application flow follows a layered structure:

```text
User Interface
      |
      v
Controller
      |
      v
Service
      |
      v
Repository
      |
      v
Persistence / JSON Data
```

This separation helps keep user interaction, business logic, and data access responsibilities independent from each other.

## UML Documentation

UML documentation for the project is stored in the `docs` directory.

The project documentation includes:

- Use Case Diagram
- Use Case Descriptions
- Class Diagrams
- Sequence Diagrams

The diagrams describe the main system structure and interactions between users, controllers, services, repositories, and domain objects.

## Data Persistence

PlatePal uses JSON-based file persistence through Gson.

Application data is stored in:

```text
data/platepal-data.json
```

The persistent data includes information such as:

- Users
- Restaurants
- Ratings
- Reviews

The persistence layer loads the data when needed and saves application changes back to the JSON file.

## Requirements

To build the project, the following software is required:

- Java 17 or later
- Maven

You can verify the installed versions with:

```bash
java -version
mvn -version
```

## Build and Test

Clone the repository:

```bash
git clone https://github.com/X1nc74/PlatePal.git
```

Enter the project directory:

```bash
cd PlatePal
```

Run all automated tests:

```bash
mvn clean test
```

To compile and package the project:

```bash
mvn clean package
```

## Running the Application

After cloning the repository, compile the project using Maven:

```bash
mvn clean package
```

Then run the application's `Main` class from your IDE or Java development environment.

The application uses the JSON file in the `data` directory for persistent storage.

## Testing

JUnit 5 is used for automated testing.

The test suite covers important application behavior, including restaurant discovery, ratings and reviews, personal lists, user operations, administrator functionality, and other service-level behavior.

Tests can be executed with:

```bash
mvn test
```

## Repository Contents

This repository contains the complete PlatePal project, including:

- Java source code
- Unit tests
- Maven configuration
- Sample JSON data
- UML class diagrams
- UML sequence diagrams
- Project documentation

## Authors

- Xinpeng Cheng
- Sunny Chen
- Nicole Zhang
