package com.dormammu.BooklogWeb.controller;

import com.dormammu.BooklogWeb.config.auth.PrincipalDetails;
import com.dormammu.BooklogWeb.domain.meeting.Meeting;
import com.dormammu.BooklogWeb.domain.user.User;
import com.dormammu.BooklogWeb.domain.user.UserRepository;
import com.dormammu.BooklogWeb.dto.PostMeetingReq;
import com.dormammu.BooklogWeb.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final UserRepository userRepository;

    @PostMapping("/auth/meeting")
    public String createMeeting(@RequestBody PostMeetingReq postMeetingReq, Authentication authentication){
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        if (postMeetingReq.getUserId() == principalDetails.getUser().getId()){
            meetingService.createMeeting(principalDetails.getUser(), postMeetingReq);
            return "모임 생성 완료";

        }
        return null;
    }

    @GetMapping("/meetings")
    public List<Meeting> meetingList(){
        System.out.println("controller로 들어옴");
        return meetingService.meetingList();
    }

    @GetMapping("/api/user/{id}/meetings")
    public List<Meeting> myMeetingList(@PathVariable int id, Authentication authentication){

        User user = userRepository.findById(id);
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        if (user.getId() == principalDetails.getUser().getId()){
            System.out.println("현재 로그인된 유저 : " + principalDetails.getUser().getUsername());
            return meetingService.myMeetingList(user);
        }
        return null;
    }
}