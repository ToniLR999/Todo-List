package com.tonilr.ToDoList.service;


import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

/**
 * Service class for sanitizing user input to prevent XSS attacks and ensure data security.
 * Provides methods to clean HTML content, plain text, and email addresses
 * by removing potentially dangerous characters and allowing only safe content.
 */
@Service
public class SanitizationService {
    
    /**
     * Sanitizes HTML content by allowing only safe HTML tags and attributes.
     * Uses JSoup library to clean potentially malicious HTML while preserving
     * basic formatting elements like paragraphs, lists, and links.
     * @param htmlContent HTML content to sanitize
     * @return Sanitized HTML content or null if input is null/empty
     */
    public String sanitizeHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return htmlContent;
        }
        
        Safelist safelist = Safelist.basic()
            .addTags("span", "div", "p", "br", "strong", "em", "u", "ol", "ul", "li")
            .addAttributes(":all", "style", "class")
            .addProtocols("a", "href", "http", "https", "mailto");
        
        return Jsoup.clean(htmlContent, safelist);
    }
    
    /**
     * Sanitizes plain text by removing potentially dangerous HTML characters.
     * Prevents XSS attacks by escaping or removing characters that could be used
     * in malicious scripts or HTML injection.
     * @param text Plain text to sanitize
     * @return Sanitized text or null if input is null
     */
    public String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        
        return text.replaceAll("[<>\"']", "");
    }
    
    /**
     * Sanitizes email addresses by allowing only valid email characters.
     * Removes any characters that are not typically found in valid email addresses
     * to prevent injection attacks and ensure email format compliance.
     * @param email Email address to sanitize
     * @return Sanitized email address or null if input is null
     */
    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        return email.replaceAll("[^a-zA-Z0-9@._-]", "");
    }
}