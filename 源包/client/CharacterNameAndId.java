package client;

public class CharacterNameAndId {

    private final int id;
    private final String name;
    private final String group;

    public CharacterNameAndId(int id, String name, String group) {
        this.id = id;
        this.name = name;
        this.group = group;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getGroup() {
        return this.group;
    }
}
