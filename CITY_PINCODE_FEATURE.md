# City and Pincode Management Feature

## Overview
This feature allows administrators to manage cities and their associated pincodes in the NukkadSeva platform. The feature includes complete CRUD operations for cities and pincodes.

## Components Created

### 1. Entity Classes
- **City.java** - Represents a city with its properties
- **Pincode.java** - Represents a pincode associated with a city

### 2. Repository Interfaces
- **CityRepository.java** - Data access layer for City entity
- **PincodeRepository.java** - Data access layer for Pincode entity

### 3. Service Layer
- **CityService.java** - Service interface for city operations
- **CityServiceImpl.java** - Implementation of city service

### 4. DTOs
- **CityWithPincodesRequest.java** - Request DTO for creating/updating cities with pincodes
- **CityWithPincodesResponse.java** - Response DTO for city and pincode data

### 5. Database Migration
- **v6-create-city-pincode-tables.json** - Liquibase changelog for creating city and pincode tables

## API Endpoints

All endpoints require **ADMIN** role authorization.

### 1. Add New City with Pincodes
**POST** `/api/admin/cities`

**Request Body:**
```json
{
  "cityName": "DELHI",
  "state": "Delhi",
  "pincodes": [
    {
      "pincode": "110001",
      "areaName": "Connaught Place"
    },
    {
      "pincode": "110002",
      "areaName": "Daryaganj"
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "City and pincodes added successfully",
  "data": {
    "id": 1,
    "cityName": "DELHI",
    "state": "Delhi",
    "isActive": true,
    "pincodes": [
      {
        "id": 1,
        "pincode": "110001",
        "areaName": "Connaught Place",
        "isActive": true
      },
      {
        "id": 2,
        "pincode": "110002",
        "areaName": "Daryaganj",
        "isActive": true
      }
    ],
    "createdAt": "2026-02-19T10:30:00",
    "updatedAt": "2026-02-19T10:30:00"
  }
}
```

### 2. Get All Cities with Pincodes
**GET** `/api/admin/cities`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "cityName": "DELHI",
    "state": "Delhi",
    "isActive": true,
    "pincodes": [
      {
        "id": 1,
        "pincode": "110001",
        "areaName": "Connaught Place",
        "isActive": true
      }
    ],
    "createdAt": "2026-02-19T10:30:00",
    "updatedAt": "2026-02-19T10:30:00"
  }
]
```

### 3. Get City by ID
**GET** `/api/admin/cities/{cityId}`

**Response (200 OK):**
```json
{
  "id": 1,
  "cityName": "DELHI",
  "state": "Delhi",
  "isActive": true,
  "pincodes": [...],
  "createdAt": "2026-02-19T10:30:00",
  "updatedAt": "2026-02-19T10:30:00"
}
```

### 4. Add Pincodes to Existing City
**POST** `/api/admin/cities/{cityId}/pincodes`

**Request Body:**
```json
[
  {
    "pincode": "110003",
    "areaName": "Kashmere Gate"
  }
]
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Pincodes added successfully",
  "data": {
    "id": 1,
    "cityName": "DELHI",
    "pincodes": [...]
  }
}
```

### 5. Toggle City Active Status
**PATCH** `/api/admin/cities/{cityId}/status`

**Request Body:**
```json
{
  "isActive": false
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "City status updated successfully",
  "data": {
    "id": 1,
    "cityName": "DELHI",
    "isActive": false,
    ...
  }
}
```

### 6. Delete City
**DELETE** `/api/admin/cities/{cityId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "City deleted successfully"
}
```

## Database Schema

### City Table
| Column     | Type                      | Constraints                    |
|------------|---------------------------|--------------------------------|
| city_id    | BIGINT                    | PRIMARY KEY, AUTO_INCREMENT    |
| city_name  | VARCHAR(100)              | NOT NULL, UNIQUE               |
| state      | VARCHAR(100)              | NOT NULL                       |
| is_active  | BOOLEAN                   | NOT NULL, DEFAULT TRUE         |
| created_at | TIMESTAMP WITHOUT TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP WITHOUT TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### Pincode Table
| Column     | Type                      | Constraints                    |
|------------|---------------------------|--------------------------------|
| pincode_id | BIGINT                    | PRIMARY KEY, AUTO_INCREMENT    |
| pincode    | VARCHAR(10)               | NOT NULL                       |
| area_name  | VARCHAR(200)              |                                |
| city_id    | BIGINT                    | NOT NULL, FOREIGN KEY (city)   |
| is_active  | BOOLEAN                   | NOT NULL, DEFAULT TRUE         |
| created_at | TIMESTAMP WITHOUT TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP WITHOUT TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Constraints:**
- Unique constraint on (pincode, city_id)
- Foreign key from pincode.city_id to city.city_id with CASCADE DELETE
- Index on pincode.city_id for better query performance

## Features

1. **Automatic Uppercase Conversion**: City names are automatically converted to uppercase for consistency
2. **Duplicate Prevention**: The system prevents adding duplicate cities or duplicate pincodes for the same city
3. **Cascade Delete**: Deleting a city automatically deletes all associated pincodes
4. **Active/Inactive Status**: Cities can be marked as active or inactive without deleting them
5. **Validation**: Input validation using Jakarta Bean Validation annotations
6. **Error Handling**: Comprehensive error handling with meaningful error messages

## Testing the Endpoints

You can test these endpoints using tools like Postman or cURL. Make sure to:

1. Authenticate as an admin user
2. Include the JWT token in the Authorization header
3. Set Content-Type header to `application/json` for POST/PATCH requests

### Example cURL Command:
```bash
curl -X POST http://localhost:8080/api/admin/cities \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cityName": "Mumbai",
    "state": "Maharashtra",
    "pincodes": [
      {"pincode": "400001", "areaName": "Churchgate"},
      {"pincode": "400002", "areaName": "Kalbadevi"}
    ]
  }'
```

## Running the Application

1. Make sure your database is properly configured in `application.yml`
2. Run the application - Liquibase will automatically create the tables
3. The endpoints will be available at `/api/admin/cities`

## Future Enhancements

Potential improvements for this feature:
- Bulk import/export of cities and pincodes via CSV
- Search and filter functionality
- Pagination for large datasets
- Analytics on city/pincode usage
- Integration with external pincode validation APIs

