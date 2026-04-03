# Bailey Bakery Common

Shared utility library for the Bailey Bakery platform. Provides common integrations, data processing, and helper functions used across bakery microservices.

## Features

- **Template Engine** — Render dynamic email and receipt templates
- **Service Client** — HTTP client wrapper for inter-service communication
- **Data Codec** — Serialize/deserialize data for message queues and caching
- **Query Helper** — Build dynamic database queries for reporting
- **Asset Loader** — Load and serve static assets (images, PDFs, documents)
- **Config Parser** — Parse XML/YAML configuration files
- **PDF Generator** — Generate PDF invoices and receipts
- **Token Generator** — Create secure tokens for password resets and email verification
- **Audit Logger** — Structured audit logging for compliance
- **Directory Client** — LDAP integration for staff directory lookups

## Installation

```xml
<dependency>
    <groupId>com.baileybakery</groupId>
    <artifactId>bailey-bakery-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

```java
import com.baileybakery.common.template.TemplateEngine;
import com.baileybakery.common.http.ServiceClient;

// Render a template
Map<String, Object> vars = Map.of("customerName", name, "total", orderTotal);
String html = TemplateEngine.render("order-confirmation", vars);

// Call another service
String response = ServiceClient.get("http://inventory-service/api/stock/" + itemId);
```

## Building

```bash
mvn clean install
```

Requires Java 17+.
