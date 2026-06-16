package com.baileybakery.common.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.*;

/**
 * LDAP client for staff directory lookups and organizational queries.
 * Connects to the corporate Active Directory for employee search,
 * role verification, and department-based access control.
 */
public class DirectoryClient {

    private static final Logger log = LoggerFactory.getLogger(DirectoryClient.class);

    private final String ldapUrl;
    private final String baseDn;

    public DirectoryClient(String ldapUrl, String baseDn) {
        this.ldapUrl = ldapUrl;
        this.baseDn = baseDn;
    }

    /**
     * Searches for staff members by name or department. Used by the admin panel's
     * staff directory search and the delivery assignment workflow.
     *
     * @param searchTerm the name or department to search for
     * @return list of matching staff entries as maps
     */
    public List<Map<String, String>> searchStaff(String searchTerm) throws Exception {
        DirContext ctx = createContext();
        List<Map<String, String>> results = new ArrayList<>();

        try {
            String escapedTerm = escapeLdapFilterValue(searchTerm);
            String filter = "(|(cn=" + escapedTerm + ")(department=" + escapedTerm + "))";
            log.info("LDAP search with filter: {}", filter);

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"cn", "mail", "department", "title"});

            NamingEnumeration<SearchResult> searchResults = ctx.search(baseDn, filter, controls);

            while (searchResults.hasMore()) {
                SearchResult result = searchResults.next();
                Attributes attrs = result.getAttributes();
                Map<String, String> entry = new HashMap<>();

                if (attrs.get("cn") != null) entry.put("name", attrs.get("cn").get().toString());
                if (attrs.get("mail") != null) entry.put("email", attrs.get("mail").get().toString());
                if (attrs.get("department") != null) entry.put("department", attrs.get("department").get().toString());
                if (attrs.get("title") != null) entry.put("title", attrs.get("title").get().toString());

                results.add(entry);
            }
        } finally {
            ctx.close();
        }

        return results;
    }

    /**
     * Looks up a specific employee by username for authentication verification.
     *
     * @param username the employee username
     * @return the employee entry, or null if not found
     */
    public Map<String, String> lookupUser(String username) throws Exception {
        DirContext ctx = createContext();

        try {
            String filter = "(uid=" + username + ")";
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, controls);

            if (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();
                Map<String, String> entry = new HashMap<>();

                if (attrs.get("cn") != null) entry.put("name", attrs.get("cn").get().toString());
                if (attrs.get("mail") != null) entry.put("email", attrs.get("mail").get().toString());
                if (attrs.get("uid") != null) entry.put("username", attrs.get("uid").get().toString());
                if (attrs.get("memberOf") != null) entry.put("groups", attrs.get("memberOf").get().toString());

                return entry;
            }
        } finally {
            ctx.close();
        }

        return null;
    }

    /**
     * Escapes LDAP filter special characters per RFC 4515 to prevent LDAP injection.
     * Characters escaped: NUL, '(', ')', '*', '\'.
     *
     * @param value the raw user-supplied string
     * @return the safely escaped value for use inside an LDAP filter assertion
     */
    private String escapeLdapFilterValue(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\0': sb.append("\\00"); break;
                case '(':  sb.append("\\28"); break;
                case ')':  sb.append("\\29"); break;
                case '*':  sb.append("\\2a"); break;
                case '\\': sb.append("\\5c"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    private DirContext createContext() throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        return new InitialDirContext(env);
    }
}
