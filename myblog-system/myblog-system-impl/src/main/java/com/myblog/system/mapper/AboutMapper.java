package com.myblog.system.mapper;

import com.myblog.system.entity.AboutExperienceEntity;
import com.myblog.system.entity.AboutSkillEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 关于我（技能栈 / 经历）Mapper（只负责数据库访问，必须明确字段）
 */
@Mapper
public interface AboutMapper {

    /** 技能栈列表（按排序） */
    @Select("SELECT id, name, level_label, percent, sort FROM about_skill ORDER BY sort ASC, id ASC")
    List<AboutSkillEntity> selectSkills();

    /** 新增技能 */
    @Insert("INSERT INTO about_skill (name, level_label, percent, sort) VALUES (#{name}, #{levelLabel}, #{percent}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSkill(AboutSkillEntity entity);

    /** 清空技能栈（全量覆盖保存用） */
    @Delete("DELETE FROM about_skill")
    int deleteAllSkills();

    /** 经历列表（按排序） */
    @Select("SELECT id, title, organization, start_date, end_date, description, sort " +
            "FROM about_experience ORDER BY sort ASC, id ASC")
    List<AboutExperienceEntity> selectExperiences();

    /** 新增经历 */
    @Insert("INSERT INTO about_experience (title, organization, start_date, end_date, description, sort) " +
            "VALUES (#{title}, #{organization}, #{startDate}, #{endDate}, #{description}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExperience(AboutExperienceEntity entity);

    /** 清空经历（全量覆盖保存用） */
    @Delete("DELETE FROM about_experience")
    int deleteAllExperiences();
}