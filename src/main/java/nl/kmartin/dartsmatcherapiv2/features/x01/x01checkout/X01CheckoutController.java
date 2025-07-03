package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import nl.kmartin.dartsmatcherapiv2.common.RestEndpoints;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class X01CheckoutController {
    private final IX01CheckoutService checkoutService;

    public X01CheckoutController(IX01CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping(path = RestEndpoints.GET_CHECKOUTS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<X01Checkout> getCheckouts() {
        return checkoutService.getCheckoutsAsList();
    }

    @GetMapping(path = RestEndpoints.GET_CHECKOUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Checkout getCheckout(@PathVariable int remaining) {
        return checkoutService.getCheckout(remaining).orElse(null);
    }
}
