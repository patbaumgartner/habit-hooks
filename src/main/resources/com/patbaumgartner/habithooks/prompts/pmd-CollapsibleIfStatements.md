Nested if statements with no intervening behavior often express one condition in two places. That makes the path harder to read and gives future changes an extra branch level to preserve.

Combine the conditions when doing so keeps the intent obvious:

```java
if (owner != null && owner.hasPets()) {
    render(owner);
}
```

Keep separate if statements when each branch needs its own comment, logging, early return, or domain name. The goal is flatter control flow, not clever boolean expressions.
