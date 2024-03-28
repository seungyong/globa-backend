package org.y2k2.globa.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.service.UserService;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<?> getRanking(@RequestParam(required = false, defaultValue = "1", value = "page") int page,
                                        @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        ObjectNode result = userService.getRanking(page, count);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/error/bad-request")
    public ResponseEntity<?> getBadRequest() {
        userService.errorTestBadRequest();

        return ResponseEntity.ok("어라 이게 나오면 안되는뎅");
    }
}


