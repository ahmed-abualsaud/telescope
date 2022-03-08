
package org.telescope.handler;

import io.netty.channel.ChannelHandler;

import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.model.Position;
import org.telescope.server.BaseDataHandler;

@ChannelHandler.Sharable
public class HemisphereHandler extends BaseDataHandler {

    private int latitudeFactor;
    private int longitudeFactor;

    public HemisphereHandler(Config config) {
        String latitudeHemisphere = config.getString(Keys.LOCATION_LATITUDE_HEMISPHERE);
        if (latitudeHemisphere != null) {
            if (latitudeHemisphere.equalsIgnoreCase("N")) {
                latitudeFactor = 1;
            } else if (latitudeHemisphere.equalsIgnoreCase("S")) {
                latitudeFactor = -1;
            }
        }
        String longitudeHemisphere = config.getString(Keys.LOCATION_LATITUDE_HEMISPHERE);
        if (longitudeHemisphere != null) {
            if (longitudeHemisphere.equalsIgnoreCase("E")) {
                longitudeFactor = 1;
            } else if (longitudeHemisphere.equalsIgnoreCase("W")) {
                longitudeFactor = -1;
            }
        }
    }

    @Override
    protected Position handlePosition(Position position) {
        if (latitudeFactor != 0) {
            position.setLatitude(Math.abs(position.getLatitude()) * latitudeFactor);
        }
        if (longitudeFactor != 0) {
            position.setLongitude(Math.abs(position.getLongitude()) * longitudeFactor);
        }
        return position;
    }

}
