package com.myblog.user.convert;

import com.myblog.user.api.dto.ProfileUpdateDTO;
import com.myblog.user.api.vo.ProfileVO;
import com.myblog.user.api.vo.UserVO;
import com.myblog.user.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 用户对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    /**
     * Entity -> VO（密码等敏感字段不映射）
     */
    UserVO toVO(UserEntity entity);

    /**
     * Entity -> 个人资料 VO（密码不映射）
     */
    ProfileVO toProfileVO(UserEntity entity);

    /**
     * ProfileUpdateDTO -> Entity（只覆盖非空字段，避免把未填字段清空）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProfileUpdateDTO dto, @MappingTarget UserEntity entity);
}