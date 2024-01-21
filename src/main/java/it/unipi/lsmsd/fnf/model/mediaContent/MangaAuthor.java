package it.unipi.lsmsd.fnf.model.mediaContent;

public class MangaAuthor {
    private Integer id;
    private String name;
    private String role;

    public MangaAuthor() {
    }

    public MangaAuthor(Integer id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name= name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
