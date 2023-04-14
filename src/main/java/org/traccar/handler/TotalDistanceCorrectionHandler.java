package org.traccar.handler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.traccar.BaseDataHandler;
import org.traccar.model.Position;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.Storage;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.ChannelHandler;

@Singleton
@ChannelHandler.Sharable
public class TotalDistanceCorrectionHandler extends BaseDataHandler {
    private final CacheManager cacheManager;
    private final Storage storage;

    private static final Logger LOGGER = LoggerFactory.getLogger(TotalDistanceCorrectionHandler.class);

    @Inject
    public TotalDistanceCorrectionHandler(CacheManager cacheManager, Storage storage) {
        this.cacheManager = cacheManager;
        this.storage = storage;
    }

    @Override
    protected Position handlePosition(Position position) {
        var deviceId = position.getDeviceId();
        var existingPositions = cacheManager.getPositionsList(deviceId);

        if (existingPositions == null || existingPositions.size() == 0)
            return position;

        Position lowerClosestPosition = null;
        List<Position> greaterFixTimePositions = new ArrayList<>();

        for (Position p : existingPositions) {
            if (lowerClosestPosition == null || lowerClosestPosition.getFixTime().before(p.getFixTime()))
                lowerClosestPosition = p;

            if (p.getFixTime().after(position.getFixTime())) {
                greaterFixTimePositions.add(p);
            }
        }

        if (greaterFixTimePositions.size() == 0)
            return position;

        // rectify totalDistance of all positions with greater fixTime
        double totalDistance = lowerClosestPosition.getDouble(Position.KEY_TOTAL_DISTANCE);

        totalDistance += position.getDouble(Position.KEY_DISTANCE);

        position.set(Position.KEY_TOTAL_DISTANCE, totalDistance);

        for (Position p : greaterFixTimePositions) {
            totalDistance += p.getDouble(Position.KEY_DISTANCE);
            p.set(Position.KEY_TOTAL_DISTANCE, totalDistance);
        }

        cacheManager.updatePosition(greaterFixTimePositions.get(greaterFixTimePositions.size() - 1));

        // update multiple positions in database within a single query

        try {
            storage.updateObjects(greaterFixTimePositions, new Request(new Columns.All()));

            // Update the changed position in the cache list

            cacheManager.updatePosition(greaterFixTimePositions, deviceId);

        } catch (Exception e) {

            LOGGER.error(" Error in total distance correction :  ", e);

        }

        return position;
    }
}