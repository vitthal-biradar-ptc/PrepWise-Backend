# PrepWise Backend - Spring Boot Application

A comprehensive Spring Boot backend application for user authentication, profile management, resume analysis, and learning path generation using Google's Gemini AI.

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Spring Boot Concepts Used](#spring-boot-concepts-used)
4. [File Structure and Explanations](#file-structure-and-explanations)
5. [Data Flow and Transfer](#data-flow-and-transfer)
6. [Database Design](#database-design)
7. [API Endpoints](#api-endpoints)
8. [Setup and Configuration](#setup-and-configuration)
9. [Key Features](#key-features)

## Project Overview

PrepWise is a career development platform that helps users:
- Create and manage professional profiles
- Analyze resumes using AI
- Generate personalized learning paths
- Track skills, certifications, and achievements

## Architecture Overview

The application follows the **MVC (Model-View-Controller)** pattern with additional layers:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │    │    Services     │    │   Repositories  │
│  (REST APIs)    │────│  (Business      │────│  (Data Access)  │
│                 │    │   Logic)        │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      DTOs       │    │   Entities      │    │    Database     │
│ (Data Transfer) │    │ (Domain Models) │    │   (PostgreSQL)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Spring Boot Concepts Used

### 1. **Dependency Injection (DI)**
```java
@RequiredArgsConstructor  // Lombok annotation for constructor injection
public class UserService {
    private final UserRepository userRepository;  // Automatically injected
    private final PasswordEncoder passwordEncoder;
}
```

**Why:** Makes code loosely coupled, testable, and maintainable. Spring manages object creation and dependencies.

### 2. **Annotations Explained**

#### **@SpringBootApplication**
```java
@SpringBootApplication
public class PrepWiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrepWiseApplication.class, args);
    }
}
```
- **What:** Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`
- **Why:** Single annotation to bootstrap Spring Boot application

#### **@RestController**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // REST endpoints
}
```
- **What:** Combines `@Controller` + `@ResponseBody`
- **Why:** All methods return JSON responses automatically

#### **@Service**
```java
@Service
public class UserService {
    // Business logic
}
```
- **What:** Marks class as a service layer component
- **Why:** Spring manages lifecycle, enables dependency injection

#### **@Repository**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Data access methods
}
```
- **What:** Marks class as data access component
- **Why:** Provides database operations, exception translation

#### **@Entity & @Table**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```
- **What:** JPA annotations for database mapping
- **Why:** Maps Java objects to database tables

## File Structure and Explanations

### 📁 **Config Package**

#### **GeminiConfig.java**
```java
@Configuration
public class GeminiConfig {
    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }
}
```
**Purpose:** Configuration for external AI service integration
- **@Configuration:** Tells Spring this class contains bean definitions
- **@Bean:** Methods annotated create Spring-managed objects
- **WebClient:** Reactive HTTP client for API calls

#### **SecurityConfig.java**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        // Security rules
    }
}
```
**Purpose:** Security configuration and CORS setup
- **CORS:** Cross-Origin Resource Sharing for frontend-backend communication
- **JWT:** Stateless authentication using JSON Web Tokens

#### **JwtUtil.java**
```java
@Component
public class JwtUtil {
    public String generateToken(String username) {
        // JWT creation logic
    }
    
    public boolean validateToken(String token) {
        // JWT validation logic
    }
}
```
**Purpose:** JWT token management
- **@Component:** Generic Spring-managed component
- **JWT Benefits:** Stateless, secure, scalable authentication

### 📁 **Entities Package** (Domain Models)

#### **User.java**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Skill> skills = new ArrayList<>();
}
```
**JPA Annotations Explained:**
- **@Id:** Primary key
- **@GeneratedValue:** Auto-increment ID
- **@OneToMany:** One user has many skills
- **cascade = CascadeType.ALL:** Operations on User affect related entities
- **mappedBy = "user":** Foreign key in Skill entity

#### **Relationships Overview:**
```
User (1) ──── (Many) Skills
User (1) ──── (Many) Certifications  
User (1) ──── (Many) Achievements
User (1) ──── (Many) LearningPaths
LearningPath (1) ──── (Many) LearningPeriods
LearningPeriod (1) ──── (Many) LearningResources
LearningPeriod (1) ──── (Many) LearningTasks
```

### 📁 **DTOs Package** (Data Transfer Objects)

#### **Purpose of DTOs:**
```java
public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    // Validation annotations
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;
}
```
**Why DTOs:**
- **Security:** Don't expose internal entity structure
- **Validation:** Input validation at API layer
- **Flexibility:** Different views of same data for different endpoints

### 📁 **Services Package** (Business Logic)

#### **UserService.java**
```java
@Service
public class UserService {
    @Transactional
    public Map<String, Object> updateUserProfile(String token, UpdateProfileRequest request) {
        // Business logic here
    }
}
```
**Key Concepts:**
- **@Transactional:** Database operations are atomic (all or nothing)
- **Business Logic:** Validation, transformation, orchestration
- **Repository Pattern:** Service uses repository for data access

#### **GeminiService.java**
```java
@Service
public class GeminiService {
    public String askGemini(String prompt) {
        // AI API integration
        Map<String, Object> requestBody = createGeminiRequest(prompt);
        
        String response = geminiWebClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
```
**Purpose:** External AI service integration
- **WebClient:** Reactive HTTP client
- **Mono/Flux:** Reactive programming concepts (though using .block() here makes it synchronous)

### 📁 **Controllers Package** (REST API Layer)

#### **AuthController.java**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@Valid @RequestBody SignUpRequest request) {
        // Handle registration
    }
}
```
**Annotations Explained:**
- **@PostMapping:** HTTP POST endpoint
- **@RequestBody:** Maps JSON request to Java object
- **@Valid:** Triggers validation on DTO
- **ResponseEntity:** Wrapper for HTTP response with status codes

## Data Flow and Transfer

### 1. **Registration Flow**
```
Frontend Request (JSON)
    ↓
@RequestBody → SignUpRequest DTO
    ↓
Controller validation (@Valid)
    ↓
AuthController.register()
    ↓
UserService.registerUser()
    ↓
Password encoding + User entity creation
    ↓
UserRepository.save() → Database
    ↓
JWT token generation
    ↓
AuthResponse DTO → JSON Response
```

### 2. **Profile Update Flow**
```
Frontend (JSON + JWT Token)
    ↓
@RequestHeader Authorization + @RequestBody UpdateProfileRequest
    ↓
JWT token validation
    ↓
UserService.updateUserProfile()
    ↓
Entity relationship management (Skills, Certifications, etc.)
    ↓
@Transactional database update
    ↓
Success response
```

### 3. **Resume Analysis Flow**
```
PDF File Upload
    ↓
PdfService.extractTextFromPdf()
    ↓
GeminiService.analyzeResume()
    ↓
External AI API call
    ↓
JSON parsing to ResumeAnalysisResponse
    ↓
UserProfileService.updateUserProfileFromResume()
    ↓
Smart merging with existing data
    ↓
Database update
```

## Database Design

### **Core Tables:**
```sql
users
├── id (Primary Key)
├── username (Unique)
├── email (Unique)
├── password (Encrypted)
├── name
├── location
├── domain_badge
└── domain_distribution (JSON)

skills
├── id (Primary Key)
├── user_id (Foreign Key → users.id)
├── name
└── proficiency

certifications
├── id (Primary Key)
├── user_id (Foreign Key → users.id)
├── name
├── issuer
└── date

learning_paths
├── id (Primary Key)
├── user_id (Foreign Key → users.id)
├── skill
├── level
├── duration
└── created_at
```

### **Relationship Management in Code:**
```java
// In User entity
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Skill> skills = new ArrayList<>();

// When updating skills
user.getSkills().clear();  // Remove existing
if (request.getSkills() != null) {
    request.getSkills().forEach(skillDto -> {
        Skill skill = new Skill(skillDto.getName(), skillDto.getProficiency());
        skill.setUser(user);  // Set relationship
        user.getSkills().add(skill);
    });
}
```

## API Endpoints

### **Authentication**
- `POST /api/auth/sign-up` - User registration
- `POST /api/auth/sign-in` - User login
- `GET /api/auth/validate` - Token validation

### **Profile Management**
- `GET /api/get-user` - Get user profile
- `PUT /api/update-profile` - Update user profile

### **Resume Services**
- `POST /api/analyze-resume` - Analyze uploaded PDF
- `POST /api/analyze-text` - Analyze text input
- `POST /api/parse-resume` - Parse and update profile

### **Learning Paths**
- `POST /api/learning-path/generate` - Generate learning path
- `GET /api/learning-path/user/{userId}` - Get user's paths
- `DELETE /api/learning-path/delete/{userId}/{pathId}` - Delete path

## Setup and Configuration

### **Application Properties**
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/prepwise
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Gemini AI Configuration
gemini.api.key=your-gemini-api-key
```

### **Dependencies (Key ones)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
</dependency>
```

## Key Features

### 1. **Security Features**
- JWT-based authentication
- Password encryption with BCrypt
- CORS configuration for frontend integration
- Input validation and sanitization

### 2. **AI Integration**
- Google Gemini AI for resume analysis
- Intelligent profile data extraction
- Learning path generation based on skills

### 3. **Data Management**
- Smart merging of resume data with existing profiles
- Cascade operations for related entities
- Transactional integrity

### 4. **API Design**
- RESTful endpoints
- Consistent error handling
- JSON request/response format
- Proper HTTP status codes

## Common Spring Boot Patterns Used

### 1. **Constructor Injection**
```java
@RequiredArgsConstructor  // Lombok
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

### 2. **Exception Handling**
```java
try {
    // Business logic
} catch (RuntimeException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
}
```

### 3. **Builder Pattern (with WebClient)**
```java
WebClient.builder()
    .baseUrl("https://api.example.com")
    .build();
```

### 4. **Repository Pattern**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
}
```

This architecture provides a robust, scalable, and maintainable backend solution for the PrepWise platform, leveraging Spring Boot's powerful features and best practices.
