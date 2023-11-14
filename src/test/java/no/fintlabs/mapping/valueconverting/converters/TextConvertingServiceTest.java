package no.fintlabs.mapping.valueconverting.converters;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextConvertingServiceTest {

    TextConvertingService textConvertingService = new TextConvertingService();

    @Test
    public void shouldHandleNullInConvertToUpperCase() {
        assertThat(textConvertingService.toUpperCase(null)).isNull();
    }

    @Test
    public void shouldHandleEmptyInConvertToUpperCase() {
        assertThat(textConvertingService.toUpperCase(""))
                .isEqualTo("");
    }

    @Test
    public void shouldBlankEmptyInConvertToUpperCase() {
        assertThat(textConvertingService.toUpperCase(" "))
                .isEqualTo(" ");
    }

    @Test
    public void shouldConvertToUpperCase() {
        assertThat(textConvertingService.toUpperCase("AbC 123 dEF"))
                .isEqualTo("ABC 123 DEF");
    }


    @Test
    public void shouldHandleNullInConvertToLowerCase() {
        assertThat(textConvertingService.toLowerCase(null)).isNull();
    }

    @Test
    public void shouldHandleEmptyInConvertToLowerCase() {
        assertThat(textConvertingService.toLowerCase(""))
                .isEqualTo("");
    }

    @Test
    public void shouldBlankEmptyInConvertToLowerCase() {
        assertThat(textConvertingService.toLowerCase(" "))
                .isEqualTo(" ");
    }

    @Test
    public void shouldConvertToLowerCase() {
        assertThat(textConvertingService.toLowerCase("AbC 123 dEF"))
                .isEqualTo("abc 123 def");
    }

    @Test
    public void shouldHandleNullInConvertToLowerCaseWithFirstLetterUpperCase() {
        assertThat(textConvertingService.toLowerCaseWithFirstLetterUpperCase(null)).isNull();
    }

    @Test
    public void shouldHandleEmptyInConvertToLowerCaseWithFirstLetterUpperCase() {
        assertThat(textConvertingService.toLowerCaseWithFirstLetterUpperCase(""))
                .isEqualTo("");
    }

    @Test
    public void shouldHandleBlankInConvertToLowerCaseWithFirstLetterUpperCase() {
        assertThat(textConvertingService.toLowerCaseWithFirstLetterUpperCase(" "))
                .isEqualTo(" ");
    }

    @Test
    public void shouldConvertToLowerCaseWithFirstLetterUpperCase() {
        assertThat(textConvertingService.toLowerCaseWithFirstLetterUpperCase("abC 123 dEF"))
                .isEqualTo("Abc 123 def");
    }

}
