Cyclomatic complexity counts the independent paths through a method. PMD flags methods
whose decision count is high, because every extra branch is another case to test and
another path for a reader to trace.

**How to fix it:**

1. **Guard clauses** — return early for edge cases to flatten nesting.
2. **Extract boolean helpers** — give complex conditions descriptive names.
3. **Replace conditionals with polymorphism** — swap `switch`/`if-else` chains on a type
   for dispatch, or use a strategy/lookup map.

```java
// Before — many branches in one method
String classify(int score) {
    if (score >= 90) return "A";
    if (score >= 80) return "B";
    if (score >= 70) return "C";
    return "F";
}

// After — drive it from data
private static final NavigableMap<Integer, String> GRADES = ...;
String classify(int score) {
    return GRADES.floorEntry(score).getValue();
}
```

Each `if`, `for`, `while`, `case`, `catch`, `&&`, `||`, and `?:` adds a path. Aim to keep
methods focused on a single decision.
