package io.github.opendme.server.controller;

import io.github.opendme.ITBase;
import io.github.opendme.SpringBootIntegrationTest;
import io.github.opendme.server.entity.MemberDto;
import io.github.opendme.server.entity.Status;
import io.github.opendme.server.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;


public class MemberControllerPatchIT extends ITBase {
    private Long memberId;
    @Autowired
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;


    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        createMember();
    }


    @Test
    @WithMockUser(roles = {"admin"})
    void should_patch_status() throws Exception {
        MockHttpServletResponse response = sendPatchStatusRequestWith(Status.AVAILABLE, memberId);

        assertThat(response.getStatus()).isEqualTo(202);
        assertThat(memberRepository.findById(memberId).get().getStatus()).isEqualTo(Status.AVAILABLE);
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_fail_on_wrong_memberId() throws Exception {
        MockHttpServletResponse response = sendPatchStatusRequestWith(Status.AVAILABLE, 99L);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_patch_awayUntil() throws Exception {
        Date awayUntil = Date.from(Instant.now().plusSeconds(60));

        MockHttpServletResponse response = sendPatchAwayRequestWith(awayUntil, memberId);

        assertThat(response.getStatus()).isEqualTo(202);
        assertThat(memberRepository.findById(memberId).get().getAwayUntil().getTime()).isEqualTo(awayUntil.getTime());
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_fail_on_wrong_member() throws Exception {
        Date awayUntil = Date.from(Instant.now().plusSeconds(60));

        MockHttpServletResponse response = sendPatchAwayRequestWith(awayUntil, 99L);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    private void createMember() {
        memberId = memberService.create(new MemberDto(null, "noob", "valid@mail.com")).getId();
    }

    private MockHttpServletResponse sendPatchStatusRequestWith(Object status, Long memberId) throws Exception {
        String requestJson = getRequestJson(status);

        return mvc.perform(
                          patch("/member/{memberId}/status", memberId)
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .with(csrf())
                                  .content(requestJson))
                  .andReturn()
                  .getResponse();
    }

    private MockHttpServletResponse sendPatchAwayRequestWith(Object awayUntil, Long memberId) throws Exception {
        String requestJson = getRequestJson(awayUntil);

        return mvc.perform(
                          patch("/member/{memberId}/awayUntil", memberId)
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .with(csrf())
                                  .content(requestJson))
                  .andReturn()
                  .getResponse();
    }

    private String getRequestJson(Object status) throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(status);
        return requestJson;
    }
}
