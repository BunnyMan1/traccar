/*
 * Copyright 2022 Anton Tananaev (anton@traccar.org)
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
package org.traccar.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.traccar.config.Config;
import org.traccar.model.BaseModel;
import org.traccar.model.Device;
import org.traccar.model.Group;
import org.traccar.model.GroupedModel;
import org.traccar.model.Permission;
import org.traccar.model.Position;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatabaseStorage extends Storage {

    private final Config config;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final String databaseType;

    @Inject
    public DatabaseStorage(Config config, DataSource dataSource, ObjectMapper objectMapper) {
        this.config = config;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;

        try {
            databaseType = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> getObjects(Class<T> clazz, Request request) throws StorageException {
        StringBuilder query = new StringBuilder("SELECT ");
        if (request.getColumns() instanceof Columns.All) {
            query.append('*');
        } else {
            query.append(formatColumns(request.getColumns().getColumns(clazz, "set"), c -> c));
        }
        query.append(" FROM ").append(getStorageName(clazz));
        query.append(formatCondition(request.getCondition()));
        query.append(formatOrder(request.getOrder()));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            for (Map.Entry<String, Object> variable : getConditionVariables(request.getCondition()).entrySet()) {
                builder.setValue(variable.getKey(), variable.getValue());
            }
            return builder.executeQuery(clazz);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public <T> long addObject(T entity, Request request) throws StorageException {
        List<String> columns = request.getColumns().getColumns(entity.getClass(), "get");
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(getStorageName(entity.getClass()));
        query.append("(");
        query.append(formatColumns(columns, c -> c));
        query.append(") VALUES (");
        query.append(formatColumns(columns, c -> ':' + c));
        query.append(")");
        // System.out.println(" query " + query.toString());
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            builder.setObject(entity, columns);
            return builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public <T> void updateObject(T entity, Request request) throws StorageException {
        List<String> columns = request.getColumns().getColumns(entity.getClass(), "get");
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(getStorageName(entity.getClass()));
        query.append(" SET ");
        query.append(formatColumns(columns, c -> c + " = :" + c));
        query.append(formatCondition(request.getCondition()));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            builder.setObject(entity, columns);
            for (Map.Entry<String, Object> variable : getConditionVariables(request.getCondition()).entrySet()) {
                builder.setValue(variable.getKey(), variable.getValue());
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void updatePositions(List<Position> entities, Request request) throws StorageException, SQLException {

        System.out.println(" to updateObjects " + entities.size());
        if (entities.isEmpty()) {
            return;
        }
        List<String> columns = Arrays.asList(
                "address",
                "protocol",
                "valid",
                "longitude",
                "latitude",
                "network",
                "deviceTime",
                "accuracy",
                "serverTime",
                "fixTime",
                "altitude",
                "speed",
                "course",
                "deviceId",
                "attributes",
                "id");
        // columns = ["valid", "longitude", "latitude", "network", "deviceTime",
        // "accuracy", "serverTime", "fixTime", "altitude", "speed", "course",
        // "address", "protocol", "deviceId", "attributes", "id"];
        System.out.println(" columns " + columns);
        var classname = getStorageName(entities.get(0).getClass());
        System.out.println(" classname " + classname);
        StringBuilder query = new StringBuilder(
                "INSERT INTO " + classname + " (" + formatColumns(columns, c -> c) + " ) VALUES ");

        for (int i = 0; i < entities.size(); i++) {
            var qs = "";
            for (int j = 0; j < columns.size(); j++) {
                if (j == columns.size() - 1)
                    qs += "?";
                else
                    qs += "?, ";
            }

            query.append("(" + qs + ") ");
            if (i != entities.size() - 1)
                query.append(", ");
            // query.append("(?), ");
        }

        query.append("ON DUPLICATE KEY UPDATE attributes = VALUES(attributes)");

        System.out.println(" query " + query.toString());

        var connection = dataSource.getConnection();

        PreparedStatement ps = connection.prepareStatement(query.toString());

        for (int i = 0; i < entities.size(); i++) {

            var entity = entities.get(i);

            int index = (i * columns.size() + 1);

            System.out.println(" index " + index);
            ps.setString(index, entity.getAddress());
            ps.setString(index + 1, entity.getProtocol());
            ps.setBoolean(index + 2, entity.getValid());
            ps.setDouble(index + 3, entity.getLongitude());
            ps.setDouble(index + 4, entity.getLatitude());
            ps.setObject(index + 5, entity.getNetwork().toString());
            ps.setDate(index + 6, new java.sql.Date(entity.getDeviceTime().getTime()));
            ps.setDouble(index + 7, entity.getAccuracy());
            ps.setDate(index + 8, new java.sql.Date(entity.getServerTime().getTime()));
            ps.setDate(index + 9, new java.sql.Date(entity.getFixTime().getTime()));
            ps.setDouble(index + 10, entity.getAltitude());
            ps.setDouble(index + 11, entity.getSpeed());
            ps.setDouble(index + 12, entity.getCourse());
            ps.setLong(index + 13, entity.getDeviceId());
            try {
                ps.setObject(index + 14, objectMapper.writeValueAsString(entity.getAttributes()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            ps.setLong(index + 15, entity.getId());

        }
        try {
            var res = ps.executeUpdate();
            System.out.println(" res " + res);

            System.out.println(" Succesfully updated : " + entities.size() + " items.");

        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            ps.close();
            connection.close();

        }
    }
    // FIRST TRY
    // @Override
    // public <T> long addObjects(List<T> entities, Request request) throws
    // StorageException {
    // if (entities == null || entities.isEmpty()) {
    // return 0;
    // }

    // List<String> columns =
    // request.getColumns().getColumns(entities.get(0).getClass(), "get");
    // StringBuilder query = new StringBuilder("INSERT INTO ");
    // query.append(getStorageName(entities.get(0).getClass()));
    // query.append("(");
    // query.append(formatColumns(columns, c -> c));
    // query.append(") VALUES ");

    // // Appending multiple values
    // for (int i = 0; i < entities.size(); i++) {
    // final var a = i;
    // query.append("(");
    // query.append(formatColumns(columns, c -> ':' + c + a));
    // query.append(")");
    // if (i < entities.size() - 1) {
    // query.append(",");
    // }
    // }

    // // On duplicate key update
    // query.append(" ON DUPLICATE KEY UPDATE ");
    // for (int i = 0; i < columns.size(); i++) {
    // if (!"id".equalsIgnoreCase(columns.get(i))) {
    // query.append(columns.get(i)).append(" =
    // VALUES(").append(columns.get(i)).append(")");
    // if (i < columns.size() - 1) {
    // query.append(",");
    // }
    // }
    // }

    // try {
    // QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper,
    // query.toString(), true);
    // for (int i = 0; i < entities.size(); i++) {
    // final var a = i;
    // builder.setObject(entities.get(i), columns.stream().map(c -> c +
    // a).collect(Collectors.toList()));
    // }
    // return builder.executeUpdate();
    // } catch (SQLException e) {
    // throw new StorageException(e);
    // }
    // }

    public <T> long addObjects(List<T> entities, Request request) throws StorageException {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        List<String> columns = request.getColumns().getColumns(entities.get(0).getClass(), "get");
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(getStorageName(entities.get(0).getClass()));
        query.append("(");
        query.append(formatColumns(columns, c -> c));
        query.append(") VALUES ");

        // Appending multiple values
        for (int i = 0; i < entities.size(); i++) {
            final var a = i;
            query.append("(");
            query.append(formatColumns(columns, c -> ':' + c + a));
            query.append(")");
            if (i < entities.size() - 1) {
                query.append(",");
            }
        }

        // On duplicate key update
        query.append(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < columns.size(); i++) {
            if (!"id".equalsIgnoreCase(columns.get(i))) {
                query.append(columns.get(i)).append(" = VALUES(").append(columns.get(i)).append(")");
                if (i < columns.size() - 1) {
                    query.append(",");
                }
            }
        }
        // if the last character in query is a comma, remove it
        if (query.charAt(query.length() - 1) == ',') {
            query.deleteCharAt(query.length() - 1);
        }
        System.out.println(" Query : " + query.toString());
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            for (int i = 0; i < entities.size(); i++) {
                Map<String, Object> paramMap = new HashMap<>();
                builder.setObject(entities.get(i), columns, paramMap);
                builder.setParameters(paramMap);
            }
            return builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removeObject(Class<?> clazz, Request request) throws StorageException {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(getStorageName(clazz));
        query.append(formatCondition(request.getCondition()));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            for (Map.Entry<String, Object> variable : getConditionVariables(request.getCondition()).entrySet()) {
                builder.setValue(variable.getKey(), variable.getValue());
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) throws StorageException {
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(Permission.getStorageName(ownerClass, propertyClass));
        var conditions = new LinkedList<Condition>();
        if (ownerId > 0) {
            conditions.add(new Condition.Equals(Permission.getKey(ownerClass), ownerId));
        }
        if (propertyId > 0) {
            conditions.add(new Condition.Equals(Permission.getKey(propertyClass), propertyId));
        }
        Condition combinedCondition = Condition.merge(conditions);
        query.append(formatCondition(combinedCondition));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            for (Map.Entry<String, Object> variable : getConditionVariables(combinedCondition).entrySet()) {
                builder.setValue(variable.getKey(), variable.getValue());
            }
            return builder.executePermissionsQuery();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void addPermission(Permission permission) throws StorageException {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(permission.getStorageName());
        query.append(" VALUES (");
        query.append(permission.get().keySet().stream().map(key -> ':' + key).collect(Collectors.joining(", ")));
        query.append(")");
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            for (var entry : permission.get().entrySet()) {
                builder.setLong(entry.getKey(), entry.getValue());
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removePermission(Permission permission) throws StorageException {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(permission.getStorageName());
        query.append(" WHERE ");
        query.append(permission
                .get().keySet().stream().map(key -> key + " = :" + key).collect(Collectors.joining(" AND ")));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            for (var entry : permission.get().entrySet()) {
                builder.setLong(entry.getKey(), entry.getValue());
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    private String getStorageName(Class<?> clazz) throws StorageException {
        StorageName storageName = clazz.getAnnotation(StorageName.class);
        if (storageName == null) {
            throw new StorageException("StorageName annotation is missing");
        }
        return storageName.value();
    }

    private Map<String, Object> getConditionVariables(Condition genericCondition) {
        Map<String, Object> results = new HashMap<>();
        if (genericCondition instanceof Condition.Compare) {
            var condition = (Condition.Compare) genericCondition;
            if (condition.getValue() != null) {
                results.put(condition.getVariable(), condition.getValue());
            }
        } else if (genericCondition instanceof Condition.Between) {
            var condition = (Condition.Between) genericCondition;
            results.put(condition.getFromVariable(), condition.getFromValue());
            results.put(condition.getToVariable(), condition.getToValue());
        } else if (genericCondition instanceof Condition.Binary) {
            var condition = (Condition.Binary) genericCondition;
            results.putAll(getConditionVariables(condition.getFirst()));
            results.putAll(getConditionVariables(condition.getSecond()));
        } else if (genericCondition instanceof Condition.Permission) {
            var condition = (Condition.Permission) genericCondition;
            if (condition.getOwnerId() > 0) {
                results.put(Permission.getKey(condition.getOwnerClass()), condition.getOwnerId());
            } else {
                results.put(Permission.getKey(condition.getPropertyClass()), condition.getPropertyId());
            }
        } else if (genericCondition instanceof Condition.LatestPositions) {
            var condition = (Condition.LatestPositions) genericCondition;
            if (condition.getDeviceId() > 0) {
                results.put("deviceId", condition.getDeviceId());
            }
        }
        return results;
    }

    private String formatColumns(List<String> columns, Function<String, String> mapper) {
        return columns.stream().map(mapper).collect(Collectors.joining(", "));
    }

    private String formatCondition(Condition genericCondition) throws StorageException {
        return formatCondition(genericCondition, true);
    }

    private String formatCondition(Condition genericCondition, boolean appendWhere) throws StorageException {
        StringBuilder result = new StringBuilder();
        if (genericCondition != null) {
            if (appendWhere) {
                result.append(" WHERE ");
            }
            if (genericCondition instanceof Condition.Compare) {

                var condition = (Condition.Compare) genericCondition;
                result.append(condition.getColumn());
                result.append(" ");
                result.append(condition.getOperator());
                result.append(" :");
                result.append(condition.getVariable());

            } else if (genericCondition instanceof Condition.Between) {

                var condition = (Condition.Between) genericCondition;
                result.append(condition.getColumn());
                result.append(" BETWEEN :");
                result.append(condition.getFromVariable());
                result.append(" AND :");
                result.append(condition.getToVariable());

            } else if (genericCondition instanceof Condition.Binary) {

                var condition = (Condition.Binary) genericCondition;
                result.append(formatCondition(condition.getFirst(), false));
                result.append(" ");
                result.append(condition.getOperator());
                result.append(" ");
                result.append(formatCondition(condition.getSecond(), false));

            } else if (genericCondition instanceof Condition.Permission) {

                var condition = (Condition.Permission) genericCondition;
                result.append("id IN (");
                result.append(formatPermissionQuery(condition));
                result.append(")");

            } else if (genericCondition instanceof Condition.LatestPositions) {

                var condition = (Condition.LatestPositions) genericCondition;
                result.append("id IN (");
                result.append("SELECT positionId FROM ");
                result.append(getStorageName(Device.class));
                if (condition.getDeviceId() > 0) {
                    result.append(" WHERE id = :deviceId");
                }
                result.append(")");

            }
        }
        return result.toString();
    }

    private String formatOrder(Order order) {
        StringBuilder result = new StringBuilder();
        if (order != null) {
            result.append(" ORDER BY ");
            result.append(order.getColumn());
            if (order.getDescending()) {
                result.append(" DESC");
            }
            if (order.getLimit() > 0) {
                if (databaseType.equals("Microsoft SQL Server")) {
                    result.append(" OFFSET 0 ROWS FETCH FIRST ");
                    result.append(order.getLimit());
                    result.append(" ROWS ONLY");
                } else {
                    result.append(" LIMIT ");
                    result.append(order.getLimit());
                }
            }
        }
        return result.toString();
    }

    private String formatPermissionQuery(Condition.Permission condition) throws StorageException {
        StringBuilder result = new StringBuilder();

        String outputKey;
        String conditionKey;
        if (condition.getOwnerId() > 0) {
            outputKey = Permission.getKey(condition.getPropertyClass());
            conditionKey = Permission.getKey(condition.getOwnerClass());
        } else {
            outputKey = Permission.getKey(condition.getOwnerClass());
            conditionKey = Permission.getKey(condition.getPropertyClass());
        }

        String storageName = Permission.getStorageName(condition.getOwnerClass(), condition.getPropertyClass());
        result.append("SELECT ");
        result.append(storageName).append('.').append(outputKey);
        result.append(" FROM ");
        result.append(storageName);
        result.append(" WHERE ");
        result.append(conditionKey);
        result.append(" = :");
        result.append(conditionKey);

        if (condition.getIncludeGroups()) {

            boolean expandDevices;
            String groupStorageName;
            if (GroupedModel.class.isAssignableFrom(condition.getOwnerClass())) {
                expandDevices = Device.class.isAssignableFrom(condition.getOwnerClass());
                groupStorageName = Permission.getStorageName(Group.class, condition.getPropertyClass());
            } else {
                expandDevices = Device.class.isAssignableFrom(condition.getPropertyClass());
                groupStorageName = Permission.getStorageName(condition.getOwnerClass(), Group.class);
            }

            result.append(" UNION ");

            result.append("SELECT DISTINCT ");
            if (!expandDevices) {
                if (outputKey.equals("groupId")) {
                    result.append("all_groups.");
                } else {
                    result.append(groupStorageName).append('.');
                }
            }
            result.append(outputKey);
            result.append(" FROM ");
            result.append(groupStorageName);

            result.append(" INNER JOIN (");
            result.append("SELECT id as parentId, id as groupId FROM ");
            result.append(getStorageName(Group.class));
            result.append(" UNION ");
            result.append("SELECT groupId as parentId, id as groupId FROM ");
            result.append(getStorageName(Group.class));
            result.append(" WHERE groupId IS NOT NULL");
            result.append(" UNION ");
            result.append("SELECT g2.groupId as parentId, g1.id as groupId FROM ");
            result.append(getStorageName(Group.class));
            result.append(" AS g2");
            result.append(" INNER JOIN ");
            result.append(getStorageName(Group.class));
            result.append(" AS g1 ON g2.id = g1.groupId");
            result.append(" WHERE g2.groupId IS NOT NULL");
            result.append(") AS all_groups ON ");
            result.append(groupStorageName);
            result.append(".groupId = all_groups.parentId");

            if (expandDevices) {
                result.append(" INNER JOIN (");
                result.append("SELECT groupId as parentId, id as deviceId FROM ");
                result.append(getStorageName(Device.class));
                result.append(" WHERE groupId IS NOT NULL");
                result.append(") AS devices ON all_groups.groupId = devices.parentId");
            }

            result.append(" WHERE ");
            result.append(conditionKey);
            result.append(" = :");
            result.append(conditionKey);

        }

        return result.toString();
    }

}
