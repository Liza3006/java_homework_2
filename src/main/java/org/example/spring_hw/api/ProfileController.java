package org.example.spring_hw.api;

import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {

  @GetMapping("/profile")
  public Map<String, String> profile(Principal principal) {
    return Map.of("username", principal.getName(), "profile", "user profile");
  }

  @GetMapping("/docs")
  public Map<String, String> docs() {
    return Map.of("docs", "read privilege content");
  }
}
