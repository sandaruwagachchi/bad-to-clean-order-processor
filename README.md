# Bad OrderProcessor — Day 4 Refactoring Exercise

A deliberately terrible `OrderProcessor` class violating every SOLID principle.
Your job is to refactor it. This project makes it **runnable** without any external infrastructure.

## What's been changed to make it runnable

| Original (broken)         | This version (runnable)                        |
|---------------------------|------------------------------------------------|
| MySQL on localhost         | H2 in-memory database (auto-created on start)  |
| Real HTTP payment APIs     | `FakePaymentGateway` stub (logs the calls)     |
| `javax.mail` SMTP          | Logs email body to console + `orders.log`      |
| No `main` method           | `Main.java` with 5 demo scenarios              |
| No tests                   | `OrderProcessorTest.java` (12 JUnit 5 tests)   |

**All SOLID violations and code smells are preserved exactly as-is.**

---

## Prerequisites

- Java 21+
- Maven 3.6+

---

## Run

```bash
# Run the demo scenarios
mvn exec:java

# Run the tests
mvn test

# Build a fat JAR and run it
mvn package
java -jar target/bad-order-processor.jar
```

---

## What the demo runs

| Scenario | Customer | Items                        | Payment      | Expected     |
|----------|----------|------------------------------|--------------|--------------|
| 1        | Alice    | 2x Mouse + 1x Keyboard       | Credit Card  | ✅ Success   |
| 2        | Bob      | 2x USB Hub                   | PayPal       | ✅ Success   |
| 3        | Alice    | 1x Laptop + 1x Monitor       | Bank Transfer| ✅ Success   |
| 4        | Alice    | Various bad inputs            | —            | ❌ Rejected  |
| 5        | Alice    | Mouse (declined card)         | Credit Card  | ❌ Declined  |

---

## Output files generated

- `orders.log` — append-only audit log of every processed order
- `invoice_1.txt`, `invoice_2.txt`, ... — one text invoice per successful order

---

## Code smells to find and fix

| Smell                        | Location                                   |
|------------------------------|--------------------------------------------|
| God class / God method       | `OrderProcessor` + `processOrder()` ~180 lines |
| Magic numbers                | `0.08`, `0.05`, `100`, `500`, `1000`, `16`, `9`, `587` |
| Cryptic abbreviations        | `c`, `u`, `p`, `tx`, `nm`, `pr`, `qty`, `fw`, `disc1` |
| Hardcoded credentials        | `apiKey`, `emailPass`, `p` (DB password)   |
| No Strategy pattern          | `if/else` chain on `paymentType` string    |
| No Builder pattern           | Scattered setters, no construction safety  |
| Broken Singleton             | Not thread-safe; `reset()` is dangerous    |
| Duplicate code               | `addItem()` vs `addItemWithDiscount()`     |
| Dead code                    | `processCreditCardOrder()`, `calcShipping()`|
| Violation of DIP             | Direct use of `FakePaymentGateway`, JDBC, `FileWriter` |
| Data consistency bug         | Payment charged before DB save — no rollback |

---

## Refactoring target

Split into **at least 5 classes/interfaces**:

```
Order              (Builder pattern)
OrderItem
PaymentStrategy    (interface)
  ├── CreditCardPaymentStrategy
  ├── PayPalPaymentStrategy
  └── BankTransferPaymentStrategy
OrderRepository    (interface)
DiscountCalculator
NotificationService (interface)
OrderService       (orchestrator - replaces OrderProcessor)
```
