A utility class — one that exposes only static members — should not have a public
constructor. The default public constructor invites callers to instantiate a class that
was never meant to be created, which is confusing and pointless.

**How to fix it:**

1. **Declare a private constructor** to prevent instantiation.
2. **Mark the class `final`** so it cannot be subclassed.
3. **Consider whether the behaviour belongs in an instance type** instead of a static
   helper bag.

```java
// Better: a proper utility class
public final class StringUtils {

    private StringUtils() {
        // no instances
    }

    public static String trimToEmpty(String value) { ... }
}
```

A private constructor makes the "this class is not instantiable" contract explicit.
