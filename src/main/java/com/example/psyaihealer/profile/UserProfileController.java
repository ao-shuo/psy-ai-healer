package com.example.psyaihealer.profile;

import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserRepository userRepository;
    private final UserProfileService profileService;

    public UserProfileController(UserRepository userRepository, UserProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseEntity.ok(profileService.getOrCreate(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfile> patchMe(@AuthenticationPrincipal UserDetails principal,
                                               @RequestBody Map<String, Object> body) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String preferredTone = body.get("preferredTone") instanceof String s ? s : null;
        String goals = body.get("goals") instanceof String s ? s : null;
        String triggers = body.get("triggers") instanceof String s ? s : null;
        String copingPreferences = body.get("copingPreferences") instanceof String s ? s : null;

        return ResponseEntity.ok(profileService.patchPreferences(user, preferredTone, goals, triggers, copingPreferences));
    }
}
