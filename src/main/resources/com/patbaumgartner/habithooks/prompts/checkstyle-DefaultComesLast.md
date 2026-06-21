By convention the `default` label belongs at the end of a `switch`. Placing it among the
cases makes the control flow harder to scan and can hide fall-through behaviour.

**How to fix it:**

1. **Move `default` to the bottom**, after all `case` labels.
2. **Keep each case terminated** with `break`, `return`, or an arrow form so order does not
   change behaviour.
3. **Prefer arrow switches** (`case X ->`) which read clearly and avoid fall-through
   entirely.

```java
// Better: default last
switch (status) {
    case OPEN -> open();
    case CLOSED -> close();
    default -> unknown();
}
```

A trailing `default` matches reader expectations and keeps the catch-all clearly separated
from the specific cases.
