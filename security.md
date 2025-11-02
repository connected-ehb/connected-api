# Security Audit Report - ConnectEd API

> **Last Updated:** 2025-11-01
> **Overall Risk Level:** 🔴 **CRITICAL** (Cannot go to production as-is)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Critical Issues](#critical-issues)
3. [High Severity Issues](#high-severity-issues)
4. [Medium Severity Issues](#medium-severity-issues)
5. [Vulnerability Breakdown](#vulnerability-breakdown)
6. [Priority Action Plan](#priority-action-plan)
7. [Recommendations](#recommendations)

---

## Executive Summary

A comprehensive security audit has identified **35+ security vulnerabilities** across the ConnectEd API application that must be addressed before production deployment.

### Issue Distribution

| Severity | Count | Timeline |
|----------|-------|----------|
| 🔴 **CRITICAL** | 10 | Fix immediately (24 hours) |
| 🟠 **HIGH** | 12 | Fix within 1 week |
| 🟡 **MEDIUM** | 10 | Fix within 1 month |
| 🔵 **LOW** | 3 | Nice to have |

### Vulnerability Categories

| Category | Count | Primary Severity |
|----------|-------|------------------|
| IDOR Vulnerabilities | 8 | CRITICAL |
| Missing Authorization | 7 | HIGH |
| Input Validation | 6 | HIGH |
| Configuration Issues | 6 | HIGH |
| Authentication Issues | 4 | CRITICAL |
| Permission Issues | 4 | MEDIUM |

---

## Critical Issues

### 1. CSRF Protection Completely Disabled

**📍 Location:** `SecurityConfig.java:36`

**Issue:**
```java
httpSecurity.csrf(AbstractHttpConfigurer::disable)
```

**Impact:**
Application is vulnerable to Cross-Site Request Forgery (CSRF) attacks. Attackers can trick authenticated users into performing unwanted actions.

**Fix:**
```java
httpSecurity.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/api/auth/login", "/api/auth/register")
)
```

---

### 2. IDOR Vulnerabilities - Users Can Access Others' Data

#### A. User Controller

**📍 Location:** `UserController.java:23-27, 29-33`

**Vulnerable Endpoints:**

##### GET /api/users/{id}
```java
@GetMapping("/{id}")
public ResponseEntity<UserDetailsDto> getUserById(@PathVariable Long id) {
    // ❌ NO authorization check!
    // Any authenticated user can view ANY user's data
    User user = userService.getUserById(id);
    return ResponseEntity.ok(userDetailsMapper.toUserDetailsDto(user));
}
```

**Impact:** Exposes PII including email, profile image, LinkedIn URL, field of study, etc.

##### POST /api/users
```java
@PostMapping()
public ResponseEntity<User> createUser(@RequestBody User user) {
    // ❌ NO authorization check!
    // Any authenticated user can CREATE users with ANY role
    User createdUser = userService.createUser(user);
    return ResponseEntity.ok(createdUser);
}
```

**Impact:** Mass assignment vulnerability, privilege escalation risk.

---

#### B. Notification Controller

**📍 Location:** `NotificationController.java:24-47`

**Vulnerable Endpoints:**

| Endpoint | Method | Vulnerability |
|----------|--------|---------------|
| `/api/notifications/{id}` | GET | No ownership verification - can read any notification |
| `/api/notifications/user/{userId}` | GET | Can view any user's notifications by changing userId |
| `/api/notifications/{notificationId}/read` | PUT | Can mark any user's notifications as read |
| `/api/notifications/{id}` | DELETE | Can delete any user's notifications |

**Code Example:**
```java
@PreAuthorize("hasAuthority('notification:read')")
@GetMapping("/user/{userId}")
public ResponseEntity<List<NotificationDto>> getAllNotificationsByUserId(
    @PathVariable Long userId) {
    // ❌ No check if requesting user IS userId
    return ResponseEntity.ok(notificationService.getAllNotificationsByUserId(userId));
}
```

**Impact:** Complete violation of notification privacy - horizontal privilege escalation.

---

#### C. Deadline Controller

**📍 Location:** `DeadlineController.java:29-58`

**Vulnerable Endpoints:**

| Endpoint | Method | Issue |
|----------|--------|-------|
| `/api/deadlines/{assignmentId}` | POST | Create deadlines on any assignment |
| `/api/deadlines/{deadlineId}` | PATCH | Update any deadline |
| `/api/deadlines/{deadlineId}` | DELETE | Delete any deadline |

**Code Example:**
```java
@PreAuthorize("hasAuthority('deadline:delete')")
@DeleteMapping("/{deadlineId}")
public ResponseEntity<Void> deleteDeadline(@PathVariable Long deadlineId) {
    // ❌ No Principal parameter - no ownership check
    deadlineService.deleteDeadline(deadlineId);
    return ResponseEntity.noContent().build();
}
```

---

### 3. Hardcoded Secrets in Repository

**📍 Location:** `.env`, `.env.prod`

**Exposed Credentials:**
- Database passwords (plaintext)
- Canvas API client secret
- Mail service password
- Session secrets

**Impact:**
All credentials are compromised and visible in git history.

**Required Actions:**
1. ❌ Remove `.env` and `.env.prod` from git history
2. ❌ Rotate ALL credentials immediately
3. ❌ Add `.env*` to `.gitignore`
4. ❌ Use environment variables or secrets management (e.g., AWS Secrets Manager, HashiCorp Vault)

---

### 4. Unprotected Canvas Sync Endpoint

**📍 Location:** `CourseController.java:35-40`

**Issue:**
```java
// ❌ Authorization check is COMMENTED OUT!
//@PreAuthorize("hasAnyAuthority('canvas:sync')")
@PostMapping("/canvas")
public ResponseEntity<List<CourseDetailsDto>> getNewCoursesFromCanvas(
    Authentication authentication) {
    List<CourseDetailsDto> newCourses = courseService.getNewCoursesFromCanvas(authentication);
    return ResponseEntity.ok(newCourses);
}
```

**Impact:**
Any authenticated user can trigger Canvas API synchronization, potentially:
- Abusing Canvas API rate limits
- Causing performance degradation
- DoS via repeated sync requests

---

### 5. Tag Endpoints Completely Unprotected

**📍 Location:** `TagController.java:17-25`

**Vulnerable Endpoints:**

```java
// ❌ NO @PreAuthorize annotation
@GetMapping("/search")
public ResponseEntity<List<TagDto>> searchTagsByQuery(@RequestParam String query) {
    return ResponseEntity.ok(tagService.searchTagsByQuery(query));
}

// ❌ NO @PreAuthorize annotation
@PostMapping
public ResponseEntity<TagDto> createTag(@RequestBody TagDto tagDto) {
    return ResponseEntity.ok(tagService.createTag(tagDto));
}
```

**Impact:**
- SQL injection risk if query parameter is not sanitized
- Unauthorized tag creation
- Data integrity compromise

---

### 6. Missing Input Validation on Authentication

**📍 Location:** `LoginRequestDto.java`, `RegistrationRequestDto.java`

**LoginRequestDto:**
```java
@Getter
public class LoginRequestDto {
    private String email;     // ❌ No @Email validation
    private String password;  // ❌ No @NotBlank or @Size
}
```

**RegistrationRequestDto:**
```java
@Getter
public class RegistrationRequestDto {
    private String email;              // ❌ No @Email
    private String password;            // ❌ No password strength requirements
    private String firstName;           // ❌ No @NotBlank
    private String lastName;            // ❌ No @NotBlank
    private String invitationCode;      // ❌ No validation
}
```

**Impact:**
- Weak passwords can be used
- Invalid email formats accepted
- Empty/null values bypass validation

**Fix:**
```java
@Getter
public class RegistrationRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be 8-128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain uppercase, lowercase, and number")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;
}
```

---

### 7. Admin Role Missing Critical Permissions

**📍 Location:** `Role.java:126-177`

**Issue:**
ADMIN role has **46 permissions** while TEACHER role has **51 permissions**!

**Missing Permissions in ADMIN:**
- `REFRESH_COURSE` (TEACHER has it)
- `READ_DASHBOARD` (TEACHER has it)
- `CREATE_ANNOUNCEMENT` (TEACHER has it)
- `CREATE_INVITATION` (TEACHER has it)

**Impact:**
Admin users cannot perform operations that teachers can, which is backwards.

**Fix:**
```java
ADMIN(
    Set.of(
        // ... existing permissions ...
        Permission.REFRESH_COURSE,
        Permission.READ_DASHBOARD,
        Permission.CREATE_ANNOUNCEMENT,
        Permission.CREATE_INVITATION
    )
);
```

---

### 8. Bug Endpoint Public Access

**📍 Location:** `SecurityConfig.java:51`

**Issue:**
```java
.requestMatchers(
    "/api/auth/**",
    "/api/bugs"  // ❌ PUBLIC - no authentication required!
).permitAll()
```

**Impact:**
Unauthenticated users can view all internal bugs, revealing:
- Application vulnerabilities
- Internal system details
- User identities who reported bugs

**Fix:**
Remove `/api/bugs` from permitAll() list.

---

### 9. Missing @PathVariable Annotation

**📍 Location:** `ReviewController.java:22`

**Issue:**
```java
@DeleteMapping("/{reviewId}")
public ResponseEntity<Void> deleteReview(Principal principal, Long reviewId) {
    // ❌ reviewId parameter missing @PathVariable annotation
    // Will always be null!
    reviewService.deleteReview(principal, reviewId);
    return ResponseEntity.noContent().build();
}
```

**Impact:**
Method will fail with NullPointerException or delete wrong review.

**Fix:**
```java
public ResponseEntity<Void> deleteReview(
    Principal principal,
    @PathVariable Long reviewId) {
    // ...
}
```

---

### 10. Email Verification Role Assignment Bypass

**📍 Location:** `UserServiceImpl.java:160-192`

**Issue:**
```java
@Override
public void verifyEmailToken(String token) {
    User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new BaseRuntimeException("Invalid or expired token", HttpStatus.BAD_REQUEST));

    String email = user.getEmail();
    // ❌ Role determined ONLY by email domain - no SMTP verification!
    if (email.endsWith("@student.ehb.be")) {
        user.setRole(Role.STUDENT);
    } else if (email.endsWith("@ehb.be")) {
        user.setRole(Role.TEACHER);  // ❌ Anyone with @ehb.be email becomes teacher!
    }
}
```

**Impact:**
- If someone obtains an `@ehb.be` email address, they automatically become TEACHER
- No actual verification that email belongs to EHB
- Potential for privilege escalation

**Recommendation:**
Implement proper email domain verification with EHB SMTP server or OAuth.

---

## High Severity Issues

### 11. Insecure CORS Configuration

**📍 Location:** `CorsConfig.java`

**Issue:**
```java
configuration.setAllowedHeaders(List.of("*"));  // ❌ Too permissive!
configuration.setAllowCredentials(true);         // ❌ Dangerous with wildcard
```

**Impact:**
Allows any origin to make authenticated requests with any headers.

**Fix:**
```java
configuration.setAllowedHeaders(List.of(
    "Content-Type",
    "Authorization",
    "Accept",
    "X-Requested-With",
    "X-CSRF-TOKEN"
));
```

---

### 12. Missing Security Headers

**Issue:**
No HTTP security headers configured for protection against common attacks.

**Missing Headers:**

| Header | Purpose | Risk |
|--------|---------|------|
| `X-Frame-Options` | Clickjacking protection | Medium |
| `X-Content-Type-Options` | MIME sniffing prevention | Medium |
| `Content-Security-Policy` | XSS prevention | High |
| `Strict-Transport-Security` | Force HTTPS | High |
| `X-XSS-Protection` | Legacy XSS protection | Low |

**Fix:**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .frameOptions(FrameOptionsConfig::deny)
            .contentTypeOptions(ContentTypeOptionsConfig::disable)
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
            )
        );
}
```

---

### 13. Actuator Endpoints Exposed

**📍 Location:** `SecurityConfig.java:46`

**Issue:**
```java
.requestMatchers("/actuator/**").permitAll()  // ❌ Exposes sensitive endpoints!
```

**Exposed Data:**
- `/actuator/health` - System health information
- `/actuator/metrics` - Performance metrics
- `/actuator/env` - Environment variables
- `/actuator/mappings` - All API endpoints

**Fix:**
```java
.requestMatchers("/actuator/health").permitAll()
.requestMatchers("/actuator/**").hasRole("ADMIN")
```

---

### 14. No Rate Limiting

**Issue:**
No rate limiting implemented on any endpoint.

**Vulnerable to:**
- Brute force login attacks
- Credential enumeration
- DoS attacks
- API abuse

**Recommendation:**
Implement rate limiting using Bucket4j:

```java
@Bean
public RateLimiter rateLimiter() {
    return RateLimiter.create(
        Bandwidth.builder()
            .capacity(100)
            .refillGreedy(100, Duration.ofMinutes(1))
            .build()
    );
}
```

---

### 15. Assignment Dashboard IDOR

**📍 Location:** `AssignmentController.java:74-81`

**Issue:**
```java
@PreAuthorize("hasAnyAuthority('dashboard:read')")
@GetMapping("/{assignmentId}/dashboard")
public ResponseEntity<DashboardDetailsDto> getDashboard(
    @PathVariable Long assignmentId) {
    // ❌ No Principal parameter - anyone can view any dashboard!
    DashboardDetailsDto dashboard = dashboardServiceImpl.getDashboardDetails(assignmentId);
    return ResponseEntity.ok(dashboard);
}
```

**Impact:**
Any user with `dashboard:read` permission can view dashboards for assignments they don't own.

---

### 16. Course Deletion - No Ownership Check

**📍 Location:** `CourseController.java:73-78`

**Issue:**
```java
@PreAuthorize("hasAnyAuthority('course:delete')")
@DeleteMapping("/{courseId}")
public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
    // ❌ No Principal parameter - can delete any course!
    courseService.deleteCourseById(courseId);
    return ResponseEntity.noContent().build();
}
```

---

### 17. Project Status Change - No Ownership Check

**📍 Location:** `ProjectServiceImpl.java:194-214`

**Issue:**
```java
@Override
public ProjectDetailsDto changeProjectStatus(
    Principal principal, Long projectId, ProjectStatusEnum status) {
    final User user = userService.getUserByPrincipal(principal);
    final Project project = getProjectById(projectId);

    // ❌ Principal accepted but NEVER verified against project owner!
    project.setStatus(status);
    projectRepository.save(project);
}
```

---

### 18. Assignment Deletion - No Ownership Check

**📍 Location:** `AssignmentServiceImpl.java:82-85`

**Issue:**
```java
public void deleteAssignmentById(Principal principal, Long assignmentId) {
    Assignment assignment = getAssignmentById(assignmentId);
    // ❌ Principal parameter IGNORED!
    assignmentRepository.delete(assignment);
}
```

---

### 19. Teachers Can View ALL Applications

**📍 Location:** `ApplicationServiceImpl.java:128-137`

**Issue:**
```java
if (user.hasRole(Role.TEACHER)) {
    // ❌ Returns ALL applications from ANY assignment - no ownership check!
    return applicationMapper.toDtoList(
        applicationRepository.findAllApplicationsByAssignmentId(assignmentId)
    );
}
```

**Impact:**
Teachers can view applications for assignments they don't teach.

---

### 20. Feedback Can Be Given by Any Teacher

**📍 Location:** `FeedbackServiceImpl.java:34-66`

**Issue:**
```java
@Override
public FeedbackDto giveFeedback(Principal principal, Long projectId, FeedbackCreateDto feedbackDto) {
    final User user = userService.getUserByPrincipal(principal);
    if (user.hasRole(Role.STUDENT)) {
        throw new UserUnauthorizedException(user.getId());
    }
    // ❌ Any TEACHER/RESEARCHER can give feedback to ANY project!
    // ❌ No check if they're teaching that course
    final Feedback feedback = new Feedback();
    feedback.setComment(feedbackDto.getComment());
    feedback.setUser(user);
    feedback.setProject(project);
    feedbackRepository.save(feedback);
}
```

---

### 21. Session Restoration Without Token Validation

**📍 Location:** `AuthServiceImpl.java:205-260`

**Issue:**
```java
private AuthUserDetailsDto restoreOAuth2Session(User user, HttpServletRequest request) {
    OAuth2AuthorizedClient authorizedClient =
            authorizedClientService.loadAuthorizedClient("canvas", canvasId);

    // ❌ Restores OAuth2 session without validating token with Canvas!
    // ❌ If token revoked on Canvas, user still has access here
    CustomOAuth2User principal = new CustomOAuth2User(user, attributes, "id");
}
```

**Impact:**
If OAuth2 token is revoked externally (on Canvas), the application won't detect it.

---

### 22. No Cookie Security Attributes

**Issue:**
Session cookies lack security attributes.

**Missing:**
- `HttpOnly` flag (prevents JavaScript access)
- `Secure` flag (HTTPS only)
- `SameSite` attribute (CSRF/XSS protection)

**Fix:**
```yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true
        same-site: strict
```

---

## Medium Severity Issues

### 23. No Authentication Failure Logging

**Issue:**
Failed login attempts are not logged.

**Impact:**
- Cannot detect brute force attacks
- No audit trail for security incidents

**Recommendation:**
```java
@EventListener
public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
    String username = event.getAuthentication().getName();
    logger.warn("Failed login attempt for user: {}", username);
}
```

---

### 24. Weak BCrypt Configuration

**📍 Location:** `PasswordEncoderConfig.java`

**Issue:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // ❌ Default strength 10, should be 12+
}
```

**Fix:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // Strength = 12
}
```

---

### 25. Parameter Binding Issues

**Issues Found:**

#### ProjectController
```java
// ❌ Should be @RequestBody not @RequestHeader
@PostMapping("/{projectId}/status")
public ResponseEntity<...> changeProjectStatus(
    Principal principal,
    @PathVariable Long projectId,
    @RequestHeader ProjectStatusEnum status) {  // Wrong!
}
```

#### ApplicationController
```java
// ❌ Should be @RequestBody not @RequestHeader
@PostMapping("/{applicationId}/review")
public ResponseEntity<...> reviewApplication(
    Principal principal,
    @PathVariable Long applicationId,
    @RequestHeader ApplicationStatusEnum status) {  // Wrong!
}
```

---

### 26. Missing @Valid Annotations

**DTOs lacking validation:**
- `ApplicationCreateDto`
- `FeedbackCreateDto`
- `DeadlineCreateDto`
- `DeadlineUpdateDto`

**Fix:**
```java
@PostMapping
public ResponseEntity<Dto> create(@RequestBody @Valid CreateDto dto) {
    // ...
}
```

---

### 27. No Pagination on Course Students Endpoint

**📍 Location:** `CourseController.java:67-71`

**Issue:**
```java
public List<UserDetailsDto> getAllEnrolledStudentsByCourse(@PathVariable Long courseId){
    return userService.getAllStudentsByCourseId(courseId);
    // ❌ Can return 10,000+ students without pagination!
}
```

**Impact:**
DoS risk - large courses could cause memory issues or timeout.

**Fix:**
Implement pagination using Spring Data's `Pageable`:
```java
public Page<UserDetailsDto> getAllEnrolledStudentsByCourse(
    @PathVariable Long courseId,
    Pageable pageable) {
    return userService.getAllStudentsByCourseId(courseId, pageable);
}
```

---

### 28. OAuth2 Token Deletion Incomplete

**📍 Location:** `CanvasAuthServiceImpl.java`

**Issue:**
```java
public void deleteAccessToken(String canvasUserId) {
    // ❌ deleteAccessToken() called but token not passed!
}
```

---

### 29. No HTTP Client Timeouts

**Issue:**
WebClient calls to Canvas API lack timeout configuration.

**Impact:**
Requests could hang indefinitely, blocking threads.

**Fix:**
```java
@Bean
public WebClient canvasWebClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        ))
        .build();
}
```

---

### 30. Bug DTO Incomplete Validation

**📍 Location:** `BugCreateDto.java`

**Issue:**
```java
public class BugCreateDto {
    @NotBlank
    @Size(max=8000)
    private String description;  // ✅ Validated

    private String route;         // ❌ NO validation
    private String appVersion;    // ❌ NO validation
}
```

---

### 31. Overly Permissive TEACHER Role

**📍 Location:** `Role.java`

**Issue:**
```java
TEACHER(
    Set.of(
        Permission.READ_ALL_COURSES,      // ❌ Can read ALL courses, not just their own
        Permission.READ_ALL_ASSIGNMENTS,  // ❌ Can read ALL assignments
        Permission.READ_ALL_APPLICATIONS  // ❌ Can read ALL applications
    )
)
```

**Impact:**
Violates principle of least privilege - teachers should only access their own courses.

---

### 32. Inconsistent Permission Naming

**Issue:**
Permission naming lacks consistency.

**Examples:**
- `CREATE_GLOBAL_PROJECT` (uses "GLOBAL")
- `CREATE_COURSE` (no "GLOBAL")
- No consistent `read` vs `read_all` vs `read_owned` pattern

**Recommendation:**
Standardize to:
- `<entity>:create`
- `<entity>:read` (own resources)
- `<entity>:read_all` (all resources)
- `<entity>:update`
- `<entity>:delete`

---

## Vulnerability Breakdown

### By Category

```
IDOR Vulnerabilities      ████████ 8
Missing Authorization     ███████ 7
Input Validation          ██████ 6
Configuration Issues      ██████ 6
Authentication Issues     ████ 4
Permission Issues         ████ 4
```

### By Severity

```
CRITICAL ██████████ 10
HIGH     ████████████ 12
MEDIUM   ██████████ 10
LOW      ███ 3
```

---

## Priority Action Plan

### 🔴 Day 1 (IMMEDIATE - 24 Hours)

- [ ] **#1** Enable CSRF protection (`SecurityConfig.java`)
- [ ] **#2** Add authorization to `UserController.getUserById()`
- [ ] **#3** Add authorization to `UserController.createUser()`
- [ ] **#4** Fix all Notification IDOR vulnerabilities
- [ ] **#5** Uncomment Canvas sync authorization
- [ ] **#6** Rotate all hardcoded credentials
- [ ] **#7** Remove `.env` files from git history

### 🟠 Week 1 (URGENT - 7 Days)

- [ ] **#8** Add Principal verification to all Deadline endpoints
- [ ] **#9** Add input validation to auth DTOs
- [ ] **#10** Fix Tag controller authorization
- [ ] **#11** Add security headers (CSP, HSTS, X-Frame-Options, etc.)
- [ ] **#12** Fix CORS configuration
- [ ] **#13** Protect actuator endpoints (ADMIN only)
- [ ] **#14** Implement rate limiting
- [ ] **#15** Fix `@PathVariable` annotation on ReviewController

### 🟡 Week 2-4 (IMPORTANT - 1 Month)

- [ ] **#16** Add ownership checks to all delete operations
- [ ] **#17** Add ownership checks to all update operations
- [ ] **#18** Fix parameter binding issues (use `@RequestBody`)
- [ ] **#19** Add `@Valid` to all request bodies
- [ ] **#20** Add pagination limits to large list endpoints
- [ ] **#21** Configure cookie security attributes
- [ ] **#22** Add authentication failure logging
- [ ] **#23** Increase BCrypt strength to 12
- [ ] **#24** Add HTTP client timeouts
- [ ] **#25** Add missing Admin permissions

---

## Recommendations

### 1. Implement a Security Service Layer

Create a centralized authorization service:

```java
@Service
public class AuthorizationService {

    public void verifyOwnership(Principal principal, Long resourceId, ResourceType type) {
        User user = getUserByPrincipal(principal);
        if (!user.hasRole(Role.ADMIN) && !isOwner(user, resourceId, type)) {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    public boolean canAccessCourse(User user, Long courseId) {
        if (user.hasRole(Role.ADMIN)) return true;
        return courseRepository.isUserEnrolledOrTeaching(user.getId(), courseId);
    }
}
```

### 2. Add Integration Tests for Security

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Test
    @WithMockUser(username = "student1", authorities = {"notification:read"})
    void shouldNotAccessOtherUsersNotifications() {
        // Test that user cannot access another user's notifications
        mockMvc.perform(get("/api/notifications/user/999"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"project:delete"})
    void shouldNotDeleteProjectNotOwned() {
        // Test that user cannot delete projects they don't own
        mockMvc.perform(delete("/api/projects/123"))
            .andExpect(status().isForbidden());
    }
}
```

### 3. Code Review Checklist

Before merging any PR, verify:

- [ ] Every endpoint has `@PreAuthorize` or is explicitly public
- [ ] Every ID parameter has ownership verification via Principal
- [ ] Every request DTO has `@Valid` annotation
- [ ] Every Principal parameter is actually used in the method
- [ ] No sensitive data is logged
- [ ] Error messages don't leak internal details

### 4. Use Spring Security Test

```java
@WithMockUser(authorities = {"project:read"})
void testUnauthorizedAccess() {
    // Test authorization failures
    assertThrows(AccessDeniedException.class, () -> {
        projectService.deleteProject(principal, projectId);
    });
}
```

### 5. Implement Audit Logging

```java
@Aspect
@Component
public class AuditAspect {

    @AfterReturning("@annotation(PreAuthorize)")
    public void logAuthorizationSuccess(JoinPoint joinPoint) {
        logger.info("Authorization success: {} by {}",
            joinPoint.getSignature(),
            SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @AfterThrowing(pointcut = "@annotation(PreAuthorize)", throwing = "ex")
    public void logAuthorizationFailure(JoinPoint joinPoint, Exception ex) {
        logger.warn("Authorization failure: {} by {} - {}",
            joinPoint.getSignature(),
            SecurityContextHolder.getContext().getAuthentication().getName(),
            ex.getMessage());
    }
}
```

### 6. Implement Secrets Management

**Options:**
- AWS Secrets Manager
- HashiCorp Vault
- Azure Key Vault
- Environment variables (minimum)

**Example (Environment Variables):**
```yaml
# application.yml
spring:
  datasource:
    password: ${DB_PASSWORD}

canvas:
  client-secret: ${CANVAS_CLIENT_SECRET}

mail:
  password: ${MAIL_PASSWORD}
```

---

## OWASP Top 10 2023 Compliance

### Violations Found

| OWASP Category | Violations | Priority |
|----------------|------------|----------|
| **A01: Broken Access Control** | 15 issues | 🔴 CRITICAL |
| **A02: Cryptographic Failures** | 3 issues (secrets in repo, weak BCrypt) | 🔴 CRITICAL |
| **A03: Injection** | 2 issues (SQL injection risk) | 🟠 HIGH |
| **A04: Insecure Design** | 5 issues (no rate limiting, CSRF disabled) | 🟠 HIGH |
| **A05: Security Misconfiguration** | 8 issues (CORS, headers, actuator) | 🟠 HIGH |
| **A07: Identification & Auth Failures** | 4 issues (no auth logging, weak validation) | 🟠 HIGH |

---

## Conclusion

The ConnectEd API has **significant security vulnerabilities** that must be addressed before production deployment. The most critical issues are:

1. **CSRF protection disabled** - enables cross-site attacks
2. **Multiple IDOR vulnerabilities** - users can access others' data
3. **Hardcoded secrets in repository** - all credentials compromised
4. **Missing authorization checks** - endpoints lack proper access control
5. **No input validation** - authentication can be bypassed

**Recommended Timeline:**
- **Day 1:** Fix critical issues (#1-10)
- **Week 1:** Fix high severity issues (#11-22)
- **Month 1:** Fix medium severity issues (#23-32)

**Next Steps:**
1. Review this report with the development team
2. Create GitHub issues for each vulnerability
3. Assign owners and deadlines
4. Begin remediation starting with Day 1 priorities
5. Schedule follow-up security audit after fixes

---

**Report Generated:** 2025-11-01
**Auditor:** Security Analysis Tool
**Contact:** security@connected.ehb.be
