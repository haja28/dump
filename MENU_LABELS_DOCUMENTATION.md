# Menu Labels API Documentation

## Overview

Menu Labels allow restaurants/kitchens to categorize and describe menu items with custom tags. Labels can be used to mark items as vegan, spicy, homemade, organic, gluten-free, or any other custom attribute. This helps users filter menu items based on their preferences.

---

## Table of Contents

1. [Endpoints](#endpoints)
2. [Sample Requests](#sample-requests)
3. [Response Examples](#response-examples)
4. [Error Handling](#error-handling)
5. [Use Cases](#use-cases)
6. [Best Practices](#best-practices)

---

## Endpoints

### 1. Create Menu Label (Admin Only)

**POST** `/api/v1/menu-labels`

Creates a new menu label in the system. Only admin users can create labels.

#### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| name | string | Yes | Label name (e.g., "vegan", "spicy", "gluten-free") |
| description | string | No | Detailed description of what this label represents |

#### Headers

```
Authorization: Bearer {adminToken}
Content-Type: application/json
```

#### Response Status

- **201 Created** - Label created successfully
- **400 Bad Request** - Validation error or missing required fields
- **401 Unauthorized** - Invalid or missing token
- **403 Forbidden** - User does not have admin role
- **409 Conflict** - Label with same name already exists

---

### 2. Get All Labels

**GET** `/api/v1/menu-labels`

Retrieves all active menu labels in the system. This endpoint is public and doesn't require authentication.

#### Request Parameters

None

#### Headers

```
Content-Type: application/json
```

#### Response Status

- **200 OK** - Labels retrieved successfully
- **204 No Content** - No labels found

---

### 3. Get Label by ID

**GET** `/api/v1/menu-labels/{labelId}`

Retrieves a specific menu label by its ID.

#### Request Parameters

| Parameter | Type | Location | Required | Description |
|-----------|------|----------|----------|-------------|
| labelId | integer | Path | Yes | Unique identifier of the label |

#### Headers

```
Content-Type: application/json
```

#### Response Status

- **200 OK** - Label found and returned
- **404 Not Found** - Label with given ID does not exist

---

### 4. Update Menu Label (Admin Only)

**PUT** `/api/v1/menu-labels/{labelId}`

Updates an existing menu label. Only admin users can update labels.

#### Request Parameters

| Parameter | Type | Location | Required | Description |
|-----------|------|----------|----------|-------------|
| labelId | integer | Path | Yes | Label ID to update |
| name | string | Query | Yes | New label name |
| description | string | Query | No | New label description |

#### Headers

```
Authorization: Bearer {adminToken}
Content-Type: application/json
```

#### Response Status

- **200 OK** - Label updated successfully
- **400 Bad Request** - Validation error
- **401 Unauthorized** - Invalid or missing token
- **403 Forbidden** - User does not have admin role
- **404 Not Found** - Label not found
- **409 Conflict** - New name already exists

---

### 5. Deactivate Menu Label (Admin Only)

**PATCH** `/api/v1/menu-labels/{labelId}/deactivate`

Deactivates a menu label (soft delete). Deactivated labels are hidden from users but not permanently deleted.

#### Request Parameters

| Parameter | Type | Location | Required | Description |
|-----------|------|----------|----------|-------------|
| labelId | integer | Path | Yes | Label ID to deactivate |

#### Headers

```
Authorization: Bearer {adminToken}
Content-Type: application/json
```

#### Response Status

- **200 OK** - Label deactivated successfully
- **401 Unauthorized** - Invalid or missing token
- **403 Forbidden** - User does not have admin role
- **404 Not Found** - Label not found

---

## Sample Requests

### Create Label Examples

#### Example 1: Create "Vegan" Label

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=vegan&description=Vegan%20friendly%20-%20No%20animal%20products" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c" \
  -H "Content-Type: application/json"
```

**Postman Request:**
```
Method: POST
URL: http://localhost:8080/api/v1/menu-labels
Params:
  - name: vegan
  - description: Vegan friendly - No animal products
Headers:
  - Authorization: Bearer {adminToken}
  - Content-Type: application/json
```

---

#### Example 2: Create "Spicy" Label

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=spicy&description=Contains%20spicy%20ingredients%20-%20Level%203%20or%20above" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

#### Example 3: Create "Gluten-Free" Label

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=gluten-free&description=Free%20from%20gluten%20-%20Safe%20for%20celiac%20disease" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

#### Example 4: Create "Homemade" Label

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=homemade&description=Made%20with%20traditional%20homemade%20recipe" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

#### Example 5: Create "Low-Calorie" Label

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=low-calorie&description=Nutritious%20option%20with%20reduced%20calories" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

#### Example 6: Create "Organic" Label

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=organic&description=Made%20with%20certified%20organic%20ingredients" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

### Get All Labels Example

```bash
curl -X GET "http://localhost:8080/api/v1/menu-labels" \
  -H "Content-Type: application/json"
```

**Postman Request:**
```
Method: GET
URL: http://localhost:8080/api/v1/menu-labels
Headers:
  - Content-Type: application/json
```

---

### Get Label by ID Example

```bash
curl -X GET "http://localhost:8080/api/v1/menu-labels/1" \
  -H "Content-Type: application/json"
```

**Postman Request:**
```
Method: GET
URL: http://localhost:8080/api/v1/menu-labels/1
Headers:
  - Content-Type: application/json
```

---

### Update Label Example

```bash
curl -X PUT "http://localhost:8080/api/v1/menu-labels/1?name=vegan&description=Strictly%20vegan%20-%20No%20animal%20products%20or%20byproducts" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

**Postman Request:**
```
Method: PUT
URL: http://localhost:8080/api/v1/menu-labels/1
Params:
  - name: vegan
  - description: Strictly vegan - No animal products or byproducts
Headers:
  - Authorization: Bearer {adminToken}
  - Content-Type: application/json
```

---

### Deactivate Label Example

```bash
curl -X PATCH "http://localhost:8080/api/v1/menu-labels/5/deactivate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

**Postman Request:**
```
Method: PATCH
URL: http://localhost:8080/api/v1/menu-labels/5/deactivate
Headers:
  - Authorization: Bearer {adminToken}
  - Content-Type: application/json
```

---

## Response Examples

### Success Responses

#### Create Label - 201 Created

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "vegan",
    "description": "Vegan friendly - No animal products",
    "createdAt": "2026-02-05T10:15:30Z",
    "updatedAt": "2026-02-05T10:15:30Z"
  },
  "message": "Label created successfully",
  "timestamp": "2026-02-05T10:15:30Z",
  "errors": []
}
```

---

#### Get All Labels - 200 OK

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "vegan",
      "description": "Vegan friendly - No animal products",
      "createdAt": "2026-02-01T08:00:00Z",
      "updatedAt": "2026-02-01T08:00:00Z"
    },
    {
      "id": 2,
      "name": "spicy",
      "description": "Contains spicy ingredients - Level 3 or above",
      "createdAt": "2026-02-01T08:05:00Z",
      "updatedAt": "2026-02-01T08:05:00Z"
    },
    {
      "id": 3,
      "name": "gluten-free",
      "description": "Free from gluten - Safe for celiac disease",
      "createdAt": "2026-02-01T08:10:00Z",
      "updatedAt": "2026-02-01T08:10:00Z"
    },
    {
      "id": 4,
      "name": "homemade",
      "description": "Made with traditional homemade recipe",
      "createdAt": "2026-02-01T08:15:00Z",
      "updatedAt": "2026-02-01T08:15:00Z"
    },
    {
      "id": 5,
      "name": "low-calorie",
      "description": "Nutritious option with reduced calories",
      "createdAt": "2026-02-01T08:20:00Z",
      "updatedAt": "2026-02-01T08:20:00Z"
    },
    {
      "id": 6,
      "name": "organic",
      "description": "Made with certified organic ingredients",
      "createdAt": "2026-02-01T08:25:00Z",
      "updatedAt": "2026-02-01T08:25:00Z"
    }
  ],
  "message": "Labels retrieved successfully",
  "timestamp": "2026-02-05T10:20:00Z",
  "errors": []
}
```

---

#### Get Single Label - 200 OK

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "vegan",
    "description": "Vegan friendly - No animal products",
    "createdAt": "2026-02-01T08:00:00Z",
    "updatedAt": "2026-02-01T08:00:00Z"
  },
  "message": "Label retrieved successfully",
  "timestamp": "2026-02-05T10:20:00Z",
  "errors": []
}
```

---

#### Update Label - 200 OK

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "vegan",
    "description": "Strictly vegan - No animal products or byproducts",
    "createdAt": "2026-02-01T08:00:00Z",
    "updatedAt": "2026-02-05T10:25:30Z"
  },
  "message": "Label updated successfully",
  "timestamp": "2026-02-05T10:25:30Z",
  "errors": []
}
```

---

#### Deactivate Label - 200 OK

```json
{
  "success": true,
  "data": {
    "id": 5,
    "name": "low-calorie",
    "description": "Nutritious option with reduced calories",
    "createdAt": "2026-02-01T08:20:00Z",
    "updatedAt": "2026-02-05T10:30:00Z"
  },
  "message": "Label deactivated successfully",
  "timestamp": "2026-02-05T10:30:00Z",
  "errors": []
}
```

---

### Error Responses

#### 400 Bad Request - Missing Required Field

```json
{
  "success": false,
  "data": null,
  "message": "Validation failed",
  "timestamp": "2026-02-05T10:15:30Z",
  "errors": [
    "Label name is required and cannot be blank"
  ]
}
```

---

#### 401 Unauthorized - Missing Token

```json
{
  "success": false,
  "data": null,
  "message": "Unauthorized",
  "timestamp": "2026-02-05T10:15:30Z",
  "errors": [
    "Authorization header is missing or invalid"
  ]
}
```

---

#### 403 Forbidden - Insufficient Permissions

```json
{
  "success": false,
  "data": null,
  "message": "Access denied",
  "timestamp": "2026-02-05T10:15:30Z",
  "errors": [
    "User must have ADMIN role to create labels"
  ]
}
```

---

#### 404 Not Found - Label Does Not Exist

```json
{
  "success": false,
  "data": null,
  "message": "Label not found",
  "timestamp": "2026-02-05T10:20:00Z",
  "errors": [
    "Label with ID 999 does not exist"
  ]
}
```

---

#### 409 Conflict - Duplicate Label Name

```json
{
  "success": false,
  "data": null,
  "message": "Label already exists",
  "timestamp": "2026-02-05T10:15:30Z",
  "errors": [
    "Label 'vegan' already exists in the system"
  ]
}
```

---

## Error Handling

### Common Error Scenarios

| Error Code | Scenario | Solution |
|-----------|----------|----------|
| 400 | Missing name parameter | Provide label name in query parameters |
| 400 | Label name is blank | Provide non-empty label name |
| 401 | Missing authorization token | Include Authorization header with valid JWT token |
| 403 | User is not admin | Use admin account credentials |
| 404 | Label ID not found | Verify the label ID exists |
| 409 | Label name already exists | Choose a different label name |

### Retry Strategy

For transient errors (500, 502, 503):
- Retry with exponential backoff
- Maximum 3 retries with 2-4 second delays
- Log failed attempts for monitoring

---

## Use Cases

### Use Case 1: Dietary Labels

Kitchen owners create labels for dietary preferences:

```bash
# Create Dietary Labels
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=vegan" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=vegetarian" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=gluten-free" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=dairy-free" \
  -H "Authorization: Bearer {adminToken}"
```

---

### Use Case 2: Allergen Labels

Labels for common allergens:

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=contains-nuts&description=Contains%20peanuts%20or%20tree%20nuts" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=contains-shellfish&description=Contains%20shellfish" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=contains-soy&description=Contains%20soy" \
  -H "Authorization: Bearer {adminToken}"
```

---

### Use Case 3: Preparation Labels

Labels for preparation methods:

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=homemade&description=Made%20with%20homemade%20recipe" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=fried&description=Deep%20fried" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=grilled&description=Grilled%20preparation" \
  -H "Authorization: Bearer {adminToken}"
```

---

### Use Case 4: Ingredient Quality Labels

Labels for ingredient sourcing:

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=organic&description=Made%20with%20certified%20organic%20ingredients" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=locally-sourced&description=Made%20with%20locally%20sourced%20ingredients" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=fair-trade&description=Fair%20trade%20certified" \
  -H "Authorization: Bearer {adminToken}"
```

---

### Use Case 5: Flavor Profile Labels

Labels for taste characteristics:

```bash
curl -X POST "http://localhost:8080/api/v1/menu-labels?name=spicy&description=Spicy%20dish" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=sweet&description=Sweet%20dish" \
  -H "Authorization: Bearer {adminToken}"

curl -X POST "http://localhost:8080/api/v1/menu-labels?name=savory&description=Savory%20dish" \
  -H "Authorization: Bearer {adminToken}"
```

---

## Best Practices

### 1. Label Naming Conventions

Use consistent naming patterns:

```
✅ Good:
- vegan (simple, lowercase)
- gluten-free (use hyphens for multi-word)
- low-calorie (descriptive)
- contains-nuts (clear indication)

❌ Bad:
- Vegan (inconsistent casing)
- Gluten Free (spaces instead of hyphens)
- VEG (unclear abbreviations)
- V (too cryptic)
```

---

### 2. Label Organization

Group labels by category:

```
Dietary Labels:
- vegan
- vegetarian
- pescatarian

Allergen Labels:
- contains-nuts
- contains-shellfish
- contains-soy

Quality Labels:
- organic
- locally-sourced
- fair-trade

Preparation Labels:
- homemade
- fried
- grilled
```

---

### 3. Caching Strategy

Cache labels on the client side:

```javascript
// Fetch and cache labels
async function getCachedLabels() {
  const cached = localStorage.getItem('menu_labels');
  
  if (cached && isCacheValid(cached)) {
    return JSON.parse(cached);
  }
  
  const labels = await fetch('/api/v1/menu-labels').then(r => r.json());
  localStorage.setItem('menu_labels', JSON.stringify({
    data: labels,
    timestamp: Date.now()
  }));
  
  return labels;
}
```

---

### 4. Error Handling Best Practices

```javascript
async function createLabel(name, description) {
  try {
    const response = await fetch('/api/v1/menu-labels', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ name, description })
    });
    
    if (!response.ok) {
      const error = await response.json();
      
      if (response.status === 409) {
        throw new Error(`Label "${name}" already exists`);
      } else if (response.status === 403) {
        throw new Error('You do not have permission to create labels');
      } else {
        throw new Error(error.message);
      }
    }
    
    return await response.json();
  } catch (error) {
    console.error('Failed to create label:', error);
    // Show user-friendly error message
    showErrorNotification(error.message);
  }
}
```

---

### 5. Permission Validation

Always check user role before attempting admin operations:

```javascript
async function attemptCreateLabel(name) {
  if (!isUserAdmin()) {
    showErrorNotification('Only admins can create labels');
    return;
  }
  
  // Proceed with label creation
  return createLabel(name);
}
```

---

### 6. Rate Limiting Awareness

Consider rate limiting when bulk creating labels:

```javascript
// Bulk create labels with delay
async function bulkCreateLabels(labels) {
  for (const label of labels) {
    await createLabel(label.name, label.description);
    // Wait 500ms between requests
    await new Promise(resolve => setTimeout(resolve, 500));
  }
}
```

---

### 7. Label Consistency

Maintain data consistency by:

- Using same label names across kitchens
- Regularly auditing for duplicate labels
- Deactivating unused labels instead of deleting
- Documenting label purposes in descriptions

---

### 8. Performance Optimization

Fetch all labels once and reuse:

```javascript
// Service for label management
class LabelService {
  constructor() {
    this.labels = null;
    this.lastFetch = null;
  }
  
  async getLabels(forceRefresh = false) {
    const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    
    if (!forceRefresh && this.labels && 
        Date.now() - this.lastFetch < CACHE_DURATION) {
      return this.labels;
    }
    
    const response = await fetch('/api/v1/menu-labels');
    this.labels = await response.json();
    this.lastFetch = Date.now();
    return this.labels;
  }
}
```

---

## Integration with Menu Items

Once labels are created, they can be associated with menu items:

```bash
# Create a menu item with labels
curl -X POST "http://localhost:8080/api/v1/menu-items" \
  -H "Authorization: Bearer {kitchenToken}" \
  -H "X-Kitchen-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Vegan Buddha Bowl",
    "description": "Nutritious bowl with organic vegetables",
    "cost": 12.99,
    "isVeg": true,
    "isHalal": false,
    "spicyLevel": 1,
    "labelIds": [1, 6, 2]
  }'
```

Where labelIds correspond to:
- 1 = vegan
- 6 = organic
- 2 = low-calorie

---

## Postman Collection

### Import into Postman

Save the following as a `.json` file and import into Postman:

```json
{
  "info": {
    "name": "Menu Labels API",
    "description": "Complete Menu Labels API collection"
  },
  "item": [
    {
      "name": "Create Label",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/v1/menu-labels?name=vegan&description=Vegan%20friendly",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "menu-labels"],
          "query": [
            {
              "key": "name",
              "value": "vegan"
            },
            {
              "key": "description",
              "value": "Vegan friendly"
            }
          ]
        }
      }
    },
    {
      "name": "Get All Labels",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/v1/menu-labels",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "menu-labels"]
        }
      }
    },
    {
      "name": "Get Label by ID",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/v1/menu-labels/1",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "menu-labels", "1"]
        }
      }
    },
    {
      "name": "Update Label",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/v1/menu-labels/1?name=vegan&description=Updated%20description",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "menu-labels", "1"],
          "query": [
            {
              "key": "name",
              "value": "vegan"
            },
            {
              "key": "description",
              "value": "Updated description"
            }
          ]
        }
      }
    },
    {
      "name": "Deactivate Label",
      "request": {
        "method": "PATCH",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/v1/menu-labels/1/deactivate",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "menu-labels", "1", "deactivate"]
        }
      }
    }
  ]
}
```

---

## Testing Checklist

- [ ] Create new label successfully
- [ ] Retrieve all labels
- [ ] Get specific label by ID
- [ ] Update label details
- [ ] Deactivate label
- [ ] Verify 401 error with invalid token
- [ ] Verify 403 error with non-admin user
- [ ] Verify 404 error with non-existent ID
- [ ] Verify 409 error with duplicate name
- [ ] Verify labels appear in menu item creation

---

**Last Updated**: February 5, 2026
**API Version**: v1
**Base URL**: http://localhost:8080
