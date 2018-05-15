package com.walmartlabs.concord.server.process;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 Wal-Mart Store, Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.project.InternalConstants;
import com.walmartlabs.concord.server.api.process.ProcessQueueResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Named
public class ProcessQueueResourceImpl implements ProcessQueueResource, Resource {

    private static final Logger log = LoggerFactory.getLogger(ProcessQueueResourceImpl.class);

    private final ProcessManager processManager;

    @Inject
    public ProcessQueueResourceImpl(ProcessManager processManager) {
        this.processManager = processManager;
    }

    @Override
    public Response take() {
        try {
            ProcessManager.PayloadEntry p = processManager.nextPayload();
            if (p == null) {
                return null;
            }

            return Response.ok(
                    (StreamingOutput) output -> {
                        try {
                            Files.copy(p.getPayloadArchive(), output);
                        } finally {
                            Files.deleteIfExists(p.getPayloadArchive());
                        }
                    }, MediaType.APPLICATION_OCTET_STREAM)
                    .header(InternalConstants.Headers.PROCESS_INSTANCE_ID, p.getProcessEntry().getInstanceId().toString())
                    .build();
        } catch (IOException e) {
            log.error("take -> error", e);
            return Response.serverError().entity("Error while loading payload: " + e.getMessage()).build();
        }
    }
}