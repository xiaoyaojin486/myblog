package com.myblog.toolbox.service;

import com.myblog.common.result.PageResult;
import com.myblog.toolbox.api.dto.ToolCategoryDTO;
import com.myblog.toolbox.api.dto.ToolCommandCreateDTO;
import com.myblog.toolbox.api.dto.ToolCommandUpdateDTO;
import com.myblog.toolbox.api.vo.ToolCategoryVO;
import com.myblog.toolbox.api.vo.ToolCommandVO;
import com.myblog.toolbox.api.vo.ToolboxOverviewVO;
import com.myblog.toolbox.query.ToolCommandQuery;

import java.util.List;

/**
 * 运维工具箱业务接口（模块内部使用）
 */
public interface ToolboxService {

    /** 前台：总览（启用分类 + 上架命令），一次返回 */
    ToolboxOverviewVO overview();

    /** 后台：命令分页查询 */
    PageResult<ToolCommandVO> pageCommands(ToolCommandQuery query);

    /** 后台：命令详情（下架也可查看，编辑回填用） */
    ToolCommandVO getCommand(Long id);

    /** 后台：新增命令 */
    Long createCommand(ToolCommandCreateDTO dto);

    /** 后台：更新命令 */
    void updateCommand(Long id, ToolCommandUpdateDTO dto);

    /** 后台：删除命令 */
    void deleteCommand(Long id);

    /** 后台：全部分类 */
    List<ToolCategoryVO> listCategories();

    /** 后台：新增分类 */
    Long createCategory(ToolCategoryDTO dto);

    /** 后台：更新分类 */
    void updateCategory(Long id, ToolCategoryDTO dto);

    /** 后台：删除分类（分类下有命令时禁止删除） */
    void deleteCategory(Long id);
}
