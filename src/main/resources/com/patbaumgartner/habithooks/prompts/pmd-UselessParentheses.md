Parentheses that do not change evaluation order add visual noise and can make an expression
look more complicated than it is. Readers pause to work out whether the grouping matters,
when in fact it changes nothing.

**How to fix it:**

1. **Remove the redundant parentheses** when operator precedence already produces the intended
   result.
2. **Keep parentheses only where they clarify** genuinely ambiguous mixed-operator expressions.
3. **Prefer extracting a named variable** over deep parenthesising when an expression is hard
   to read.

```java
// Smell: the inner parentheses are redundant
return (value * scale) / total;

// Better: same result, less noise
return value * scale / total;
```

Lean expressions read faster and signal precedence intent only where it actually matters.
