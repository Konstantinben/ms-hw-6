package otus.ms.app.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemDto {
    private UUID itemUuid;
    private int quantity;
}
