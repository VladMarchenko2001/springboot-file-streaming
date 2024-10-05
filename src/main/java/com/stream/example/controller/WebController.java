package com.stream.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @RequestMapping("/video")
    public String playVideo() {
        return "stream_video.html";
    }

    @RequestMapping("/image")
    public String getImage() {
        return "stream_image.html";
    }

    @RequestMapping("/file")
    public String getFile() {
        return "stream_file.html";
    }

}
