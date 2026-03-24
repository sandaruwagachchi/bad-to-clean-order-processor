# Day 4 Completed - Bad OrderProcessor to Clean Architecture

This project now contains both:

- `com.training.bad` - original monolithic code kept for before/after comparison
- `com.training.clean` - refactored layered solution using SOLID + patterns

## Before vs After

### Before (bad)

- Single `OrderProcessor` class with 200+ lines and one large `processOrder()` method
- String-based payment `if/else`
- Scattered mutable state and setters
- Direct DB + gateway + file operations in one class

### After (clean)

- `controller` layer accepts request DTO and returns response DTO
- `service` layer orchestrates business flow
- `model` layer contains domain objects and Builder
- `repository` layer handles persistence behind an interface
- `payment` layer uses Strategy pattern
- `notification` layer handles email/audit/invoice side effects

## What was requested and what is now implemented

- MVC-style clean flow (`controller` -> `service` -> `repository`/`payment`/`notification`)
- Strategy pattern for payment processing:
  - `CreditCardPaymentStrategy`
  - `PayPalPaymentStrategy`
  - `BankTransferPaymentStrategy`
- Builder pattern for safe order creation: `Order.builder()`
- SOLID-driven separation of responsibilities
- Beginner-friendly naming and small focused methods
- Clean layer written with **Java classes and interfaces only** (`record` is not used in the clean package)


## Patterns and principles applied

- **Builder**: `Order.builder()` ensures valid object construction
- **Strategy**: `PaymentStrategy` implementations per payment type
- **SRP**: each class has one focused responsibility
- **OCP**: new payment type can be added without changing `OrderService`
- **DIP**: service depends on `OrderRepository`, `PaymentGateway`, `NotificationService` abstractions
- **Clean code**: meaningful names, extracted methods, constants for business rules

## How demo works now

`com.training.bad.Main` is now a clean runner that uses:

1. `OrderController`
2. DTO request objects
3. `OrderService` orchestration
4. Strategy-based payment execution

The same business scenarios are demonstrated (success, validation failures, payment failure).

## Tests

`src/test/java/com/training/bad/OrderProcessorTest.java` now validates the clean flow (controller + service + strategy + builder).

## Run

```bash
mvn test
mvn exec:java
mvn package
java -jar target/bad-order-processor.jar
```
