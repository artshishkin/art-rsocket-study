package com.artarkatesoft.rsocketserver.controllers;

import com.artarkatesoft.rsocketserver.data.Video;
import com.artarkatesoft.rsocketserver.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final VideoService videoService;

    @GetMapping
    public String index(Model model) {
        Flux<Video> allVideoFiles = videoService.getAllVideoFiles();
        model.addAttribute("videos", allVideoFiles);
        return "home";
    }
}
