/*
 * Copyright 2015 - 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.database.ConnectionManager;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TripEventSocket extends WebSocketAdapter implements ConnectionManager.TripEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripEventSocket.class);

    private final String logId;
    private final boolean dataSource;

    public TripEventSocket(String logId, boolean dataSource) {
        this.logId = logId;
        this.dataSource = dataSource;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        Map<String, String> data = new HashMap<>();
        data.put("message", "connected successfully");
        sendData(data);

        Context.getConnectionManager().addListener(logId, this);
    }
    
    @Override
    public void onUpdateDriverLocation(String message) {
        if (!dataSource) {
            Map<String, String> data = new HashMap<>();
            data.put("location", message);
            sendData(data);
        }
    }
    
    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        
        Context.getConnectionManager().updateDriverLocation(logId, message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        Context.getConnectionManager().removeListener(logId, this);
    }

    private void sendData(Map<String, String> data) {
        if (isConnected()) {
            try {
                getRemote().sendString(Context.getObjectMapper().writeValueAsString(data), null);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Socket JSON formatting error", e);
            }
        }
    }
}
