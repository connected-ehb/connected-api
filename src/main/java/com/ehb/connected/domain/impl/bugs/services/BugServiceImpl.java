package com.ehb.connected.domain.impl.bugs.services;

import com.ehb.connected.domain.impl.bugs.dto.BugCreateDto;
import com.ehb.connected.domain.impl.bugs.entities.Bug;
import com.ehb.connected.domain.impl.bugs.repositories.BugRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final UserService userService;

    @Override
    public void create(Principal principal, BugCreateDto bugCreateDto) {

        Bug bug = new Bug();
        bug.setDescription(bugCreateDto.getDescription());
        bug.setRoute(bugCreateDto.getRoute());
        bug.setAppVersion(bugCreateDto.getAppVersion());
        if (principal != null) {
            final User user = userService.getUserByPrincipal(principal);
            bug.setCreatedBy(user);
        }

        bugRepository.save(bug);
    }
}
