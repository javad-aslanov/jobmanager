# README

This repository contains a **Job Manager** application consisting of a **Java gRPC backend service**, a **Vue.js frontend application**, and an **Envoy Proxy** configuration to facilitate communication between the frontend and backend services.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setting Up the Backend Service](#setting-up-the-backend-service)
    - [1. Navigate to the Backend Directory](#1-navigate-to-the-backend-directory)
    - [2. Build the Backend Service](#2-build-the-backend-service)
    - [3. Run the Backend Service](#3-run-the-backend-service)
- [Setting Up the Frontend Application](#setting-up-the-frontend-application)
    - [1. Navigate to the Frontend Directory](#1-navigate-to-the-frontend-directory)
    - [2. Install Dependencies](#2-install-dependencies)
    - [3. Configure the Frontend](#3-configure-the-frontend)
    - [4. Run the Frontend Application](#4-run-the-frontend-application)
- [Setting Up the Envoy Proxy](#setting-up-the-envoy-proxy)
    - [1. Install Envoy](#1-install-envoy)
    - [2. Verify Envoy Installation](#2-verify-envoy-installation)
    - [3. Configure Envoy](#3-configure-envoy)
    - [4. Run Envoy](#4-run-envoy)
---

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java Development Kit (JDK) 8 or higher**
- **Maven** (for building the Java backend service)
- **Node.js and npm** (for running the Vue.js frontend application)
- **Envoy Proxy** (we'll cover installation steps)
- **Git** (to clone the repository, if needed)

---

## Project Structure

Your folder structure should look like this:

```
.
├── jobmanager-backend/     # Java gRPC backend service
├── jobmanager-frontend/    # Vue.js frontend application
└── envoy.yaml              # Envoy proxy configuration
```

---

## Setting Up the Backend Service

### 1. Navigate to the Backend Directory

Open your terminal and navigate to the `jobmanager-backend` directory:

```bash
cd jobmanager-backend
```

### 2. Build the Backend Service

Ensure you have Maven installed. Then, build the project using:

```bash
mvn clean install
```

This command will compile the code and package it into an executable JAR file.

### 3. Run the Backend Service

Start the gRPC backend service with the following command:

```bash
mvn exec:java -Dexec.mainClass="com.example.jobmanager.JobManagerServer"
```

**Notes:**

- Replace `com.example.jobmanager.JobManagerServer` with the actual main class of your backend application if it's different.
- The backend service listens on **port 8080** by default.
- **This service must be run first** to ensure it is ready to receive requests.

---

## Setting Up the Frontend Application

### 1. Navigate to the Frontend Directory

Open a new terminal window or tab, and navigate to the `jobmanager-frontend` directory:

```bash
cd jobmanager-frontend
```

### 2. Install Dependencies

Ensure you have Node.js and npm installed. Then, install the required dependencies:

```bash
npm install
```

### 3. Configure the Frontend

By default, the Vue.js development server runs on **port 8080**, which may conflict with the backend service. We'll change the frontend to run on **port 8081**.

1. **Create a `vue.config.js` file** in the `jobmanager-frontend` directory if it doesn't exist:

   ```bash
   touch vue.config.js
   ```

2. **Add the following content** to `vue.config.js`:

   ```javascript
   module.exports = {
     devServer: {
       port: 8081,
     },
   };
   ```

3. **Update the API Base URL in the Frontend Code:**

   Ensure that your frontend application is configured to send API requests to **http://localhost:8085/**, where Envoy will be listening.

    - If you have an environment configuration file (e.g., `.env.development`), set the API base URL:

      ```
      VUE_APP_API_BASE_URL=http://localhost:8085/
      ```

    - Alternatively, update the API endpoint in your code to use `http://localhost:8085/`.

### 4. Run the Frontend Application

Start the Vue.js development server:

```bash
npm run serve
```

The frontend application will now be accessible at **http://localhost:8081**.

---

## Setting Up the Envoy Proxy

### 1. Install Envoy

#### macOS

Install Envoy using Homebrew:

```bash
brew install envoy
```

#### Windows and Linux

Refer to the [official Envoy installation guide](https://www.envoyproxy.io/docs/envoy/latest/start/install) for instructions specific to your operating system.

### 2. Verify Envoy Installation

Check that Envoy is installed correctly:

```bash
envoy --version
```

You should see output similar to:

```
envoy 1.x.x (commit xxxxxxx, built YYYY-MM-DD)
```

### 3. Configure Envoy

Ensure your `envoy.yaml` file contains the following configuration:

```yaml
static_resources:
  listeners:
    - name: listener_0
      address:
        socket_address:
          address: 0.0.0.0
          port_value: 8085  # Port where Envoy listens for incoming gRPC-Web requests
      filter_chains:
        - filters:
            - name: envoy.filters.network.http_connection_manager
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                stat_prefix: ingress_http
                codec_type: AUTO
                route_config:
                  name: local_route
                  virtual_hosts:
                    - name: backend
                      domains: ["*"]
                      routes:
                        - match:
                            prefix: "/"
                          route:
                            cluster: java_backend
                            cors:
                              allow_origin_string_match:
                                - exact: "http://localhost:8081"  # Matches your frontend
                              allow_methods: "GET, POST, OPTIONS"
                              allow_headers: "content-type, x-grpc-web, authorization, x-user-agent"
                              expose_headers: "grpc-status, grpc-message"
                              max_age: "86400"
                              allow_credentials: true
                http_filters:
                  - name: envoy.filters.http.cors
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.cors.v3.Cors
                  - name: envoy.filters.http.grpc_web
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_web.v3.GrpcWeb
                  - name: envoy.filters.http.router
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
  clusters:
    - name: java_backend
      connect_timeout: 0.5s
      type: LOGICAL_DNS
      lb_policy: ROUND_ROBIN
      load_assignment:
        cluster_name: java_backend
        endpoints:
          - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: localhost  # Address of your Java backend
                      port_value: 8080    # Port where your Java backend is listening
      http2_protocol_options: {}  # Enable HTTP/2 for the backend without TLS
```

**Important Configuration Details:**

- **Envoy Listener Port:** 8085
- **Frontend Origin:** `http://localhost:8081`
- **Backend Address and Port:** `localhost:8080`

### 4. Run Envoy

From the root directory of your project (where `envoy.yaml` is located), run Envoy:

```bash
envoy -c envoy.yaml
```