# Document Management System (DMS)

A simple backend for managing workspaces, folders, and documents.  
Built using **Spring Boot 3**, **MongoDB**, **JWT Authentication**, and local file storage.

---

## 🚀 How to Run

### 1. Start MongoDB (Docker example)
```bash
docker run -d --name dev-mongo -p 27017:27017 -e MONGO_INITDB_DATABASE=dms mongo:6.0
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

Server runs at:
```
http://localhost:8080
```

Default seeded user:
```
email: test@local
password: password123
```

---

## 🔑 Auth Endpoints

### Register
```
POST /api/auth/register
```
```json
{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User"
}
```

### Login
```
POST /api/auth/login
```
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

Use token:
```
Authorization: Bearer <token>
```

---

## 🗂️ Workspace Endpoints

### Create workspace
```
POST /api/workspaces
```
```json
{
  "name": "MyWorkspace"
}
```

### List workspaces
```
GET /api/workspaces
```

### Get workspace by ID
```
GET /api/workspaces/{id}
```

### Update workspace
```
PUT /api/workspaces/{id}
```
```json
{
  "name": "NewName"
}
```

### Delete workspace
```
DELETE /api/workspaces/{id}
```

---

## 📁 Folder Endpoints

### Create folder
```
POST /api/folders
```
```json
{
  "workspaceId": "workspace-id",
  "parentId": null,
  "name": "Documents"
}
```

### List folders
```
GET /api/folders?workspaceId=xxx
```

---

## 📄 Document Endpoints

### Upload document
```
POST /api/documents/upload
```
Form-data:
- workspaceId
- folderId
- file (binary)
- tags (optional)

### List documents
```
GET /api/documents?workspaceId=xxx&folderId=yyy
```

### Get document
```
GET /api/documents/{id}
```

### Download
```
GET /api/documents/{id}/download
```

### Preview (base64)
```
GET /api/documents/{id}/preview
```

### Update metadata
```
PATCH /api/documents/{id}
```
```json
{
  "tags": ["pdf", "invoice"],
  "version": 2
}
```

### Soft delete
```
DELETE /api/documents/{id}
```

---