# Stock Management System

A small teaching/demo monorepo for a Rwanda Polytechnic DevOps assignment. It contains a Spring Boot API, a React/Vite frontend, and GitHub Actions CI integrated with Jira. The app demonstrates registration, login, logout, and a sample stock dashboard; it is not a production inventory or authentication system.

## Project structure

```text
.
├── .github/workflows/ci.yml
├── backend/
│   ├── pom.xml
│   └── src/
└── frontend/
    ├── index.html
    ├── package.json
    └── src/
```

### Create the directory tree

The project is already scaffolded in this workspace. To create the same directories and empty file paths from a fresh project root, run:

```bash
cd /var/www/html/Jira_stock_management_automation
mkdir -p .github/workflows \
  backend/src/main/java/com/example/stockmanagement/{config,user,stock} \
  backend/src/main/resources \
  backend/src/test/java/com/example/stockmanagement/user \
  backend/src/test/resources \
  frontend/src
touch .gitignore README.md \
  .github/workflows/ci.yml \
  backend/pom.xml \
  backend/src/main/java/com/example/stockmanagement/StockManagementApplication.java \
  backend/src/main/java/com/example/stockmanagement/config/SecurityConfig.java \
  backend/src/main/java/com/example/stockmanagement/config/PasswordConfig.java \
  backend/src/main/java/com/example/stockmanagement/user/AppUser.java \
  backend/src/main/java/com/example/stockmanagement/user/AppUserRepository.java \
  backend/src/main/java/com/example/stockmanagement/user/AuthController.java \
  backend/src/main/java/com/example/stockmanagement/stock/StockItem.java \
  backend/src/main/java/com/example/stockmanagement/stock/StockController.java \
  backend/src/main/resources/application.properties \
  backend/src/test/java/com/example/stockmanagement/user/AuthControllerTest.java \
  backend/src/test/resources/application.properties \
  frontend/index.html frontend/package.json frontend/vite.config.js \
  frontend/src/main.jsx frontend/src/api.js frontend/src/App.jsx frontend/src/style.css
```

The brace expansion in `mkdir -p` creates `config`, `user`, and `stock` as sibling Java packages. `touch` only creates missing files; it does not populate them with source code.

## Run locally

Requirements: Java 17+, Maven 3.8+, PostgreSQL 14+, Node.js 20.19+ (or 22.12+), and npm.

Create a local database and application role (replace the development password with your own):

```bash
sudo -u postgres psql
```

In the PostgreSQL prompt:

```sql
CREATE USER stock_user WITH PASSWORD 'change_me';
CREATE DATABASE stock_management OWNER stock_user;
\q
```

Set the connection settings to match your PostgreSQL server. Do not commit real passwords:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/stock_management'
export DB_USERNAME='stock_user'
export DB_PASSWORD='change_me'
```

Run the backend from the project root:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. The API runs on <http://localhost:8081> and connects to PostgreSQL using `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The default connection settings target a local database named `stock_management`; set a real password in your environment before starting the backend. Hibernate updates the schema automatically for this assignment demo.

The automated backend tests use an isolated in-memory H2 database so `mvn clean test` does not require PostgreSQL to be running. The application itself uses PostgreSQL.

## Authentication and dashboard

- Register with a username, email address, and password of at least 8 characters.
- Login with email and password; logout clears the server-side HTTP session.
- The dashboard calls `GET /api/auth/me` and `GET /api/stock`.
- Sessions use an HTTP-only server session cookie. Spring Security CSRF protection is enabled; the frontend fetches a CSRF token before each state-changing request.
- The sample stock endpoint returns illustrative data and is not connected to a stock database.

This is starter code for a class demonstration. Before production use, add persistent storage, HTTPS, rate limiting, account recovery, stronger validation, and deployment-specific CORS/CSRF settings.

## GitHub Actions CI

`.github/workflows/ci.yml` runs:

1. `mvn clean test` on Java 17.
2. `npm ci` and `npm run build` on Node.js 20.

Both jobs run on pushes to `main` and `feature/**`, and on pull requests targeting `main`. The Jira transition job runs only after a merged pull request has passed both build jobs. It looks for a Jira key such as `SMS-2` in the PR title, source branch, or PR description and transitions that issue to the Jira status named `Done`.

### Add the repository to GitHub

If this folder is not already a Git repository, run:

```bash
git init
git branch -M main
git add .
git commit -m "SMS-2: add stock management CI demo"
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

Replace the remote URL with your repository URL. If a remote or Git repository already exists, keep it and use its configured remote instead of repeating `git init` or `git remote add`.

### Link Jira and GitHub

1. In Jira Cloud, open **Apps → Explore more apps** (or **Apps → GitHub for Jira**) and install the official **GitHub for Jira** app if your site administrator permits it.
2. In the app configuration, connect the GitHub account/organization and select the repository. This makes branches, commits, and pull requests with Jira issue keys visible on Jira issues. If your school uses Jira Data Center or a different integration, follow that installation's repository-linking instructions.
3. Use the same Jira project key in branch names, PR titles, and commit messages, for example `SMS-2`.
4. In GitHub, open **Settings → Secrets and variables → Actions**. Add:
   - Secret `JIRA_BASE_URL`: Jira site URL, e.g. `https://your-site.atlassian.net` (no trailing slash).
   - Secret `JIRA_USER_EMAIL`: the Jira account email used for API authentication.
   - Secret `JIRA_API_TOKEN`: an Atlassian API token for that account.
   - Variable `JIRA_TRANSITIONS_ENABLED`: `true`.
5. In Jira, confirm the integration account can browse the project and transition issues. Confirm the issue workflow has an available transition into a status whose name is `Done`. Jira transition IDs vary by project; this workflow discovers the available transition by its destination status name.
6. Merge the workflow file to `main`, then create a PR whose title contains the issue key, for example `SMS-2 Add authentication demo`. A failed backend test or frontend build causes the Jira job to be skipped, so CI does not transition the issue to Done. On a successful merge, GitHub Actions calls Jira's transition API.

Do not treat a commit message such as `SMS-2 #done` as CI gating. Smart Commits can update Jira when the commit is processed, regardless of whether a later workflow passes. They are useful for comments/work logs and manual demonstrations, but the workflow above is the safer mechanism when completion must depend on green CI. Users with permission may still transition an issue manually unless the Jira workflow is configured to restrict that transition.

If Jira secrets/variable are not configured, the Jira job is skipped; the Maven and frontend CI jobs still run.

## Demonstration workflow and screenshots

Use an existing Jira issue such as `SMS-2`, or create one and substitute its key in the commands below.

```bash
git switch -c feature/SMS-2-auth-ci
# Make a small change, such as updating the dashboard heading.
git add .
git commit -m "SMS-2: demonstrate auth and CI"
git push -u origin feature/SMS-2-auth-ci
```

Then:

1. In GitHub, open **Actions** and capture the successful backend test and frontend build jobs.
2. Open a pull request from `feature/SMS-2-auth-ci` to `main`. Use a title containing `SMS-2`, e.g. `SMS-2 Add auth and CI demo`.
3. Capture the PR checks showing both jobs green. To demonstrate the failure gate, make a temporary failing test or build, push it, and capture the red check and the skipped Jira transition; remove the deliberate failure before merging.
4. Merge the green PR. Capture the successful Jira transition job and the Jira issue history showing its move to Done.
5. For the app demonstration, start backend and frontend as shown above; capture the register form, dashboard after registration/login, and the logged-out state.

The `SMS-2` key shown in this guide is an example. Jira issue keys and workflow statuses must exist in your own Jira project.
