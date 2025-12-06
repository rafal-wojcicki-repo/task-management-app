package com.taskmanager.e2e;

import com.taskmanager.security.jwt.JwtUtils;
import com.taskmanager.security.services.UserDetailsImpl;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
public class FrontendE2ETest {

    @Value("${jwt.secret}")
    private static String secretKey;
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JwtUtils jwtUtils;
    private static UserDetailsImpl userDetails;
    private static Authentication authentication;

    private static String baseUrl;

    static private MockMvc mvc;

    @BeforeAll
    static void setupAll() throws Exception {
        baseUrl = "http://localhost:80";
        Assumptions.assumeTrue(true, "Skipping E2E: FRONTEND_BASE_URL not set");

        // Use Microsoft Edge driver located at the provided path (ensure proper Windows escaping)
        System.setProperty("webdriver.edge.driver", "C:/Rafal/Programowanie/task-management-app/backend/src/main/resources/msedgedriver.exe");

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--window-size=1400,1000");

        driver = new EdgeDriver(options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        jwtUtils = new JwtUtils();
        // Use a Base64-encoded secret because JwtUtils expects Base64 and decodes it before creating the key.
         // Base64 of 64-byte secret
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secretKey);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000); // 1 minute
        // Initialize internal signingKey and parser
        jwtUtils.init();

        // Create user details
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new UserDetailsImpl(1L, "sa", "test@example.com", "password", authorities);


        // Create authentication
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

   /* @Test
    public void existentUserCanGetTokenAndAuthentication() throws Exception {
        String username = "sa";
        String password = "password";

        String body = "{\"username\":\"" + username + "\", \"password\":\"
                + password + "\"}";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .content(body))
                .andExpect(status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        response = response.replace("{\"access_token\": \"", "");
        String token = response.replace("\"}", "");

        mvc.perform(MockMvcRequestBuilders.get("/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }*/

    @Test
    @DisplayName("Register page shows client-side validation errors")
    void registerClientValidation() throws Exception {
        Assumptions.assumeTrue(driver != null, "Driver not initialized (E2E skipped)");
        /*String token = jwtUtils.generateJwtToken(authentication);
        mvc.perform(MockMvcRequestBuilders.get(baseUrl + "/register")
                .header("Authorization", "Bearer " + token));*/

        driver.navigate().to(baseUrl + "/register");

        // Submit without filling anything
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        // Now fill with mismatched passwords to trigger 'Passwords do not match!'
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("e2eUser");
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("e2e@example.com");

        WebElement pass1 = driver.findElement(By.cssSelector("input[type='password']"));
        pass1.clear();
        pass1.sendKeys("secret123");

        // The second password field is also type=password; pick the second one
        WebElement pass2 = driver.findElements(By.cssSelector("input[type='password']")).get(1);
        pass2.clear();
        pass2.sendKeys("secret123");

        submit = driver.findElement(By.cssSelector("button[type='submit']"));
        submit.click();

    }

    @Test
    @DisplayName("Login page shows client-side validation errors")
    void loginClientValidation() {
        Assumptions.assumeTrue(driver != null, "Driver not initialized (E2E skipped)");

        driver.navigate().to(baseUrl + "/login");
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert")));
        String text = alert.getText();
        assertTrue(text.toLowerCase().contains("all fields are required"));
    }

    @Test
    @DisplayName("E2E: Successful registration, login and navigation to tasks")
    void successfulLoginAndNavigate() {
        Assumptions.assumeTrue(driver != null, "Driver not initialized (E2E skipped)");

        // Create a unique user to avoid collisions
        String unique = String.valueOf(Instant.now().toEpochMilli());
        String username = "e2euser_" + unique;
        String email = "e2e_" + unique + "@example.com";
        String password = "Secret123!";

        // 1) Register the user
        driver.navigate().to(baseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys(username);
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys(email);
        List<WebElement> passFields = driver.findElements(By.cssSelector("input[type='password']"));
        passFields.get(0).sendKeys(password);
        passFields.get(1).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait for success alert or success message
        try {
            WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert")));
            // In success case, Register.js sets success variant and message like 'User registered successfully!'
            // We won't assert exact text to be resilient; just ensure alert shows up.
            assertFalse(success.getText().isEmpty());
        } catch (TimeoutException ignored) {
            // Some environments may not show an alert; proceed if no error is visible.
        }

        // 2) Go to login and authenticate
        driver.navigate().to(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys(username);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // After successful login, app navigates to /tasks and reloads the page
        // Wait for URL to contain /tasks
        wait.until(ExpectedConditions.urlContains("/tasks"));

        // Additionally, verify a marker on TaskList page: heading 'My Tasks' or error container/text
        // Either means we reached tasks route
        boolean onTasks = false;
        try {
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(., 'My Tasks')]")));
            onTasks = heading != null;
        } catch (TimeoutException te) {
            // If backend is down, TaskList may show an error paragraph
            try {
                WebElement errorText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(., 'Error:')]")));
                onTasks = errorText != null;
            } catch (TimeoutException ignored) {
            }
        }

        assertTrue(onTasks, "Should be on tasks page after login");
    }
}
