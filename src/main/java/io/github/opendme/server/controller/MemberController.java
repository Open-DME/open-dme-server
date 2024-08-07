package io.github.opendme.server.controller;

import io.github.opendme.server.entity.Member;
import io.github.opendme.server.entity.MemberDto;
import io.github.opendme.server.entity.Status;
import io.github.opendme.server.service.KeycloakService;
import io.github.opendme.server.service.MailService;
import io.github.opendme.server.service.MemberService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MemberController {
    private static final Logger log = LogManager.getLogger(MemberController.class);
    private final MemberService service;
    private final KeycloakService keycloakService;
    private final MailService mailService;
    private final String departmentName;
    private final String mailSubject;

    public MemberController(
            MemberService service,
            KeycloakService keycloakService,
            MailService mailService,
            @Value("${department.name}") String departmentName,
            @Value("${mail.subject}") String mailSubject
    ) {
        this.service = service;
        this.keycloakService = keycloakService;
        this.mailService = mailService;
        this.departmentName = departmentName;
        this.mailSubject = mailSubject;
    }

    @PostMapping(value = "/member", produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("admin")
    public Member create(@RequestBody @Valid MemberDto dto) {
        Member member = service.create(dto);
        log.atInfo().log("Member created");
        var password = keycloakService.createUser(member);

        Map<String, Object> templateParameter = new HashMap<>();
        templateParameter.put("email", member.getEmail());
        templateParameter.put("name", member.getName());
        templateParameter.put("password", password);
        templateParameter.put("department", departmentName);
        mailService.sendMessageUsingThymeleafTemplate(
                member.getEmail(),
                mailSubject,
                "user-created.html",
                templateParameter
        );

        return member;
    }

    @PatchMapping(value = "/member/{memberId}/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setStatus(@PathVariable Long memberId, @RequestBody Status status) {
        service.setMemberStatus(memberId, status);
        log.atInfo().log("Member status patched");

    }

    @PatchMapping(value = "/member/{memberId}/awayUntil")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setStatus(@PathVariable Long memberId, @RequestBody Date awayUntil) {
        service.setMemberAway(memberId, awayUntil);
        log.atInfo().log("Member awayUntil patched");

    }
}
