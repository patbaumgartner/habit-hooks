Reassigning a `for` loop's control variable inside the loop body makes the loop's range
hard to reason about. A reader can no longer trust the loop header to describe how the
variable advances.

**How to fix it:**

1. **Leave the control variable to the loop header** — let only the update clause change it.
2. **Use a separate local variable** if the body needs to adjust a value derived from the
   index.
3. **Switch to a for-each or stream** when you are simply iterating a collection.

```java
// Smell: body mutates the loop variable
for (int i = 0; i < items.size(); i++) {
    if (skip(items.get(i))) i++; // surprising jump
}

// Better: make the skip explicit with its own logic / different structure
for (int i = 0; i < items.size(); i++) {
    if (skip(items.get(i))) continue;
    process(items.get(i));
}
```

Keeping the control variable under the header's control makes loops predictable.
