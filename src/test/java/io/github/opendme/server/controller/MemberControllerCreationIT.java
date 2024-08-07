package io.github.opendme.server.controller;

import io.github.opendme.ITBase;
import io.github.opendme.server.entity.DepartmentDto;
import io.github.opendme.server.entity.Member;
import io.github.opendme.server.entity.MemberDto;
import io.github.opendme.server.entity.Skill;
import io.github.opendme.server.service.DepartmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class MemberControllerCreationIT extends ITBase {
    private Long departmentId;
    @Autowired
    private DepartmentService departmentService;
    @Captor
    private ArgumentCaptor<Member> memberCaptor;
    @Autowired
    private ObjectMapper objectMapper;



    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void setUp() {
        memberRepository.removeAllDepartments();
        departmentRepository.deleteAll();
        memberRepository.deleteAll();
        skillRepository.deleteAll();

        doReturn("password").when(keycloakService).createUser(any());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_create_minimal_member() throws Exception {
        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(null, "Jon Doe", "valid@mail.com"));

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getContentAsString()).contains("Jon Doe");
        verify(keycloakService).createUser(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getName()).isEqualTo("Jon Doe");
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_create_member_with_department() throws Exception {
        createDepartment();

        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(departmentId, "Jon Doe", "valid@mail.com"));

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getContentAsString()).contains("Jon Doe");
        assertThat(response.getContentAsString()).contains(departmentId.toString());
        verify(keycloakService).createUser(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getName()).isEqualTo("Jon Doe");
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_reject_invalid_department() throws Exception {
        createDepartment();

        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(666L, "Jon Doe", "valid@mail.com"));

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_create_member_with_all() throws Exception {
        createDepartment();

        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(departmentId, "Jon Doe", "valid@mail.com"));

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getContentAsString()).contains("Jon Doe");
        assertThat(response.getContentAsString()).contains(departmentId.toString());
        verify(keycloakService).createUser(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getName()).isEqualTo("Jon Doe");
    }

    @Test
    @WithMockUser(roles = {"admin"})
    @Disabled
    void should_reject_invalid_skill() throws Exception {
        createSkills();

        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(null, "name", "valid@mail.com"));

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_reject_invalid_email() throws Exception {
        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(null, "name", "notValid.com"));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("email");
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void should_reject_empty_name() throws Exception {
        MockHttpServletResponse response = sendCreateRequestWith(new MemberDto(null, "", "notValid.com"));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("name");
    }

    private void createDepartment() {
        Member admin = new Member(null, null, "master", null, "valid@mail.com");
        admin = memberRepository.save(admin);
        departmentId = departmentService
                .create(new DepartmentDto("blub", admin.getId()))
                .getId();
    }

    private List<Long> createSkills() {
        List<Long> skillIds = new ArrayList<>();
        skillIds.add(skillRepository.save(new Skill(null, "Fahrer")).getId());
        skillIds.add(skillRepository.save(new Skill(null, "Atemschutzträger")).getId());
        return skillIds;
    }

    private MockHttpServletResponse sendCreateRequestWith(MemberDto memberDto) throws Exception {
        String requestJson = objectMapper.writeValueAsString(memberDto);

        return mvc.perform(
                          post("/member")
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .with(csrf())
                                  .content(requestJson))
                  .andReturn()
                  .getResponse();
    }
}
