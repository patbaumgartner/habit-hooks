Deeply nested conditions create "arrow code" — each level of nesting pushes the
happy path further right and makes the logic harder to read linearly.

**How to fix it:**

**Guard clauses** (early returns) are the most effective tool:

```java
// Before — nested
void process(Order order) {
    if (order != null) {
        if (order.isValid()) {
            if (order.hasItems()) {
                doWork(order);
            }
        }
    }
}

// After — guard clauses
void process(Order order) {
    if (order == null) return;
    if (!order.isValid()) return;
    if (!order.hasItems()) return;
    doWork(order);
}
```

Guard clauses express preconditions clearly and keep the happy path un-nested.
When nesting remains after guard clauses, extract the inner block to a named method.
