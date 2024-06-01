package org.example.utility.jwt;

public class JwtUser {
    public String id;
    public String name;
    public String role;
    public Object tag;
    public String preferredCampusIds;

    public JwtUser() {

    }
    public JwtUser(String id, String name) {
        this(id, name, "","");
    }
    public JwtUser(String id, String name, String role, String preferredCampusIds) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.preferredCampusIds = preferredCampusIds;
    }
}
