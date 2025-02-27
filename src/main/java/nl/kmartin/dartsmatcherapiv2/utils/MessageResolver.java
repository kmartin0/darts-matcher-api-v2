package nl.kmartin.dartsmatcherapiv2.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageResolver {
    private final ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    public MessageResolver(ReloadableResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get a description with arguments from the description source using the description key.
     *
     * @param msgKey String Key of the description.
     * @param args   Object... argument parameters of the description.
     * @return String description from description source.
     */
    public String getMessage(String msgKey, Object... args) {
        try {
            return messageSource.getMessage(msgKey, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException ex) {
            return "No message found.";
        }
    }
}