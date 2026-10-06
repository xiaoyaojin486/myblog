package com.myblog.system.service;

import com.myblog.system.api.dto.AboutSaveDTO;
import com.myblog.system.api.vo.AboutVO;

/**
 * 关于我业务接口（模块内部使用）
 */
public interface AboutService {

    /** 关于我内容（自我介绍 + 简历 + 技能栈 + 经历） */
    AboutVO getAbout();

    /** 保存关于我（技能栈与经历全量覆盖） */
    void saveAbout(AboutSaveDTO dto);
}