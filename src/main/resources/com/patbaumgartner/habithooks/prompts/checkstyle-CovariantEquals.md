A covariant `equals` — for example `boolean equals(MyType other)` — does not override
`Object.equals(Object)`; it overloads it. Collections and most library code call
`equals(Object)`, so your specialised method is silently skipped and equality breaks.

**How to fix it:**

1. **Override `equals(Object)`** with the exact signature from `Object`.
2. **Add `@Override`** so the compiler confirms you really are overriding it.
3. **Override `hashCode()` too**, keeping it consistent with `equals`.

```java
// Smell: this overloads, it does not override
public boolean equals(MyType other) { ... }

// Better
@Override
public boolean equals(Object o) {
    if (!(o instanceof MyType other)) return false;
    return ...;
}
```

Matching `Object.equals(Object)` is the only way your equality logic actually gets used.
