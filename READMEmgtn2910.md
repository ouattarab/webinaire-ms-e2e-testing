
# Guide de Migration vers Spring 6.0.5

## Table des Matières
1. [Migration des Imports `javax` vers `jakarta`](#1-migration-des-imports-javax-vers-jakarta)
2. [Problèmes Spécifiques et Solutions](#2-problèmes-spécifiques-et-solutions)
3. [Méthodes Dépréciées et Alternatives](#3-méthodes-dépréciées-et-alternatives)
4. [Résolution de Problèmes Courants](#4-résolution-de-problèmes-courants)
5. [Exemples d’Intégration et de Configuration](#5-exemples-dintégration-et-de-configuration)

---

## 1. Migration des Imports `javax` vers `jakarta`

Spring 6.0.5 nécessite de remplacer tous les imports `javax` par `jakarta` pour garantir la compatibilité avec Jakarta EE 9+. Voici une liste complète des changements d'import :

- `javax.persistence.*` -> `jakarta.persistence.*`
- `javax.servlet.*` -> `jakarta.servlet.*`
- `javax.annotation.*` -> `jakarta.annotation.*`
- `javax.xml.bind.*` -> `jakarta.xml.bind.*`
- `javax.validation.*` -> `jakarta.validation.*`
- `javax.ws.rs.*` -> `jakarta.ws.rs.*`

### Exemple de remplacement :
```java
// Avant
import javax.persistence.Entity;

// Après
import jakarta.persistence.Entity;
```

Assurez-vous également de mettre à jour vos dépendances Maven ou Gradle pour Jakarta.

---

## 2. Problèmes Spécifiques et Solutions

### Problème : `org.springframework.http.client.ClientHttpResponse` est déprécié
**Cause** : `ClientHttpResponse` a été déprécié pour encourager l'utilisation de `WebClient`.
**Solution** : Utilisez `WebClient` avec `ClientResponse`.

**Exemple de Migration :**
```java
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

WebClient webClient = WebClient.create();
webClient.get()
    .uri("https://api.example.com")
    .exchangeToMono(response -> Mono.just(response.rawStatusCode()))
    .subscribe(statusCode -> System.out.println("Code de statut : " + statusCode));
```

### Problème : `Cannot resolve symbol getMethodValue` dans `HttpRequest`
**Cause** : La méthode `getMethodValue()` a été supprimée.
**Solution** : Utilisez `getMethod().name()`.

**Exemple :**
```java
HttpMethod method = request.getMethod();
System.out.println("Méthode HTTP : " + (method != null ? method.name() : "Inconnue"));
```

### Problème : `Cannot resolve symbol reactive`
**Cause** : `spring-webflux` n'est pas inclus dans les dépendances.
**Solution** : Ajoutez `spring-webflux` pour utiliser `WebClient`.

**Dépendance Maven :**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 3. Méthodes Dépréciées et Alternatives

### Remplacement de `ClientHttpRequestFactorySupplier` dans Spring 6.0.5

Dans Spring 6.0.5, la classe `ClientHttpRequestFactorySupplier` n’est plus disponible, ce qui rend obsolètes les appels comme `ClientHttpRequestFactorySupplier().get().createRequest()`. À la place, il est recommandé d’utiliser `RestTemplate`, `SimpleClientHttpRequestFactory`, ou `HttpComponentsClientHttpRequestFactory` pour gérer les requêtes HTTP.

#### Exemple de Remplacement avec `SimpleClientHttpRequestFactory`

Pour recréer une requête HTTP de manière similaire, utilisez `SimpleClientHttpRequestFactory` :

```java
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;

import java.net.URI;

public class HttpRequestExample {

    public void createRequestExample() throws Exception {
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        URI uri = new URI("http://example.com/api/resource"); // Remplacez par votre URL
        ClientHttpRequest request = factory.createRequest(uri, HttpMethod.GET);

        request.getHeaders().add("Custom-Header", "value");
        request.execute();
    }
}
```

#### Utilisation de `HttpComponentsClientHttpRequestFactory`

Pour une gestion plus avancée, comme le contrôle des connexions, utilisez `HttpComponentsClientHttpRequestFactory` :

```java
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;

import java.net.URI;

public class HttpRequestExample {

    public void createRequestExample() throws Exception {
        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        URI uri = new URI("http://example.com/api/resource");
        ClientHttpRequest request = factory.createRequest(uri, HttpMethod.GET);

        request.getHeaders().add("Custom-Header", "value");
        request.execute();
    }
}
```

#### Utilisation de `RestTemplate` pour Simplifier les Requêtes HTTP

L’utilisation de `RestTemplate` est fortement recommandée, car elle simplifie grandement l’envoi de requêtes HTTP :

```java
import org.springframework.web.client.RestTemplate;

public class HttpRequestExample {

    public void makeRequest() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject("http://example.com/api/resource", String.class);
        System.out.println(response);
    }
}
```

### Pourquoi `ClientHttpRequestFactorySupplier` a été Retiré

Spring a rationalisé son API HTTP, remplaçant les classes intermédiaires comme `ClientHttpRequestFactorySupplier` par des solutions plus flexibles et modernes (`RestTemplate` et `WebClient`). Cela permet une meilleure compatibilité et des options de personnalisation étendues pour les requêtes HTTP.


Voici une liste étendue des méthodes dépréciées et leurs remplacements :

- **`ClientHttpResponse.getRawStatusCode()`**  
  - **Remplacement** : `ClientResponse.getStatusCode()`.

- **`RestClientResponseException.getRawStatusCode()`**  
  - **Remplacement** : `RestClientResponseException.getStatusCode()`.

- **`UnknownContentTypeException.getRawStatusCode()`**  
  - **Remplacement** : `UnknownContentTypeException.getStatusCode()`.

- **`RestTemplate.doExecute(URI, HttpMethod, RequestCallback, ResponseExtractor<T>)`**  
  - **Remplacement** : `RestTemplate.doExecute(URI, String, HttpMethod, RequestCallback, ResponseExtractor)`.

- **`AbstractRefreshableWebApplicationContext.getTheme(String)`**  
  - **Remplacement** : Pas de remplacement direct. L'implémentation des thèmes peut nécessiter une approche personnalisée.

- **`DefaultResponseErrorHandler.hasError(int)`**  
  - **Remplacement** : `DefaultResponseErrorHandler.hasError(HttpStatusCode)`.

- **`ModelAndViewContainer.setIgnoreDefaultModelOnRedirect(boolean)`**  
  - **Remplacement** : Aucun remplacement direct ; le modèle par défaut sera toujours ignoré sur redirection.

- **`ResponseStatusExceptionHandler.determineRawStatusCode(Throwable)`**  
  - **Remplacement** : Utilisez `ResponseStatusExceptionHandler.determineStatus(Throwable)`.

- **`MethodNotAllowedException.getResponseHeaders()`**  
  - **Remplacement** : Utilisez `MethodNotAllowedException.getHeaders()`.

- **`NotAcceptableStatusException.getResponseHeaders()`**  
  - **Remplacement** : Utilisez `NotAcceptableStatusException.getHeaders()`.

- **`UnsupportedMediaTypeStatusException.getResponseHeaders()`**  
  - **Remplacement** : Utilisez `UnsupportedMediaTypeStatusException.getHeaders()`.

- **`PathPatternParser.isMatchOptionalTrailingSeparator()`**  
  - **Remplacement** : Utilisez explicitement les redirections via un filtre Servlet ou un contrôleur.

- **`UrlPathHelper.getLookupPathForRequest(HttpServletRequest, String)`**  
  - **Remplacement** : Utilisez `UrlPathHelper.resolveAndCacheLookupPath(HttpServletRequest)`.

- **`HttpMediaTypeException(String)` et `HttpMediaTypeException(String, List<MediaType>)`**  
  - **Remplacement** : Pas de remplacement direct.

- **`HttpRequestMethodNotSupportedException(String, String)`**  
  - **Remplacement** : `HttpRequestMethodNotSupportedException(String, Collection)`.

- **`HttpRequestMethodNotSupportedException(String, String[])`**  
  - **Remplacement** : `HttpRequestMethodNotSupportedException(String, Collection)`.

---

## 4. Résolution de Problèmes Courants

- **Problèmes de compatibilité avec JAXB**  
  - **Cause** : Les classes JAXB sont maintenant fournies par `jakarta.xml.bind`.
  - **Solution** : Remplacez `javax.xml.bind.*` par `jakarta.xml.bind.*`.
  
  **Dépendance :**
  ```xml
  <dependency>
      <groupId>jakarta.xml.bind</groupId>
      <artifactId>jakarta.xml.bind-api</artifactId>
      <version>3.0.1</version>
  </dependency>
  ```

- **Problème avec `javax.annotation.*`**  
  - **Solution** : Utilisez `jakarta.annotation.*`.
  - Exemple de remplacement :
    ```java
    // Avant
    import javax.annotation.PostConstruct;
    
    // Après
    import jakarta.annotation.PostConstruct;
    ```

---

## 5. Exemples d’Intégration et de Configuration

### Exemple avec `WebClient` et gestion des erreurs HTTP
```java
WebClient webClient = WebClient.builder().build();
webClient.get().uri("https://api.example.com")
    .retrieve()
    .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Erreur HTTP")))
    .bodyToMono(String.class)
    .doOnError(e -> System.out.println("Erreur détectée : " + e.getMessage()))
    .subscribe(response -> System.out.println("Réponse : " + response));
```

### Intégration pour `Spring Security JWT` et Hibernate 6.5.2
Mettez à jour les dépendances et ajustez les imports pour être compatible avec Jakarta EE, notamment en vérifiant les annotations dans vos entités et les configurations de sécurité.

---

Ce guide couvre les principaux changements et solutions nécessaires pour migrer vers Spring 6.0.5 et Jakarta EE. Assurez-vous de bien ajuster vos configurations et dépendances pour garantir une compatibilité totale.
