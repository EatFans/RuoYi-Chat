package com.ruoyi.chat.controller;

import com.ruoyi.chat.service.IChatMessageService;
import com.ruoyi.chat.service.IChatSessionService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @PostMapping("/session/private")
    @Log(title =  "创建单聊会话", businessType =  BusinessType.INSERT)
    public AjaxResult createPrivateSession(){

        return AjaxResult.success();
    }

    @PostMapping("/session/group")
    @Log(title = "创建群聊会话", businessType = BusinessType.INSERT)
    public AjaxResult createGroupSession() {
        return AjaxResult.success();
    }
}
