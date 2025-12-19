package com.internhub.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages. Automatically uses the
 * current user's locale from LocaleContextHolder.
 */
@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get message for the given key using current locale.
     *
     * @param key Message key from properties file
     * @return Localized message
     */
    public String getMessage(String key) {
        return getMessage(key, (Object[]) null);
    }

    /**
     * Get message for the given key with parameters using current locale.
     *
     * @param key Message key from properties file
     * @param params Parameters to replace placeholders {0}, {1}, etc.
     * @return Localized message with parameters replaced
     */
    public String getMessage(String key, Object[] params) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, params, key, locale);
    }

    /**
     * Get message for the given key with specific locale.
     *
     * @param key Message key from properties file
     * @param locale Specific locale to use
     * @return Localized message
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }

    /**
     * Get message for the given key with parameters and specific locale.
     *
     * @param key Message key from properties file
     * @param params Parameters to replace placeholders
     * @param locale Specific locale to use
     * @return Localized message with parameters replaced
     */
    public String getMessage(String key, Object[] params, Locale locale) {
        return messageSource.getMessage(key, params, key, locale);
    }

    /**
     * Get current locale from context.
     *
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}
