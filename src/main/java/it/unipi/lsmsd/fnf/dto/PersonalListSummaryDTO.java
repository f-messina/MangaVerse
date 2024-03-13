package it.unipi.lsmsd.fnf.dto;

public class PersonalListSummaryDTO {
    private String listId;
    private String userId;
    private String name;

    public PersonalListSummaryDTO() {
    }

    public PersonalListSummaryDTO(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public PersonalListSummaryDTO(String listId, String userId, String name) {
        this.listId = listId;
        this.userId = userId;
        this.name = name;
    }

    public String getListId() {
        return listId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name= name;
    }

    @Override
    public String toString() {
        return "PersonalListSummaryDTO{" +
                "id=" + listId +
                ", userId=" + userId +
                ", name=" + name +
                "}";
    }
}
