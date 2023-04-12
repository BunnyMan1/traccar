package org.traccar.handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.traccar.BaseDataHandler;
import org.traccar.config.Config;
import org.traccar.model.Position;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Request;

import io.netty.channel.ChannelHandler;

@Singleton
@ChannelHandler.Sharable
public class TotalDistanceCorrectionHandler extends BaseDataHandler {

    private final CacheManager cacheManager;
    private final Storage storage;

    // private final boolean filter;

    @Inject
    public TotalDistanceCorrectionHandler(Config config, CacheManager cacheManager, Storage storage) {
        this.cacheManager = cacheManager;
        this.storage = storage;
        // this.filter = config.getBoolean(Keys.COORDINATES_FILTER);
    }

    @Override
    protected Position handlePosition(Position position) {



        System.out.println("=====================================> checking this:    " + position.getFixTime()  + "  " + position.getDouble( Position.KEY_TOTAL_DISTANCE) );

        // 1, 4, 5, 2, 3
        // when a new position arrives - 2
        // check based on fixtime if there positions that are greater that 2's fixtime.
        // if yes, update 2 & after 2's total distance.
        // Update/insert all the updated positions into the database.

        List<Position> lastList = cacheManager.getPositionsList(position.getDeviceId());

        if (lastList == null || lastList.size() == 0) {
            return position;
        }

        List<Position> positionsToUpdate = new ArrayList<>();
        Position lastGoodPosition = null;
        for (int i = lastList.size() - 1; i >= 0; i--) {
            /// if the position is greater than the current position mark them for update.
            if (lastList.get(i).getFixTime().after(position.getFixTime()) || lastList.get(i).getFixTime().equals(position.getFixTime())) {

                System.out.println(lastList.get(i).getFixTime() + ", " + lastList.get(i).getDouble(Position.KEY_TOTAL_DISTANCE )  +  "  is after the time of  current " + position.getFixTime() + "  " + position.getDouble(Position.KEY_TOTAL_DISTANCE)  );

                positionsToUpdate.add(lastList.get(i));
            } else {
                lastGoodPosition = lastList.get(i);
                System.out.println(" last good position found is: " + lastList.get(i).getFixTime() + ", " + lastList.get(i).getDouble(Position.KEY_TOTAL_DISTANCE )  );
                break;
            }
        }
        
        System.out.println(" number of items to be updated: " + positionsToUpdate.size());
        if (positionsToUpdate.size() > 0) {
            if(lastGoodPosition == null)
            {
                System.out.println(" last good position is null");
                return position;
            }
            // lastGoodPosition = positionsToUpdate.get(positionsToUpdate.size() - 1);
            
            position.set(Position.KEY_TOTAL_DISTANCE, position.getDouble(Position.KEY_DISTANCE)
            + lastGoodPosition.getDouble(Position.KEY_TOTAL_DISTANCE));
            double totalDistance = lastGoodPosition.getDouble(Position.KEY_TOTAL_DISTANCE);
            System.out.print(" $$$$$$$$$$   last good position: " + totalDistance);
            for (int i = positionsToUpdate.size() - 1; i >= 0; i--) {
                totalDistance += positionsToUpdate.get(i).getDouble(Position.KEY_DISTANCE);
                positionsToUpdate.get(i).set(Position.KEY_TOTAL_DISTANCE, totalDistance);
            }
            try {
                // storage.addObjects(positionsToUpdate, new Request(new Columns.All()));
                storage.updatePositions(positionsToUpdate, new Request(new Columns.All()));
            } catch (StorageException e) {
                e.printStackTrace();
                return null;
            } 
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        return position;
    }

}
