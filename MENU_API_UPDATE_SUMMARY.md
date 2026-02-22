# Menu Service API Reference - Update Summary

## What's Changed

The **MENU_AVAILABILITY_ENDPOINTS.md** file has been completely transformed into **MENU_SERVICE_API_REFERENCE.md** - a comprehensive API reference guide for frontend developers.

## New File Location
📄 **MENU_SERVICE_API_REFERENCE.md**

## What's Included

### Complete API Coverage

#### Menu Items API (8 endpoints):
1. ✅ **POST** `/api/v1/menu-items` - Create menu item
2. ✅ **GET** `/api/v1/menu-items/{itemId}` - Get menu item by ID
3. ✅ **GET** `/api/v1/menu-items/kitchen/{kitchenId}` - Get kitchen menu (paginated)
4. ✅ **GET** `/api/v1/menu-items/search` - Advanced search with filters
5. ✅ **PUT** `/api/v1/menu-items/{itemId}` - Update menu item
6. ✅ **PATCH** `/api/v1/menu-items/{itemId}/deactivate` - Deactivate menu item
7. ✅ **GET** `/api/v1/menu-items/{itemId}/availability` - Check availability
8. ✅ **PATCH** `/api/v1/menu-items/{itemId}/availability` - Update availability

#### Menu Labels API (5 endpoints):
1. ✅ **POST** `/api/v1/menu-labels` - Create label
2. ✅ **GET** `/api/v1/menu-labels` - Get all labels
3. ✅ **GET** `/api/v1/menu-labels/{labelId}` - Get label by ID
4. ✅ **PUT** `/api/v1/menu-labels/{labelId}` - Update label
5. ✅ **PATCH** `/api/v1/menu-labels/{labelId}/deactivate` - Deactivate label

### Frontend Integration Examples

Each endpoint includes:
- ✅ **Request/Response examples** in JSON format
- ✅ **Frontend code examples** (Axios, Fetch, React, Vue)
- ✅ **Query parameters** documentation
- ✅ **Headers** requirements
- ✅ **Error handling** patterns

### Data Models & TypeScript Interfaces

Complete TypeScript interfaces for:
- `MenuItemDTO` (response model)
- `MenuItemRequest` (create/update model)
- `MenuLabelDTO`
- `PagedResponse<T>`
- `ApiResponse<T>`

### Practical Examples

1. **React Component** - Complete menu items list with pagination
2. **Vue Component** - Advanced search interface
3. **Error Handling** - Comprehensive error handling pattern
4. **Common Use Cases** - Quick reference for typical workflows

### Features Documented

#### Advanced Search Capabilities:
- Full-text search (name, description, ingredients)
- Filter by: kitchen, price range, dietary (veg/halal), spicy level, labels
- Sort by: rating, price, name (ascending/descending)
- Pagination support

#### Availability Management:
- Check real-time availability
- Update stock quantities
- Integration patterns with order service

## URL Structure

### Development (Direct):
```
http://localhost:8083/api/v1/menu-items
http://localhost:8083/api/v1/menu-labels
```

### Production (via API Gateway):
```
http://localhost:8080/menu/api/v1/menu-items
http://localhost:8080/menu/api/v1/menu-labels
```

## Quick Start for Frontend Developers

### 1. View Customer Menu
```javascript
// Get kitchen menu with pagination
const response = await axios.get(
  'http://localhost:8080/menu/api/v1/menu-items/kitchen/1',
  { params: { page: 0, size: 20 } }
);
```

### 2. Search Menu Items
```javascript
// Search with filters
const results = await axios.get(
  'http://localhost:8080/menu/api/v1/menu-items/search',
  { 
    params: {
      query: 'biryani',
      halal: true,
      maxPrice: 15,
      sort: 'rating_desc'
    }
  }
);
```

### 3. Check Availability Before Order
```javascript
// Verify item is available
const item = await axios.get(
  `http://localhost:8080/menu/api/v1/menu-items/${itemId}/availability`
);

if (item.data.data.isActive && item.data.data.quantityAvailable > 0) {
  // Proceed with order
}
```

### 4. Kitchen Owner Updates Stock
```javascript
// Update available quantity
await axios.patch(
  `http://localhost:8080/menu/api/v1/menu-items/${itemId}/availability`,
  null,
  { params: { quantityAvailable: 50 } }
);
```

## Error Handling Pattern

All endpoints return standardized error responses:

```javascript
{
  "success": false,
  "data": null,
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "timestamp": "2026-02-22T10:30:00"
}
```

Common error codes:
- `MENU_ITEM_NOT_FOUND` (404)
- `LABEL_NOT_FOUND` (404)
- `VALIDATION_ERROR` (400)
- `INTERNAL_SERVER_ERROR` (500)

## Integration Notes

### For Order Service Integration:
1. Check availability before creating order
2. Verify `isActive == true` and `quantityAvailable > 0`
3. After successful order, decrease `quantityAvailable`
4. Handle out-of-stock scenarios gracefully

### For Kitchen Dashboard:
1. Use paginated kitchen menu endpoint
2. Implement real-time stock updates
3. Allow bulk availability management
4. Show preparation times for order planning

### For Customer App:
1. Use advanced search for discovery
2. Display availability status clearly
3. Show preparation times in cart
4. Filter by dietary preferences (veg/halal)
5. Sort by ratings or price

## Next Steps

1. ✅ **Read the full API reference**: `MENU_SERVICE_API_REFERENCE.md`
2. ⚡ **Rebuild menu-service** (if needed):
   ```powershell
   cd C:\workspace\makanforyou\menu-service
   mvn clean install -DskipTests
   ```
3. 🚀 **Start implementing** using the code examples provided
4. 🧪 **Test endpoints** via Swagger UI: `http://localhost:8083/swagger-ui.html`
5. 🔗 **Configure API Gateway** routes if not already set up

## Additional Resources

- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **API Docs**: http://localhost:8083/v3/api-docs
- **Service Port**: 8083 (direct) | 8080 (gateway)

## Key Benefits

✅ **Frontend-Ready**: All examples are production-ready code  
✅ **Type-Safe**: TypeScript interfaces included  
✅ **Error-Handled**: Comprehensive error handling patterns  
✅ **Framework-Agnostic**: Examples for React, Vue, vanilla JS  
✅ **Complete**: All 13 endpoints documented with examples  
✅ **Tested**: All endpoints match actual controller implementations

## Support

For issues or questions:
1. Check Swagger UI for real-time API testing
2. Review code examples in the reference guide
3. Verify service is running on correct port
4. Check API Gateway routing configuration

---

**Last Updated**: February 22, 2026  
**Version**: 1.0  
**Status**: ✅ Complete & Ready for Frontend Integration

