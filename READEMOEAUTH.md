À partir de la version 2.3.0 de Spring Boot, et avec la migration vers Spring Security 5.x et plus, plusieurs classes et approches dans Spring Security OAuth2 ont été remplacées ou dépréciées. Voici les détails des changements, les alternatives, et la nouvelle implémentation.
1. Dépendances nécessaires

Pour configurer un serveur de ressources OAuth2 ou un client, vous devez inclure les dépendances suivantes dans votre pom.xml :
Pour un serveur de ressources OAuth2 :

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

Pour un client OAuth2 :

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

2. Modifications dans les imports (et alternatives)

Les classes que vous avez mentionnées ont été remplacées ou sont désormais inutiles dans les nouvelles versions.
2.1. Dépréciations et alternatives
Ancien Import	Alternative ou Nouvelle Approche
DefaultOAuth2ClientContext	N'existe plus. Les clients OAuth2 gèrent les contextes automatiquement.
OAuth2RestOperations	Remplacé par RestTemplate ou par des composants natifs.
OAuth2RestTemplate	Remplacé par des mécanismes natifs de Spring WebClient.
TokenRequest et DefaultAccessTokenRequest	Jetons gérés via TokenRelayFilter et WebClient.
ResourceOwnerPasswordAccessTokenProvider (Grant Type Password)	Déprécié car le flux Password Grant est considéré comme non sécurisé.
3. Nouvelle Implémentation
   3.1. Client OAuth2 (avec WebClient)

Le OAuth2RestTemplate a été remplacé par le WebClient de Spring, qui intègre désormais le support d’OAuth2.
Configuration du Client OAuth2 (par exemple, avec Google) :

    Ajoutez les propriétés dans application.properties :

spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/auth
spring.security.oauth2.client.provider.google.user-info-uri=https://openidconnect.googleapis.com/v1/userinfo

    Configurez le WebClient :

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations,
                               ServerOAuth2AuthorizedClientRepository authorizedClients) {
        return WebClient.builder()
            .filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients))
            .build();
    }
}

    Exemple d’utilisation dans un contrôleur :

@RestController
public class GoogleController {

    private final WebClient webClient;

    public GoogleController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/google-profile")
    public Mono<String> getGoogleProfile(OAuth2AuthenticationToken authentication) {
        return webClient.get()
            .uri("https://openidconnect.googleapis.com/v1/userinfo")
            .attributes(oauth2AuthorizedClient(authentication))
            .retrieve()
            .bodyToMono(String.class);
    }
}

3.2. Serveur de Ressources OAuth2

Pour sécuriser une API en tant que serveur de ressources, utilisez JWT comme format de jeton :

    Ajoutez les propriétés dans application.properties :

spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json

    Configurez Spring Security pour valider les jetons JWT :

@Configuration
@EnableWebSecurity
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt(); // Utilisation de JWT pour valider les jetons
    }
}

3.3. Flux Password Grant (déconseillé, mais voici comment le gérer)

Le flux Resource Owner Password Grant est déprécié car il n'est pas sécurisé. Cependant, si vous devez encore l'utiliser dans un contexte particulier, voici comment le configurer avec WebClient :
Configuration dans application.properties :

spring.security.oauth2.client.registration.password-grant.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.password-grant.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.password-grant.authorization-grant-type=password
spring.security.oauth2.client.registration.password-grant.scope=read,write
spring.security.oauth2.client.provider.password-grant.token-uri=http://auth-server/oauth/token

Implémentation avec WebClient :

@RestController
public class PasswordGrantController {

    private final WebClient webClient;

    public PasswordGrantController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://auth-server").build();
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestParam String username, @RequestParam String password) {
        return webClient.post()
            .uri("/oauth/token")
            .body(BodyInserters.fromFormData("grant_type", "password")
                .with("username", username)
                .with("password", password)
                .with("client_id", "YOUR_CLIENT_ID")
                .with("client_secret", "YOUR_CLIENT_SECRET"))
            .retrieve()
            .bodyToMono(String.class);
    }
}

4. Résumé des changements

   OAuth2RestTemplate remplacé par WebClient.
   Gestion des jetons intégrée via Spring Security (pas besoin de DefaultOAuth2ClientContext ou TokenRequest).
   Les flux Authorization Code et Client Credentials sont privilégiés, tandis que Password Grant est déconseillé.

Ces nouvelles approches améliorent la sécurité et la compatibilité avec les standards modernes d'OAuth2. Si vous avez un cas particulier ou un besoin spécifique, n’hésitez pas à préciser ! 😊


---------------------------------------

1. Structure du Projet
   Dépendances Maven

Ajoutez les dépendances nécessaires dans le fichier pom.xml :

<dependencies>
    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- OAuth2 Client -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>

2. Anciennes Classes Dépréciées et leurs Remplaçants
   Classe dépréciée	Remplaçant moderne
   DefaultOAuth2ClientContext	Géré implicitement par Spring Security.
   OAuth2RestOperations / OAuth2RestTemplate	Remplacé par WebClient avec des filtres OAuth2.
   TokenRequest, DefaultAccessTokenRequest	Géré implicitement dans les nouveaux composants.
   ResourceOwnerPasswordAccessTokenProvider	Remplacé par WebClient et des flux modernes.
3. Exemple Complet
   3.1. Configuration pour un Client OAuth2

Nous allons configurer un client OAuth2 qui interagit avec Google comme fournisseur.
Configurer les propriétés (application.properties)

# Configuration pour Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://openidconnect.googleapis.com/v1/userinfo

Configurer le WebClient avec OAuth2

Remplaçant moderne pour OAuth2RestTemplate :

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations,
                               ServerOAuth2AuthorizedClientRepository authorizedClients) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);
        oauth2Filter.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder()
                .filter(oauth2Filter)
                .build();
    }
}

Créer un contrôleur pour utiliser l'API Google

@RestController
@RequestMapping("/google")
public class GoogleController {

    private final WebClient webClient;

    public GoogleController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/profile")
    public Mono<String> getUserProfile(OAuth2AuthenticationToken authentication) {
        return webClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .attributes(oauth2AuthorizedClient(authentication))
                .retrieve()
                .bodyToMono(String.class);
    }
}

3.2. Configuration pour un Serveur de Ressources OAuth2

Nous allons configurer une API REST sécurisée qui valide les jetons JWT.
Configurer les propriétés (application.properties)

# JWT configuration
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json

Configurer la sécurité avec Spring Security

@Configuration
@EnableWebSecurity
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/public").permitAll()
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt();
    }
}

Créer une API sécurisée

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint.";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "This is a private endpoint, accessible only with a valid token.";
    }
}

3.3. (Optionnel) Flux Password Grant (Déconseillé)

Bien que déconseillé, voici une configuration minimale pour utiliser le flux Password Grant.
Configurer les propriétés

spring.security.oauth2.client.registration.password-grant.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.password-grant.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.password-grant.authorization-grant-type=password
spring.security.oauth2.client.provider.password-grant.token-uri=http://auth-server/oauth/token

Configurer un client avec WebClient

@RestController
@RequestMapping("/login")
public class LoginController {

    private final WebClient webClient;

    public LoginController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://auth-server").build();
    }

    @PostMapping
    public Mono<String> login(@RequestParam String username, @RequestParam String password) {
        return webClient.post()
                .uri("/oauth/token")
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("username", username)
                        .with("password", password)
                        .with("client_id", "YOUR_CLIENT_ID")
                        .with("client_secret", "YOUR_CLIENT_SECRET"))
                .retrieve()
                .bodyToMono(String.class);
    }
}

4. Résumé des Changements
   Ancien	Nouveau
   OAuth2RestTemplate	WebClient avec des filtres OAuth2.
   DefaultOAuth2ClientContext	Géré implicitement par Spring Security.
   ResourceOwnerPasswordAccessTokenProvider	WebClient pour les flux manuels.
   TokenRequest et AccessTokenRequest	Intégré dans la gestion des flux.
   Conclusion

Avec Spring Boot Security OAuth2 version 2.3.0 et au-delà, la gestion OAuth2 repose sur WebClient pour les interactions client et sur JWT pour les serveurs de ressources. Les anciennes classes sont soit supprimées, soit intégrées automatiquement, rendant les configurations plus simples et modernes.

Pour aller plus loin, testez ces exemples avec des fournisseurs comme Google, Okta ou Keycloak pour expérimenter des cas réels. 😊