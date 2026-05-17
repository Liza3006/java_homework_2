package org.example.spring_hw.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PreferencesController.class)
class PreferencesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Test
  void getViewPreference_WithCookie_ShouldReturnValue() throws Exception {
    mockMvc.perform(get("/api/preferences/view")
        .cookie(new jakarta.servlet.http.Cookie("viewPreference", "compact")))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").value("compact"));
  }

  @Test
  void getViewPreference_WithoutCookie_ShouldReturnDefault() throws Exception {
    mockMvc.perform(get("/api/preferences/view"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").value("detailed"));
  }

  @Test
  void setViewPreference_ValidCompact_ShouldSetCookie() throws Exception {
    mockMvc.perform(post("/api/preferences/view")
        .param("mode", "compact"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(cookie().exists("viewPreference"))
      .andExpect(cookie().value("viewPreference", "compact"))
      .andExpect(jsonPath("$.viewMode").value("compact"));
  }

  @Test
  void setViewPreference_ValidDetailed_ShouldSetCookie() throws Exception {
    mockMvc.perform(post("/api/preferences/view")
        .param("mode", "detailed"))
      .andExpect(status().isOk())
      .andExpect(cookie().value("viewPreference", "detailed"))
      .andExpect(jsonPath("$.viewMode").value("detailed"));
  }

  @Test
  void resetViewPreference_ShouldDeleteCookie() throws Exception {
    mockMvc.perform(delete("/api/preferences/view"))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(cookie().maxAge("viewPreference", 0));
  }


  @Test
  void setViewPreference_InvalidMode_ShouldFallbackToDefault() throws Exception {
    mockMvc.perform(post("/api/preferences/view")
        .param("mode", "invalid"))
      .andExpect(status().isOk())
      .andExpect(cookie().value("viewPreference", "detailed"))
      .andExpect(jsonPath("$.viewMode").value("detailed"));
  }

  @Test
  void setViewPreference_EmptyMode_ShouldFallbackToDefault() throws Exception {
    mockMvc.perform(post("/api/preferences/view")
        .param("mode", ""))
      .andExpect(status().isOk())
      .andExpect(cookie().value("viewPreference", "detailed"))
      .andExpect(jsonPath("$.viewMode").value("detailed"));
  }
}
