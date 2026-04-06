package com.example.externaldata.controller;

import com.example.externaldata.service.DashboardFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardFacadeService dashboardFacadeService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardFacadeService.getDashboard());
        return "dashboard";
    }
}