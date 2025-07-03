package nl.kmartin.dartsmatcherapiv2.utils;

import nl.kmartin.dartsmatcherapiv2.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.config.LocaleConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocaleConfig.class)
@Import({MessageResolver.class})
class MessageResolverTests {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MessageResolver messageResolver;

    @AfterEach
    void after() {
        LocaleContextHolder.setLocale(Locale.getDefault());
    }

    @Test
    void testGetMessageNoArgs_isCorrect() {
        Assertions.assertEquals(
                "Cannot contain whitespaces.",
                messageResolver.getMessage("message.no.whitespace.allowed")
        );
    }

    @Test
    void testGetMessageNoArgsLocaleNl_isCorrect() {
        LocaleContextHolder.setLocale(new Locale("nl"));
        Assertions.assertEquals(
                "Mag geen spaties bevatten.",
                messageResolver.getMessage("message.no.whitespace.allowed")
        );
    }

    @Test
    void testNotExistingMessageNoArgs_isNoMessageAvailable() {
        Assertions.assertEquals(
                "No message found.",
                messageResolver.getMessage("test.non.existing.message")
        );
    }

    @Test
    void testMessageWithArgs_isCorrect() {
        Assertions.assertEquals(
                "test is already in use.",
                messageResolver.getMessage("message.resource.already.exists", "test")
        );
    }

    @Test
    void testNotExistingMessageWithArgs_isNoMessageAvailable() {
        Assertions.assertEquals("No message found.",
                messageResolver.getMessage("test.non.existing.message", 1, 12)
        );
    }

}