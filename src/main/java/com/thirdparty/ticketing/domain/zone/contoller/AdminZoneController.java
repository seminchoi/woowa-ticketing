package com.thirdparty.ticketing.domain.zone.contoller;

import com.thirdparty.ticketing.domain.common.TicketingException;
import com.thirdparty.ticketing.domain.member.MemberRole;
import com.thirdparty.ticketing.domain.zone.dto.ZoneCreationRequest;
import com.thirdparty.ticketing.domain.zone.service.AdminZoneService;
import com.thirdparty.ticketing.global.security.Authentication;
import com.thirdparty.ticketing.global.security.AuthenticationContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller("/api/performances/{performanceId}/zones")
@RequiredArgsConstructor
public class AdminZoneController {
    private final AuthenticationContext authenticationContext;
    private final AdminZoneService adminZoneService;

    @PostMapping
    public ResponseEntity<Void> createZones(
            @PathVariable long performanceId,
            @RequestBody @Valid ZoneCreationRequest zoneCreationRequest
    ) {
        Authentication authentication = authenticationContext.getAuthentication();
        String authority = authentication.getAuthority();
        MemberRole memberRole = MemberRole.valueOf(authority);

        if (memberRole != MemberRole.ADMIN) {
            throw new TicketingException("");
        }

        adminZoneService.createZones(performanceId, zoneCreationRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
}
