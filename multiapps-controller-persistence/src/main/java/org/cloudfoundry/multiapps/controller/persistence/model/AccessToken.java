package org.cloudfoundry.multiapps.controller.persistence.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@JsonSerialize(as = ImmutableAccessToken.class)
@JsonDeserialize(as = ImmutableAccessToken.class)
public interface AccessToken {

    @Value.Default
    default long getId() {
        return 0;
    }

    byte[] getValue();

    String getUsername();

    Date getExpiresAt();
}
