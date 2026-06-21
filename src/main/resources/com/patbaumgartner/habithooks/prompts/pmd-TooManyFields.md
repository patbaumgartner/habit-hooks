A class with too many fields usually carries more than one responsibility. The fields
tend to form clusters that change for different reasons, which is a sign the class should
be split.

**How to fix it:**

1. **Group related fields** — if several fields always travel together, extract them into
   a small value object or record.
2. **Apply the Single Responsibility Principle** — move each cluster of state plus the
   behaviour that uses it into its own class.
3. **Question configuration soup** — long lists of flags often belong in a dedicated
   options/config type.

```java
// Smell: Order has customerName, customerEmail, street, city, zip, ...

// Better:
record Customer(String name, String email) {}
record Address(String street, String city, String zip) {}
class Order { Customer customer; Address shipTo; }
```

Fewer fields per class means clearer ownership of state and easier testing.
