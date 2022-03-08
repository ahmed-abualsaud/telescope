
package org.telescope.database;

import org.telescope.model.Device;
import org.telescope.model.Position;

public interface IdentityManager {

    long addUnknownDevice(String uniqueId);

    Device getById(long id);

    Device getByUniqueId(String uniqueId) throws Exception;

    String getDevicePassword(long id, String protocol, String defaultPassword);

    Position getLastPosition(long deviceId);

    boolean isLatestPosition(Position position);

    boolean lookupAttributeBoolean(
            long deviceId, String attributeName, boolean defaultValue, boolean lookupServer, boolean lookupConfig);

    String lookupAttributeString(
            long deviceId, String attributeName, String defaultValue, boolean lookupServer, boolean lookupConfig);

    int lookupAttributeInteger(
            long deviceId, String attributeName, int defaultValue, boolean lookupServer, boolean lookupConfig);

    long lookupAttributeLong(
            long deviceId, String attributeName, long defaultValue, boolean lookupServer, boolean lookupConfig);

    double lookupAttributeDouble(
            long deviceId, String attributeName, double defaultValue, boolean lookupServer, boolean lookupConfig);

}
