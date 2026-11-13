# Contributing

Iron Quarry is my hobby project, and since the actual development is where the fun lies, I prefer to write the code myself. Translations are an exception and are very welcome. All other pull requests will be closed.

## Translations

Add a file named after your Minecraft locale code (e.g. `de_de.json`) next to `en_us.json` in `common/src/main/resources/assets/iron_quarry/lang/`.

- Keep the keys from `en_us.json` and only translate the values.
- Keep every `%s` and `\n`, but feel free to move them where they fit your language.
- Blocks like `§{RGB_LIGHT_GRAY Usage:}` start with a color name. Keep it and only translate the text after it.
- A block has to end before the next `\n`.

The documentation in `docs/` stays in English.

## Bugs

Found one? Open an [issue](https://github.com/RXJpaw/Minecraft-Iron-Quarry/issues).
