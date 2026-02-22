# Menu Item Image Upload API Documentation

This document describes the image upload and management functionality for menu items in the Makan For You application.

## Overview

The Menu Item Image feature allows kitchen owners to:
- Upload multiple images per menu item
- Automatic thumbnail generation
- UUID-based unique identification across all kitchens
- Set primary/featured image
- Manage display order
- Soft delete and permanent delete options

## Database Schema

A new table `menu_item_images` stores image metadata:

```sql
CREATE TABLE menu_item_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id INT NOT NULL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    original_image_path VARCHAR(500) NOT NULL,
    thumbnail_image_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(50),
    file_size BIGINT,
    image_width INT,
    image_height INT,
    thumbnail_width INT,
    thumbnail_height INT,
    display_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_item_id) REFERENCES kitchen_menu(item_id) ON DELETE CASCADE
);
```

## API Endpoints

### 1. Upload Single Image
**POST** `/api/v1/menu-items/{menuItemId}/images`

**Content-Type:** `multipart/form-data`

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | File | Yes | Image file (JPEG, PNG, GIF, WebP) |
| altText | String | No | Alt text for accessibility |
| isPrimary | Boolean | No | Set as primary image (default: false) |

**Response:**
```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "id": 1,
    "menuItemId": 123,
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "originalFilename": "biryani.jpg",
    "originalImageUrl": "/api/v1/images/550e8400-e29b-41d4-a716-446655440000/original",
    "thumbnailImageUrl": "/api/v1/images/550e8400-e29b-41d4-a716-446655440000/thumbnail",
    "contentType": "image/jpeg",
    "fileSize": 245760,
    "imageWidth": 1920,
    "imageHeight": 1080,
    "thumbnailWidth": 300,
    "thumbnailHeight": 169,
    "displayOrder": 1,
    "isPrimary": true,
    "isActive": true,
    "altText": "Delicious Hyderabadi Biryani",
    "createdAt": "2026-02-22T10:30:00",
    "updatedAt": "2026-02-22T10:30:00"
  }
}
```

### 2. Upload Multiple Images
**POST** `/api/v1/menu-items/{menuItemId}/images/batch`

**Content-Type:** `multipart/form-data`

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| files | File[] | Yes | Multiple image files |
| altText | String | No | Alt text for all images |

### 3. Get All Images for Menu Item
**GET** `/api/v1/menu-items/{menuItemId}/images`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "originalImageUrl": "/api/v1/images/550e8400-e29b-41d4-a716-446655440000/original",
      "thumbnailImageUrl": "/api/v1/images/550e8400-e29b-41d4-a716-446655440000/thumbnail",
      "isPrimary": true,
      "displayOrder": 1
    },
    {
      "id": 2,
      "uuid": "660f9500-f30c-52e5-b827-557766551111",
      "originalImageUrl": "/api/v1/images/660f9500-f30c-52e5-b827-557766551111/original",
      "thumbnailImageUrl": "/api/v1/images/660f9500-f30c-52e5-b827-557766551111/thumbnail",
      "isPrimary": false,
      "displayOrder": 2
    }
  ]
}
```

### 4. Set Primary Image
**PATCH** `/api/v1/menu-items/{menuItemId}/images/{imageId}/primary`

### 5. Update Display Order
**PATCH** `/api/v1/menu-items/{menuItemId}/images/{imageId}/order?displayOrder=2`

### 6. Update Alt Text
**PATCH** `/api/v1/menu-items/{menuItemId}/images/{imageId}/alt-text?altText=New+description`

### 7. Deactivate Image (Soft Delete)
**PATCH** `/api/v1/menu-items/{menuItemId}/images/{imageId}/deactivate`

### 8. Delete Image (Permanent)
**DELETE** `/api/v1/menu-items/{menuItemId}/images/{imageId}`

### 9. Get Original Image
**GET** `/api/v1/images/{uuid}/original`

Returns the original full-size image file with appropriate content-type header.

### 10. Get Thumbnail Image
**GET** `/api/v1/images/{uuid}/thumbnail`

Returns the thumbnail image file with appropriate content-type header.

## Configuration

Add to `application.yml`:

```yaml
storage:
  upload-dir: ./uploads
  menu-images-dir: menu-images
  originals-dir: originals
  thumbnails-dir: thumbnails
  thumbnail-width: 300
  thumbnail-height: 300
  max-file-size: 10485760  # 10MB
  allowed-content-types:
    - image/jpeg
    - image/png
    - image/gif
    - image/webp
  base-url: /api/v1/images
  image-quality: 0.85

spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
```

## File Storage Structure

```
uploads/
└── menu-images/
    ├── originals/
    │   ├── 550e8400-e29b-41d4-a716-446655440000.jpg
    │   └── 660f9500-f30c-52e5-b827-557766551111.png
    └── thumbnails/
        ├── 550e8400-e29b-41d4-a716-446655440000.jpg
        └── 660f9500-f30c-52e5-b827-557766551111.png
```

## Features

### UUID-Based Naming
- Each image is stored with a UUID filename
- Ensures uniqueness across all kitchens
- Prevents filename collisions
- Original filename preserved in database

### Automatic Thumbnail Generation
- Thumbnails generated on upload
- Maintains aspect ratio
- High-quality bicubic interpolation
- JPEG compression for thumbnails
- Configurable dimensions (default 300x300)

### Multiple Images per Menu Item
- Unlimited images per item
- Display order management
- Primary image designation
- First uploaded image auto-set as primary

### Image Management
- Soft delete (deactivate)
- Permanent delete with file cleanup
- Alt text for accessibility
- Display order sorting

## Error Handling

| Error Code | Description |
|------------|-------------|
| MENU_ITEM_NOT_FOUND | Menu item does not exist |
| IMAGE_NOT_FOUND | Image does not exist |
| EMPTY_FILE | No file provided |
| FILE_TOO_LARGE | File exceeds max size |
| INVALID_FILE_TYPE | File type not allowed |
| INVALID_IMAGE | Cannot read image file |
| FILE_UPLOAD_FAILED | Server error during upload |

## Example Usage

### cURL - Upload Image
```bash
curl -X POST "http://localhost:8083/api/v1/menu-items/123/images" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@biryani.jpg" \
  -F "altText=Delicious Hyderabadi Biryani" \
  -F "isPrimary=true"
```

### cURL - Upload Multiple Images
```bash
curl -X POST "http://localhost:8083/api/v1/menu-items/123/images/batch" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg" \
  -F "files=@image3.jpg" \
  -F "altText=Food images"
```

### cURL - Get Thumbnail
```bash
curl "http://localhost:8083/api/v1/images/550e8400-e29b-41d4-a716-446655440000/thumbnail" \
  -o thumbnail.jpg
```

