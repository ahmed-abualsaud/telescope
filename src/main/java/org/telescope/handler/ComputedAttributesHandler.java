
package org.telescope.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.netty.channel.ChannelHandler;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.AttributesManager;
import org.telescope.database.IdentityManager;
import org.telescope.model.Attribute;
import org.telescope.model.Device;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

@ChannelHandler.Sharable
public class ComputedAttributesHandler extends BaseDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputedAttributesHandler.class);

    private final IdentityManager identityManager;
    private final AttributesManager attributesManager;

    private final JexlEngine engine;

    private final boolean includeDeviceAttributes;

    public ComputedAttributesHandler(
            Config config, IdentityManager identityManager, AttributesManager attributesManager) {
        this.identityManager = identityManager;
        this.attributesManager = attributesManager;
        engine = new JexlEngine();
        engine.setStrict(true);
        engine.setFunctions(Collections.singletonMap("math", Math.class));
        includeDeviceAttributes = config.getBoolean(Keys.PROCESSING_COMPUTED_ATTRIBUTES_DEVICE_ATTRIBUTES);
    }

    private MapContext prepareContext(Position position) {
        MapContext result = new MapContext();
        if (includeDeviceAttributes) {
            Device device = identityManager.getById(position.getDeviceId());
            if (device != null) {
                for (Object key : device.getAttributes().keySet()) {
                    result.set((String) key, device.getAttributes().get(key));
                }
            }
        }
        Set<Method> methods = new HashSet<>(Arrays.asList(position.getClass().getMethods()));
        methods.removeAll(Arrays.asList(Object.class.getMethods()));
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                String name = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);

                try {
                    if (!method.getReturnType().equals(Map.class)) {
                        result.set(name, method.invoke(position));
                    } else {
                        for (Object key : ((Map) method.invoke(position)).keySet()) {
                            result.set((String) key, ((Map) method.invoke(position)).get(key));
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException error) {
                    LOGGER.warn("Attribute reflection error", error);
                }
            }
        }
        return result;
    }

    /**
     * @deprecated logic needs to be extracted to be used in API resource
     */
    @Deprecated
    public Object computeAttribute(Attribute attribute, Position position) throws JexlException {
        return engine.createExpression(attribute.getExpression()).evaluate(prepareContext(position));
    }

    @Override
    protected Position handlePosition(Position position) {
        Collection<Attribute> attributes = attributesManager.getItems(
                attributesManager.getAllDeviceItems(position.getDeviceId()));
        for (Attribute attribute : attributes) {
            if (attribute.getAttribute() != null) {
                Object result = null;
                try {
                    result = computeAttribute(attribute, position);
                } catch (JexlException error) {
                    LOGGER.warn("Attribute computation error", error);
                }
                if (result != null) {
                    try {
                        switch (attribute.getType()) {
                            case "number":
                                Number numberValue = (Number) result;
                                position.getAttributes().put(attribute.getAttribute(), numberValue);
                                break;
                            case "boolean":
                                Boolean booleanValue = (Boolean) result;
                                position.getAttributes().put(attribute.getAttribute(), booleanValue);
                                break;
                            default:
                                position.getAttributes().put(attribute.getAttribute(), result.toString());
                        }
                    } catch (ClassCastException error) {
                        LOGGER.warn("Attribute cast error", error);
                    }
                }
            }
        }
        return position;
    }

}
