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
    private String searchAttributeForGroup = "member";

    /**
     * After the initial connect and search for the user's group members,
     * use the searchAttributeForDn to extract the user's actual ldap login name.
     */
    private String searchAttributeForDn = "distinguishedName";

    /**
     * The name of the UI language for new users.
     * This must be a language code that is used in the Cinnamon database in the ui_languages table.
     */
    private String defaultLanguageCode = "und";

    /**
     * If you want to use a static bind string with a fixed password, you must set the staticBindPassword field
     * with a non-empty String value.
     * <b>WARNING</b>: This means anyone who knows a valid user's login name will be able to login as that user.
     * Only recommended in fully trusted environments or for testing and initial batch-scripted user generation.
     */
    private String staticBindPassword = null;

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

    public String getSearchAttributeForGroup() {
        return searchAttributeForGroup;
    }

    public void setSearchAttributeForGroup(String searchAttributeForGroup) {
        this.searchAttributeForGroup = searchAttributeForGroup;
    }

    public List<GroupMapping> getGroupMappings() {
        return groupMappings;
    }

    public void setGroupMappings(List<GroupMapping> groupMappings) {
        this.groupMappings = groupMappings;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public String getStaticBindPassword() {
        return staticBindPassword;
    }

    public void setStaticBindPassword(String staticBindPassword) {
        this.staticBindPassword = staticBindPassword;
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

    public boolean useStaticBindPassword(){
        return staticBindPassword != null && staticBindPassword.trim().length() > 0;
    }

    public String getSearchAttributeForDn() {
        return searchAttributeForDn;
    }

    public void setSearchAttributeForDn(String searchAttributeForDn) {
        this.searchAttributeForDn = searchAttributeForDn;
    }

    @Override
    public String toString() {
        return "LdapConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", bindDnFormatString='" + bindDnFormatString + '\'' +
                ", searchBaseDnFormatString='" + searchBaseDnFormatString + '\'' +
                ", searchFilter='" + searchFilter + '\'' +
                ", searchAttributeForGroup='" + searchAttributeForGroup + '\'' +
                ", searchAttributeForDn='" + searchAttributeForDn + '\'' +
                ", defaultLanguageCode='" + defaultLanguageCode + '\'' +
                ", staticBindPassword='" + staticBindPassword + '\'' +
                ", groupMappings=" + groupMappings +
                '}';
    }
}
