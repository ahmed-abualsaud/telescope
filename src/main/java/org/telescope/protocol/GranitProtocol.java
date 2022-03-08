/*
 * Copyright 2016 - 2019 Anton Tananaev (anton@telescope.org)
 * Copyright 2017 - 2018 Andrey Kunitsyn (andrey@telescope.org)
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
package org.telescope.protocol;

import org.telescope.model.Command;
import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

public class GranitProtocol extends BaseProtocol {

    public GranitProtocol() {
        setSupportedDataCommands(
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE);
        setTextCommandEncoder(new GranitProtocolSmsEncoder(this));
        setSupportedTextCommands(
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_PERIODIC);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new GranitFrameDecoder());
                pipeline.addLast(new GranitProtocolEncoder(GranitProtocol.this));
                pipeline.addLast(new GranitProtocolDecoder(GranitProtocol.this));
            }
        });
    }

}
