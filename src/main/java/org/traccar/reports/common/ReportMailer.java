/*
 * Copyright 2023 Anton Tananaev (anton@traccar.org)
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
package org.traccar.reports.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.security.PermissionsService;
import org.traccar.mail.MailManager;
import org.traccar.model.User;
import org.traccar.storage.StorageException;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class ReportMailer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMailer.class);

    private final PermissionsService permissionsService;
    private final MailManager mailManager;

    @Inject
    public ReportMailer(PermissionsService permissionsService, MailManager mailManager) {
        this.permissionsService = permissionsService;
        this.mailManager = mailManager;
    }

    // TODO: and add those to the mail subject and body.
    public void sendAsync(long userId, ReportExecutor executor, String type, Date from, Date to) {
        new Thread(() -> {
            try {
                var stream = new ByteArrayOutputStream();
                executor.execute(stream);

                MimeBodyPart attachment = new MimeBodyPart();

                attachment.setFileName("report" + "-" + type + ".xlsx");
                attachment.setDataHandler(new DataHandler(new ByteArrayDataSource(
                        stream.toByteArray(), "application/octet-stream")));

                User user = permissionsService.getUser(userId);

                String appendage = "";

                if (type == "trips") {
                    appendage += " Trips";
                } else if (type == "route") {
                    appendage += " Routes";
                } else if (type == "summary") {
                    appendage += " Summary";
                } else if (type == "stops") {
                    appendage += " Stops";
                } else if (type == "events") {
                    appendage += " Events";
                }

                appendage += "(" + from.toString() + " to " + to.toString() + ")";

                mailManager.sendMessage(user,
                        // --> Subject of the email:
                        /*
                         * Report - <type> (<from> to <to>)
                         */

                        "Report" + appendage,

                        // --> Body of the email:
                        /*
                         * Report - <type> (<from> to <to>)
                         * 
                         * The report is in the attachment.
                         */
                        "Report" + appendage + "\n\n" + "The report is in the attachment.", attachment);
            } catch (StorageException | IOException | MessagingException e) {
                LOGGER.warn("Email report failed", e);
            }
        }).start();
    }

}

// report-trips.xlsx
// Report - Trips (From DateTime to DateTime)