package com.tonilr.ToDoList.service;


import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class SanitizationService {
    
    /**
     * Sanitiza contenido HTML permitiendo solo etiquetas básicas seguras
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
     * Sanitiza texto plano eliminando caracteres peligrosos
     */
    public String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        
        return text.replaceAll("[<>\"']", "");
    }
    
    /**
     * Sanitiza email permitiendo solo caracteres válidos
     */
    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        return email.replaceAll("[^a-zA-Z0-9@._-]", "");
    }
}