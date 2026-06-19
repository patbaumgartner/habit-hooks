Methods with more than 5 parameters are hard to call correctly, hard to mock in tests,
and signal that either the method is doing too much or that related parameters belong together.

**How to fix it:**

1. Group related parameters into a Parameter Object (a record or a small immutable class).
2. If parameters represent separate concerns, split the method.
3. If a parameter is only needed for one code path, consider splitting into two focused methods.

```java
// Before — 6 parameters
void createOrder(String userId, String productId, int qty, String currency, String address, boolean urgent)

// After — Parameter Object
record OrderRequest(String userId, String productId, int qty, String currency, String address, boolean urgent) {}
void createOrder(OrderRequest request)
```

The test setup effort of a 6-parameter method is a compounding cost. Every collaborator
that calls it becomes brittle; every refactor touches too many sites.
