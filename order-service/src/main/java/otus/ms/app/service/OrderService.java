package otus.ms.app.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import otus.ms.app.model.dto.CreateOrderDto;
import otus.ms.app.model.dto.OrderItemDto;
import otus.ms.app.model.entity.AuthUser;
import otus.ms.app.model.entity.OrderItem;
import otus.ms.app.model.entity.WarehouseItem;
import otus.ms.app.model.entity.Order;
import otus.ms.app.model.exception.BadRequestException;
import otus.ms.app.model.exception.ETagException;
import otus.ms.app.model.mapper.OrderItemMapper;
import otus.ms.app.model.mapper.OrderMapper;
import otus.ms.app.repository.OrderItemRepository;
import otus.ms.app.repository.OrderRepository;
import otus.ms.app.repository.WarehouseRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final WarehouseRepository warehouseRepository;

    private final OrderItemMapper orderItemMapper;

    private final OrderMapper orderMapper;

    @Transactional
    public Order createOrder(CreateOrderDto orderDto, String warehouseHash, AuthUser authUser) {
        List<WarehouseItem> warehouseItems = warehouseRepository.findAllByOrderByItemUuid();
        if (StringUtils.isBlank(warehouseHash) || warehouseItems.hashCode() != Integer.valueOf(warehouseHash).intValue()) {
            throw new ETagException("Cannot create an order. Warehouse state changed. Please renew");
        }
        Map<UUID, WarehouseItem> itemsByUuid = warehouseItems.stream().collect(
                Collectors.toMap(WarehouseItem::getItemUuid, Function.identity()));
        List<WarehouseItem> warehouseItemsUpdateList = new ArrayList<>();
        List<OrderItem> orderItemList = new ArrayList<>();

        Order order = orderMapper.toEntity(orderDto);
        order.setUserUuid(authUser.getUuid());
        order.setOrderUuid(UUID.randomUUID());
        order.setEtag(Integer.valueOf(warehouseHash));
        orderRepository.save(order);

        for (OrderItemDto orderItemDto : orderDto.getItems()) {
            WarehouseItem warehouseItem = itemsByUuid.get(orderItemDto.getItemUuid());
            if (warehouseItem.getItemQuantity() < orderItemDto.getQuantity()) {
                throw new BadRequestException("Ordered quantity is more than exists for item [" + warehouseItem.getItemName() + "]; UUID: " + orderItemDto.getItemUuid());
            }
            if (orderItemDto.getQuantity() <= 0) {
                throw new BadRequestException("Ordered quantity must be more than zero for item [" + warehouseItem.getItemName() + "]; UUID: " + orderItemDto.getItemUuid());
            }
            warehouseItem.setItemQuantity(warehouseItem.getItemQuantity() - orderItemDto.getQuantity());
            warehouseItemsUpdateList.add(warehouseItem);

            OrderItem orderItem = orderItemMapper.toEntity(orderItemDto);
            orderItem.setItemId(warehouseItem.getItemId());
            orderItem.setOrderId(order.getOrderId());
            orderItemList.add(orderItem);
        }
        /*
            TODO atomic insert into two tables like due to spring data jdbc limitations
            WITH order AS
            (INSERT INTO order_user (order_uuid, user_uuid) VALUES (?, ?) RETURNING order_id AS order_id)
            INSERT INTO order_item (order_id, item_id, item_uuid, quantity) ...
         */
        //orderItemRepository.saveAll(orderItemList); // does not work due to spring data jdbc limitations
        orderItemList.forEach(orderItem -> orderItemRepository.saveOrderItem(orderItem.getOrderId(), orderItem.getItemId(), orderItem.getItemUuid(), orderItem.getQuantity()));
        warehouseRepository.saveAll(warehouseItemsUpdateList);

        return order;
    }


    public Order getOrder(UUID orderUuid) {
        return orderRepository.getOrderByOrderUuid(orderUuid).orElse(null);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.getByOrderId(orderId);
    }

    public Order findExistingOrder(String warehouseHash, CreateOrderDto createOrderDto, AuthUser authUser) {
        Order order = orderRepository.getOrderByUserUuidAndEtag(authUser.getUuid(), Integer.valueOf(warehouseHash)).orElse(null);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemRepository.getByOrderId(order.getOrderId());
            List<OrderItemDto> orderItemDtoList = orderItemMapper.toDtoList(orderItemList);
            if (CollectionUtils.isEqualCollection(orderItemDtoList, createOrderDto.getItems())) {
                return order;
            }
        }
        return null;
    }
}
