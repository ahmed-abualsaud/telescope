
package org.telescope.javel.framework.notification;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.User;
import org.telescope.reports.ReportUtils;
import org.telescope.server.Context;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;

public final class TextTemplateFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextTemplateFormatter.class);

    private TextTemplateFormatter() {
    }

    public static VelocityContext prepareContext(User user) {

        VelocityContext velocityContext = new VelocityContext();

        if (user != null) {
            velocityContext.put("user", user);
            velocityContext.put("timezone", ReportUtils.getTimezone(user.getId()));
        }

        velocityContext.put("webUrl", Context.getVelocityEngine().getProperty("web.url"));
        velocityContext.put("dateTool", new DateTool());
        velocityContext.put("numberTool", new NumberTool());
        velocityContext.put("locale", Locale.getDefault());

        return velocityContext;
    }

    public static Template getTemplate(String name, String path) {

        String templateFilePath;
        Template template;

        try {
            templateFilePath = Paths.get(path, name + ".vm").toString();
            template = Context.getVelocityEngine().getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        } catch (ResourceNotFoundException error) {
            LOGGER.warn("Notification template error", error);
            templateFilePath = Paths.get(path, "unknown.vm").toString();
            template = Context.getVelocityEngine().getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        }
        return template;
    }

    public static FullMessage formatFullMessage(VelocityContext velocityContext, String name) {
        String formattedMessage = formatMessage(velocityContext, name, "full");
        return new FullMessage((String) velocityContext.get("subject"), formattedMessage);
    }

    public static String formatShortMessage(VelocityContext velocityContext, String name) {
        return formatMessage(velocityContext, name, "short");
    }

    private static String formatMessage(
            VelocityContext velocityContext, String name, String templatePath) {

        StringWriter writer = new StringWriter();
        getTemplate(name, templatePath).merge(velocityContext, writer);
        return writer.toString();
    }

}
