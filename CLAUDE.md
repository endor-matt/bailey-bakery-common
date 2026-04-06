# Bailey Bakery Common — Intentionally Vulnerable Shared Library

## What This Is

A companion shared library for `bailey-bakery-shop`. Contains intentionally vulnerable utility classes designed to demonstrate **cross-repo vulnerability detection** in Endor Labs' AURI SAST scanner.

## Architecture

Each utility class exposes methods that look safe in isolation but become exploitable when the main app (`bailey-bakery-shop`) passes unsanitized user input through them.

- **Vulnerable sink classes (shop → common):** TemplateEngine, ServiceClient, DataCodec, QueryHelper, AssetLoader, ConfigParser, PdfGenerator, TokenGenerator, AuditLogger, DirectoryClient
- **Attacker-influenced source classes (common → shop):** DataFeedClient (fetches external API data), WebhookStore (stores/returns attacker-sent webhook payloads)
- **Safe classes (FP bait):** SafeQueryHelper, SafeConfigParser, SafeAssetLoader, InputValidator, SecureTokenGenerator

## Key Rules

- **Java 17** required
- **Must compile independently** — `mvn compile`
- **Must be consumable** via `mvn install` to local repo
- **ANSWER_KEY.md** documents all cross-repo taint flows
- **GroupId:** `com.baileybakery`, **ArtifactId:** `bailey-bakery-common`

## How to Use with Main App

```bash
# Install to local Maven repo
cd bailey-bakery-common && mvn install

# Main app already has the dependency in pom.xml
cd bailey-bakery-shop && mvn compile
```
