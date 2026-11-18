package org.example.gameAuth;

import org.example.gameHttp.GameCookie;

import jakarta.validation.Valid;
import org.example.auth.LoginRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gameAuth")
public class GameAuthController {
    private final GameAuthService authService;

    public GameAuthController(GameAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    GameCookie login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
