//Class for structuring user data for simple read/write operations
package com.example.ugotthis;

class User {
    private String username;
    private String email;

    public User()
    {

    }
    public User(String user)
    {
        this.username = user;
    }
    public User(String user, String em)
    {
        this.username = user;
        this.email = em;
    }

    public String getUser()
    {
        return username;
    }

    public void setUser(String user)
    {
        this.username = user;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String em)
    {
        this.email = em;
    }
}
