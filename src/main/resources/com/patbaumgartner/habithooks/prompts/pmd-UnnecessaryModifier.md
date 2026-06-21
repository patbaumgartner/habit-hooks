Some modifiers are implied by their context and repeating them is redundant. Interface methods
are already `public abstract`, interface fields are already `public static final`, and nested
types in interfaces are implicitly `static`. Spelling these out adds noise without changing
meaning.

**How to fix it:**

1. **Drop modifiers that the language already implies** in the surrounding context.
2. **Let interfaces stay declarative** — omit `public`/`abstract` on their members.
3. **Trust `final` defaults** on interface constants instead of restating them.

```java
// Smell: every modifier here is implied
interface Repository {
    public static final int MAX = 100;
    public abstract void save(Item item);
}

// Better
interface Repository {
    int MAX = 100;
    void save(Item item);
}
```

Removing implied modifiers keeps declarations concise and highlights the modifiers that do
carry intent.
