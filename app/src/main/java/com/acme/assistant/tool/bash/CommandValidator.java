package com.acme.assistant.tool.bash;

import java.util.List;
import java.util.regex.Pattern;

public class CommandValidator {

    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile("\\brm\\s+-[^\\s]*r[^\\s]*f"),
            Pattern.compile("\\brm\\s+-[^\\s]*f[^\\s]*r"),
            Pattern.compile("\\bshutdown\\b"),
            Pattern.compile("\\breboot\\b"),
            Pattern.compile("\\bmkfs\\b"),
            Pattern.compile("\\bdd\\s+.*of=/dev/"),
            Pattern.compile(":(\\s*)\\{\\s*:\\|:\\s*&\\s*\\}"),
            Pattern.compile("\\bchmod\\s+-R\\s+777\\s+/\\s*$"),
            Pattern.compile("\\b>\\.?/dev/[sh]d[a-z]"),
            Pattern.compile("\\bcurl\\b.*\\|\\s*\\bsh\\b"),
            Pattern.compile("\\bwget\\b.*\\|\\s*\\bsh\\b")
    );

    public void validate(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("명령어가 비어 있습니다");
        }

        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(command).find()) {
                throw new SecurityException("차단된 명령 패턴: " + pattern.pattern());
            }
        }
    }
}
