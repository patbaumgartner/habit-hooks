This field is only ever used inside a single method, so it does not need to be a field at
all. Storing transient state on the instance widens the object's lifecycle and invites
accidental sharing between calls.

**How to fix it:**

1. **Make it a local variable** — declare it where it is used so its scope matches its
   purpose.
2. **Pass it as a parameter** if a helper method needs the value.
3. **Keep fields for genuine object state** — values that must persist across method calls
   or define the object's identity.

```java
// Smell: temp is a field but only used in process()
private List<String> temp;
void process() {
    temp = new ArrayList<>();
    // ... uses temp ...
}

// Better: scope it locally
void process() {
    List<String> temp = new ArrayList<>();
    // ... uses temp ...
}
```

Local variables make data flow obvious and keep instances free of incidental state.
