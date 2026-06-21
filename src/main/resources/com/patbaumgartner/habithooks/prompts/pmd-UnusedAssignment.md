# Unused Assignment

An assignment that is overwritten or never read is either noise or a hidden bug. It asks readers to track state that has no effect on the result.

Prefer deleting the assignment. If the assigned value was meant to influence behavior, add the missing read or return the intended value explicitly.

## Refactor moves

1. Remove initial values that are always overwritten before use.
2. Replace temporary variables with direct returns when the variable adds no meaning.
3. Keep assignments that intentionally trigger side effects out of this pattern; make those side effects explicit.
4. Add or update a test when the assignment looked like a missed branch or return value.

## Check yourself

Run the relevant unit test and then `habit-hooks`.
