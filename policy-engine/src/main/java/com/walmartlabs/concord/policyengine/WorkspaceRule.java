package com.walmartlabs.concord.policyengine;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Wal-Mart Store, Inc.
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class WorkspaceRule implements Serializable {

    private final String msg;
    private final Long maxSizeInBytes;

    @JsonCreator
    public WorkspaceRule(@JsonProperty("msg") String msg,
                         @JsonProperty("maxSizeInBytes") Long maxSizeInBytes) {

        this.msg = msg;
        this.maxSizeInBytes = maxSizeInBytes;
    }

    public String getMsg() {
        return msg;
    }

    public Long getMaxSizeInBytes() {
        return maxSizeInBytes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, Utils.NotNullToStringStyle.NOT_NULL_STYLE)
                .append("msg", msg)
                .append("maxSizeInBytes", maxSizeInBytes)
                .toString();
    }
}