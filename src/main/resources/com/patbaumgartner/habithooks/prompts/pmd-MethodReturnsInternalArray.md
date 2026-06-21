Returning a reference to an internal array breaks encapsulation: the caller can mutate the
array's elements and silently change the object's state from the outside.

**How to fix it:**

1. **Return a copy** — `array.clone()` or `Arrays.copyOf(...)` for arrays.
2. **Return an unmodifiable view** — expose `List.of(...)` or
   `Collections.unmodifiableList(...)` instead of the raw array where practical.
3. **Expose intent, not storage** — provide focused accessors (e.g. `get(int index)`,
   `size()`) rather than handing out the backing structure.

```java
// Smell: callers can mutate our internal state
public int[] getScores() {
    return scores;
}

// Better: hand back a defensive copy
public int[] getScores() {
    return scores.clone();
}
```

Defensive copies keep an object the sole owner of its data and prevent action-at-a-distance
bugs.
