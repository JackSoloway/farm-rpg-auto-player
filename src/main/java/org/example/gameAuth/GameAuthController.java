package org.example.gameAuth;

import jakarta.validation.Valid;
import org.example.auth.LoginRequest;
import java.security.Principal;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gameAuth")
public class GameAuthController {
    private final GameAuthService authService;
    private final GameSessionAccess sessions;

    public GameAuthController(GameAuthService authService, GameSessionAccess sessions) {
        this.authService = authService;
        this.sessions = sessions;
    }

    @PostMapping("/login")
    GameSessionAccess.Status login(@Valid @RequestBody LoginRequest request, Principal principal) {
        authService.login(request, principal.getName());
        return new GameSessionAccess.Status(true, request.username());
    }

    @GetMapping("/status")
    ResponseEntity<GameSessionAccess.Status> status(Principal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(sessions.status(principal.getName()));
    }
}
