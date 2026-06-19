Unused local variables add noise. They force the reader to track a name that never
contributes to the outcome. They frequently survive as remnants of incomplete
refactorings where the logic was removed but the variable was left.

**How to fix it:**

Remove the declaration. If the right-hand side has a side effect you want to keep
(e.g. a void method call), call the method directly without assigning the result.

If you intentionally want to discard a value (e.g. a method returning a result you
don't need), an explicit comment or rename to `_` makes the intent clear in some
languages. In Java, simply don't assign the result.

```java
// Before
int unusedCount = calculateTotal();  // result never read

// After
calculateTotal();                    // if the side-effect is the point
// or remove the call entirely if neither the result nor the side-effect is needed
```
