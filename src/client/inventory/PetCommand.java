package client.inventory;

public class PetCommand {

    private final int petId;
    private final int command;
    private final int prob;
    private final int inc;

    public PetCommand(int petId, int command, int prob, int inc) {
        this.petId = petId;
        this.command = command;
        this.prob = prob;
        this.inc = inc;
    }

    public int getPetId() {
        return this.petId;
    }

    public int getCommand() {
        return this.command;
    }

    public int getProbability() {
        return this.prob;
    }

    public int getIncrease() {
        return this.inc;
    }
}
