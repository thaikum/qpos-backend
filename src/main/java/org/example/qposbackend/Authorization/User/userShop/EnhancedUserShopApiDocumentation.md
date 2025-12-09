# Enhanced UserShop API Documentation

## Overview
The enhanced UserShop API now supports creating new users on-the-fly during UserShop association creation. You can either associate existing users or create new users in a single operation.

## New Features

### 1. User Creation During UserShop Association
The `CreateUserShopRequest` now supports two modes:
- **Existing User Mode**: Use `userId` to associate an existing user
- **New User Mode**: Use `userDto` to create a new user and associate them

### 2. Standalone User Creation
New endpoint to create users independently using the `UserDto`.

## API Endpoints

### User Creation

#### Create User (Standalone)
**POST** `/users/create`

Creates a new user without associating them to any shop.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+254712345678",
  "idType": "NATIONAL_ID",
  "idNumber": "12345678",
  "password": "customPassword123"
}
```

**Response:**
```json
{
  "data": {
    "id": 5,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "idType": "NATIONAL_ID",
    "idNumber": "12345678",
    "enabled": true,
    "isLoggedIn": false
  },
  "message": null
}
```

### Enhanced UserShop Operations

#### Create UserShop with Existing User
**POST** `/user-shops`

**Request Body (Existing User):**
```json
{
  "userId": 5,
  "roleId": "CASHIER",
  "isDefault": false
}
```

#### Create UserShop with New User
**POST** `/user-shops`

**Request Body (New User):**
```json
{
  "userDto": {
    "email": "jane.smith@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+254798765432",
    "idType": "NATIONAL_ID",
    "idNumber": "87654321",
    "password": "securePassword456"
  },
  "roleId": "MANAGER",
  "isDefault": true
}
```

**Response (for both modes):**
```json
{
  "data": {
    "id": 3,
    "user": {
      "id": 5,
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "enabled": true
    },
    "shop": {
      "id": 1,
      "name": "My Shop",
      "code": "SHOP001",
      "active": true
    },
    "role": {
      "name": "MANAGER",
      "privileges": [...]
    },
    "isDefault": true,
    "isActive": true,
    "createdAt": "2023-12-01T10:00:00Z",
    "updatedAt": null
  },
  "message": null
}
```

## UserDto Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | String | ✅ | Valid email address (must be unique) |
| `firstName` | String | ✅ | User's first name |
| `lastName` | String | ✅ | User's last name |
| `phoneNumber` | String | ❌ | Phone number |
| `idType` | Enum | ❌ | ID document type (NATIONAL_ID, ALIEN_ID, DRIVING_LICENCE, PASSPORT) |
| `idNumber` | String | ❌ | ID document number |
| `password` | String | ❌ | Custom password (defaults to "12345678" if not provided) |

## Enhanced Business Logic

### User Creation Rules
1. **Email Uniqueness**: Email must be unique across the system
2. **Default Password**: If no password provided, defaults to "12345678"
3. **Password Encoding**: All passwords are encrypted using BCrypt
4. **User Status**: New users are created as enabled by default

### UserShop Association Rules
1. **Either/Or Requirement**: Must provide either `userId` OR `userDto`, not both
2. **Existing User Validation**: If using `userId`, user must exist
3. **New User Creation**: If using `userDto`, user is created first, then associated
4. **Duplicate Prevention**: Same user cannot be associated with the same shop twice
5. **Shop Scoping**: All operations are scoped to the authenticated user's shop

### Default Settings
- Only one UserShop can be marked as default per user across all shops
- **Automatic Default Assignment**: If this is the user's only shop association, it will automatically be set as default regardless of the `isDefault` request value
- When setting a UserShop as default explicitly, all other UserShops for that user are automatically unmarked as default
- **Smart Default Management**: When a user is removed from shops and left with only one shop, that remaining shop automatically becomes the default

## Error Handling

### User Creation Errors
```json
{
  "data": null,
  "message": "User with email john@example.com already exists"
}
```

### UserShop Creation Errors
```json
{
  "data": null,
  "message": "Either userId or userDto must be provided"
}
```

```json
{
  "data": null,
  "message": "User is already associated with this shop"
}
```

## Example Usage Scenarios

### Scenario 1: Add Existing Employee to Shop
```bash
curl -X POST http://localhost:8080/user-shops \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 3,
    "roleId": "CASHIER",
    "isDefault": false
  }'
```

### Scenario 2: Create New Employee and Add to Shop
```bash
curl -X POST http://localhost:8080/user-shops \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userDto": {
      "email": "newemployee@shop.com",
      "firstName": "New",
      "lastName": "Employee",
      "phoneNumber": "+254700000000",
      "idType": "NATIONAL_ID",
      "idNumber": "11223344"
    },
    "roleId": "CASHIER",
    "isDefault": false
  }'
```

### Scenario 3: Create User Without Shop Association
```bash
curl -X POST http://localhost:8080/users/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "standaloneuser@example.com",
    "firstName": "Standalone",
    "lastName": "User",
    "password": "customPassword123"
  }'
```

### Scenario 4: Automatic Default Shop Assignment
When creating a user's first shop association, it automatically becomes default:

```bash
# This will automatically set isDefault=true since it's the user's only shop
curl -X POST http://localhost:8080/user-shops \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userDto": {
      "email": "firsttime@example.com",
      "firstName": "First",
      "lastName": "Timer"
    },
    "roleId": "CASHIER",
    "isDefault": false
  }'
# Response will show "isDefault": true regardless of request value
```

## Workflow Examples

### Onboarding New Employee
1. **Option A - Single Step**: Use UserShop creation with `userDto` to create user and associate in one call
2. **Option B - Two Steps**: 
   - First, create user using `/users/create`
   - Then, associate user using `/user-shops` with `userId`

### Managing Existing Users
- Use `/user-shops` with `userId` to associate existing users with shops
- Use standard UserShop endpoints for updates and management

## Security Considerations

1. **Authentication Required**: All endpoints require valid JWT authentication
2. **Shop Scoping**: Users can only manage associations within their own shop
3. **Email Validation**: Email format validation prevents invalid data entry
4. **Password Security**: Passwords are encrypted using BCrypt before storage
5. **Input Validation**: Jakarta Bean Validation ensures data integrity

## Integration Notes

This enhanced API integrates with:
- **UserService**: For standalone user creation
- **SpringSecurityAuditorAware**: For automatic shop scoping
- **SystemRole**: For role validation and assignment
- **Password Management**: For secure password handling
- **Email Validation**: For preventing duplicate users

The system maintains backward compatibility while adding powerful new functionality for user management in multi-shop environments. 