# **Luidium CloudSync**

Luidium CloudSync is a real-time synchronization service that connects local directories to S3-compatible cloud storage using MinIO and PostgreSQL. It ensures seamless file synchronization and management between your local system and the cloud.

## **Features**

- **Real-time Synchronization**: Automatically sync files between local directories and cloud storage.
- **S3-Compatible Support**: Works with any S3-compatible storage, including MinIO.
- **Dynamic Configurations**: Manage connections between directories and cloud buckets through REST APIs.
- **Database Integration**: PostgreSQL is used for managing active connections and synchronization states.
- **Event-Driven Processing**: Utilizes MinIO webhook events for efficient sync operations.

## **Installation**

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/luidium-cloudsync.git
   cd luidium-cloudsync
   ```

2. Build the project:

   ```bash
   ./gradlew build
   ```

3. Set up the database schema:
   ```bash
   psql -U dbuser -d cloudsync -f src/main/resources/init.sql
   ```

## **Configuration**

The application reads its configurations from an external `config.yaml` file.

### **Default Configuration File**

By default, the configuration file is expected to be located at:

```
~/.luidium-cloudsync/config.yaml
```

The default `config.yaml` file should look like this:

```yaml
database:
  url: jdbc:postgresql://localhost:5432/cloudsync
  username: dbuser
  password: dbpassword

minio:
  endpoint: http://localhost:9000
  access-key: <ACCESS_KEY>
  secret-key: <SECRET_KEY>
```

## **Usage**

1. Start the application:

   ```bash
   ./gradlew bootRun
   ```

2. Access the REST API (default: `http://localhost:8080`) to manage connections and monitor sync operations.

## **REST API Endpoints**

### **1. Create a new connection**

- **Endpoint**: `POST /connections`
- **Payload**:
  ```json
  {
    "directoryPath": "/path/to/local/directory",
    "bucketName": "my-bucket"
  }
  ```

### **2. Get all connections**

- **Endpoint**: `GET /connections`

### **3. Update connection status**

- **Endpoint**: `PUT /connections/{id}`
- **Payload**:
  ```json
  {
    "isActive": false
  }
  ```

### **4. Delete a connection**

- **Endpoint**: `DELETE /connections/{id}`

## **License**

This project is licensed under the MIT License. See the `LICENSE` file for details.
