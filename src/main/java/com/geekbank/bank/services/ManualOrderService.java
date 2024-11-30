package com.geekbank.bank.services;

import com.geekbank.bank.models.Product;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.repositories.ProductRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import com.twocaptcha.TwoCaptcha;
import com.twocaptcha.captcha.HCaptcha;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class ManualOrderService {

    @Value("${login.email}")
    private String loginEmail;

    @Value("${login.password}")
    private String loginPassword;

    @Value("${captcha.api.key}")
    private String captchaApiKey;

    // Sitekey fijo proporcionado
    private final String FIXED_SITEKEY = "df2f3ec1-3c26-4daa-97f3-22b71daccb97";

    private final TwoCaptcha solver;
    private Transaction transaction;
    private Product product;
    private ProductRepository productRepository;

    private TransactionRepository transactionRepository;

    public ManualOrderService(@Value("${captcha.api.key}") String captchaApiKey) {
        this.solver = new TwoCaptcha(captchaApiKey);
    }

    /**
     * Ejecuta el proceso de pedido manual utilizando Selenium WebDriver.
     *
     * @return Mensaje de estado de la ejecución.
     */

    public String runManualOrder(String transactionNumber) {

        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber);
        Optional<Product> product = productRepository.findById(transaction.getProducts().get(0).getProductId());

        // Configurar ChromeOptions
        ChromeOptions options = new ChromeOptions();
        // Comenta o elimina esta línea para ver el navegador durante la depuración
        // options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Establecer un User-Agent común para evitar detección
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/113.0.0.0 Safari/537.36");

        // Inicializar WebDriver (Selenium Manager manejará el driver automáticamente)
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60)); // Aumentado a 60 segundos

        // Ajustar el tamaño de la ventana
        driver.manage().window().setSize(new Dimension(1920, 1080));

        String hCaptchaResponse = null;
        boolean captchaResolved = false; // Indicador de resolución del captcha

        try {
            // Navegar a la página principal
            System.out.println("Navegando a https://gold.razer.com/mx/es");
            driver.get("https://gold.razer.com/mx/es");
            System.out.println("Navegación completada.");

            // Captura de pantalla
            takeScreenshot(driver, "home_page.png");

            // Cerrar banner de cookies si está visible
            cerrarBannerDeCookies(driver, wait);

            // Hacer clic en el botón "ACEPTO" si está presente
            clickButtonAcceptor(driver, wait);

            // Esperar y hacer clic en el botón de inicio de sesión
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[@aria-label='Log in your razer id account' and contains(text(), 'INICIAR SESIÓN')]")
            ));
            loginButton.click();
            System.out.println("Botón de inicio de sesión clickeado.");

            // Captura de pantalla
            takeScreenshot(driver, "login_page.png");

            // Hacer clic en el botón "ACEPTO" en la página de inicio de sesión si está presente
            clickButtonAcceptor(driver, wait);

            // Esperar a que la página de inicio de sesión cargue
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#input-login-email")));
            System.out.println("Página de inicio de sesión cargada.");

            // Captura de pantalla
            takeScreenshot(driver, "login_form.png");

            // Ingresar las credenciales de inicio de sesión
            WebElement emailInput = driver.findElement(By.cssSelector("#input-login-email"));
            WebElement passwordInput = driver.findElement(By.cssSelector("#input-login-password"));

            emailInput.sendKeys(loginEmail);
            passwordInput.sendKeys(loginPassword);
            System.out.println("Credenciales ingresadas.");

            // Hacer clic en el botón de inicio de sesión
            WebElement submitButton = driver.findElement(By.cssSelector("#btn-log-in"));
            submitButton.click();
            System.out.println("Botón de inicio de sesión clickeado.");

            // Captura de pantalla
            takeScreenshot(driver, "after_login_click.png");

            // Esperar a que la navegación después del login se complete
            // Descomenta y ajusta si es necesario
            // wait.until(ExpectedConditions.urlContains("/dashboard")); // Ajusta según la URL real después del login
            // System.out.println("Login exitoso, navegación a dashboard completada.");

            // Captura de pantalla
            takeScreenshot(driver, "dashboard_page.png");

            // Navegar a la página de Free Fire
            System.out.println("Navegando a la página de Free Fire.");
            driver.get("https://gold.razer.com/mx/es/gold/catalog/free-fire");
            System.out.println("Navegación a Free Fire completada.");

            // Captura de pantalla
            takeScreenshot(driver, "free_fire_page.png");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".selection-tile__text")));
            System.out.println("Página de Free Fire cargada correctamente.");

            // Seleccionar "100 Diamantes + Bono 10"
            seleccionarProducto(driver, wait, "100 Diamantes + Bono 10");

            // Esperar a que la selección se procese
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".selection-tile__label")));
            System.out.println("Producto '100 Diamantes + Bono 10' seleccionado.");

            // Captura de pantalla
            takeScreenshot(driver, "selected_product.png");

            // Seleccionar método de pago "Razer Gold"
            selectRazerGoldPaymentOption(driver, wait);

            // Hacer clic en "PASAR POR LA CAJA" usando XPath
            clickCheckoutButtonWithXPath(driver, wait);

            // Captura de pantalla
            takeScreenshot(driver, "after_checkout_click.png");

            // Esperar a que la página de checkout cargue
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#btn99")));
            System.out.println("Página de checkout cargada.");

            // Captura de pantalla
            takeScreenshot(driver, "checkout_page.png");

            // Hacer clic en el botón "CONTINUAR"
            WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#btn99")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", continueButton);
            continueButton.click();
            System.out.println("Botón 'CONTINUAR' clickeado.");

            // Captura de pantalla
            takeScreenshot(driver, "after_continue_click.png");

            // Resolver el captcha usando 2Captcha con reintentos
            hCaptchaResponse = solveCaptchaWithRetries(driver, 3, 10); // 3 intentos con 10 segundos de espera

            if (hCaptchaResponse == null) {
                System.out.println("No se pudo resolver el captcha.");
                return "No se pudo resolver el captcha.";
            }

            System.out.println("hCaptcha resuelto: " + hCaptchaResponse);
            // No marcar inmediatamente como resuelto; esperar la inyección y la redirección

            // Inyectar la respuesta del captcha en el campo oculto
            inyectarRespuestaCaptcha(driver, hCaptchaResponse);

            // Captura de pantalla
            takeScreenshot(driver, "after_captcha_injection.png");

            // Hacer clic en el checkbox
            clickCheckbox(driver, wait);

            // Captura de pantalla
            takeScreenshot(driver, "after_checkbox_click.png");

            // Hacer clic en el botón de envío del formulario
            clickSubmitFormButton(driver, wait);

            // Esperar a que la página de confirmación cargue
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmation-message, #confirmation")));
                System.out.println("Página de confirmación cargada.");
                takeScreenshot(driver, "confirmation_page.png");
                captchaResolved = true; // Marcar que el captcha fue resuelto y la redirección ocurrió
                return "Interacción completada exitosamente.";
            } catch (TimeoutException e) {
                System.out.println("No se detectó la redirección a la página de confirmación.");
                takeScreenshot(driver, "confirmation_not_detected.png");
                return "Formulario enviado, pero no se detectó la redirección a la confirmación.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error ejecutando el script: " + e.getMessage();
        } finally {
            // Cerrar el navegador solo si el captcha fue resuelto exitosamente y la redirección ocurrió
            if (captchaResolved) {
                driver.quit();
                System.out.println("Navegador cerrado.");
            } else {
                System.out.println("Proceso incompleto. El navegador permanecerá abierto para inspección manual.");
                System.out.println("Por favor, resuelve el captcha manualmente en el navegador abierto.");
            }
        }
    }

    /**
     * Cierra el banner de cookies si está presente.
     *
     * @param driver WebDriver
     * @param wait   WebDriverWait
     */
    private void cerrarBannerDeCookies(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement cookieBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("onetrust-accept-btn-handler")));
            if (cookieBanner.isDisplayed()) {
                cookieBanner.click();
                System.out.println("Banner de cookies cerrado.");
                // Captura de pantalla
                takeScreenshot(driver, "cookies_closed.png");
            }
        } catch (TimeoutException e) {
            System.out.println("No se encontró el banner de cookies, continuando...");
        }
    }

    /**
     * Hace clic en el botón "ACEPTO" si está presente.
     *
     * @param driver WebDriver
     * @param wait   WebDriverWait
     */
    private void clickButtonAcceptor(WebDriver driver, WebDriverWait wait) {
        try {
            // Esperar a que el botón esté presente en el DOM
            WebElement agreeButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btn-policy-agree")));

            // Desplazarse hasta el botón para asegurarse de que esté en el viewport
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", agreeButton);

            // Verificar visibilidad y clickeabilidad
            wait.until(ExpectedConditions.visibilityOf(agreeButton));
            wait.until(ExpectedConditions.elementToBeClickable(agreeButton));

            // Hacer clic en el botón usando JavaScript para asegurar la interacción
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", agreeButton);
            System.out.println("Botón 'ACEPTO' clickeado exitosamente.");

            // Captura de pantalla
            takeScreenshot(driver, "policy_agree_clicked.png");

        } catch (TimeoutException e) {
            System.out.println("No se encontró el botón 'ACEPTO', continuando...");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al intentar hacer clic en el botón 'ACEPTO'.");
        }
    }

    /**
     * Selecciona un producto específico basado en su texto visible.
     *
     * @param driver   WebDriver
     * @param wait     WebDriverWait
     * @param producto Texto del producto a seleccionar
     */
    private void seleccionarProducto(WebDriver driver, WebDriverWait wait, String producto) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('.selection-tile__text').forEach((el) => {" +
                            "if (el.textContent.trim() === '" + producto + "') {" +
                            "el.click();" +
                            "}" +
                            "});"
            );
            System.out.println("Producto '" + producto + "' seleccionado.");

            // Captura de pantalla
            takeScreenshot(driver, "product_selected.png");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al seleccionar el producto '" + producto + "': " + e.getMessage());
        }
    }

    /**
     * Selecciona la opción de pago "Razer Gold".
     *
     * @param driver WebDriver
     * @param wait   WebDriverWait
     */
    private void selectRazerGoldPaymentOption(WebDriver driver, WebDriverWait wait) {
        try {
            // Esperar hasta que el elemento Razer Gold esté visible y clickeable
            WebElement paymentOption = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@class='selection-tile__label' and text()='Razer Gold']")
            ));

            // Desplazarse al elemento para asegurarse de que esté en el viewport
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", paymentOption);

            // Hacer clic en el método de pago "Razer Gold"
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", paymentOption);
            System.out.println("Método de pago 'Razer Gold' seleccionado exitosamente.");

            // Captura de pantalla
            takeScreenshot(driver, "payment_option_selected.png");

        } catch (TimeoutException e) {
            System.out.println("No se encontró el método de pago 'Razer Gold'. Verifica el selector.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al intentar seleccionar el método de pago 'Razer Gold'.");
        }
    }

    /**
     * Hace clic en el botón "PASAR POR LA CAJA" usando XPath.
     *
     * @param driver WebDriver
     * @param wait   WebDriverWait
     */
    private void clickCheckoutButtonWithXPath(WebDriver driver, WebDriverWait wait) {
        try {
            // Esperar hasta que el botón esté presente en el DOM
            WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id='main_content']/div/div[3]/div/div/div/div[3]/button")
            ));

            // Desplazarse hasta el botón para asegurarse de que esté en el viewport
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", checkoutButton);

            // Hacer clic en el botón
            checkoutButton.click();
            System.out.println("Botón 'PASAR POR LA CAJA' clickeado exitosamente usando XPath.");

            // Captura de pantalla
            takeScreenshot(driver, "checkout_clicked.png");

        } catch (TimeoutException e) {
            System.out.println("No se encontró el botón 'PASAR POR LA CAJA' usando XPath.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al intentar hacer clic en el botón 'PASAR POR LA CAJA' usando XPath.");
        }
    }

    /**
     * Inyecta la respuesta del captcha en el campo oculto correspondiente dentro del iframe de hCaptcha.
     *
     * @param driver           WebDriver
     * @param hCaptchaResponse Respuesta del captcha proporcionada por 2Captcha
     */
    /**
     * Inyecta la respuesta del captcha en el textarea de hCaptcha dentro del iframe "razerOTP".
     *
     * @param driver           WebDriver
     * @param hCaptchaResponse Respuesta del captcha proporcionada por 2Captcha
     */
    /**
     * Inyecta la respuesta del captcha en el textarea de hCaptcha dentro del iframe "razerOTP".
     *
     * @param driver           WebDriver
     * @param hCaptchaResponse Respuesta del captcha proporcionada por 2Captcha
     */
    private void inyectarRespuestaCaptcha(WebDriver driver, String hCaptchaResponse) {
        try {
            // Cambiar al iframe "razerOTP"
            waitAndSwitchToFrameById(driver, "razerOTP");
            System.out.println("Cambiado al iframe 'razerOTP'.");

            // Esperar a que el div con id "portal" esté presente
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Aumentado a 30 segundos
            WebElement portalDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("portal")));
            System.out.println("Div 'portal' encontrado.");

            // Dentro del div "portal", encontrar el div "rz-hcaptcha"
            WebElement rzHcaptchaDiv = portalDiv.findElement(By.id("rz-hcaptcha"));
            if (rzHcaptchaDiv == null) {
                System.out.println("No se encontró el div 'rz-hcaptcha' dentro de 'portal'.");
                driver.switchTo().defaultContent();
                return;
            }

            // Dentro de "rz-hcaptcha", encontrar el textarea de hCaptcha
            WebElement captchaTextarea = null;
            try {
                // Método 1: Por nombre
                captchaTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("textarea[name='h-captcha-response']")));
                System.out.println("Textarea de hCaptcha encontrada por nombre.");
            } catch (TimeoutException e1) {
                System.out.println("No se encontró el textarea de hCaptcha por nombre. Intentando con otro selector...");
                try {
                    // Método 2: Por XPath utilizando starts-with en id
                    captchaTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//textarea[starts-with(@id, 'h-captcha-response') and @name='h-captcha-response']")
                    ));
                    System.out.println("Textarea de hCaptcha encontrada por XPath con starts-with en id.");
                } catch (TimeoutException e2) {
                    System.out.println("No se pudo localizar el textarea de hCaptcha con los selectores proporcionados.");
                    takeScreenshot(driver, "captcha_textarea_not_found.png");
                    driver.switchTo().defaultContent();
                    return;
                }
            }

            // Verificar si el textarea está presente
            if (captchaTextarea == null) {
                System.out.println("El textarea de hCaptcha no está presente.");
                takeScreenshot(driver, "captcha_textarea_not_present.png");
                driver.switchTo().defaultContent();
                return;
            }

            // Inyectar la respuesta del captcha usando JavaScript
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.display = 'block';" + // Asegura que el campo sea visible
                            "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('change'));",
                    captchaTextarea, hCaptchaResponse
            );
            System.out.println("Respuesta del captcha inyectada exitosamente.");

            // Esperar brevemente para que la página procese la respuesta
            Thread.sleep(2000); // Espera de 2 segundos

            // Captura de pantalla para depuración
            takeScreenshot(driver, "captcha_injected.png");

            // Volver al contexto principal
            driver.switchTo().defaultContent();
            System.out.println("Volvió al contexto principal.");

        } catch (NoSuchElementException e) {
            System.out.println("No se encontró el div o textarea de hCaptcha: " + e.getMessage());
            takeScreenshot(driver, "hcaptcha_elements_not_found.png");
        } catch (TimeoutException e) {
            System.out.println("Timeout al intentar inyectar la respuesta del captcha: " + e.getMessage());
            takeScreenshot(driver, "timeout_captcha_injection.png");
        } catch (StaleElementReferenceException e) {
            System.out.println("StaleElementReferenceException al inyectar la respuesta del captcha: " + e.getMessage());
            // Intentar re-inyectar la respuesta
            intentarReinyectarCaptcha(driver, hCaptchaResponse);
        } catch (InterruptedException e) {
            System.out.println("Interrupción durante el sleep: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Error al inyectar la respuesta del captcha: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot(driver, "exception_captcha_injection.png");
        }
    }
    /**
     * Método para reintentar inyectar la respuesta del captcha en caso de StaleElementReferenceException.
     *
     * @param driver           WebDriver
     * @param hCaptchaResponse Respuesta del captcha proporcionada por 2Captcha
     */
    /**
     * Método para reintentar inyectar la respuesta del captcha en caso de StaleElementReferenceException.
     *
     * @param driver           WebDriver
     * @param hCaptchaResponse Respuesta del captcha proporcionada por 2Captcha
     */
    /**
     * Método para reintentar inyectar la respuesta del captcha en caso de StaleElementReferenceException.
     *
     * @param driver           WebDriver
     * @param hCaptchaResponse Respuesta del captcha proporcionada por 2Captcha
     */
    private void intentarReinyectarCaptcha(WebDriver driver, String hCaptchaResponse) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Aumentado a 30 segundos

            // Cambiar al iframe "razerOTP" nuevamente
            driver.switchTo().defaultContent();
            waitAndSwitchToFrameById(driver, "razerOTP");
            System.out.println("Re-cambiado al iframe 'razerOTP'.");

            // Encontrar el iframe de hCaptcha nuevamente
            WebElement portalDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("portal")));
            List<WebElement> hCaptchaIframes = portalDiv.findElements(By.xpath(".//iframe[contains(@src, 'hcaptcha.com')]"));
            if (hCaptchaIframes.isEmpty()) {
                System.out.println("No se encontró el iframe de hCaptcha en el reintento.");
                driver.switchTo().defaultContent();
                return;
            }

            WebElement hCaptchaIframe = hCaptchaIframes.get(0); // Asumir que hay al menos uno

            // Cambiar al iframe de hCaptcha nuevamente
            driver.switchTo().frame(hCaptchaIframe);
            System.out.println("Re-cambiado al iframe de hCaptcha.");

            // Intentar localizar el textarea por diferentes métodos
            WebElement captchaTextarea = null;
            try {
                // Método 1: Por nombre
                captchaTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("textarea[name='h-captcha-response']")));
                System.out.println("Textarea de hCaptcha encontrada por nombre en el reintento.");
            } catch (TimeoutException e1) {
                System.out.println("No se encontró el textarea de hCaptcha por nombre en el reintento. Intentando con otro selector...");
                try {
                    // Método 2: Por XPath utilizando starts-with en id
                    captchaTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//textarea[starts-with(@id, 'h-captcha-response') and @name='h-captcha-response']")
                    ));
                    System.out.println("Textarea de hCaptcha encontrada por XPath con starts-with en id en el reintento.");
                } catch (TimeoutException e2) {
                    System.out.println("No se pudo localizar el textarea de hCaptcha con los selectores proporcionados en el reintento.");
                    takeScreenshot(driver, "captcha_textarea_not_found_reintento.png");
                    driver.switchTo().defaultContent();
                    return;
                }
            }

            // Verificar si el textarea está presente
            if (captchaTextarea == null) {
                System.out.println("El textarea de hCaptcha no está presente en el reintento.");
                takeScreenshot(driver, "captcha_textarea_not_present_reintento.png");
                driver.switchTo().defaultContent();
                return;
            }

            // Re-inyectar la respuesta del captcha usando JavaScript
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.display = 'block';" + // Asegura que el campo sea visible
                            "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('change'));",
                    captchaTextarea, hCaptchaResponse
            );
            System.out.println("Respuesta del captcha re-inyectada exitosamente en el reintento.");

            // Esperar brevemente para que la página procese la respuesta
            Thread.sleep(2000); // Espera de 2 segundos

            // Captura de pantalla para depuración
            takeScreenshot(driver, "captcha_reinjected.png");

            // Volver al contexto principal
            driver.switchTo().defaultContent();
            System.out.println("Volvió al contexto principal después del reintento.");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Fallo al intentar re-inyectar la respuesta del captcha en el reintento.");
            takeScreenshot(driver, "captcha_reinjection_failed.png");
        }
    }


    /**
     * Resuelve el captcha utilizando la API de 2Captcha.
     *
     * @param driver       WebDriver
     * @param maxAttempts  Número máximo de intentos
     * @param delaySeconds Tiempo de espera entre intentos en segundos
     * @return Respuesta del captcha si se resuelve correctamente, null en caso contrario.
     */
    private String solveCaptchaWithRetries(WebDriver driver, int maxAttempts, int delaySeconds) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("Intento " + attempt + " de resolver el captcha.");
            String response = solveCaptcha(driver);
            if (response != null) {
                return response;
            }
            System.out.println("Intento " + attempt + " fallido. Esperando " + delaySeconds + " segundos antes de reintentar.");
            try {
                Thread.sleep(delaySeconds * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupción durante la espera de reintento: " + ie.getMessage());
                break;
            }
        }
        return null;
    }

    /**
     * Resuelve el captcha utilizando la API de 2Captcha.
     *
     * @param driver WebDriver
     * @return Respuesta del captcha si se resuelve correctamente, null en caso contrario.
     */
    private String solveCaptcha(WebDriver driver) {
        try {
            // Tomar una captura de pantalla antes de intentar resolver el captcha para depuración
            takeScreenshot(driver, "before_solveCaptcha.png");

            // Usar el sitekey fijo
            String siteKey = FIXED_SITEKEY;
            String pageUrl = driver.getCurrentUrl();

            System.out.println("Usando sitekey fijo: " + siteKey);
            System.out.println("URL de la página: " + pageUrl);

            // Crear y configurar el objeto HCaptcha para 2Captcha
            HCaptcha captcha = new HCaptcha();
            captcha.setSiteKey(siteKey);
            captcha.setUrl(pageUrl);

            // Enviar el captcha a 2Captcha para su resolución
            solver.solve(captcha);
            String captchaCode = captcha.getCode();

            if (captchaCode != null && !captchaCode.isEmpty()) {
                System.out.println("Captcha resuelto exitosamente.");
                return captchaCode;
            } else {
                System.out.println("2Captcha no pudo resolver el captcha.");
                takeScreenshot(driver, "2captcha_failed.png");
                return null;
            }

        } catch (Exception e) {
            System.out.println("Error al resolver el captcha: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot(driver, "exception_solveCaptcha.png");
            return null;
        }
    }

    /**
     * Toma una captura de pantalla y la guarda en la carpeta 'screenshots'.
     *
     * @param driver   WebDriver
     * @param fileName Nombre del archivo de la captura de pantalla
     */
    private void takeScreenshot(WebDriver driver, String fileName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            // Especifica la ruta donde deseas guardar las capturas de pantalla
            File destination = new File("screenshots/" + fileName);
            destination.getParentFile().mkdirs(); // Crea el directorio si no existe
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Captura de pantalla guardada en: " + destination.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al guardar la captura de pantalla: " + e.getMessage());
        }
    }

//    private void clickCheckbox(WebDriver driver, WebDriverWait wait) {
//        try {
//            // Esperar a que el checkbox esté presente en el DOM
//            WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("checkbox")));
//
//            // Desplazarse hasta el checkbox para asegurarse de que esté en el viewport
//            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", checkbox);
//
//            // Verificar visibilidad y que no esté oculto
//            wait.until(ExpectedConditions.visibilityOf(checkbox));
//            wait.until(ExpectedConditions.elementToBeClickable(checkbox));
//
//            // Obtener el estado actual del checkbox
//            String ariaChecked = checkbox.getAttribute("aria-checked");
//            if ("false".equalsIgnoreCase(ariaChecked)) {
//                // Hacer clic en el checkbox usando JavaScript para asegurar la interacción
//                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
//                System.out.println("Checkbox clickeado exitosamente.");
//            } else {
//                System.out.println("El checkbox ya está seleccionado.");
//            }
//
//            // Captura de pantalla
//            takeScreenshot(driver, "checkbox_clicked.png");
//
//        } catch (TimeoutException e) {
//            System.out.println("No se encontró el checkbox con id 'checkbox' dentro del tiempo esperado.");
//            takeScreenshot(driver, "checkbox_not_found.png");
//        } catch (ElementClickInterceptedException e) {
//            System.out.println("Error al intentar clicar el checkbox: " + e.getMessage());
//            // Intentar clicar usando JavaScript
//            try {
//                WebElement checkbox = driver.findElement(By.id("checkbox"));
//                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
//                System.out.println("Checkbox clickeado exitosamente usando JavaScript.");
//                takeScreenshot(driver, "checkbox_clicked_js.png");
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println("Fallo al intentar clicar el checkbox usando JavaScript.");
//                takeScreenshot(driver, "checkbox_click_failed.png");
//            }
//        } catch (NoSuchElementException e) {
//            System.out.println("No se encontró el checkbox con id 'checkbox': " + e.getMessage());
//            takeScreenshot(driver, "checkbox_no_such_element.png");
//        } catch (Exception e) {
//            System.out.println("Error al intentar clicar el checkbox: " + e.getMessage());
//            e.printStackTrace();
//            takeScreenshot(driver, "checkbox_general_error.png");
//        }
//    }

    private void listarIframes(WebDriver driver) {
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        System.out.println("Cantidad de iframes encontrados: " + iframes.size());
        for (int i = 0; i < iframes.size(); i++) {
            System.out.println("Iframe " + (i + 1) + " SRC: " + iframes.get(i).getAttribute("src"));
        }
    }

    private void clickCheckbox(WebDriver driver, WebDriverWait wait) {
        try {
            // Cambiar al contexto principal para evitar errores previos
            driver.switchTo().defaultContent();
            System.out.println("Volvió al contexto principal.");

            // Localizar el div que contiene el iframe de hCaptcha
            WebElement rzHcaptchaDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rz-hcaptcha")));
            System.out.println("Div 'rz-hcaptcha' encontrado.");

            // Localizar el iframe dentro de 'rz-hcaptcha' cuyo src contiene 'hcaptcha.com'
            WebElement hcaptchaIframe = rzHcaptchaDiv.findElement(By.xpath(".//iframe[contains(@src, 'hcaptcha.com')]"));
            System.out.println("Iframe de hCaptcha encontrado.");

            // Cambiar al iframe de hCaptcha
            driver.switchTo().frame(hcaptchaIframe);
            System.out.println("Cambiado al iframe de hCaptcha.");

            // Esperar a que el checkbox esté presente y sea visible
            WebElement checkbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkbox")));
            System.out.println("Checkbox encontrado y visible.");

            // Esperar a que el checkbox sea clickeable
            wait.until(ExpectedConditions.elementToBeClickable(checkbox));
            System.out.println("Checkbox es clickeable.");

            // Intentar hacer clic en el checkbox
            checkbox.click();
            System.out.println("Checkbox clickeado exitosamente.");

            // Captura de pantalla
            takeScreenshot(driver, "checkbox_clicked.png");

            // Volver al contexto principal
            driver.switchTo().defaultContent();
            System.out.println("Volvió al contexto principal después de clicar el checkbox.");

        } catch (TimeoutException e) {
            System.out.println("No se encontró el checkbox con id 'checkbox' dentro del tiempo esperado.");
            takeScreenshot(driver, "checkbox_not_found.png");
        } catch (NoSuchElementException e) {
            System.out.println("No se encontró el checkbox con id 'checkbox': " + e.getMessage());
            takeScreenshot(driver, "checkbox_no_such_element.png");
        } catch (ElementClickInterceptedException e) {
            System.out.println("Error al intentar clicar el checkbox: " + e.getMessage());
            // Intentar clicar usando JavaScript
            try {
                WebElement checkbox = driver.findElement(By.id("checkbox"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                System.out.println("Checkbox clickeado exitosamente usando JavaScript.");
                takeScreenshot(driver, "checkbox_clicked_js.png");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Fallo al intentar clicar el checkbox usando JavaScript.");
                takeScreenshot(driver, "checkbox_click_failed.png");
            }
        } catch (Exception e) {
            System.out.println("Error al intentar clicar el checkbox: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot(driver, "checkbox_general_error.png");
        }
    }

    /**
     * Hace clic en el botón de envío del formulario.
     *
     * @param driver WebDriver
     * @param wait   WebDriverWait
     */
    private void clickSubmitFormButton(WebDriver driver, WebDriverWait wait) {
        try {
            // Esperar a que el botón de envío esté presente y sea clickeable
            WebElement submitFormButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@type, 'submit') or contains(@class, 'submit-button') or contains(text(), 'Enviar') or contains(text(), 'Submit')]")
            ));

            // Desplazarse hasta el botón para asegurarse de que esté en el viewport
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitFormButton);

            // Hacer clic en el botón usando JavaScript para asegurar la interacción
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitFormButton);
            System.out.println("Botón de envío del formulario clickeado exitosamente.");

            // Captura de pantalla
            takeScreenshot(driver, "after_form_submit.png");

        } catch (TimeoutException e) {
            System.out.println("No se encontró el botón de envío del formulario dentro del tiempo esperado.");
            takeScreenshot(driver, "submit_button_not_found.png");
        } catch (NoSuchElementException e) {
            System.out.println("No se encontró el botón de envío del formulario: " + e.getMessage());
            takeScreenshot(driver, "submit_button_no_such_element.png");
        } catch (ElementClickInterceptedException e) {
            System.out.println("Error al intentar clicar el botón de envío del formulario: " + e.getMessage());
            // Intentar clicar usando JavaScript de nuevo
            try {
                WebElement submitFormButton = driver.findElement(By.xpath("//button[contains(@type, 'submit') or contains(@class, 'submit-button') or contains(text(), 'Enviar') or contains(text(), 'Submit')]"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitFormButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitFormButton);
                System.out.println("Botón de envío del formulario clickeado usando JavaScript.");
                takeScreenshot(driver, "after_form_submit_intercepted.png");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Fallo al intentar clicar el botón de envío del formulario usando JavaScript.");
                takeScreenshot(driver, "submit_button_click_failed.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al intentar clicar el botón de envío del formulario: " + e.getMessage());
            takeScreenshot(driver, "submit_button_general_error.png");
        }
    }


    /**
     * Espera a que un iframe esté disponible y cambia al contexto del iframe por su ID.
     *
     * @param driver WebDriver
     * @param frameId ID del iframe al que se desea cambiar
     */
    private void waitAndSwitchToFrameById(WebDriver driver, String frameId) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameId));
    }
}
