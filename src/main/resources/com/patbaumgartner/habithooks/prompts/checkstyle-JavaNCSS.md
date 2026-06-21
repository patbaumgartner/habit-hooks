Non-Commenting Source Statements (NCSS) counts the executable statements in a method,
class, or file. A high count means the unit is doing too much work in one place,
which makes it harder to read, test, and change safely.

**How to fix it:**

1. **Extract methods** — group related statements into well-named helpers that each do
   one thing.
2. **Split the class** — if a class is large, move cohesive groups of fields and methods
   into focused collaborators.
3. **Replace branching with polymorphism** — long procedural blocks often hide a missing
   abstraction.

```java
// Smell: one method with 30+ statements doing parse + validate + transform + persist

// Better: each step is its own intention-revealing method
var parsed = parse(input);
validate(parsed);
var result = transform(parsed);
repository.save(result);
```

NCSS is a size signal, not a style nit. Keep methods around 20 statements and classes
small enough to hold in your head.
