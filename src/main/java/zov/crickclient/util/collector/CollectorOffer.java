package zov.crickclient.util.collector;

import lombok.Getter;

@Getter
public class CollectorOffer {
    private final int page;
    private final int slotId;
    private final int price;

    public CollectorOffer(int page, int slotId, int price) {
        this.page = page;
        this.slotId = slotId;
        this.price = price;
    }
}
