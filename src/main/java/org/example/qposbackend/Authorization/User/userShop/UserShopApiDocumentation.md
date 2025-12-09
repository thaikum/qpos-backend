# UserShop API Documentation

## Overview
The UserShop API provides CRUD operations for managing user-shop associations. All operations are automatically scoped to the current authenticated user's shop using the SpringSecurityAuditorAware component.

## Base URL
```
/user-shops
```

## Authentication
All endpoints require authentication. The system uses JWT tokens and the SpringSecurityAuditorAware to automatically determine the current user's shop context.

## Endpoints

### 1. Create UserShop Association
**POST** `/user-shops`

Creates a new user-shop association for the current authenticated user's shop.

**Request Body:**
```json
{
  "userId": 1,
  "roleId": "OWNER",
  "isDefault": false
}
```

**Response:**
```json
{
  "data": {
    "id": 1,
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "enabled": true
    },
    "shop": {
      "id": 1,
      "name": "My Shop",
      "code": "SHOP001",
      "active": true
    },
    "role": {
      "name": "OWNER",
      "privileges": [...]
    },
    "isDefault": false,
    "isActive": true,
    "createdAt": "2023-12-01T10:00:00Z",
    "updatedAt": null
  },
  "message": null
}
```

### 2. Get All UserShops
**GET** `/user-shops`

Retrieves all user-shop associations for the current authenticated user's shop.

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "user": {...},
      "shop": {...},
      "role": {...},
      "isDefault": false,
      "isActive": true,
      "createdAt": "2023-12-01T10:00:00Z",
      "updatedAt": null
    }
  ],
  "message": null
}
```

### 3. Get UserShop by ID
**GET** `/user-shops/{id}`

Retrieves a specific user-shop association by ID (only if it belongs to the current user's shop).

**Response:**
```json
{
  "data": {
    "id": 1,
    "user": {...},
    "shop": {...},
    "role": {...},
    "isDefault": false,
    "isActive": true,
    "createdAt": "2023-12-01T10:00:00Z",
    "updatedAt": null
  },
  "message": null
}
```

### 4. Update UserShop (Body)
**PUT** `/user-shops`

Updates a user-shop association using the ID in the request body.

**Request Body:**
```json
{
  "id": 1,
  "roleId": "MANAGER",
  "isDefault": true,
  "isActive": true
}
```

### 5. Update UserShop (Path Variable)
**PUT** `/user-shops/{id}`

Updates a user-shop association using the ID from the URL path.

**Request Body:**
```json
{
  "roleId": "MANAGER",
  "isDefault": true,
  "isActive": true
}
```

**Response (for both update methods):**
```json
{
  "data": {
    "id": 1,
    "user": {...},
    "shop": {...},
    "role": {
      "name": "MANAGER",
      "privileges": [...]
    },
    "isDefault": true,
    "isActive": true,
    "createdAt": "2023-12-01T10:00:00Z",
    "updatedAt": "2023-12-01T11:00:00Z"
  },
  "message": null
}
```

### 6. Delete UserShop
**DELETE** `/user-shops/{id}`

Soft deletes a user-shop association (sets isDeleted=true and isActive=false).

**Response:**
```json
{
  "message": "UserShop association deleted successfully"
}
```

## Business Rules

### Automatic Shop Scoping
- All operations are automatically scoped to the current authenticated user's shop
- Users can only manage UserShop associations within their own shop
- Cannot access or modify UserShop associations from other shops

### Role Management
- Role must exist in the system (validated against SystemRoleRepository)
- Common roles: "OWNER", "MANAGER", "CASHIER", etc.

### Default Settings
- Only one UserShop can be marked as default per user per shop
- When setting a UserShop as default, all other UserShops for that user in the same shop are automatically unmarked as default

### Self-Protection
- Users cannot delete their own UserShop association
- This prevents users from accidentally removing their own access to the shop

### Soft Delete
- Delete operations are soft deletes (isDeleted=true)
- Deleted UserShops are filtered out from all read operations
- This maintains data integrity and audit trails

## Error Handling

### Common Error Responses
```json
{
  "data": null,
  "message": "Error description here"
}
```

### Typical Error Scenarios
- **400 Bad Request**: Invalid request data, user already associated with shop
- **401 Unauthorized**: User not authenticated
- **403 Forbidden**: User doesn't have permission for the operation
- **404 Not Found**: UserShop not found or not accessible
- **500 Internal Server Error**: System error

## Example Usage

### Create a new user-shop association
```bash
curl -X POST http://localhost:8080/user-shops \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 5,
    "roleId": "CASHIER",
    "isDefault": false
  }'
```

### Get all user-shop associations for current shop
```bash
curl -X GET http://localhost:8080/user-shops \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Update a user-shop association
```bash
curl -X PUT http://localhost:8080/user-shops/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "roleId": "MANAGER",
    "isActive": true
  }'
```

### Delete a user-shop association
```bash
curl -X DELETE http://localhost:8080/user-shops/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Security Features

1. **Authentication Required**: All endpoints require valid JWT authentication
2. **Shop Scoping**: Operations are automatically scoped to the user's shop
3. **Role-Based Access**: Users can only perform operations they have privileges for
4. **Self-Protection**: Users cannot delete their own shop association
5. **Data Validation**: Input validation using Jakarta Bean Validation
6. **Audit Trail**: Soft deletes maintain data integrity and audit trails

## Integration Notes

This API integrates with:
- **SpringSecurityAuditorAware**: For automatic shop scoping
- **SystemRole**: For role management and validation
- **User**: For user validation and association
- **Shop**: For shop context and scoping
- **JWT Authentication**: For security and user identification 