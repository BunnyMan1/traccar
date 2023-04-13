/*
 * Copyright 2014 - 2022 Anton Tananaev (anton@traccar.org)
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
package org.traccar.handler;

import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseDataHandler;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.UnitsConverter;
import org.traccar.helper.model.AttributeUtil;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;

@Singleton
@ChannelHandler.Sharable
public class FilterHandler extends BaseDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterHandler.class);

    private final boolean enabled;
    private final boolean filterInvalid;
    private final boolean filterZero;
    private final boolean filterDuplicate;
    private final long filterFuture;
    private final long filterPast;
    private final boolean filterApproximate;
    private final int filterAccuracy;
    private final boolean filterStatic;
    private final int filterDistance;
    private final int filterMaxSpeed;
    private final long filterMinPeriod;
    private final boolean filterRelative;
    private final long skipLimit;
    private final boolean skipAttributes;

    private final CacheManager cacheManager;
    private final Storage storage;

    @Inject
    public FilterHandler(Config config, CacheManager cacheManager, Storage storage) {
        // System.out.println("inside filter handler");
        enabled = config.getBoolean(Keys.FILTER_ENABLE);
        filterInvalid = config.getBoolean(Keys.FILTER_INVALID);
        filterZero = config.getBoolean(Keys.FILTER_ZERO);
        filterDuplicate = config.getBoolean(Keys.FILTER_DUPLICATE);
        filterFuture = config.getLong(Keys.FILTER_FUTURE) * 1000;
        filterPast = config.getLong(Keys.FILTER_PAST) * 1000;
        filterAccuracy = config.getInteger(Keys.FILTER_ACCURACY);
        filterApproximate = config.getBoolean(Keys.FILTER_APPROXIMATE);
        filterStatic = config.getBoolean(Keys.FILTER_STATIC);
        filterDistance = config.getInteger(Keys.FILTER_DISTANCE);
        filterMaxSpeed = config.getInteger(Keys.FILTER_MAX_SPEED);
        filterMinPeriod = config.getInteger(Keys.FILTER_MIN_PERIOD) * 1000L;
        filterRelative = config.getBoolean(Keys.FILTER_RELATIVE);
        skipLimit = config.getLong(Keys.FILTER_SKIP_LIMIT) * 1000;
        skipAttributes = config.getBoolean(Keys.FILTER_SKIP_ATTRIBUTES_ENABLE);
        this.cacheManager = cacheManager;
        this.storage = storage;
    }

    private Position getPrecedingPosition(long deviceId, Date date) throws StorageException {
        // System.out.println(" inside get preceeding pos");
        return storage.getObject(Position.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("deviceId", deviceId),
                        new Condition.Compare("fixTime", "<=", "time", date)),
                new Order("fixTime", true, 1)));
    }

    private boolean filterInvalid(Position position) {
        // System.out.println("inside filter invalid");
        return filterInvalid && (!position.getValid()
                || position.getLatitude() > 90 || position.getLongitude() > 180
                || position.getLatitude() < -90 || position.getLongitude() < -180);
    }

    private boolean filterZero(Position position) {
        // System.out.println("inside filter zero");
        return filterZero && position.getLatitude() == 0.0 && position.getLongitude() == 0.0;
    }

    private boolean filterDuplicate(Position position, Position last) {
    //    System.out.println(" ---------------------------------> cond.  " );
    //    System.out.println(last!= null);
        if (filterDuplicate && last != null && position.getFixTime().equals(last.getFixTime())) {
            for (String key : position.getAttributes().keySet()) {

                if (key.equals("archive"))
                    return true;
                if (key.equals("hours"))
                    continue;

                if (!last.hasAttribute(key)) {
                    System.out.println("Sending false in filterDuplicate due to : " + key);
                    return false;
                }
            }
            // System.out.println("True : " + position.getAddress() +
            // position.getFixTime());
            return true;
        }
        return false;
    }

    private boolean filterDuplicateFromList(Position position, List<Position> lastItems) {
        // System.out.println("inside filter duplicate from list");
        if (lastItems == null)
            return false;
        for (Position p : lastItems) {
            var res = filterDuplicate(position, p);
            if (res) {
                // System.out.println("Sending true in filterDuplicateFromList.");
                return true;
            }
        }
        return false;
    }

    private boolean filterFuture(Position position) {
        // System.out.println("inside filter future");
        return filterFuture != 0 && position.getFixTime().getTime() > System.currentTimeMillis() + filterFuture;
    }

    private boolean filterPast(Position position) {
        // System.out.println("inside filter past");
        return filterPast != 0 && position.getFixTime().getTime() < System.currentTimeMillis() - filterPast;
    }

    private boolean filterAccuracy(Position position) {
        // System.out.println("inside filter accuracy");
        return filterAccuracy != 0 && position.getAccuracy() > filterAccuracy;
    }

    private boolean filterApproximate(Position position) {
        // System.out.println("inside filter approximate");
        return filterApproximate && position.getBoolean(Position.KEY_APPROXIMATE);
    }

    private boolean filterStatic(Position position) {
        // System.out.println("inside filter static");
        return filterStatic && position.getSpeed() == 0.0;
    }

    private boolean filterDistance(Position position, Position last) {
        // System.out.println("inside filter distance");
        if (filterDistance != 0 && last != null) {
            return position.getDouble(Position.KEY_DISTANCE) < filterDistance;
        }
        return false;
    }

    private boolean filterMaxSpeed(Position position, Position last) {
        // System.out.println("inside filter max speed");
        if (filterMaxSpeed != 0 && last != null) {
            double distance = position.getDouble(Position.KEY_DISTANCE);
            double time = position.getFixTime().getTime() - last.getFixTime().getTime();
            return UnitsConverter.knotsFromMps(distance / (time / 1000)) > filterMaxSpeed;
        }
        return false;
    }

    private boolean filterMinPeriod(Position position, Position last) {
        // System.out.println("inside filter min period");
        if (filterMinPeriod != 0 && last != null) {
            long time = position.getFixTime().getTime() - last.getFixTime().getTime();
            return time > 0 && time < filterMinPeriod;
        }
        return false;
    }

    // private boolean filterMaxSpeedFromList(Position position, List<Position>
    // lastItems) {
    // if (lastItems == null)
    // return false;
    // for (Position p : lastItems) {
    // var res = filterMaxSpeed(position, p);
    // if (res) {
    // System.out.println("Sending true in filterMaxSpeedFromList.");
    // return true;
    // }
    // }
    // return false;
    // }

    // private boolean filterMinPeriodFromList(Position position, List<Position>
    // lastItems) {
    // if (lastItems == null)
    // return false;
    // for (Position p : lastItems) {
    // var res = filterMinPeriod(position, p);
    // if (res)
    // return true;
    // }
    // return false;
    // }

    private boolean skipLimit(Position position, Position last) {
        // System.out.println("inside skip limit");
        if (skipLimit != 0 && last != null) {
            return (position.getServerTime().getTime() - last.getServerTime().getTime()) > skipLimit;
        }
        return false;
    }

    // private boolean skipLimitFromList(Position position, List<Position>
    // lastItems) {
    // if (lastItems == null) return false;
    // for (Position p : lastItems) {
    // var res = skipLimit(position, p);
    // if (res)
    // return true;
    // }
    // return false;
    // }

    private boolean skipAttributes(Position position) {
        // System.out.println("inside skip attributes");
        if (skipAttributes) {
            String string = AttributeUtil.lookup(cacheManager, Keys.FILTER_SKIP_ATTRIBUTES, position.getDeviceId());
            for (String attribute : string.split("[ ,]")) {
                if (position.hasAttribute(attribute)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean filter(Position position) {
        System.out.println("inside filter");
        StringBuilder filterType = new StringBuilder();

        // filter out invalid data
        if (filterInvalid(position)) {
            filterType.append("Invalid ");
        }
        if (filterZero(position)) {
            filterType.append("Zero ");
        }
        if (filterFuture(position)) {
            filterType.append("Future ");
        }
        if (filterPast(position)) {
            filterType.append("Past ");
        }
        if (filterAccuracy(position)) {
            filterType.append("Accuracy ");
        }
        if (filterApproximate(position)) {
            filterType.append("Approximate ");
        }

        // filter out excessive data
        long deviceId = position.getDeviceId();
        if (filterDuplicate || filterStatic || filterDistance > 0 || filterMaxSpeed > 0 || filterMinPeriod > 0) {
            Position preceding = null;
            List<Position> precedingList = null;
            if (filterRelative) {
                try {
                    Date newFixTime = position.getFixTime();
                    preceding = getPrecedingPosition(deviceId, newFixTime);
                } catch (StorageException e) {
                    LOGGER.warn("Error retrieving preceding position; fallbacking to last received position.", e);
                    preceding = cacheManager.getPosition(deviceId);
                }
            } else {
                preceding = cacheManager.getPosition(deviceId);
                precedingList = cacheManager.getPositionsList(deviceId);
            }
            if ((filterDuplicate(position, preceding) || filterDuplicateFromList(position, precedingList))
                    && !skipLimit(position, preceding)
                    && !skipAttributes(position)) {
                filterType.append("Duplicate ");
            }
            if (filterStatic(position)
                    && !skipLimit(position, preceding)
                    && !skipAttributes(position)) {
                filterType.append("Static ");
            }
            if (filterDistance(position, preceding)
                    && !skipLimit(position, preceding)
                    && !skipAttributes(position)) {
                filterType.append("Distance ");
            }

            // || filterMaxSpeedFromList(position, precedingList)
            // Not using above comment in below if condition because maxSpeed is enough to
            // check with last single position only.
            // No need to check with many more previous positions.
            if (filterMaxSpeed(position, preceding)) {
                filterType.append("MaxSpeed ");
            }

            // || filterMinPeriodFromList(position, precedingList)
            if (filterMinPeriod(position, preceding)) {
                filterType.append("MinPeriod ");
            }
        }

        if (filterType.length() > 0) {
            String uniqueId = cacheManager.getObject(Device.class, deviceId).getUniqueId();
            LOGGER.info("Position filtered by {}filters from device: {}", filterType, uniqueId);
            return true;
        }

        return false;
    }

    @Override
    protected Position handlePosition(Position position) {
        if (enabled && filter(position)) {
            return null;
        }
        return position;
    }

}
