Public fields and mutable accessible state break encapsulation.
The class cannot enforce invariants, and every caller becomes an implicit dependent
on the internal representation. Changing the representation requires touching all callers.

**How to fix it:**

1. Make fields `private`.
2. Expose only what callers need, via focused accessor methods or record components.
3. Prefer immutable value objects — records are ideal for data carriers.
4. When mutation is needed, validate in the setter instead of trusting callers.

Encapsulation is not about hiding — it's about owning your invariants. A class that
lets anyone write to its fields is not an object; it's a struct with extra steps.
