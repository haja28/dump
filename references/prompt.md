Home-Cooked Food Marketplace (Flutter + Spring Boot Microservices)

You are a senior full-stack architect with deep expertise in Spring Boot microservices, mobile-first API design, and Flutter backend integration.

Design and implement a production-ready backend to power a Flutter mobile application for a home-cooked food marketplace.

The platform must allow:

Home kitchens (sellers) to register, get approved, and manage menus

Customers to discover, search, filter, and order home-cooked food

APIs must be mobile-optimized, secure, scalable, and well-documented.

1. High-Level Architecture

Spring Boot 3.x

Microservices architecture

Spring Cloud Gateway as API Gateway

JWT-based authentication

REST APIs (JSON)

OpenAPI / Swagger for all services

Clean Architecture (Controller → Service → Domain → Repository)

2. Core User Roles
   a. Customer

Browse kitchens & menus

Search food items

Filter by labels (veg, halal, spicy, homemade, etc.)

Place orders

b. Kitchen User (Home Chef)

Register as a kitchen

Add/edit menus & items

Assign labels/tags to menu items

Manage availability & pricing

c. Admin

Approve/reject kitchens

Moderate menus & labels

3. Microservices Design
   3.1 API Gateway

Route traffic to services

JWT validation

Rate limiting

Request logging

3.2 Auth & User Service

Responsibilities

User registration & login

Role management (CUSTOMER, KITCHEN, ADMIN)

Token refresh for Flutter

Key APIs

POST /auth/register
POST /auth/login
POST /auth/refresh

3.3 Kitchen Service (Home Chef Onboarding)

Responsibilities

Kitchen registration & profile management

Admin approval workflow

Kitchen visibility (active/inactive)

Kitchen Entity

id

kitchenName

ownerUserId

description

cuisineTypes

address / deliveryArea

approvalStatus (PENDING / APPROVED / REJECTED)

rating

isActive

APIs

POST /kitchens/register
GET  /kitchens/{id}
GET  /kitchens?approved=true

3.4 Menu & Food Item Service (Core Feature)

This is the most important service.

MenuItem Entity

id

kitchenId

name

description

price

labels (array of strings or tag IDs)

cuisineType

isVeg (boolean)

isHalal (boolean)

spicyLevel

availability

rating

createdAt

4. Search, Filter & Labeling (Flutter-Optimized)
   4.1 Search Capabilities

Full-text search on:

food name

description

kitchen name

Case-insensitive

Partial matches

4.2 Filters Supported

Kitchen

Cuisine type

Price range

Labels/tags

Veg / Non-Veg

Halal

Availability

Rating

Example API
GET /menu-items/search
?query=biryani
&kitchenId=123
&labels=halal,spicy,home-made
&minPrice=5
&maxPrice=15
&veg=false
&sort=rating_desc

5. Labels / Tags System

Labels are searchable metadata

Examples:

veg

halal

spicy

sugar-free

homemade

bestseller

Requirements

Kitchen users can assign labels

Admin can manage global labels

Labels indexed for fast filtering

6. Flutter-Friendly API Design
   Standard Response Wrapper
   {
   "success": true,
   "data": {},
   "message": "",
   "timestamp": "2026-01-30T10:15:00Z"
   }

Mobile Optimizations

Pagination everywhere

Minimal payloads

Consistent DTOs

ISO-8601 timestamps

7. Order Service (Optional but Recommended)

Create food orders

Order status tracking

Kitchen order acceptance

Order States

CREATED

ACCEPTED

PREPARING

READY

COMPLETED

CANCELLED

8. Technical Implementation Details

Spring Data JPA

Specification / Criteria API for dynamic filtering

DTO + MapStruct

Validation (Jakarta Validation)

Global exception handling

Redis caching for:

kitchens

menus

labels

9. Security & Access Control

JWT authentication

Role-based authorization

Public:

browse kitchens

search menu items

Protected:

kitchen registration

menu creation

order management

10. Deliverables

For each microservice:

Project structure

Entities & DTOs

Controllers, Services, Repositories

Search & filter logic

Swagger documentation

Sample API requests & responses

Database schema (DDL)

Ensure the solution is scalable, secure, and ready for Flutter integration.