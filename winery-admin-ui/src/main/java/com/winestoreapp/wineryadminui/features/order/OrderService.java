package com.winestoreapp.wineryadminui.features.order;

import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.order.dto.OrderDto;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderFeignClient orderClient;
    private final SpanTagger spanTagger;

    @Observed(name = ObservationNames.ORDER_FIND_ALL)
    public List<OrderDto> getAll() {
        List<OrderDto> orders = orderClient.getAll();
        spanTagger.tag(ObservationTags.ORDERS_COUNT, orders.size());
        return orders;
    }

    @Observed(name = ObservationNames.ORDER_SET_PAID)
    public Boolean setPaidStatus(Long id) {
        spanTagger.tag(ObservationTags.ORDER_ID, id);
        return orderClient.setPaidStatus(id);
    }

    @Observed(name = ObservationNames.ORDER_DELETE)
    public void deleteOrder(Long id) {
        spanTagger.tag(ObservationTags.ORDER_ID, id);
        orderClient.deleteOrder(id);
    }
}
