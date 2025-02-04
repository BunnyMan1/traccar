# Stage 1: Build environment
FROM openjdk:11-jdk-slim as builder

# Set environment variables
ENV BRANCH_NAME=version-6.2
ENV TRACCAR_HOME=/opt/traccar

# Install required packages
RUN apt-get update && \
    apt-get install -y git unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Clone specific repository and branch
WORKDIR $TRACCAR_HOME
RUN git clone -b version-6.2 https://github.com/BunnyMan1/traccar.git . && \
    git checkout version-6.2

# Build with Gradle
RUN chmod +x ./gradlew
RUN ./gradlew clean build

# Stage 2: Runtime environment
FROM openjdk:11-jre-slim

# Set environment variables
ENV TRACCAR_HOME=/opt/traccar

# Install required packages for runtime
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    default-mysql-client && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create necessary directories
RUN mkdir -p /opt/traccar/logs \
    /opt/traccar/data \
    /opt/traccar/conf \
    /opt/traccar/lib \
    /opt/traccar/traccar-web \
    /opt/traccar/target/media

# Copy built application from builder stage
COPY --from=builder $TRACCAR_HOME/target $TRACCAR_HOME/target/
COPY --from=builder $TRACCAR_HOME/schema $TRACCAR_HOME/schema
COPY --from=builder $TRACCAR_HOME/traccar-web $TRACCAR_HOME/traccar-web
COPY --from=builder $TRACCAR_HOME/target/lib $TRACCAR_HOME/lib/
COPY --from=builder $TRACCAR_HOME/target/tracker-server.jar $TRACCAR_HOME/tracker-server.jar

# Create MySQL configuration
RUN echo '<?xml version="1.0"?>\n\
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">\n\
    <properties>\n\
    <entry key="database.driver">com.mysql.cj.jdbc.Driver</entry>\n\
    <entry key="database.url">jdbc:mysql://${MYSQL_HOST}:3306/${MYSQL_DATABASE}?allowPublicKeyRetrieval=true&amp;useSSL=false&amp;allowMultiQueries=true&amp;autoReconnect=true&amp;useUnicode=yes&amp;characterEncoding=UTF-8&amp;sessionVariables=sql_mode=NO_ENGINE_SUBSTITUTION</entry>\n\
    <entry key="database.user">${MYSQL_USER}</entry>\n\
    <entry key="database.password">${MYSQL_PASSWORD}</entry>\n\
    <entry key="web.path">/opt/traccar/traccar-web/simple</entry>\n\
    <entry key="web.debug">true</entry>\n\
    <entry key="web.console">true</entry>\n\
    <entry key="media.path">/opt/traccar/target/media</entry>\n\
    <entry key="logger.console">true</entry>\n\
    <entry key="logger.queries">false</entry>\n\
    <entry key="logger.fullStackTraces">true</entry>\n\
    </properties>' > /opt/traccar/conf/traccar.xml

# Create startup script
RUN echo '#!/bin/sh\n\
    # Wait for MySQL to be ready\n\
    while ! mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT 1" >/dev/null 2>&1; do\n\
    echo "Waiting for MySQL..."\n\
    sleep 1\n\
    done\n\
    \n\
    # Replace environment variables in config\n\
    sed -i "s/\${MYSQL_HOST}/$MYSQL_HOST/g" /opt/traccar/conf/traccar.xml\n\
    sed -i "s/\${MYSQL_DATABASE}/$MYSQL_DATABASE/g" /opt/traccar/conf/traccar.xml\n\
    sed -i "s/\${MYSQL_USER}/$MYSQL_USER/g" /opt/traccar/conf/traccar.xml\n\
    sed -i "s/\${MYSQL_PASSWORD}/$MYSQL_PASSWORD/g" /opt/traccar/conf/traccar.xml\n\
    \n\
    # Start Traccar\n\
    exec java -Xms512m -Xmx512m \
    -cp "/opt/traccar/tracker-server.jar:/opt/traccar/lib/*" \
    org.traccar.Main /opt/traccar/conf/traccar.xml' > $TRACCAR_HOME/start.sh && \
    chmod +x $TRACCAR_HOME/start.sh

# Expose web interface port
EXPOSE 8082

# Set working directory
WORKDIR $TRACCAR_HOME

# Start Traccar server
CMD ["./start.sh"]
