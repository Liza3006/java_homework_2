package org.example.spring_hw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences", description = "Управление пользовательскими настройками (куки)")
public class PreferencesController {

  private static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
  private static final String DEFAULT_VIEW_MODE = "detailed";

  @GetMapping("/view")
  @Operation(summary = "Получить настройку отображения", description = "Читает значение куки viewPreference")
  public ResponseEntity<ViewPreferenceResponse> getViewPreference(
    @CookieValue(name = VIEW_PREFERENCE_COOKIE, required = false, defaultValue = DEFAULT_VIEW_MODE)
    @Parameter(description = "Режим отображения", example = "detailed") String viewMode) {

    return ResponseEntity.ok()
      .header("X-API-Version", "2.0.0")
      .body(new ViewPreferenceResponse(viewMode));
  }

  @PostMapping("/view")
  @Operation(summary = "Установить настройку отображения", description = "Устанавливает куку viewPreference")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Настройка сохранена"),
    @ApiResponse(responseCode = "400", description = "Неверный режим (допустимые: compact, detailed)")
  })
  public ResponseEntity<ViewPreferenceResponse> setViewPreference(
    @RequestParam @Parameter(description = "Режим отображения: compact или detailed", example = "detailed") String mode,
    HttpServletResponse response) {

    if (!mode.equals("compact") && !mode.equals("detailed")) {
      mode = DEFAULT_VIEW_MODE;
    }

    Cookie cookie = new Cookie(VIEW_PREFERENCE_COOKIE, mode);
    cookie.setPath("/");
    cookie.setMaxAge(60 * 60 * 24 * 365);
    cookie.setHttpOnly(true);

    response.addCookie(cookie);

    return ResponseEntity.ok()
      .header("X-API-Version", "2.0.0")
      .body(new ViewPreferenceResponse(mode));
  }

  @DeleteMapping("/view")
  @Operation(summary = "Сбросить настройку отображения", description = "Удаляет куку viewPreference")
  public ResponseEntity<Void> resetViewPreference(HttpServletResponse response) {
    Cookie cookie = new Cookie(VIEW_PREFERENCE_COOKIE, "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);

    return ResponseEntity.noContent()
      .header("X-API-Version", "2.0.0")
      .build();
  }

  record ViewPreferenceResponse(String viewMode) {}
}