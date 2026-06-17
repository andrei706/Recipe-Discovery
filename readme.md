# Recipe Discovery

A web application that helps users discover recipes based on the ingredients they already have. Users can manage their personal ingredient inventory, explore matching recipes, chat with an AI cooking assistant, and generate personalized meal plans.

Project documentation with more diagrams and information regarding the development of the project in Romanian can be found on the following link:
 📄 [Recipe Discovery - Technical Report](https://docs.google.com/document/d/1DnVt5wJ-H-TxImE4ZdVCxzWtQZN0lEsnuDr36GdX1hg/edit?tab=t.0)
 
## Features

- **Ingredient Inventory** — Add, update, and remove ingredients with quantities
- **Recipe Matching** — See which recipes you can cook with what you have, with exact match percentages and missing ingredient lists
- **Diet Filtering** — Filter recipes by 10 diet types
- **Recipe Details** — View nutritional information, ingredient status, and step-by-step cooking instructions
- **Cook a Recipe** — Automatically deduct used ingredients from your inventory if the user chooses, then view the cooking steps of the recipe
- **AI Chef** — Chat with an AI assistant that knows your inventory and suggests personalized recipes
- **Meal Planner** — Plan meals for 1 to 31 days across breakfast, lunch, dinner, and snack slots
- **AI Meal Plan Generation** — Generate a full meal plan automatically based on a text prompt
- **Progress Tracking** — Mark meals as completed and track your daily progress

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 3.4.5 |
| Frontend | React 18, Vite |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security + JWT |
| AI Integration | LangChain4j |
| Build | Maven (backend), npm (frontend) |

## Arhitecture

<img width="1871" height="1072" alt="image" src="https://github.com/user-attachments/assets/6076caa2-68f5-49fa-97ad-a3d56c7dc86a" />


The application follows a client-server architecture with a clear separation between frontend and backend.

The **client side** is built with React and consists of three layers: the visual layer (pages and components), a state management layer (AuthContext) that keeps the user session alive across the app, and an API client module that handles all HTTP communication using `fetch`.

The **server side** is a Spring Boot application organized into three layers: a presentation layer with REST controllers that expose all endpoints under `/api/`, an application layer containing the business logic services and the LangChain4j AI integration module, and a data access layer with Spring Data JPA repositories and Hibernate-mapped entities.

All communication between client and server goes through a JSON REST API secured with JWT tokens.

## Prerequisites

- Java 21
- Maven (or use the included `mvnw` wrapper)
- Node.js 20+
- MySQL 8
- (Optional) [Ollama](https://ollama.com) for local AI — run `ollama pull llama3` after installing
- (Optional) A Google Gemini API key for cloud AI

## Getting Started

### 1. Database Setup

Create the database and run the schema + seed script:

```sql
CREATE DATABASE recipe_discovery_db;
```

Then run `src/main/resources/sql/schema.sql` against the newly created database. This will create all tables and populate them with initial data.

### 2. Backend Configuration

Open `src/main/resources/application.properties` and update the following:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/recipe_discovery_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

# AI Provider — choose one: gemini, ollama, mock
ai.provider=mock

# If using Gemini
langchain4j.gemini.api-key=your_gemini_api_key

# If using Ollama (must be running locally on port 11434)
langchain4j.ollama.base-url=http://localhost:11434
langchain4j.ollama.model-name=llama3
```

Use `ai.provider=mock` if you want to run the app without any AI setup.

### 3. Run the Backend

```bash
# On Linux / macOS
./mvnw spring-boot:run

# On Windows
.\mvnw.cmd spring-boot:run
```

The backend starts on `http://localhost:8081`.

### 4. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.

### 5. Test Users

The database comes pre-populated with test accounts:

| Username | Password |
|----------|----------|
| Andrei | passAndrei1 |
| Elena | passElena2 |
| Cristian | passCristian3 |
| Ioana | passIoana4 |
