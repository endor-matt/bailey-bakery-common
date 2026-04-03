# Bailey Bakery Common — Cross-Repo Vulnerability Answer Key

This documents all vulnerabilities in the shared library that are **only exploitable when called from the main app** (`bailey-bakery-shop`). Each entry shows the taint flow across the repo boundary.

## Cross-Repo Taint Flows (Vulnerable)

### 1. SQL Injection via QueryHelper
- **CWE:** CWE-89 (SQL Injection)
- **Library sink:** `common/query/QueryHelper.java` — `where()` (line ~48) and `search()` (line ~62) concatenate values directly into SQL
- **Main app source:** `controller/PlatformController.java` — `generateReport()` passes `filterValue` and `searchTerm` from `@RequestParam`
- **Main app bridge:** `service/PlatformService.java` — `runReport()` passes user input to `QueryHelper.where()` and `QueryHelper.search()`
- **Cross-repo flow:** `HTTP request param → PlatformController.generateReport() → PlatformService.runReport() → QueryHelper.where()/search() → SQL string`
- **Scan type:** SAST (cross-repo)

### 2. Server-Side Template Injection via TemplateEngine
- **CWE:** CWE-94 (Code Injection) / SSTI
- **Library sink:** `common/template/TemplateEngine.java` — `render()` (line ~43) evaluates `${expression}` via JavaScript ScriptEngine
- **Main app source:** `controller/PlatformController.java` — `renderTemplate()` accepts user-supplied `template` in POST body
- **Main app bridge:** `service/PlatformService.java` — `renderTemplate()` passes user template to `TemplateEngine.render()`
- **Cross-repo flow:** `HTTP POST body → PlatformController.renderTemplate() → PlatformService.renderTemplate() → TemplateEngine.render() → ScriptEngine.eval()`
- **Scan type:** SAST (cross-repo)

### 3. SSRF via ServiceClient
- **CWE:** CWE-918 (Server-Side Request Forgery)
- **Library sink:** `common/http/ServiceClient.java` — `get()` (line ~31) makes HTTP request to any URL without validation
- **Main app source:** `controller/PlatformController.java` — `proxyRequest()` accepts user-supplied `url` from `@RequestParam`
- **Main app bridge:** `service/PlatformService.java` — `fetchFromService()` passes URL to `ServiceClient.get()`
- **Cross-repo flow:** `HTTP request param → PlatformController.proxyRequest() → PlatformService.fetchFromService() → ServiceClient.get() → HttpClient.execute()`
- **Scan type:** SAST (cross-repo)

### 4. XXE via ConfigParser
- **CWE:** CWE-611 (XXE — Improper Restriction of XML External Entity Reference)
- **Library sink:** `common/config/ConfigParser.java` — `parseXml()` (line ~32) uses DocumentBuilderFactory without disabling external entities
- **Main app source:** `controller/PlatformController.java` — `importSupplierConfig()` accepts raw XML in POST body
- **Main app bridge:** `service/PlatformService.java` — `parseSupplierConfig()` passes XML to `ConfigParser.parseXml()`
- **Cross-repo flow:** `HTTP POST body (XML) → PlatformController.importSupplierConfig() → PlatformService.parseSupplierConfig() → ConfigParser.parseXml() → DocumentBuilder.parse()`
- **Scan type:** SAST (cross-repo)

### 5. Path Traversal via AssetLoader
- **CWE:** CWE-22 (Path Traversal)
- **Library sink:** `common/asset/AssetLoader.java` — `load()` (line ~30) concatenates user path with `Paths.get(basePath, relativePath)` without normalization/validation
- **Main app source:** `controller/PlatformController.java` — `getAsset()` accepts user-supplied path via `@PathVariable`
- **Main app bridge:** `service/PlatformService.java` — `loadAsset()` passes path to `AssetLoader.load()`
- **Cross-repo flow:** `HTTP path variable → PlatformController.getAsset() → PlatformService.loadAsset() → AssetLoader.load() → Files.readAllBytes()`
- **Scan type:** SAST (cross-repo)

### 6. Unsafe Deserialization via DataCodec
- **CWE:** CWE-502 (Deserialization of Untrusted Data)
- **Library sink:** `common/codec/DataCodec.java` — `decode()` (line ~41) uses `ObjectInputStream.readObject()` without type filtering
- **Main app source:** `controller/PlatformController.java` — `decodeCachedData()` accepts Base64 string in POST body
- **Main app bridge:** `service/PlatformService.java` — `decodeCachedData()` passes encoded string to `DataCodec.decode()`
- **Cross-repo flow:** `HTTP POST body → PlatformController.decodeCachedData() → PlatformService.decodeCachedData() → DataCodec.decode() → ObjectInputStream.readObject()`
- **Scan type:** SAST (cross-repo)

### 7. Command Injection via PdfGenerator
- **CWE:** CWE-78 (OS Command Injection)
- **Library sink:** `common/pdf/PdfGenerator.java` — `generate()` (line ~32) passes `outputPath` to `Runtime.exec()` via string concatenation
- **Main app source:** `controller/PlatformController.java` — `generateReceipt()` accepts `orderId` in POST body (used in output path)
- **Main app bridge:** `service/PlatformService.java` — `generateReceiptPdf()` constructs output path with `orderId` and passes to `PdfGenerator.generate()`
- **Cross-repo flow:** `HTTP POST body → PlatformController.generateReceipt() → PlatformService.generateReceiptPdf() → PdfGenerator.generate() → Runtime.exec()`
- **Scan type:** SAST (cross-repo)

### 8. LDAP Injection via DirectoryClient
- **CWE:** CWE-90 (LDAP Injection)
- **Library sink:** `common/directory/DirectoryClient.java` — `searchStaff()` (line ~37) concatenates search term into LDAP filter string
- **Main app source:** `controller/PlatformController.java` — `searchStaff()` accepts user-supplied `query` from `@RequestParam`
- **Main app bridge:** `service/PlatformService.java` — `searchStaffDirectory()` passes query to `DirectoryClient.searchStaff()`
- **Cross-repo flow:** `HTTP request param → PlatformController.searchStaff() → PlatformService.searchStaffDirectory() → DirectoryClient.searchStaff() → ctx.search(filter)`
- **Scan type:** SAST (cross-repo)

### 9. Weak Token Generation via TokenGenerator
- **CWE:** CWE-330 (Use of Insufficiently Random Values) + CWE-328 (Use of Weak Hash — MD5)
- **Library sink:** `common/token/TokenGenerator.java` — `generateResetToken()` (line ~24) uses MD5 hash of userId + predictable 10-minute time window
- **Main app source:** `controller/PlatformController.java` — `generateResetToken()` accepts `userId` from POST body
- **Main app bridge:** `service/PlatformService.java` — `generateResetToken()` calls `TokenGenerator.generateResetToken()`
- **Cross-repo flow:** `HTTP POST body → PlatformController.generateResetToken() → PlatformService.generateResetToken() → TokenGenerator.generateResetToken() → MD5(userId + timeWindow)`
- **Scan type:** SAST (cross-repo)

### 10. Log Injection via AuditLogger
- **CWE:** CWE-117 (Improper Output Neutralization for Logs)
- **Library sink:** `common/audit/AuditLogger.java` — `logEvent()` (line ~26) writes unsanitized details directly to log
- **Main app bridge:** `service/PlatformService.java` — multiple methods pass user-controlled data in the `details` parameter (e.g., `runReport()` logs the SQL query, `fetchFromService()` logs the URL)
- **Cross-repo flow:** `HTTP params → PlatformService methods → AuditLogger.logEvent(details) → log.info()`
- **Scan type:** SAST (cross-repo)

## False Positive Bait (Safe Implementations)

These classes in the library use secure patterns. Scanners should NOT flag them:

| Class | Pattern | Why Safe |
|-------|---------|----------|
| `SafeQueryHelper` | Parameterized queries with `?` placeholders | Values never concatenated into SQL |
| `SafeConfigParser` | XXE protections enabled | `disallow-doctype-decl`, external entities disabled |
| `SafeAssetLoader` | Path normalization + `startsWith()` check | Traversal attempts blocked |
| `InputValidator` | URL allowlist, path validation, log sanitization | Reusable validation utilities |
| `SecureTokenGenerator` | `SecureRandom` + Base64 | Cryptographically random tokens |

## Summary

| # | CWE | Vulnerability | Library Class | Reachable From Main App |
|---|-----|--------------|---------------|------------------------|
| 1 | CWE-89 | SQL Injection | QueryHelper | Yes — PlatformController.generateReport() |
| 2 | CWE-94 | SSTI / Code Injection | TemplateEngine | Yes — PlatformController.renderTemplate() |
| 3 | CWE-918 | SSRF | ServiceClient | Yes — PlatformController.proxyRequest() |
| 4 | CWE-611 | XXE | ConfigParser | Yes — PlatformController.importSupplierConfig() |
| 5 | CWE-22 | Path Traversal | AssetLoader | Yes — PlatformController.getAsset() |
| 6 | CWE-502 | Unsafe Deserialization | DataCodec | Yes — PlatformController.decodeCachedData() |
| 7 | CWE-78 | Command Injection | PdfGenerator | Yes — PlatformController.generateReceipt() |
| 8 | CWE-90 | LDAP Injection | DirectoryClient | Yes — PlatformController.searchStaff() |
| 9 | CWE-330 | Weak Token/Crypto | TokenGenerator | Yes — PlatformController.generateResetToken() |
| 10 | CWE-117 | Log Injection | AuditLogger | Yes — multiple PlatformService methods |

**Total: 10 cross-repo vulnerabilities across 10 CWEs, with 5 false positive bait classes.**
