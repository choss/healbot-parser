package com.example.healbotparser;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Generates HTML reports from parsed HealBot data.
 */
public class HtmlReportGenerator {
    
    private final TemplateEngine templateEngine;
    
    /**
     * Creates a new HtmlReportGenerator with a configured Thymeleaf engine.
     */
    public HtmlReportGenerator() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }
    
    /**
     * Generates an HTML report from parsed HealBot data.
     * 
     * @param parser The HealBotParser containing parsed data
     * @param outputPath Path where the HTML report should be written
     * @throws IOException if the report cannot be written
     */
    public void generateReport(HealBotParser parser, String outputPath) throws IOException {
        Map<String, Map<String, HealBotParser.KeyComboConfig>> keyCombos = parser.getEnabledKeyCombos();
        Set<String> characters = parser.getCharacters();
        
        Context context = new Context();
        context.setVariable("characters", characters);
        context.setVariable("keyCombos", keyCombos);
        
        String html = templateEngine.process("report", context);
        
        Files.writeString(Paths.get(outputPath), html);
    }
    
    /**
     * Generates an HTML report and returns it as a string.
     * 
     * @param parser The HealBotParser containing parsed data
     * @return The HTML report as a string
     */
    public String generateReportString(HealBotParser parser) {
        Map<String, Map<String, HealBotParser.KeyComboConfig>> keyCombos = parser.getEnabledKeyCombos();
        Set<String> characters = parser.getCharacters();
        
        Context context = new Context();
        context.setVariable("characters", characters);
        context.setVariable("keyCombos", keyCombos);
        
        return templateEngine.process("report", context);
    }
}
