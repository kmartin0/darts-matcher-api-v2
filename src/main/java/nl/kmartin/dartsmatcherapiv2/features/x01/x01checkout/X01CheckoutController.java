package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import nl.kmartin.dartsmatcherapiv2.utils.Endpoints;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@RestController
public class X01CheckoutController {
    private final IX01CheckoutService checkoutService;

    public X01CheckoutController(IX01CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping(path = Endpoints.GET_CHECKOUTS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<X01Checkout> getCheckouts() throws IOException {
        return checkoutService.getCheckouts();
    }

    @GetMapping(path = Endpoints.GET_CHECKOUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Checkout getCheckout(@PathVariable int remaining) throws IOException {
        return checkoutService.getCheckout(remaining);
    }
}
