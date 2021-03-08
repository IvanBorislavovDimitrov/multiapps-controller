package org.cloudfoundry.multiapps.controller.persistence.query;

import java.util.Date;

import org.cloudfoundry.multiapps.controller.persistence.model.AccessToken;

public interface AccessTokenQuery extends Query<AccessToken, AccessTokenQuery> {

    AccessTokenQuery id(Long id);

    AccessTokenQuery value(byte[] value);

    AccessTokenQuery username(String username);

    AccessTokenQuery greaterThan(Date expiresAt);

    AccessTokenQuery lessThan(Date expiresAt);
}
