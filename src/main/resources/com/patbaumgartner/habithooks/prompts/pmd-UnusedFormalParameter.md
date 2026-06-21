# Unused Formal Parameter

An unused parameter makes a method signature say more than the implementation needs. For private methods, it is usually leftover design noise from an earlier version of the code.

Prefer removing the parameter and updating the callers. If the name starts with `ignored` or `unused`, PMD treats that as intentional and leaves it alone.

## Refactor moves

1. Remove the unused parameter from the private method or constructor.
2. Update every call site to pass fewer arguments.
3. If the parameter exists only to satisfy a framework or interface contract, keep it out of private helpers or rename it with an intentional `ignored` or `unused` prefix.
4. Rerun the focused tests for the class you touched.

## Check yourself

Run the relevant unit test and then `habit-hooks`.
