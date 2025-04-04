package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.X01CheckoutServiceImpl;
import nl.kmartin.dartsmatcherapiv2.utils.MessageResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class X01CheckoutTests {

    private IX01CheckoutService checkoutService;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setup() {
        // 1. Create the checkout service
        this.checkoutService = new X01CheckoutServiceImpl(messageResolver);

        // 2. Manually load the checkouts resource
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");

        // 3. Inject the resource into the private field using reflection
        ReflectionTestUtils.setField(
                checkoutService, // Target object
                "checkoutsResourceFile", // Field name to inject
                checkoutsResource // Value to set
        );
    }

    @Test
    void testReadCheckouts() throws IOException {
        List<X01Checkout> checkouts = checkoutService.getCheckouts();
        System.out.println(checkouts);
    }

}
