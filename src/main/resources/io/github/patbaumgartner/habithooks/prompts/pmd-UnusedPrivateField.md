Unused private fields are dead weight. They increase cognitive load (the reader wonders
what the field is for), pollute serialized forms, and can confuse IDE navigation.

**How to fix it:**

Simply remove the field. If you removed the code that used it in a recent change,
the field should go with it. If it was added speculatively ("I might need this later"),
remove it now and add it back when you actually need it — you'll design it better
with real requirements.

Unused fields are also a common sign of an incomplete refactoring: a class was split
or functionality moved, but the old state was left behind.
