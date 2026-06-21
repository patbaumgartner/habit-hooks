Tab characters make formatting depend on editor settings. A file can look aligned for one developer and ragged for another, and AI-generated patches become noisier because indentation is not represented consistently.

Replace tabs with the project's configured indentation. If the project intentionally uses tabs, tune the scaffolded Checkstyle rule instead of changing hundreds of lines just to satisfy a mismatched default. Treat large tab-only diffs as formatting work, not as code cleanup.
