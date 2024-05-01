package com.example.CallHandleAPI.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin
public class GlobalQueuePolling {

    private final GlobalQueueImplementationController globalQueueImplementationController;

    @Autowired
    public GlobalQueuePolling(GlobalQueueImplementationController globalQueue) {
        this.globalQueueImplementationController = globalQueue;
    }

}
