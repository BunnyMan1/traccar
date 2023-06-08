/*
 * Copyright 2016 - 2022 Anton Tananaev (anton@traccar.org)
 * Copyright 2016 - 2018 Andrey Kunitsyn (andrey@traccar.org)
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
package org.traccar.reports;

import org.jxls.util.JxlsHelper;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.model.Device;
import org.traccar.model.EventDTO;
import org.traccar.model.Geofence;
import org.traccar.model.Maintenance;
import org.traccar.reports.common.ReportUtils;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EventsReportProvider {

    private final Config config;
    private final ReportUtils reportUtils;
    private final Storage storage;

    @Inject
    public EventsReportProvider(Config config, ReportUtils reportUtils, Storage storage) {
        this.config = config;
        this.reportUtils = reportUtils;
        this.storage = storage;
    }

    private List<EventDTO> getEvents(long deviceId, Date from, Date to, long groupId) throws StorageException {
        List<EventDTO> events = storage.getGroupEvents(EventDTO.class, new Request(// change
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("deviceId", deviceId),
                        new Condition.Between("eventTime", "from", from, "to", to)),
                new Order("eventTime")), groupId, from, to);
        return events;
    }

    public Collection<EventDTO> getObjects(
            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Collection<String> types, Date from, Date to) throws StorageException {
        reportUtils.checkPeriodLimit(from, to);

        ArrayList<EventDTO> result = new ArrayList<>();
        for (Device device : reportUtils.getAccessibleDevices(userId, deviceIds, groupIds)) {
            Collection<EventDTO> events = getEvents(device.getId(), from, to, device.getGroupId());
            boolean all = types.isEmpty() || types.contains(EventDTO.ALL_EVENTS);
            for (EventDTO event : events) {
                if (all || types.contains(event.getType())) {
                    long geofenceId = event.getGeofenceId();
                    long maintenanceId = event.getMaintenanceId();
                    if ((geofenceId == 0 || reportUtils.getObject(userId, Geofence.class, geofenceId) != null)
                            && (maintenanceId == 0
                                    || reportUtils.getObject(userId, Maintenance.class, maintenanceId) != null)) {
                        result.add(event);
                    }
                }
            }
        }
        return result;
    }

    public void getExcel(
            OutputStream outputStream, long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Collection<String> types, Date from, Date to) throws StorageException, IOException {
        reportUtils.checkPeriodLimit(from, to);

        ArrayList<EventDTO> result = new ArrayList<>();
        for (Device device : reportUtils.getAccessibleDevices(userId, deviceIds, groupIds)) {
            Collection<EventDTO> events = getEvents(device.getId(), from, to, device.getGroupId());
            boolean all = types.isEmpty() || types.contains(EventDTO.ALL_EVENTS);
            for (EventDTO event : events) {

                if (all || types.contains(event.getType())) {
                    long geofenceId = event.getGeofenceId();
                    long maintenanceId = event.getMaintenanceId();
                    if ((geofenceId == 0 || reportUtils.getObject(userId, Geofence.class, geofenceId) != null)
                            && (maintenanceId == 0
                                    || reportUtils.getObject(userId, Maintenance.class, maintenanceId) != null)) {
                        result.add(event);
                    }
                }
            }
        }

        File file = Paths.get(config.getString(Keys.TEMPLATES_ROOT), "export", "events2.xlsx").toFile();
        try (InputStream inputStream = new FileInputStream(file)) {
            var context = reportUtils.initializeContext(userId);
            context.putVar("events", result);
            context.putVar("from", from);
            context.putVar("to", to);
            JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
                    .processTemplate(inputStream, outputStream, context);
        }

    }
}
