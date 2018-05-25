package cinnamon.authentication;

import java.util.ArrayList;
import java.util.List;

public class LdapConfig {


    /**
     * The LDAP server host's address.
     */
    private String host;

    /**
     * Port on which to reach the LDAP server. The default (for testing) is 10389,
     * production port is usually 389.
     */
    private int port = 10389;

    /**
     * internal String.format string for bindDN.
     * Example:
     * cn=%s,cn=Users,dc=cinnamon,dc=dewarim,dc=com
     * <p>
     * %s will be replaced by the user name.
     */
    private String bindDnFormatString = "cn=%s,cn=Users,dc=localhost";

    /**
     * Search query.
     * Example:
     * cn=Users,dc=cinnamon,dc=dewarim,dc=coom
     * 
     * The query will be pre-pended with cn=$externalGroup from groupMappings.
     * Example:
     * cn=retrieval-users,cn=Users,dc=localhost
     */
    private String searchBaseDnFormatString = "cn=retrieval-users,cn=Users,dc=localhost";

    /**
     * Search filter to fetch the list of users allowed to login to the Cinnamon server.
     */
    private String searchFilter = "(&(objectclass=*))";

    /**
     * Name of the attribute which contains the user list.
     * Note: the expectation is currently that the returned searchResultEntry contains a list of
     * attribute "member" which is a String starting with CN=$username, for example: "CN=John Doe"
     */
    private String searchAttribute = "member";
    
    private String defaultLanguageCode = "en";

    private List<GroupMapping> groupMappings = new ArrayList<>();


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindDnFormatString() {
        return bindDnFormatString;
    }

    public void setBindDnFormatString(String bindDnFormatString) {
        this.bindDnFormatString = bindDnFormatString;
    }

    public String getSearchBaseDnFormatString() {
        return searchBaseDnFormatString;
    }

    public void setSearchBaseDnFormatString(String searchBaseDnFormatString) {
        this.searchBaseDnFormatString = searchBaseDnFormatString;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public void setSearchAttribute(String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public String getDefaultLanguageCode() {
        return this.defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public List<GroupMapping> getGroupMappings() {
        return groupMappings;
    }

    public void setGroupMappings(List<GroupMapping> groupMappings) {
        this.groupMappings = groupMappings;
    }

    public static class GroupMapping {

        private String externalGroup;
        private String cinnamonGroup;

        public String getExternalGroup() {
            return externalGroup;
        }

        public void setExternalGroup(String externalGroup) {
            this.externalGroup = externalGroup;
        }

        public String getCinnamonGroup() {
            return cinnamonGroup;
        }

        public void setCinnamonGroup(String cinnamonGroup) {
            this.cinnamonGroup = cinnamonGroup;
        }
    }
}
