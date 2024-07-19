package io.github.opendme;

import io.github.opendme.server.entity.DepartmentRepository;
import io.github.opendme.server.entity.MemberRepository;
import io.github.opendme.server.entity.SkillRepository;
import io.github.opendme.server.service.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootIntegrationTest
public class ITBase {
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected DepartmentRepository departmentRepository;
    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected SkillRepository skillRepository;

    @MockBean
    protected KeycloakService keycloakService;
    //Not in test scope
}
