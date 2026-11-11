package org.example.kitchen;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kitchen")
class KitchenController {
    @GetMapping("/cookbook")
    String cookbook() {

        return "";
    }
}
