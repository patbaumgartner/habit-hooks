A `return` statement as the last line of a `void` method does nothing — control already
returns to the caller when the method body ends. The redundant statement adds clutter and
can momentarily suggest the method has more flow than it does.

**How to fix it:**

1. **Delete the trailing `return;`** at the end of a `void` method.
2. **Keep early `return;` statements** that genuinely short-circuit execution.
3. **Reach for guard clauses** instead of a final return when you want to express "nothing
   left to do".

```java
// Smell: the final return is redundant
void process(Item item) {
    validate(item);
    store(item);
    return;
}

// Better
void process(Item item) {
    validate(item);
    store(item);
}
```

Removing dead control statements keeps method bodies honest about their real flow.
