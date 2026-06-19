A God Class accumulates too much responsibility. It knows the domain model, orchestrates
workflows, handles persistence, formats output, and validates input — all at once.
The class has no single reason to change; it has a dozen.

**How to fix it:**

1. Apply the Single Responsibility Principle: each class should have one reason to change.
2. Identify distinct responsibilities (e.g. "data container", "workflow orchestrator",
   "formatter", "validator") and move each into its own class.
3. The orchestrator can remain as a thin coordinator that delegates to focused collaborators.

```
// Smell: OrderService handles validation, pricing, persistence, and notification

// Better:
OrderValidator     — validates business rules
PricingCalculator  — computes prices
OrderRepository    — persists orders
OrderNotifier      — sends confirmations
OrderService       — thin orchestrator delegating to the above
```

God classes are the most reliable predictor of long-term maintenance pain.
Split early; splitting late is painful.
