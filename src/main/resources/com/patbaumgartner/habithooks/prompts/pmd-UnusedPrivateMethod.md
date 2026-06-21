# Unused Private Method

A private method that is never called is dead code. It makes the class harder to scan because future readers have to decide whether the method is intentionally dormant, accidentally orphaned, or part of an unfinished design.

Prefer deleting it. If the method captures behavior that still matters, move that behavior into a focused test or reconnect the call path that should use it.

## Refactor moves

1. Search for intended callers before deleting.
2. Delete the method when no caller exists.
3. Delete tests that only exercise the dead helper indirectly through reflection or loosen them to cover observable behavior.
4. If the method should exist for an upcoming feature, add the feature through a real call path instead of keeping speculative code.

## Check yourself

Run the relevant unit test and then `habit-hooks`.
