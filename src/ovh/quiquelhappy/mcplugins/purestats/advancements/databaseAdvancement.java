package ovh.quiquelhappy.mcplugins.purestats.advancements;

public class databaseAdvancement {
    String advancement;
    Long epoch;

    // constructor has type of data that is required
    public databaseAdvancement(String advancement, Long epoch)
    {
        this.epoch = epoch;
        this.advancement = advancement;
    }

    public String getAdvancement() {
        return advancement;
    }

    public Long getEpoch() {
        return epoch;
    }
}
