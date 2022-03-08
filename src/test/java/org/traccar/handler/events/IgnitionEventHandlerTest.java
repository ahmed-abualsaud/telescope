package org.telescope.handler.events;

import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;
import org.telescope.BaseTest;
import org.telescope.TestIdentityManager;
import org.telescope.model.Event;
import org.telescope.model.Position;

public class IgnitionEventHandlerTest extends BaseTest {
    
    @Test
    public void testIgnitionEventHandler() {
        
        IgnitionEventHandler ignitionEventHandler = new IgnitionEventHandler(new TestIdentityManager());
        
        Position position = new Position();
        position.set(Position.KEY_IGNITION, true);
        position.setValid(true);
        Map<Event, Position> events = ignitionEventHandler.analyzePosition(position);
        assertNull(events);
    }

}
