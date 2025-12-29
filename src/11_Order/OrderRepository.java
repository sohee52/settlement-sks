package com.back.boundedContext.market.out;

import com.back.boundedContext.market.domain.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Integer> {
}
