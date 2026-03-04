# ValueProtect Deployment Guide (Azure)

This guide explains how to deploy the current ValueProtect project to Azure.
It is based on a scan of this repository (Spring Boot 3.4 + Java 21 + MySQL backend, React frontend).

## 1) What is in this project

## Backend
- Spring Boot 3.4.2 (Java 21)
- Maven build (JAR)
- MySQL with Spring Data JPA
- JWT auth
- Stripe webhook endpoint
- File uploads stored on local filesystem (`uploads/appraisal-documents`)

## Frontend
- React (Create React App)
- API base URL from `REACT_APP_API_URL` (build-time variable)

## Important observed settings in code
- Backend expects MySQL datasource values in `spring.datasource.*`
- JWT secret comes from `jwt.secret`
- File URLs use `app.file.base.url`
- CORS currently allows localhost origins only in `src/main/java/info/quazi/valueProtect/config/CorsConfig.java`

---

## 2) Recommended Azure architecture

- Azure App Service (Linux, Java 21) for backend API
- Azure Database for MySQL Flexible Server for database
- Azure Storage Static Website (or Azure Static Web Apps) for React frontend
- Azure Key Vault (recommended) for secrets

---

## 3) Prerequisites

- Azure subscription
- Azure CLI installed and logged in
- Java 21, Maven wrapper, Node 18+
- Access to initialize MySQL schema

Login:

```bash
az login
az account set --subscription "<SUBSCRIPTION_ID_OR_NAME>"
```

Define reusable variables (example):

```bash
# Bash (Git Bash / WSL)
RG=valueprotect-rg
LOCATION=eastus
MYSQL_SERVER=valueprotect-mysql
MYSQL_ADMIN=valueprotectadmin
MYSQL_DB=actpro
APP_PLAN=valueprotect-plan
BACKEND_APP=valueprotect-api
STORAGE=valueprotectfrontend$RANDOM
```

---

## 4) Create Azure resources

## 4.1 Resource group

```bash
az group create --name $RG --location $LOCATION
```

## 4.2 MySQL Flexible Server

```bash
az mysql flexible-server create \
  --resource-group $RG \
  --name $MYSQL_SERVER \
  --location $LOCATION \
  --admin-user $MYSQL_ADMIN \
  --admin-password "<STRONG_PASSWORD>" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --version 8.0 \
  --storage-size 32

az mysql flexible-server db create \
  --resource-group $RG \
  --server-name $MYSQL_SERVER \
  --database-name $MYSQL_DB
```

Allow Azure services + your IP during setup:

```bash
az mysql flexible-server firewall-rule create \
  --resource-group $RG \
  --name $MYSQL_SERVER \
  --rule-name AllowAzure \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0
```

## 4.3 App Service Plan + Backend Web App

```bash
az appservice plan create \
  --name $APP_PLAN \
  --resource-group $RG \
  --location $LOCATION \
  --is-linux \
  --sku B1

az webapp create \
  --resource-group $RG \
  --plan $APP_PLAN \
  --name $BACKEND_APP \
  --runtime "JAVA|21-java21"
```

---

## 5) Initialize database schema

Use your preferred MySQL client and execute scripts from this repo.
Suggested order:

1. `src/Script/appraisal_schema.sql`
2. `src/Script/create_stripe_webhook_events_table.sql`
3. Additional migration scripts only if needed by your data/state:
   - `src/Script/fix_appraisal_documents_table.sql`
   - `src/Script/fix_document_enum_values.sql`
   - `src/Script/add_company_type_to_company.sql`
   - `src/Script/add_lender_preferred_appraisal_companies.sql`
   - `src/Script/employee_company_user_role_relationship.sql`
   - `src/Script/standardize_all_roles.sql`

Note: Keep enum values uppercase with underscores for appraisal document types.

---

## 6) Deploy backend (Spring Boot)

## 6.1 Build artifact

From repository root:

```bash
./mvnw clean package -DskipTests
```

Artifact path:
- `target/valueProtect-0.0.1-SNAPSHOT.jar`

## 6.2 Configure backend app settings in Azure

Set all sensitive values in App Service settings (not in source file).

```bash
az webapp config appsettings set \
  --resource-group $RG \
  --name $BACKEND_APP \
  --settings \
  SPRING_DATASOURCE_URL="jdbc:mysql://$MYSQL_SERVER.mysql.database.azure.com:3306/$MYSQL_DB?createDatabaseIfNotExist=true&useSSL=true&requireSSL=true" \
  SPRING_DATASOURCE_USERNAME="$MYSQL_ADMIN" \
  SPRING_DATASOURCE_PASSWORD="<MYSQL_PASSWORD>" \
  SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver" \
  SPRING_JPA_HIBERNATE_DDL_AUTO="none" \
  SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT="org.hibernate.dialect.MySQLDialect" \
  JWT_SECRET="<VERY_LONG_RANDOM_SECRET>" \
  JWT_EXPIRATION="86400000" \
  APP_FILE_UPLOAD_DIRECTORY="/home/site/wwwroot/uploads/appraisal-documents" \
  APP_FILE_BASE_URL="https://$BACKEND_APP.azurewebsites.net" \
  STRIPE_API_KEY="<STRIPE_SECRET_KEY>" \
  STRIPE_WEBHOOK_SECRET="<STRIPE_WEBHOOK_SECRET>" \
  APP_PAYMENT_DEFAULT_FEE_CENTS="49900" \
  APP_PAYMENT_CURRENCY="usd" \
  APP_EMAIL_FROM="no-reply@valueprotect.com"
```

If email sending is required, also set:
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true`

## 6.3 Deploy JAR

```bash
az webapp deploy \
  --resource-group $RG \
  --name $BACKEND_APP \
  --src-path target/valueProtect-0.0.1-SNAPSHOT.jar \
  --type jar
```

Health checks:

```bash
curl https://$BACKEND_APP.azurewebsites.net/swagger-ui.html
curl https://$BACKEND_APP.azurewebsites.net/v3/api-docs
```

---

## 7) Deploy frontend (React)

Because this frontend uses Create React App, `REACT_APP_API_URL` must be set at build time.

## 7.1 Build frontend with production API URL

From `valueprotect-frontend`:

```bash
# Bash
export REACT_APP_API_URL="https://$BACKEND_APP.azurewebsites.net/api"
npm ci
npm run build
```

PowerShell:

```powershell
$env:REACT_APP_API_URL="https://<BACKEND_APP>.azurewebsites.net/api"
npm ci
npm run build
```

## 7.2 Create static website storage

```bash
az storage account create \
  --name $STORAGE \
  --resource-group $RG \
  --location $LOCATION \
  --sku Standard_LRS \
  --kind StorageV2

az storage blob service-properties update \
  --account-name $STORAGE \
  --static-website \
  --index-document index.html \
  --404-document index.html
```

Upload React build output:

```bash
az storage blob upload-batch \
  --account-name $STORAGE \
  --destination '$web' \
  --source valueprotect-frontend/build \
  --overwrite
```

Get frontend URL:

```bash
az storage account show \
  --name $STORAGE \
  --resource-group $RG \
  --query "primaryEndpoints.web" -o tsv
```

---

## 8) Required CORS update before production

Current CORS config is localhost-only. Update backend CORS to include your deployed frontend origin.

Current file:
- `src/main/java/info/quazi/valueProtect/config/CorsConfig.java`

Add your Azure frontend domain(s), for example:
- `https://<storage-name>.z13.web.core.windows.net`
- or your custom domain

Recommended improvement:
- Read allowed origins from an app setting/property (for example `app.cors.allowed-origins`) instead of hardcoding.

---

## 9) Stripe webhook configuration

Set Stripe webhook endpoint to:

- `https://<BACKEND_APP>.azurewebsites.net/api/webhooks/stripe`

Then copy Stripe signing secret into:
- `STRIPE_WEBHOOK_SECRET`

---

## 10) Production hardening checklist

- Remove hardcoded secrets from local `application.properties`
- Use Key Vault references in App Service settings
- Restrict MySQL firewall/network
- Enable App Service logs and Application Insights
- Configure custom domain + HTTPS for frontend/backend
- Add backup/retention policy for MySQL

---

## 11) Quick validation flow

1. Open frontend URL
2. Login with a valid user
3. Verify appraisals load without CORS/auth errors
4. Create appraisal
5. Upload and download document
6. Test Stripe checkout and webhook callback

---

## 12) Common Azure deployment issues

## 401/403 from API
- Check JWT token in browser storage
- Confirm backend auth endpoints are reachable

## CORS blocked in browser
- Add frontend domain to backend allowed origins

## Frontend calls localhost API
- Rebuild frontend with correct `REACT_APP_API_URL`

## File upload/download path errors
- Verify `APP_FILE_UPLOAD_DIRECTORY` exists and is writable
- Keep `APP_FILE_BASE_URL` equal to backend public URL

## MySQL SSL/connection errors
- Ensure correct username/password and firewall rule
- Keep JDBC URL SSL flags enabled

---

## 13) Optional next improvement

For long-term stability, migrate document storage from local App Service filesystem to Azure Blob Storage and store blob URLs in database.