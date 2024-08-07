package io.github.opendme.server.controller;

import io.github.opendme.server.entity.Call;
import io.github.opendme.server.entity.CallCreationDto;
import io.github.opendme.server.service.CallService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CallController {
    CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    @PostMapping("/call")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("admin")
    Call create(@RequestBody CallCreationDto dto) {
        return callService.createFrom(dto.getDepartmentId(), dto.getVehicleIds());
    }

    @Operation(summary = "Save the response to a call. Also sets the member to the state DISPATCHED.")
    @PostMapping("/call/{callId}/response")
    @ResponseStatus(HttpStatus.CREATED)
    void create(@PathVariable Long callId, @RequestBody Long memberId) {
        callService.createResponse(callId, memberId);
    }

    @Operation(summary = "Save the response without a call. With enough responses, a call will be created. Also sets the member to the state DISPATCHED.")
    @PostMapping("/call/response")
    @ResponseStatus(HttpStatus.CREATED)
    void create(@RequestBody Long memberId) {
        callService.createResponse(memberId);
    }
}
