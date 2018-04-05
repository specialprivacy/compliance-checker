package com.tenforce.config;

/**
 * Created by jonathan-langens on 3/7/16.
 *
 * This file contains configuration constants used in all microservices
 */
public class Configuration {
    /**
     * PREFIXES
     */
    public final static String prefixUsers = "http://users.com/";
    public final static String prefixObjects = "http://objects.com/";
    public final static String prefixMu = "http://mu.semte.ch/vocabularies/";
    public final static String prefixAuth = "http://mu.semte.ch/vocabularies/authorization/";
    public final static String prefixUUID = "http://mu.semte.ch/vocabularies/core/uuid";

    /**
     * PREDICATES
     */
    public final static String predicateAuthBelongsTo = "auth:belongsTo";
    public final static String predicateCanUpdate = "auth:canUpdate";
    public final static String predicateCanRead = "auth:canRead";

    /**
     * DEFINITIONS
     */
    public final static String definitionUser = "users:class";
    public final static String definitionUserGroup = "auth:Group";
}
