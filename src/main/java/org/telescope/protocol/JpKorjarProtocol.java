/*
 * Copyright 2016 Nyash (nyashh@gmail.com)
 * Copyright 2018 Anton Tananaev (anton@telescope.org)
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

import org.telescope.server.BaseProtocol;
import org.telescope.server.PipelineBuilder;
import org.telescope.server.TrackerServer;

import io.netty.handler.codec.string.StringDecoder;

public class JpKorjarProtocol extends BaseProtocol {

    public JpKorjarProtocol() {
        addServer(new TrackerServer(false, this.getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new JpKorjarFrameDecoder());
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new JpKorjarProtocolDecoder(JpKorjarProtocol.this));
            }
        });
    }

}