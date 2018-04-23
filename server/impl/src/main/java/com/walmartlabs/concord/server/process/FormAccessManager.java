package com.walmartlabs.concord.server.process;

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

import com.walmartlabs.concord.project.InternalConstants;
import com.walmartlabs.concord.server.process.state.ProcessStateManager;
import com.walmartlabs.concord.server.security.UserPrincipal;
import com.walmartlabs.concord.server.security.ldap.LdapPrincipal;
import io.takari.bpm.form.Form;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.walmartlabs.concord.server.process.state.ProcessStateManager.path;

@Named
public class FormAccessManager {

    private final ProcessStateManager stateManager;

    @Inject
    public FormAccessManager(ProcessStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @SuppressWarnings("unchecked")
    public Form assertFormAccess(UUID processInstanceId, String formInstanceId) {
        Form f = getForm(processInstanceId, formInstanceId);
        if (f == null) {
            return null;
        }

        Map<String, Object> opts = f.getOptions();
        if (opts == null || opts.get(InternalConstants.Forms.RUN_AS_KEY) == null) {
            return f;
        }

        Map<String, Object> runAsParams = (Map<String, Object>) opts.get(InternalConstants.Forms.RUN_AS_KEY);

        UserPrincipal p = UserPrincipal.assertCurrent();
        String expectedUser = (String) runAsParams.get(InternalConstants.Forms.RUN_AS_USERNAME_KEY);
        if (expectedUser != null && !expectedUser.equals(p.getUsername())) {
            throw new UnauthorizedException("The current user (" + p.getUsername() + ") doesn't have " +
                    "the necessary permissions to resume process. Expected user '" + expectedUser + "'");
        }

        String group = (String) Optional.ofNullable(runAsParams.get(InternalConstants.Forms.RUN_AS_LDAP_KEY))
                .map(v -> (Map<String, Object>) v)
                .map(v -> v.get(InternalConstants.Forms.RUN_AS_GROUP_KEY))
                .orElse(null);

        LdapPrincipal ldapPrincipal = LdapPrincipal.getCurrent();
        Set<String> userLdapGroups = Optional.ofNullable(ldapPrincipal).map(LdapPrincipal::getGroups).orElse(null);

        if (group != null && !matchesLdapGroup(group, userLdapGroups)) {
            throw new UnauthorizedException("The current user (" + p.getUsername() + "[" + userLdapGroups + "]) doesn't have " +
                    "the necessary permissions to resume process. Expected LDAP group '" + group + "'");
        }

        return f;
    }

    private Form getForm(UUID processInstanceId, String formInstanceId) {
        String resource = path(InternalConstants.Files.JOB_ATTACHMENTS_DIR_NAME,
                InternalConstants.Files.JOB_STATE_DIR_NAME,
                InternalConstants.Files.JOB_FORMS_DIR_NAME,
                formInstanceId);

        Optional<Form> o = stateManager.get(processInstanceId, resource, FormAccessManager::deserialize);
        return o.orElse(null);
    }

    private static Optional<Form> deserialize(InputStream data) {
        try (ObjectInputStream in = new ObjectInputStream(data)) {
            return Optional.ofNullable((Form) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error while deserializing a form", e);
        }
    }

    private static boolean matchesLdapGroup(String pattern, Set<String> userLdapGroups) {
        if (userLdapGroups == null) {
            return false;
        }

        return userLdapGroups.stream()
                .anyMatch(g -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(g).matches());
    }
}
