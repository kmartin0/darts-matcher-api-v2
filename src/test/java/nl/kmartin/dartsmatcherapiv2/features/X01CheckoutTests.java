package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.X01CheckoutServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class X01CheckoutTests {

    private IX01CheckoutService checkoutService;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setup() {
        // 1. Manually load the checkouts resource
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");

        // 2. Create the checkout service
        this.checkoutService = new X01CheckoutServiceImpl(checkoutsResource, messageResolver);
    }

    @Test
    void testReadCheckouts() {
        Map<Integer, X01Checkout> checkouts = checkoutService.getCheckouts();
        Assertions.assertEquals(checkouts.size(), 162);
    }

}
