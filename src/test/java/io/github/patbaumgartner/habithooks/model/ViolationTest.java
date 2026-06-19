package io.github.patbaumgartner.habithooks.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViolationTest {

    @Test
    void createsViolationWithValidFields() {
        Violation v = new Violation("checkstyle:MethodLength", "src/Foo.java", 10, "Too long");
        assertThat(v.ruleId()).isEqualTo("checkstyle:MethodLength");
        assertThat(v.file()).isEqualTo("src/Foo.java");
        assertThat(v.line()).isEqualTo(10);
        assertThat(v.message()).isEqualTo("Too long");
    }

    @Test
    void locationIncludesLineWhenPositive() {
        Violation v = new Violation("pmd:GodClass", "src/Big.java", 1, "God class");
        assertThat(v.location()).isEqualTo("src/Big.java:1");
    }

    @Test
    void locationOmitsLineWhenNegative() {
        Violation v = new Violation("pmd:GodClass", "src/Big.java", -1, "God class");
        assertThat(v.location()).isEqualTo("src/Big.java");
    }

    @Test
    void throwsWhenRuleIdIsBlank() {
        assertThatThrownBy(() -> new Violation("", "src/Foo.java", 1, "msg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsWhenFileIsBlank() {
        assertThatThrownBy(() -> new Violation("rule:X", "", 1, "msg"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
