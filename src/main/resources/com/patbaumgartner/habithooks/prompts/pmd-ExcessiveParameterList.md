A long parameter list makes a method call easy to scramble and hard to extend. Even when each value is simple, the caller has to remember ordering, nullability, defaults, and which values naturally belong together.

Look for a small object that already exists in the domain, such as a form, command, request, options, or criteria type. If the parameters are framework-bound, prefer the framework's binding object over inventing a wide internal API. Keep the behavior the same while making the call site harder to misuse.
